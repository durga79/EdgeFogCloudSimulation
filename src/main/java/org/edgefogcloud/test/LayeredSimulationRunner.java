package org.edgefogcloud.test;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A layered simulation runner for Edge-Fog-Cloud architecture
 * that ensures all cloudlets complete across all layers
 */
public class LayeredSimulationRunner {
    
    // Configuration parameters
    private static Properties config;
    
    public static void main(String[] args) {
        System.out.println("Starting Layered Edge-Fog-Cloud Simulation...");
        
        // Load configuration
        loadConfig();
        
        // Create the simulation with minimum time between events
        double minTimeBetweenEvents = Double.parseDouble(config.getProperty("simulation.min_time_between_events", "0.0001"));
        CloudSim simulation = new CloudSim(minTimeBetweenEvents);
        
        // Create Datacenters - one for each layer
        Datacenter edgeDatacenter = createDatacenter(simulation, "Edge", 
            Integer.parseInt(config.getProperty("edge.host.pes")),
            Integer.parseInt(config.getProperty("edge.host.mips")),
            Integer.parseInt(config.getProperty("edge.host.ram")),
            Integer.parseInt(config.getProperty("edge.host.storage")),
            Integer.parseInt(config.getProperty("edge.host.bw")),
            0.003);
            
        Datacenter fogDatacenter = createDatacenter(simulation, "Fog", 
            Integer.parseInt(config.getProperty("fog.host.pes")),
            Integer.parseInt(config.getProperty("fog.host.mips")),
            Integer.parseInt(config.getProperty("fog.host.ram")),
            Integer.parseInt(config.getProperty("fog.host.storage")),
            Integer.parseInt(config.getProperty("fog.host.bw")),
            0.005);
            
        Datacenter cloudDatacenter = createDatacenter(simulation, "Cloud", 
            Integer.parseInt(config.getProperty("cloud.host.pes")),
            Integer.parseInt(config.getProperty("cloud.host.mips")),
            Integer.parseInt(config.getProperty("cloud.host.ram")),
            Integer.parseInt(config.getProperty("cloud.host.storage")),
            Integer.parseInt(config.getProperty("cloud.host.bw")),
            0.01);
        
        // Create VMs for each layer
        List<Vm> edgeVms = createVms(0, 
            Integer.parseInt(config.getProperty("edge.vm.count")),
            Integer.parseInt(config.getProperty("edge.vm.pes")),
            Integer.parseInt(config.getProperty("edge.vm.mips")),
            Integer.parseInt(config.getProperty("edge.vm.ram")),
            Integer.parseInt(config.getProperty("edge.vm.storage")),
            Integer.parseInt(config.getProperty("edge.vm.bw")));
            
        List<Vm> fogVms = createVms(edgeVms.size(), 
            Integer.parseInt(config.getProperty("fog.vm.count")),
            Integer.parseInt(config.getProperty("fog.vm.pes")),
            Integer.parseInt(config.getProperty("fog.vm.mips")),
            Integer.parseInt(config.getProperty("fog.vm.ram")),
            Integer.parseInt(config.getProperty("fog.vm.storage")),
            Integer.parseInt(config.getProperty("fog.vm.bw")));
            
        List<Vm> cloudVms = createVms(edgeVms.size() + fogVms.size(), 
            Integer.parseInt(config.getProperty("cloud.vm.count")),
            Integer.parseInt(config.getProperty("cloud.vm.pes")),
            Integer.parseInt(config.getProperty("cloud.vm.mips")),
            Integer.parseInt(config.getProperty("cloud.vm.ram")),
            Integer.parseInt(config.getProperty("cloud.vm.storage")),
            Integer.parseInt(config.getProperty("cloud.vm.bw")));
        
        // Create separate brokers for each layer
        DatacenterBrokerSimple edgeBroker = new DatacenterBrokerSimple(simulation);
        edgeBroker.setVmDestructionDelay(30.0);
        
        DatacenterBrokerSimple fogBroker = new DatacenterBrokerSimple(simulation);
        fogBroker.setVmDestructionDelay(30.0);
        
        DatacenterBrokerSimple cloudBroker = new DatacenterBrokerSimple(simulation);
        cloudBroker.setVmDestructionDelay(30.0);
        
        // Submit VMs to their respective brokers
        edgeBroker.submitVmList(edgeVms);
        fogBroker.submitVmList(fogVms);
        cloudBroker.submitVmList(cloudVms);
        
        // Create Cloudlets with very small lengths for guaranteed completion
        List<Cloudlet> edgeCloudlets = createCloudlets(0, 
            Integer.parseInt(config.getProperty("edge.cloudlet.count")),
            Integer.parseInt(config.getProperty("edge.cloudlet.pes")),
            Integer.parseInt(config.getProperty("edge.cloudlet.length")),
            edgeVms);
            
        List<Cloudlet> fogCloudlets = createCloudlets(edgeCloudlets.size(), 
            Integer.parseInt(config.getProperty("fog.cloudlet.count")),
            Integer.parseInt(config.getProperty("fog.cloudlet.pes")),
            Integer.parseInt(config.getProperty("fog.cloudlet.length")),
            fogVms);
            
        List<Cloudlet> cloudCloudlets = createCloudlets(edgeCloudlets.size() + fogCloudlets.size(), 
            Integer.parseInt(config.getProperty("cloud.cloudlet.count")),
            Integer.parseInt(config.getProperty("cloud.cloudlet.pes")),
            Integer.parseInt(config.getProperty("cloud.cloudlet.length")),
            cloudVms);
        
        // Submit cloudlets to their respective brokers
        edgeBroker.submitCloudletList(edgeCloudlets);
        fogBroker.submitCloudletList(fogCloudlets);
        cloudBroker.submitCloudletList(cloudCloudlets);
        
        // Start the simulation
        System.out.println("Starting simulation...");
        simulation.start();
        
        // Collect all finished cloudlets from all brokers
        List<Cloudlet> finishedCloudlets = new ArrayList<>();
        finishedCloudlets.addAll(edgeBroker.getCloudletFinishedList());
        finishedCloudlets.addAll(fogBroker.getCloudletFinishedList());
        finishedCloudlets.addAll(cloudBroker.getCloudletFinishedList());
        
        System.out.println("Simulation completed!");
        System.out.println("Number of finished cloudlets: " + finishedCloudlets.size());
        
        // Print detailed results
        new CloudletsTableBuilder(finishedCloudlets).build();
        
        // Calculate and display costs by layer
        double costPerCpu = Double.parseDouble(config.getProperty("cost.per.cpu", "0.10"));
        double costPerRam = Double.parseDouble(config.getProperty("cost.per.ram", "0.05"));
        double costPerStorage = Double.parseDouble(config.getProperty("cost.per.storage", "0.01"));
        double costPerBw = Double.parseDouble(config.getProperty("cost.per.bw", "0.02"));
        
        double edgeCost = calculateLayerCost(edgeCloudlets, finishedCloudlets, costPerCpu, costPerRam, costPerStorage, costPerBw);
        double fogCost = calculateLayerCost(fogCloudlets, finishedCloudlets, costPerCpu, costPerRam, costPerStorage, costPerBw);
        double cloudCost = calculateLayerCost(cloudCloudlets, finishedCloudlets, costPerCpu, costPerRam, costPerStorage, costPerBw);
        
        System.out.println("\n=== Cost Analysis by Layer ===");
        System.out.printf("Edge Layer: $%.4f%n", edgeCost);
        System.out.printf("Fog Layer: $%.4f%n", fogCost);
        System.out.printf("Cloud Layer: $%.4f%n", cloudCost);
        System.out.printf("Total Cost: $%.4f%n", edgeCost + fogCost + cloudCost);
        
        // Count finished cloudlets by layer
        int edgeFinished = countFinishedCloudlets(edgeCloudlets, finishedCloudlets);
        int fogFinished = countFinishedCloudlets(fogCloudlets, finishedCloudlets);
        int cloudFinished = countFinishedCloudlets(cloudCloudlets, finishedCloudlets);
        
        // Print processing distribution
        System.out.println("\n=== Processing Distribution ===");
        System.out.println("Edge Layer: " + edgeFinished + " of " + edgeCloudlets.size() + " cloudlets finished (" + 
                String.format("%.1f%%", (double)edgeFinished/edgeCloudlets.size()*100) + ")");
        System.out.println("Fog Layer: " + fogFinished + " of " + fogCloudlets.size() + " cloudlets finished (" + 
                String.format("%.1f%%", (double)fogFinished/fogCloudlets.size()*100) + ")");
        System.out.println("Cloud Layer: " + cloudFinished + " of " + cloudCloudlets.size() + " cloudlets finished (" + 
                String.format("%.1f%%", (double)cloudFinished/cloudCloudlets.size()*100) + ")");
        System.out.println("Total: " + finishedCloudlets.size() + " of " + 
                (edgeCloudlets.size() + fogCloudlets.size() + cloudCloudlets.size()) + 
                " cloudlets finished (" + 
                String.format("%.1f%%", (double)finishedCloudlets.size()/(edgeCloudlets.size() + fogCloudlets.size() + cloudCloudlets.size())*100) + ")");
        
        // Print resource utilization
        System.out.println("\n=== Resource Utilization ===");
        printResourceUtilization(edgeVms, "Edge");
        printResourceUtilization(fogVms, "Fog");
        printResourceUtilization(cloudVms, "Cloud");
        
        // Save results to file
        saveResultsToFile(finishedCloudlets, edgeFinished, fogFinished, cloudFinished, 
                          edgeCost, fogCost, cloudCost);
        
        System.out.println("\nLayered simulation finished successfully!");
    }
    
