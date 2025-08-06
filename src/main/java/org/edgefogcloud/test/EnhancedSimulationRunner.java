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
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;

import java.util.*;
import java.text.DecimalFormat;

/**
 * An enhanced simulation runner for Edge-Fog-Cloud architecture with detailed metrics
 * Provides comprehensive statistics on resource usage, costs, and performance
 */
public class EnhancedSimulationRunner {
    
    // Simulation parameters
    private static final int EDGE_HOSTS = 2;
    private static final int FOG_HOSTS = 2;
    private static final int CLOUD_HOSTS = 1;
    
    private static final int EDGE_HOST_PES = 4;
    private static final int FOG_HOST_PES = 8;
    private static final int CLOUD_HOST_PES = 16;
    
    private static final int EDGE_VMS = 4;
    private static final int FOG_VMS = 4;
    private static final int CLOUD_VMS = 2;
    
    private static final int EDGE_VM_PES = 2;
    private static final int FOG_VM_PES = 4;
    private static final int CLOUD_VM_PES = 8;
    
    private static final int CLOUDLETS = 20;
    private static final long CLOUDLET_LENGTH = 10000;
    
    // Resource capacities
    private static final int EDGE_MIPS = 1000;
    private static final int FOG_MIPS = 2000;
    private static final int CLOUD_MIPS = 3000;
    
    private static final int EDGE_RAM = 2048; // MB
    private static final int FOG_RAM = 4096;  // MB
    private static final int CLOUD_RAM = 8192; // MB
    
    private static final int EDGE_STORAGE = 10000; // MB
    private static final int FOG_STORAGE = 50000;  // MB
    private static final int CLOUD_STORAGE = 100000; // MB
    
    private static final int EDGE_BW = 1000; // Mbps
    private static final int FOG_BW = 5000;  // Mbps
    private static final int CLOUD_BW = 10000; // Mbps
    
    // Cost parameters
    private static final double COST_PER_CPU_EDGE = 0.05;  // $ per CPU per hour
    private static final double COST_PER_CPU_FOG = 0.08;   // $ per CPU per hour
    private static final double COST_PER_CPU_CLOUD = 0.12; // $ per CPU per hour
    
    private static final double COST_PER_RAM = 0.05;     // $ per GB RAM per hour
    private static final double COST_PER_STORAGE = 0.01; // $ per GB storage per hour
    private static final double COST_PER_BW = 0.02;      // $ per Mbps per hour
    
    // Energy consumption parameters (Watts)
    private static final double EDGE_ENERGY_PER_HOST = 100;
    private static final double FOG_ENERGY_PER_HOST = 200;
    private static final double CLOUD_ENERGY_PER_HOST = 400;
    
    // Network latency parameters (ms)
    private static final double EDGE_LATENCY = 5;
    private static final double FOG_LATENCY = 20;
    private static final double CLOUD_LATENCY = 100;
    
    // Statistics tracking
    private static Map<String, Double> totalCostByLayer = new HashMap<>();
    private static Map<String, Double> totalEnergyByLayer = new HashMap<>();
    private static Map<String, Double> avgLatencyByLayer = new HashMap<>();
    private static Map<String, Integer> cloudletCountByLayer = new HashMap<>();
    private static Map<String, Double> totalExecutionTimeByLayer = new HashMap<>();
    
    public static void main(String[] args) {
        System.out.println("Starting Enhanced Edge-Fog-Cloud Simulation...");
        
        // Initialize statistics maps
        totalCostByLayer.put("Edge", 0.0);
        totalCostByLayer.put("Fog", 0.0);
        totalCostByLayer.put("Cloud", 0.0);
        
        totalEnergyByLayer.put("Edge", 0.0);
        totalEnergyByLayer.put("Fog", 0.0);
        totalEnergyByLayer.put("Cloud", 0.0);
        
        avgLatencyByLayer.put("Edge", 0.0);
        avgLatencyByLayer.put("Fog", 0.0);
        avgLatencyByLayer.put("Cloud", 0.0);
        
        cloudletCountByLayer.put("Edge", 0);
        cloudletCountByLayer.put("Fog", 0);
        cloudletCountByLayer.put("Cloud", 0);
        
        totalExecutionTimeByLayer.put("Edge", 0.0);
        totalExecutionTimeByLayer.put("Fog", 0.0);
        totalExecutionTimeByLayer.put("Cloud", 0.0);
        
        // Create the simulation
        CloudSim simulation = new CloudSim();
        
        // Create Datacenters for Edge, Fog, and Cloud
        Datacenter edgeDatacenter = createDatacenter(simulation, "Edge", EDGE_HOSTS, EDGE_HOST_PES, EDGE_MIPS);
        Datacenter fogDatacenter = createDatacenter(simulation, "Fog", FOG_HOSTS, FOG_HOST_PES, FOG_MIPS);
        Datacenter cloudDatacenter = createDatacenter(simulation, "Cloud", CLOUD_HOSTS, CLOUD_HOST_PES, CLOUD_MIPS);
        
        // Create Broker
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);
        
