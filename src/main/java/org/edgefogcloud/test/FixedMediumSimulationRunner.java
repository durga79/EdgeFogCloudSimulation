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

/**
 * A fixed medium-sized simulation runner for Edge-Fog-Cloud architecture
 * with moderate resource usage and realistic timing
 */
public class FixedMediumSimulationRunner {
    
    // Simulation parameters
    private static final int EDGE_CLOUDLETS = 20;
    private static final int FOG_CLOUDLETS = 15;
    private static final int CLOUD_CLOUDLETS = 10;
    
    private static final int EDGE_VMS = 5;
    private static final int FOG_VMS = 2;
    private static final int CLOUD_VMS = 1;
    
    // Resource parameters - adjusted to avoid resource warnings
    private static final int EDGE_VM_PES = 2;
    private static final int FOG_VM_PES = 4;
    private static final int CLOUD_VM_PES = 8;
    
    private static final int EDGE_VM_MIPS = 4000;
    private static final int FOG_VM_MIPS = 8000;
    private static final int CLOUD_VM_MIPS = 16000;
    
    private static final int EDGE_VM_RAM = 2048;
    private static final int FOG_VM_RAM = 4096;
    private static final int CLOUD_VM_RAM = 8192;
    
    private static final int EDGE_VM_BW = 1000;
    private static final int FOG_VM_BW = 2000;
    private static final int CLOUD_VM_BW = 5000;
    
    // Cloudlet parameters - adjusted for realistic timing
    private static final int EDGE_CLOUDLET_LENGTH = 10000;
    private static final int FOG_CLOUDLET_LENGTH = 20000;
    private static final int CLOUD_CLOUDLET_LENGTH = 40000;
    
    // Cost parameters
    private static final double COST_PER_CPU = 0.10;
    private static final double COST_PER_RAM = 0.05;
    private static final double COST_PER_STORAGE = 0.01;
    private static final double COST_PER_BW = 0.02;
    
    // Lists to track VMs by layer
    private static List<Vm> edgeVms;
    private static List<Vm> fogVms;
    private static List<Vm> cloudVms;
    
    // Lists to track cloudlets by layer
    private static List<Cloudlet> edgeCloudlets;
    private static List<Cloudlet> fogCloudlets;
    private static List<Cloudlet> cloudCloudlets;
    
    public static void main(String[] args) {
        System.out.println("Starting Fixed Medium Edge-Fog-Cloud Simulation...");
        
        // Create the simulation
        CloudSim simulation = new CloudSim();
        
        // Create Datacenters
        Datacenter edgeDatacenter = createDatacenter(simulation, "Edge", 4, 8000, 8192, 100000, 10000, 0.003);
        Datacenter fogDatacenter = createDatacenter(simulation, "Fog", 8, 20000, 32768, 500000, 50000, 0.005);
        Datacenter cloudDatacenter = createDatacenter(simulation, "Cloud", 16, 50000, 65536, 1000000, 100000, 0.01);
        
        // Create Broker
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);
        
        // Create VMs
        edgeVms = createVms(0, EDGE_VMS, EDGE_VM_PES, EDGE_VM_MIPS, EDGE_VM_RAM, 10000, EDGE_VM_BW);
        fogVms = createVms(EDGE_VMS, FOG_VMS, FOG_VM_PES, FOG_VM_MIPS, FOG_VM_RAM, 20000, FOG_VM_BW);
        cloudVms = createVms(EDGE_VMS + FOG_VMS, CLOUD_VMS, CLOUD_VM_PES, CLOUD_VM_MIPS, CLOUD_VM_RAM, 40000, CLOUD_VM_BW);
        
        List<Vm> allVms = new ArrayList<>();
        allVms.addAll(edgeVms);
        allVms.addAll(fogVms);
        allVms.addAll(cloudVms);
        
        // Create Cloudlets
        edgeCloudlets = createCloudlets(0, EDGE_CLOUDLETS, 1, EDGE_CLOUDLET_LENGTH, edgeVms);
        fogCloudlets = createCloudlets(EDGE_CLOUDLETS, FOG_CLOUDLETS, 2, FOG_CLOUDLET_LENGTH, fogVms);
        cloudCloudlets = createCloudlets(EDGE_CLOUDLETS + FOG_CLOUDLETS, CLOUD_CLOUDLETS, 4, CLOUD_CLOUDLET_LENGTH, cloudVms);
        
        List<Cloudlet> allCloudlets = new ArrayList<>();
        allCloudlets.addAll(edgeCloudlets);
        allCloudlets.addAll(fogCloudlets);
        allCloudlets.addAll(cloudCloudlets);
        
        // Submit VMs and Cloudlets to the broker
        broker.submitVmList(allVms);
        broker.submitCloudletList(allCloudlets);
        
        // Start the simulation
        System.out.println("Starting simulation...");
        simulation.start();
        
        // Print results
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        System.out.println("Simulation completed!");
        System.out.println("Number of finished cloudlets: " + finishedCloudlets.size());
        
        // Print detailed results
        new CloudletsTableBuilder(finishedCloudlets).build();
        
        // Calculate and display costs by layer
        double edgeCost = calculateLayerCost(edgeCloudlets, finishedCloudlets);
        double fogCost = calculateLayerCost(fogCloudlets, finishedCloudlets);
        double cloudCost = calculateLayerCost(cloudCloudlets, finishedCloudlets);
        
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
        System.out.println("Edge Layer: " + edgeFinished + " of " + EDGE_CLOUDLETS + " cloudlets finished (" + 
                String.format("%.1f%%", (double)edgeFinished/EDGE_CLOUDLETS*100) + ")");
        System.out.println("Fog Layer: " + fogFinished + " of " + FOG_CLOUDLETS + " cloudlets finished (" + 
                String.format("%.1f%%", (double)fogFinished/FOG_CLOUDLETS*100) + ")");
        System.out.println("Cloud Layer: " + cloudFinished + " of " + CLOUD_CLOUDLETS + " cloudlets finished (" + 
                String.format("%.1f%%", (double)cloudFinished/CLOUD_CLOUDLETS*100) + ")");
        System.out.println("Total: " + finishedCloudlets.size() + " of " + allCloudlets.size() + " cloudlets finished (" + 
                String.format("%.1f%%", (double)finishedCloudlets.size()/allCloudlets.size()*100) + ")");
        
        System.out.println("\nFixed Medium simulation completed successfully!");
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
    
    private static double calculateLayerCost(List<Cloudlet> layerCloudlets, List<Cloudlet> finishedCloudlets) {
        double totalCost = 0;
        for (Cloudlet layerCloudlet : layerCloudlets) {
            for (Cloudlet finishedCloudlet : finishedCloudlets) {
                if (layerCloudlet.getId() == finishedCloudlet.getId()) {
                    double cpuCost = finishedCloudlet.getActualCpuTime() * COST_PER_CPU * finishedCloudlet.getNumberOfPes();
                    double ramCost = finishedCloudlet.getVm().getRam().getCapacity() / 1024.0 * COST_PER_RAM * (finishedCloudlet.getActualCpuTime() / 3600.0);
                    double storageCost = finishedCloudlet.getVm().getStorage().getCapacity() / 1024.0 * COST_PER_STORAGE * (finishedCloudlet.getActualCpuTime() / 3600.0);
                    double bwCost = finishedCloudlet.getVm().getBw().getCapacity() / 1000.0 * COST_PER_BW * (finishedCloudlet.getActualCpuTime() / 3600.0);
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
