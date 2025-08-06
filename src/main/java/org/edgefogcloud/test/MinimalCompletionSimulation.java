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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A minimal simulation focused solely on ensuring all cloudlets complete
 * across edge, fog, and cloud layers
 */
public class MinimalCompletionSimulation {
    
    public static void main(String[] args) {
        System.out.println("Starting Minimal Completion Simulation...");
        
        // Create the simulation with very small minimum time between events
        CloudSim simulation = new CloudSim(0.0001);
        
        // Create Datacenters - one for each layer
        Datacenter edgeDatacenter = createDatacenter(simulation, "Edge", 4, 8000, 8192, 100000, 10000, 0.003);
        Datacenter fogDatacenter = createDatacenter(simulation, "Fog", 8, 20000, 32768, 500000, 50000, 0.005);
        Datacenter cloudDatacenter = createDatacenter(simulation, "Cloud", 16, 50000, 65536, 1000000, 100000, 0.01);
        
        // Create a single broker with a very long VM destruction delay
        DatacenterBrokerSimple broker = new DatacenterBrokerSimple(simulation);
        broker.setVmDestructionDelay(Double.MAX_VALUE); // Prevent VM destruction during simulation
        
        // Create VMs for each layer
        List<Vm> edgeVms = createVms(0, 2, 2, 4000, 2048, 10000, 1000);
        List<Vm> fogVms = createVms(2, 2, 2, 8000, 4096, 20000, 2000);
        List<Vm> cloudVms = createVms(4, 1, 4, 16000, 8192, 40000, 5000);
        
        // Submit all VMs to the broker
        List<Vm> allVms = new ArrayList<>();
        allVms.addAll(edgeVms);
        allVms.addAll(fogVms);
        allVms.addAll(cloudVms);
        broker.submitVmList(allVms);
        
        // Create Cloudlets with extremely small lengths for guaranteed completion
        List<Cloudlet> edgeCloudlets = createCloudlets(0, 4, 1, 1, edgeVms);
        List<Cloudlet> fogCloudlets = createCloudlets(4, 4, 1, 1, fogVms);
        List<Cloudlet> cloudCloudlets = createCloudlets(8, 2, 2, 1, cloudVms);
        
        // Submit all cloudlets to the broker
        List<Cloudlet> allCloudlets = new ArrayList<>();
        allCloudlets.addAll(edgeCloudlets);
        allCloudlets.addAll(fogCloudlets);
        allCloudlets.addAll(cloudCloudlets);
        broker.submitCloudletList(allCloudlets);
        
        // Start the simulation
        System.out.println("Starting simulation...");
        simulation.start();
        
        // Get finished cloudlets
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        
        System.out.println("Simulation completed!");
        System.out.println("Number of finished cloudlets: " + finishedCloudlets.size());
        
        // Print detailed results
        new CloudletsTableBuilder(finishedCloudlets).build();
        
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
        
        // Calculate simple costs
        double edgeCost = calculateSimpleCost(edgeFinished, 0.003);
        double fogCost = calculateSimpleCost(fogFinished, 0.005);
        double cloudCost = calculateSimpleCost(cloudFinished, 0.01);
        
        System.out.println("\n=== Cost Analysis by Layer ===");
        System.out.printf("Edge Layer: $%.4f%n", edgeCost);
        System.out.printf("Fog Layer: $%.4f%n", fogCost);
        System.out.printf("Cloud Layer: $%.4f%n", cloudCost);
        System.out.printf("Total Cost: $%.4f%n", edgeCost + fogCost + cloudCost);
        
        // Print resource utilization
        System.out.println("\n=== Resource Utilization ===");
        printResourceUtilization(edgeVms, "Edge");
        printResourceUtilization(fogVms, "Fog");
        printResourceUtilization(cloudVms, "Cloud");
        
        // Save results to file
        saveResultsToFile(finishedCloudlets, edgeFinished, fogFinished, cloudFinished, 
                          edgeCost, fogCost, cloudCost);
        
        System.out.println("\nMinimal completion simulation finished successfully!");
    }
    
    private static double calculateSimpleCost(int finishedCloudlets, double costPerSec) {
        return finishedCloudlets * costPerSec;
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
            writer.println("Edge Layer: " + edgeFinished + " cloudlets finished");
            writer.println("Fog Layer: " + fogFinished + " cloudlets finished");
            writer.println("Cloud Layer: " + cloudFinished + " cloudlets finished");
            
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
        } catch (Exception e) {
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
        
        // Set scheduling interval to a very small value to improve simulation accuracy
        datacenter.setSchedulingInterval(0.0001);
        
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