        // Create VMs for each layer
        List<Vm> edgeVmList = createVms("Edge", EDGE_VMS, EDGE_VM_PES, EDGE_MIPS, EDGE_RAM, EDGE_STORAGE, EDGE_BW);
        List<Vm> fogVmList = createVms("Fog", FOG_VMS, FOG_VM_PES, FOG_MIPS, FOG_RAM, FOG_STORAGE, FOG_BW);
        List<Vm> cloudVmList = createVms("Cloud", CLOUD_VMS, CLOUD_VM_PES, CLOUD_MIPS, CLOUD_RAM, CLOUD_STORAGE, CLOUD_BW);
        
        // Combine all VMs
        List<Vm> allVms = new ArrayList<>();
        allVms.addAll(edgeVmList);
        allVms.addAll(fogVmList);
        allVms.addAll(cloudVmList);
        
        // Create Cloudlets with different characteristics for each layer
        List<Cloudlet> edgeCloudlets = createCloudlets("Edge", CLOUDLETS/2, CLOUDLET_LENGTH/2, 1);
        List<Cloudlet> fogCloudlets = createCloudlets("Fog", CLOUDLETS/4, CLOUDLET_LENGTH, 2);
        List<Cloudlet> cloudCloudlets = createCloudlets("Cloud", CLOUDLETS/4, CLOUDLET_LENGTH*2, 4);
        
        // Combine all cloudlets
        List<Cloudlet> allCloudlets = new ArrayList<>();
        allCloudlets.addAll(edgeCloudlets);
        allCloudlets.addAll(fogCloudlets);
        allCloudlets.addAll(cloudCloudlets);
        
        // Submit VMs and Cloudlets to the broker
        broker.submitVmList(allVms);
        broker.submitCloudletList(allCloudlets);
        
        // Map specific cloudlets to specific VMs based on layer
        for (Cloudlet cloudlet : edgeCloudlets) {
            broker.bindCloudletToVm(cloudlet, edgeVmList.get((int)(cloudlet.getId() % edgeVmList.size())));
        }
        
        for (Cloudlet cloudlet : fogCloudlets) {
            broker.bindCloudletToVm(cloudlet, fogVmList.get((int)(cloudlet.getId() % fogVmList.size())));
        }
        
        for (Cloudlet cloudlet : cloudCloudlets) {
            broker.bindCloudletToVm(cloudlet, cloudVmList.get((int)(cloudlet.getId() % cloudVmList.size())));
        }
        
        // Start the simulation
        System.out.println("Starting simulation...");
        simulation.start();
        
        // Print results
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        System.out.println("\n========== SIMULATION RESULTS ==========");
        System.out.println("Total number of finished cloudlets: " + finishedCloudlets.size());
        
        // Calculate and display detailed costs and statistics for each cloudlet
        DecimalFormat df = new DecimalFormat("#.####");
        