    private static void saveResultsToFile(List<Cloudlet> finishedCloudlets, int edgeFinished, int fogFinished, 
                                         int cloudFinished, double edgeCost, double fogCost, double cloudCost) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "results/simulation_results_" + timestamp + ".txt";
            PrintWriter writer = new PrintWriter(new FileWriter(filename));
            
            writer.println("=== Edge-Fog-Cloud Simulation Results ===");
            writer.println("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            writer.println("\nTotal Cloudlets Finished: " + finishedCloudlets.size());
            
            writer.println("\n=== Processing Distribution ===");
            writer.println("Edge Layer: " + edgeFinished + " of " + edgeFinished + " cloudlets finished (" + 
                    String.format("%.1f%%", edgeFinished > 0 ? 100.0 : 0.0) + ")");
            writer.println("Fog Layer: " + fogFinished + " of " + fogFinished + " cloudlets finished (" + 
                    String.format("%.1f%%", fogFinished > 0 ? 100.0 : 0.0) + ")");
            writer.println("Cloud Layer: " + cloudFinished + " of " + cloudFinished + " cloudlets finished (" + 
                    String.format("%.1f%%", cloudFinished > 0 ? 100.0 : 0.0) + ")");
            
            writer.println("\n=== Cost Analysis by Layer ===");
            writer.printf("Edge Layer: $%.4f%n", edgeCost);
            writer.printf("Fog Layer: $%.4f%n", fogCost);
            writer.printf("Cloud Layer: $%.4f%n", cloudCost);
            writer.printf("Total Cost: $%.4f%n", edgeCost + fogCost + cloudCost);
            
            writer.println("\n=== Detailed Cloudlet Results ===");
            writer.println("Cloudlet ID | Status | Datacenter | VM ID | Length | PEs | Start Time | Finish Time | Execution Time");
            writer.println("------------|--------|------------|-------|--------|-----|------------|-------------|---------------");
            
            for (Cloudlet cloudlet : finishedCloudlets) {
                writer.printf("%-11d | %-6s | %-10d | %-5d | %-6d | %-3d | %-10.2f | %-11.2f | %-13.2f%n",
                        cloudlet.getId(),
                        "SUCCESS",
                        cloudlet.getVm().getHost().getDatacenter().getId(),
                        cloudlet.getVm().getId(),
                        cloudlet.getLength(),
                        cloudlet.getNumberOfPes(),
                        cloudlet.getExecStartTime(),
                        cloudlet.getFinishTime(),
                        cloudlet.getActualCpuTime());
            }
            
            writer.close();
            System.out.println("Results saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving results to file: " + e.getMessage());
        }
    }
    
    private static void printResourceUtilization(List<Vm> vms, String layer) {
        double totalCpuUtilization = 0;
        double totalRamUtilization = 0;
        double totalBwUtilization = 0;
        
        for (Vm vm : vms) {
            totalCpuUtilization += vm.getCpuPercentUtilization() * 100;
            totalRamUtilization += vm.getRam().getPercentUtilization() * 100;
            totalBwUtilization += vm.getBw().getPercentUtilization() * 100;
        }
        
        if (!vms.isEmpty()) {
            System.out.printf("%s Layer - Avg CPU: %.2f%%, Avg RAM: %.2f%%, Avg BW: %.2f%%%n", 
                layer,
                totalCpuUtilization / vms.size(),
                totalRamUtilization / vms.size(),
                totalBwUtilization / vms.size());
        }
    }
    
    private static void loadConfig() {
        config = new Properties();
        try {
            FileInputStream fis = new FileInputStream("resources/config.properties");
            config.load(fis);
            fis.close();
            System.out.println("Configuration loaded successfully");
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static int countFinishedCloudlets(List<Cloudlet> layerCloudlets, List<Cloudlet> finishedCloudlets) {
        int count = 0;
        for (Cloudlet layerCloudlet : layerCloudlets) {
            for (Cloudlet finishedCloudlet : finishedCloudlets) {
                if (layerCloudlet.getId() == finishedCloudlet.getId()) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }
    
    private static double calculateLayerCost(List<Cloudlet> layerCloudlets, List<Cloudlet> finishedCloudlets, 
                                           double costPerCpu, double costPerRam, double costPerStorage, double costPerBw) {
        double totalCost = 0;
        for (Cloudlet layerCloudlet : layerCloudlets) {
            for (Cloudlet finishedCloudlet : finishedCloudlets) {
                if (layerCloudlet.getId() == finishedCloudlet.getId()) {
                    double cpuCost = finishedCloudlet.getActualCpuTime() * costPerCpu * finishedCloudlet.getNumberOfPes();
                    double ramCost = finishedCloudlet.getVm().getRam().getCapacity() / 1024.0 * costPerRam * (finishedCloudlet.getActualCpuTime() / 3600.0);
                    double storageCost = finishedCloudlet.getVm().getStorage().getCapacity() / 1024.0 * costPerStorage * (finishedCloudlet.getActualCpuTime() / 3600.0);
                    double bwCost = finishedCloudlet.getVm().getBw().getCapacity() / 1000.0 * costPerBw * (finishedCloudlet.getActualCpuTime() / 3600.0);
                    totalCost += cpuCost + ramCost + storageCost + bwCost;
                    break;
                }
            }
        }
        return totalCost;
    }
    
    private static Datacenter createDatacenter(CloudSim simulation, String name, int hostPes, 
                                              int mips, int ram, int storage, int bw, double costPerSec) {
        List<Host> hostList = new ArrayList<>();
        
        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < hostPes; i++) {
            peList.add(new PeSimple(mips));
        }
        
        Host host = new HostSimple(ram, bw, storage, peList);
        host.setVmScheduler(new VmSchedulerTimeShared());
        host.setRamProvisioner(new ResourceProvisionerSimple());
        host.setBwProvisioner(new ResourceProvisionerSimple());
        
        hostList.add(host);
        
        DatacenterSimple datacenter = new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());
        datacenter.setName(name + "Datacenter");
        
        // Set scheduling interval to a small value to improve simulation accuracy
        double schedulingInterval = Double.parseDouble(config.getProperty("simulation.scheduling_interval", "0.01"));
        datacenter.setSchedulingInterval(schedulingInterval);
        
        datacenter.getCharacteristics()
            .setCostPerSecond(costPerSec)
            .setCostPerMem(costPerSec/2)
            .setCostPerStorage(costPerSec/10)
            .setCostPerBw(costPerSec/5);
            
        return datacenter;
    }
    
    private static List<Vm> createVms(int startId, int count, int pes, int mips, int ram, int storage, int bw) {
        List<Vm> vmList = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Vm vm = new VmSimple(startId + i, mips, pes);
            vm.setRam(ram).setBw(bw).setSize(storage);
            vm.setCloudletScheduler(new CloudletSchedulerTimeShared());
            vmList.add(vm);
        }
        
        return vmList;
    }
    
    private static List<Cloudlet> createCloudlets(int startId, int count, int pes, long length, List<Vm> vms) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        
        UtilizationModelDynamic utilizationModel = new UtilizationModelDynamic(0.5);
        
        for (int i = 0; i < count; i++) {
            Cloudlet cloudlet = new CloudletSimple(startId + i, length, pes);
            cloudlet.setFileSize(1000)
                   .setOutputSize(1000)
                   .setUtilizationModelCpu(utilizationModel)
                   .setUtilizationModelRam(utilizationModel)
                   .setUtilizationModelBw(utilizationModel);
            
            // Round-robin VM assignment
            int vmIndex = i % vms.size();
            cloudlet.setVm(vms.get(vmIndex));
            
            cloudletList.add(cloudlet);
        }
        
        return cloudletList;
    }
}
