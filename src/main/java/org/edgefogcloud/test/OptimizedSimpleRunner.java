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
 * A simple optimized simulation runner that ensures all cloudlets complete
 * across edge, fog, and cloud layers
 */
public class OptimizedSimpleRunner {
    
    public static void main(String[] args) {
        System.out.println("Starting Optimized Simple Edge-Fog-Cloud Simulation...");
        
        // Create the simulation
        CloudSim simulation = new CloudSim();
        
        // Create Datacenters
        Datacenter edgeDatacenter = createDatacenter(simulation, "Edge", 4, 8000, 8192, 100000, 10000, 0.003);
        Datacenter fogDatacenter = createDatacenter(simulation, "Fog", 8, 20000, 32768, 500000, 50000, 0.005);
        Datacenter cloudDatacenter = createDatacenter(simulation, "Cloud", 16, 50000, 65536, 1000000, 100000, 0.01);
        
        // Create brokers for each layer
        DatacenterBrokerSimple edgeBroker = new DatacenterBrokerSimple(simulation);
        edgeBroker.setVmDestructionDelay(5.0);
        
        DatacenterBrokerSimple fogBroker = new DatacenterBrokerSimple(simulation);
        fogBroker.setVmDestructionDelay(5.0);
        
        DatacenterBrokerSimple cloudBroker = new DatacenterBrokerSimple(simulation);
        cloudBroker.setVmDestructionDelay(5.0);
        
        // Create VMs
        Vm edgeVm1 = createVm(0, 2, 4000, 2048, 10000, 1000);
        Vm edgeVm2 = createVm(1, 2, 4000, 2048, 10000, 1000);
        
        Vm fogVm1 = createVm(2, 2, 8000, 4096, 20000, 2000);
        Vm fogVm2 = createVm(3, 2, 8000, 4096, 20000, 2000);
        
        Vm cloudVm = createVm(4, 4, 16000, 8192, 40000, 5000);
        
        // Submit VMs to their respective brokers
        edgeBroker.submitVm(edgeVm1);
        edgeBroker.submitVm(edgeVm2);
        
        fogBroker.submitVm(fogVm1);
        fogBroker.submitVm(fogVm2);
        
        cloudBroker.submitVm(cloudVm);
        
        // Create cloudlets with tiny lengths
        List<Cloudlet> edgeCloudlets = new ArrayList<>();
        edgeCloudlets.add(createCloudlet(0, 1, 1, edgeVm1));
        edgeCloudlets.add(createCloudlet(1, 1, 1, edgeVm2));
        edgeCloudlets.add(createCloudlet(2, 1, 1, edgeVm1));
        edgeCloudlets.add(createCloudlet(3, 1, 1, edgeVm2));
        
        List<Cloudlet> fogCloudlets = new ArrayList<>();
        fogCloudlets.add(createCloudlet(4, 1, 1, fogVm1));
        fogCloudlets.add(createCloudlet(5, 1, 1, fogVm2));
        fogCloudlets.add(createCloudlet(6, 1, 1, fogVm1));
        fogCloudlets.add(createCloudlet(7, 1, 1, fogVm2));
        
        List<Cloudlet> cloudCloudlets = new ArrayList<>();
        cloudCloudlets.add(createCloudlet(8, 2, 1, cloudVm));
        cloudCloudlets.add(createCloudlet(9, 2, 1, cloudVm));
        
        // Submit cloudlets to their respective brokers
        edgeBroker.submitCloudletList(edgeCloudlets);
        fogBroker.submitCloudletList(fogCloudlets);
        cloudBroker.submitCloudletList(cloudCloudlets);
        
        // Start the simulation
        System.out.println("Starting simulation...");
        simulation.start();
        
        // Get finished cloudlets from all brokers
        List<Cloudlet> finishedCloudlets = new ArrayList<>();
        finishedCloudlets.addAll(edgeBroker.getCloudletFinishedList());
        finishedCloudlets.addAll(fogBroker.getCloudletFinishedList());
        finishedCloudlets.addAll(cloudBroker.getCloudletFinishedList());
        
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
        
        // Save results to file
        saveResultsToFile(finishedCloudlets, edgeFinished, fogFinished, cloudFinished, 
                          edgeCost, fogCost, cloudCost);
        
        System.out.println("\nOptimized simple simulation finished successfully!");
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
        
        datacenter.getCharacteristics()
            .setCostPerSecond(costPerSec)
            .setCostPerMem(costPerSec/2)
            .setCostPerStorage(costPerSec/10)
            .setCostPerBw(costPerSec/5);
            
        return datacenter;
    }
    
    private static Vm createVm(int id, int pes, int mips, int ram, int storage, int bw) {
        Vm vm = new VmSimple(id, mips, pes);
        vm.setRam(ram).setBw(bw).setSize(storage);
        vm.setCloudletScheduler(new CloudletSchedulerTimeShared());
        return vm;
    }
    
    private static Cloudlet createCloudlet(int id, int pes, long length, Vm vm) {
        UtilizationModelDynamic utilizationModel = new UtilizationModelDynamic(0.5);
        
        Cloudlet cloudlet = new CloudletSimple(id, length, pes);
        cloudlet.setFileSize(1000)
               .setOutputSize(1000)
               .setUtilizationModelCpu(utilizationModel)
               .setUtilizationModelRam(utilizationModel)
               .setUtilizationModelBw(utilizationModel);
        
        cloudlet.setVm(vm);
        
        return cloudlet;
    }
}