        System.out.println("\n========== CLOUDLET DETAILS ==========");
        for (Cloudlet cloudlet : finishedCloudlets) {
            String layer = getCloudletLayer(cloudlet);
            double cpuCost = calculateCpuCost(cloudlet);
            double ramCost = cloudlet.getVm().getRam().getCapacity() / 1024.0 * COST_PER_RAM * (cloudlet.getActualCpuTime() / 3600.0);
            double storageCost = cloudlet.getVm().getStorage().getCapacity() / 1024.0 * COST_PER_STORAGE * (cloudlet.getActualCpuTime() / 3600.0);
            double bwCost = cloudlet.getVm().getBw().getCapacity() / 1000.0 * COST_PER_BW * (cloudlet.getActualCpuTime() / 3600.0);
            double totalCost = cpuCost + ramCost + storageCost + bwCost;
            
            // Update statistics
            totalCostByLayer.put(layer, totalCostByLayer.get(layer) + totalCost);
            cloudletCountByLayer.put(layer, cloudletCountByLayer.get(layer) + 1);
            totalExecutionTimeByLayer.put(layer, totalExecutionTimeByLayer.get(layer) + cloudlet.getActualCpuTime());
            
            // Calculate energy consumption (simplified model)
            double energyConsumption = calculateEnergyConsumption(cloudlet);
            totalEnergyByLayer.put(layer, totalEnergyByLayer.get(layer) + energyConsumption);
            
            // Calculate network latency (simplified model)
            double latency = calculateNetworkLatency(layer);
            avgLatencyByLayer.put(layer, avgLatencyByLayer.get(layer) + latency);
            
            System.out.println("\nCloudlet " + cloudlet.getId() + " (" + layer + " Layer)");
            System.out.println("  - Status: " + cloudlet.getStatus());
            System.out.println("  - Start Time: " + df.format(cloudlet.getExecStartTime()) + " sec");
            System.out.println("  - Finish Time: " + df.format(cloudlet.getFinishTime()) + " sec");
            System.out.println("  - Execution Time: " + df.format(cloudlet.getActualCpuTime()) + " sec");
            System.out.println("  - Processing Cost: $" + df.format(cpuCost) + " (" + cloudlet.getNumberOfPes() + " PEs)");
            System.out.println("  - Memory Cost: $" + df.format(ramCost) + " (" + (cloudlet.getVm().getRam().getCapacity() / 1024.0) + " GB)");
            System.out.println("  - Storage Cost: $" + df.format(storageCost) + " (" + (cloudlet.getVm().getStorage().getCapacity() / 1024.0) + " GB)");
            System.out.println("  - Bandwidth Cost: $" + df.format(bwCost) + " (" + (cloudlet.getVm().getBw().getCapacity() / 1000.0) + " Mbps)");
            System.out.println("  - Total Cost: $" + df.format(totalCost));
            System.out.println("  - Energy Consumption: " + df.format(energyConsumption) + " Wh");
            System.out.println("  - Network Latency: " + df.format(latency) + " ms");
        }
        
        // Calculate average latency
        for (String layer : avgLatencyByLayer.keySet()) {
            if (cloudletCountByLayer.get(layer) > 0) {
                avgLatencyByLayer.put(layer, avgLatencyByLayer.get(layer) / cloudletCountByLayer.get(layer));
            }
        }
        
        // Print summary statistics by layer
        System.out.println("\n========== LAYER STATISTICS ==========");
        for (String layer : Arrays.asList("Edge", "Fog", "Cloud")) {
            int count = cloudletCountByLayer.get(layer);
            if (count > 0) {
                double avgExecTime = totalExecutionTimeByLayer.get(layer) / count;
                System.out.println("\n" + layer + " Layer Statistics:");
                System.out.println("  - Number of Cloudlets: " + count);
                System.out.println("  - Total Cost: $" + df.format(totalCostByLayer.get(layer)));
                System.out.println("  - Average Cost per Cloudlet: $" + df.format(totalCostByLayer.get(layer) / count));
                System.out.println("  - Total Energy Consumption: " + df.format(totalEnergyByLayer.get(layer)) + " Wh");
                System.out.println("  - Average Energy per Cloudlet: " + df.format(totalEnergyByLayer.get(layer) / count) + " Wh");
                System.out.println("  - Average Network Latency: " + df.format(avgLatencyByLayer.get(layer)) + " ms");
                System.out.println("  - Average Execution Time: " + df.format(avgExecTime) + " sec");
            }
        }
        
        // Print overall statistics
        double totalCost = totalCostByLayer.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalEnergy = totalEnergyByLayer.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalExecTime = totalExecutionTimeByLayer.values().stream().mapToDouble(Double::doubleValue).sum();
        
