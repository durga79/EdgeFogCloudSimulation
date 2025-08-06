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
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;

import java.util.ArrayList;
import java.util.List;

/**
 * A minimal simulation runner for testing CloudSim Plus 6.4.3 compatibility
 * with extremely low resource usage
 */
public class MinimalSimulationRunner {
    
    private static final int HOST_PES = 4;
    private static final int VM_PES = 2;
    private static final int CLOUDLET_PES = 1;
    
    // Cost parameters
    private static final double COST_PER_CPU = 0.10;     // $ per CPU per hour
    private static final double COST_PER_RAM = 0.05;     // $ per GB RAM per hour
    private static final double COST_PER_STORAGE = 0.01; // $ per GB storage per hour
    private static final double COST_PER_BW = 0.02;      // $ per Mbps per hour
    
    public static void main(String[] args) {
        System.out.println("Starting Minimal Edge-Fog-Cloud Simulation...");
        
        // Create the simulation
        CloudSim simulation = new CloudSim();
        
        // Create Datacenter
        Datacenter datacenter = createDatacenter(simulation);
        
        // Create Broker
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);
        
        // Create VMs
        List<Vm> vmList = createVms(2);
        
        // Create Cloudlets
        List<Cloudlet> cloudletList = createCloudlets(4);
        
        // Submit VMs and Cloudlets to the broker
        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);
        
        // Start the simulation
        System.out.println("Starting simulation...");
        simulation.start();
        
        // Print results
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        System.out.println("Simulation completed!");
        System.out.println("Number of finished cloudlets: " + finishedCloudlets.size());
        
        // Calculate and display detailed costs for each cloudlet
        for (Cloudlet cloudlet : finishedCloudlets) {
            double cpuCost = cloudlet.getActualCpuTime() * COST_PER_CPU * cloudlet.getNumberOfPes();
            double ramCost = cloudlet.getVm().getRam().getCapacity() / 1024.0 * COST_PER_RAM * (cloudlet.getActualCpuTime() / 3600.0);
            double storageCost = cloudlet.getVm().getStorage().getCapacity() / 1024.0 * COST_PER_STORAGE * (cloudlet.getActualCpuTime() / 3600.0);
            double bwCost = cloudlet.getVm().getBw().getCapacity() / 1000.0 * COST_PER_BW * (cloudlet.getActualCpuTime() / 3600.0);
            double totalCost = cpuCost + ramCost + storageCost + bwCost;
            
            System.out.printf("Cloudlet %d: Execution time = %.2f seconds%n", cloudlet.getId(), cloudlet.getActualCpuTime());
            System.out.printf("  - CPU Cost: $%.4f (%.0f PEs at $%.2f per CPU-hour)%n", cpuCost, (double)cloudlet.getNumberOfPes(), COST_PER_CPU);
            System.out.printf("  - RAM Cost: $%.4f (%.2f GB at $%.2f per GB-hour)%n", ramCost, cloudlet.getVm().getRam().getCapacity() / 1024.0, COST_PER_RAM);
            System.out.printf("  - Storage Cost: $%.4f (%.2f GB at $%.2f per GB-hour)%n", storageCost, cloudlet.getVm().getStorage().getCapacity() / 1024.0, COST_PER_STORAGE);
            System.out.printf("  - Bandwidth Cost: $%.4f (%.2f Mbps at $%.2f per Mbps-hour)%n", bwCost, cloudlet.getVm().getBw().getCapacity() / 1000.0, COST_PER_BW);
            System.out.printf("  - Total Cost: $%.4f%n", totalCost);
        }
        
        System.out.println("Minimal simulation completed successfully!");
    }
    
    private static Datacenter createDatacenter(CloudSim simulation) {
        List<Host> hostList = new ArrayList<>();
        
        // Create PEs (Processing Elements)
        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < HOST_PES; i++) {
            peList.add(new PeSimple(1000));
        }
        
        // Create Host
        long ram = 2048; // in MB
        long storage = 1000000; // in MB
        long bw = 10000; // in Mbps
        
        Host host = new HostSimple(ram, bw, storage, peList);
        host.setVmScheduler(new VmSchedulerTimeShared());
        host.setRamProvisioner(new ResourceProvisionerSimple());
        host.setBwProvisioner(new ResourceProvisionerSimple());
        
        hostList.add(host);
        
        // Create a datacenter with costs
        DatacenterSimple datacenter = new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());
        
        // Set datacenter characteristics with costs
        datacenter.getCharacteristics()
            .setCostPerSecond(COST_PER_CPU)
            .setCostPerMem(COST_PER_RAM)
            .setCostPerStorage(COST_PER_STORAGE)
            .setCostPerBw(COST_PER_BW);
            
        return datacenter;
    }
    
    private static List<Vm> createVms(int count) {
        List<Vm> vmList = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Vm vm = new VmSimple(i, 1000, VM_PES);
            vm.setRam(512).setBw(1000).setSize(10000);
            vm.setCloudletScheduler(new CloudletSchedulerTimeShared());
            vmList.add(vm);
        }
        
        return vmList;
    }
    
    private static List<Cloudlet> createCloudlets(int count) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        
        long length = 10000; // in Million Instructions (MI)
        long fileSize = 300; // in bytes
        long outputSize = 300; // in bytes
        
        UtilizationModelDynamic utilizationModel = new UtilizationModelDynamic(0.5);
        
        for (int i = 0; i < count; i++) {
            Cloudlet cloudlet = new CloudletSimple(i, length, CLOUDLET_PES);
            cloudlet.setFileSize(fileSize)
                   .setOutputSize(outputSize)
                   .setUtilizationModelCpu(utilizationModel)
                   .setUtilizationModelRam(utilizationModel)
                   .setUtilizationModelBw(utilizationModel);
            cloudletList.add(cloudlet);
        }
        
        return cloudletList;
    }
}
