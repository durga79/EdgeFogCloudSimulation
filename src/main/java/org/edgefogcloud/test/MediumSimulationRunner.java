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
 * A medium-sized simulation runner for Edge-Fog-Cloud architecture
 * with moderate resource usage
 */
public class MediumSimulationRunner {
    
    // Simulation parameters
    private static final int EDGE_CLOUDLETS = 20;
    private static final int FOG_CLOUDLETS = 15;
    private static final int CLOUD_CLOUDLETS = 10;
    
    private static final int EDGE_VMS = 5;
    private static final int FOG_VMS = 2;
    private static final int CLOUD_VMS = 1;
    
    // Cost parameters
    private static final double COST_PER_CPU = 0.10;
    private static final double COST_PER_RAM = 0.05;
    private static final double COST_PER_STORAGE = 0.01;
    private static final double COST_PER_BW = 0.02;
    
    public static void main(String[] args) {
        System.out.println("Starting Medium Edge-Fog-Cloud Simulation...");
        
        // Create the simulation
        CloudSim simulation = new CloudSim();
        
        // Create Datacenters
        Datacenter edgeDatacenter = createDatacenter(simulation, "Edge", 4, 8000, 8192, 100000, 10000, 0.003);
        Datacenter fogDatacenter = createDatacenter(simulation, "Fog", 8, 20000, 32768, 500000, 50000, 0.005);
        Datacenter cloudDatacenter = createDatacenter(simulation, "Cloud", 16, 50000, 65536, 1000000, 100000, 0.01);
        
        // Create Broker
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);
        
        // Create VMs
        List<Vm> edgeVms = createVms(0, EDGE_VMS, 2, 8000, 4096, 50000, 5000);
        List<Vm> fogVms = createVms(EDGE_VMS, FOG_VMS, 4, 20000, 8192, 100000, 10000);
        List<Vm> cloudVms = createVms(EDGE_VMS + FOG_VMS, CLOUD_VMS, 8, 50000, 16384, 200000, 20000);
        
        List<Vm> allVms = new ArrayList<>();
        allVms.addAll(edgeVms);
        allVms.addAll(fogVms);
        allVms.addAll(cloudVms);
        
        // Create Cloudlets
        List<Cloudlet> edgeCloudlets = createCloudlets(0, EDGE_CLOUDLETS, 1, 1000, edgeVms);
        List<Cloudlet> fogCloudlets = createCloudlets(EDGE_CLOUDLETS, FOG_CLOUDLETS, 2, 3000, fogVms);
        List<Cloudlet> cloudCloudlets = createCloudlets(EDGE_CLOUDLETS + FOG_CLOUDLETS, CLOUD_CLOUDLETS, 4, 5000, cloudVms);
        
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
        double edgeCost = calculateLayerCost(edgeCloudlets);
        double fogCost = calculateLayerCost(fogCloudlets);
        double cloudCost = calculateLayerCost(cloudCloudlets);
        
        System.out.println("\n=== Cost Analysis by Layer ===");
        System.out.printf("Edge Layer: $%.4f%n", edgeCost);
        System.out.printf("Fog Layer: $%.4f%n", fogCost);
        System.out.printf("Cloud Layer: $%.4f%n", cloudCost);
        System.out.printf("Total Cost: $%.4f%n", edgeCost + fogCost + cloudCost);
        
        // Print processing distribution
        System.out.println("\n=== Processing Distribution ===");
        System.out.println("Edge Layer: " + edgeCloudlets.size() + " cloudlets (" + 
                String.format("%.1f%%", (double)edgeCloudlets.size()/allCloudlets.size()*100) + ")");
        System.out.println("Fog Layer: " + fogCloudlets.size() + " cloudlets (" + 
                String.format("%.1f%%", (double)fogCloudlets.size()/allCloudlets.size()*100) + ")");
        System.out.println("Cloud Layer: " + cloudCloudlets.size() + " cloudlets (" + 
                String.format("%.1f%%", (double)cloudCloudlets.size()/allCloudlets.size()*100) + ")");
        
        System.out.println("\nMedium simulation completed successfully!");
    }
    
    private static double calculateLayerCost(List<Cloudlet> cloudlets) {
        double totalCost = 0;
        for (Cloudlet cloudlet : cloudlets) {
            double cpuCost = cloudlet.getActualCpuTime() * COST_PER_CPU * cloudlet.getNumberOfPes();
            double ramCost = cloudlet.getVm().getRam().getCapacity() / 1024.0 * COST_PER_RAM * (cloudlet.getActualCpuTime() / 3600.0);
            double storageCost = cloudlet.getVm().getStorage().getCapacity() / 1024.0 * COST_PER_STORAGE * (cloudlet.getActualCpuTime() / 3600.0);
            double bwCost = cloudlet.getVm().getBw().getCapacity() / 1000.0 * COST_PER_BW * (cloudlet.getActualCpuTime() / 3600.0);
            totalCost += cpuCost + ramCost + storageCost + bwCost;
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