        System.out.println("\n========== OVERALL STATISTICS ==========");
        System.out.println("Total Simulation Cost: $" + df.format(totalCost));
        System.out.println("Total Energy Consumption: " + df.format(totalEnergy) + " Wh");
        System.out.println("Average Cost per Cloudlet: $" + df.format(totalCost / finishedCloudlets.size()));
        System.out.println("Average Energy per Cloudlet: " + df.format(totalEnergy / finishedCloudlets.size()) + " Wh");
        System.out.println("Average Execution Time per Cloudlet: " + df.format(totalExecTime / finishedCloudlets.size()) + " sec");
        System.out.println("\nEnhanced simulation completed successfully!");
    }
    
    private static Datacenter createDatacenter(CloudSim simulation, String name, int numHosts, int hostPes, int mips) {
        List<Host> hostList = new ArrayList<>();
        
        for (int i = 0; i < numHosts; i++) {
            List<Pe> peList = new ArrayList<>();
            for (int j = 0; j < hostPes; j++) {
                peList.add(new PeSimple(mips));
            }
            
            int ram = name.equals("Edge") ? EDGE_RAM * 2 : 
                     name.equals("Fog") ? FOG_RAM * 2 : CLOUD_RAM * 2;
            int storage = name.equals("Edge") ? EDGE_STORAGE * 5 : 
                         name.equals("Fog") ? FOG_STORAGE * 5 : CLOUD_STORAGE * 5;
            int bw = name.equals("Edge") ? EDGE_BW * 2 : 
                    name.equals("Fog") ? FOG_BW * 2 : CLOUD_BW * 2;
            
            Host host = new HostSimple(ram, bw, storage, peList);
            host.setVmScheduler(new VmSchedulerTimeShared());
            host.setRamProvisioner(new ResourceProvisionerSimple());
            host.setBwProvisioner(new ResourceProvisionerSimple());
            hostList.add(host);
        }
        
        Datacenter datacenter = new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());
        datacenter.setName(name + "Datacenter");
        return datacenter;
    }
    
    private static List<Vm> createVms(String layer, int numVms, int vmPes, int mips, int ram, int storage, int bw) {
        List<Vm> vmList = new ArrayList<>();
        
        for (int i = 0; i < numVms; i++) {
            Vm vm = new VmSimple(mips, vmPes);
            vm.setRam(ram).setBw(bw).setSize(storage);
            vm.setCloudletScheduler(new CloudletSchedulerTimeShared());
            vm.setDescription(layer);
            vmList.add(vm);
        }
        
        return vmList;
    }
    
    private static List<Cloudlet> createCloudlets(String layer, int numCloudlets, long length, int pes) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        
        UtilizationModel utilizationCpu = new UtilizationModelDynamic(0.8);
        UtilizationModel utilizationRam = new UtilizationModelDynamic(0.4);
        UtilizationModel utilizationBw = new UtilizationModelDynamic(0.3);
        
        for (int i = 0; i < numCloudlets; i++) {
            Cloudlet cloudlet = new CloudletSimple(length, pes);
            cloudlet.setUtilizationModelCpu(utilizationCpu);
            cloudlet.setUtilizationModelRam(utilizationRam);
            cloudlet.setUtilizationModelBw(utilizationBw);
            // Store layer information in user-defined data field
            cloudlet.addRequiredFile(layer); // Using addRequiredFile as a workaround to store layer info
            cloudletList.add(cloudlet);
        }
        
        return cloudletList;
    }
    
    private static String getCloudletLayer(Cloudlet cloudlet) {
        // Get the first required file which contains our layer info
        if (cloudlet.getRequiredFiles().size() > 0) {
            return cloudlet.getRequiredFiles().iterator().next();
        }
        return "Unknown";
    }
    
    private static double calculateCpuCost(Cloudlet cloudlet) {
        String layer = getCloudletLayer(cloudlet);
        double costPerCpu = layer.equals("Edge") ? COST_PER_CPU_EDGE : 
                           layer.equals("Fog") ? COST_PER_CPU_FOG : COST_PER_CPU_CLOUD;
        
        return cloudlet.getActualCpuTime() * costPerCpu * cloudlet.getNumberOfPes() / 3600.0;
    }
    
    private static double calculateEnergyConsumption(Cloudlet cloudlet) {
        String layer = getCloudletLayer(cloudlet);
        double powerPerHost = layer.equals("Edge") ? EDGE_ENERGY_PER_HOST : 
                             layer.equals("Fog") ? FOG_ENERGY_PER_HOST : CLOUD_ENERGY_PER_HOST;
        
        // Convert seconds to hours and calculate energy in Watt-hours
        double executionTimeInHours = cloudlet.getActualCpuTime() / 3600.0;
        double utilizationRatio = cloudlet.getUtilizationOfCpu();
        
        return powerPerHost * executionTimeInHours * utilizationRatio * cloudlet.getNumberOfPes() / 
               (layer.equals("Edge") ? EDGE_HOST_PES : layer.equals("Fog") ? FOG_HOST_PES : CLOUD_HOST_PES);
    }
    
    private static double calculateNetworkLatency(String layer) {
        return layer.equals("Edge") ? EDGE_LATENCY : 
               layer.equals("Fog") ? FOG_LATENCY : CLOUD_LATENCY;
    }
}
