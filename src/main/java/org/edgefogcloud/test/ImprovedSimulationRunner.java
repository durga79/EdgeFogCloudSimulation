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
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.DecimalFormat;

/**
 * An improved simulation runner for CloudSim Plus 6.4.3 compatibility
 * with more detailed statistics and metrics
 */
public class ImprovedSimulationRunner {
    
    // Simulation parameters
    private static final int EDGE_HOST_PES = 4;
    private static final int FOG_HOST_PES = 6;
    private static final int CLOUD_HOST_PES = 8;
    
    private static final int EDGE_VM_PES = 2;
    private static final int FOG_VM_PES = 3;
    private static final int CLOUD_VM_PES = 4;
    
    private static final int CLOUDLET_PES = 1;
    
    // Resource capacities
    private static final int EDGE_MIPS = 1000;
    private static final int FOG_MIPS = 2000;
    private static final int CLOUD_MIPS = 3000;
    
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
    
    public static void main(String[] args) {
        System.out.println("Starting Improved Edge-Fog-Cloud Simulation...");
        
        // Create the simulation
        CloudSim simulation = new CloudSim();
        
        // Create Datacenters for Edge, Fog, and Cloud
        Datacenter edgeDatacenter = createDatacenter(simulation, "Edge", EDGE_HOST_PES, EDGE_MIPS);
        Datacenter fogDatacenter = createDatacenter(simulation, "Fog", FOG_HOST_PES, FOG_MIPS);
        Datacenter cloudDatacenter = createDatacenter(simulation, "Cloud", CLOUD_HOST_PES, CLOUD_MIPS);
        
        // Create Broker
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);
        
        // Create VMs for each layer
        List<Vm> edgeVms = createVms(2, EDGE_VM_PES, EDGE_MIPS);
        List<Vm> fogVms = createVms(2, FOG_VM_PES, FOG_MIPS);
        List<Vm> cloudVms = createVms(2, CLOUD_VM_PES, CLOUD_MIPS);
        
        // Combine all VMs
        List<Vm> allVms = new ArrayList<>();
        allVms.addAll(edgeVms);
        allVms.addAll(fogVms);
        allVms.addAll(cloudVms);
        
        // Create Cloudlets
        List<Cloudlet> edgeCloudlets = createCloudlets(2, 10000, CLOUDLET_PES);
        List<Cloudlet> fogCloudlets = createCloudlets(2, 20000, CLOUDLET_PES);
        List<Cloudlet> cloudCloudlets = createCloudlets(2, 30000, CLOUDLET_PES);
        
        // Combine all cloudlets
        List<Cloudlet> allCloudlets = new ArrayList<>();
        allCloudlets.addAll(edgeCloudlets);
        allCloudlets.addAll(fogCloudlets);
        allCloudlets.addAll(cloudCloudlets);
        
        // Submit VMs and Cloudlets to the broker
        broker.submitVmList(allVms);
        broker.submitCloudletList(allCloudlets);
        
        // Map specific cloudlets to specific VMs
        for (int i = 0; i < edgeCloudlets.size(); i++) {
            broker.bindCloudletToVm(edgeCloudlets.get(i), edgeVms.get(i % edgeVms.size()));
        }
        
        for (int i = 0; i < fogCloudlets.size(); i++) {
            broker.bindCloudletToVm(fogCloudlets.get(i), fogVms.get(i % fogVms.size()));
        }
        
        for (int i = 0; i < cloudCloudlets.size(); i++) {
            broker.bindCloudletToVm(cloudCloudlets.get(i), cloudVms.get(i % cloudVms.size()));
        }
        
        // Start the simulation
        System.out.println("Starting simulation...");
        simulation.start();
        
        // Print results
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        System.out.println("\n========== SIMULATION RESULTS ==========");
        System.out.println("Total number of finished cloudlets: " + finishedCloudlets.size());
        
        // Track statistics by layer
        Map<String, Double> totalCostByLayer = new HashMap<>();
        Map<String, Double> totalEnergyByLayer = new HashMap<>();
        Map<String, Integer> cloudletCountByLayer = new HashMap<>();
        Map<String, Double> totalExecutionTimeByLayer = new HashMap<>();
        
        totalCostByLayer.put("Edge", 0.0);
        totalCostByLayer.put("Fog", 0.0);
        totalCostByLayer.put("Cloud", 0.0);
        
        totalEnergyByLayer.put("Edge", 0.0);
        totalEnergyByLayer.put("Fog", 0.0);
        totalEnergyByLayer.put("Cloud", 0.0);
        
        cloudletCountByLayer.put("Edge", 0);
        cloudletCountByLayer.put("Fog", 0);
        cloudletCountByLayer.put("Cloud", 0);
        
        totalExecutionTimeByLayer.put("Edge", 0.0);
        totalExecutionTimeByLayer.put("Fog", 0.0);
        totalExecutionTimeByLayer.put("Cloud", 0.0);
        
        // Calculate and display detailed costs and statistics for each cloudlet
        DecimalFormat df = new DecimalFormat("#.####");
        
        System.out.println("\n========== CLOUDLET DETAILS ==========");
        for (Cloudlet cloudlet : finishedCloudlets) {
            // Determine which layer this cloudlet belongs to based on VM MIPS
            String layer = getLayerFromVm(cloudlet.getVm());
            
            // Calculate costs
            double cpuCost = calculateCpuCost(cloudlet, layer);
            double ramCost = cloudlet.getVm().getRam().getCapacity() / 1024.0 * COST_PER_RAM * (cloudlet.getActualCpuTime() / 3600.0);
            double storageCost = cloudlet.getVm().getStorage().getCapacity() / 1024.0 * COST_PER_STORAGE * (cloudlet.getActualCpuTime() / 3600.0);
            double bwCost = cloudlet.getVm().getBw().getCapacity() / 1000.0 * COST_PER_BW * (cloudlet.getActualCpuTime() / 3600.0);
            double totalCost = cpuCost + ramCost + storageCost + bwCost;
            
            // Calculate energy consumption (simplified model)
            double energyConsumption = calculateEnergyConsumption(cloudlet, layer);
            
            // Calculate network latency (simplified model)
            double latency = calculateNetworkLatency(layer);
            
            // Update statistics
            totalCostByLayer.put(layer, totalCostByLayer.get(layer) + totalCost);
            totalEnergyByLayer.put(layer, totalEnergyByLayer.get(layer) + energyConsumption);
            cloudletCountByLayer.put(layer, cloudletCountByLayer.get(layer) + 1);
            totalExecutionTimeByLayer.put(layer, totalExecutionTimeByLayer.get(layer) + cloudlet.getActualCpuTime());
            
            // Print cloudlet details
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
        
        // Print summary statistics by layer
        System.out.println("\n========== LAYER STATISTICS ==========");
        for (String layer : new String[]{"Edge", "Fog", "Cloud"}) {
            int count = cloudletCountByLayer.get(layer);
            if (count > 0) {
                double avgExecTime = totalExecutionTimeByLayer.get(layer) / count;
                System.out.println("\n" + layer + " Layer Statistics:");
                System.out.println("  - Number of Cloudlets: " + count);
                System.out.println("  - Total Cost: $" + df.format(totalCostByLayer.get(layer)));
                System.out.println("  - Average Cost per Cloudlet: $" + df.format(totalCostByLayer.get(layer) / count));
                System.out.println("  - Total Energy Consumption: " + df.format(totalEnergyByLayer.get(layer)) + " Wh");
                System.out.println("  - Average Energy per Cloudlet: " + df.format(totalEnergyByLayer.get(layer) / count) + " Wh");
                System.out.println("  - Average Network Latency: " + df.format(calculateNetworkLatency(layer)) + " ms");
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
        
        System.out.println("\nImproved simulation completed successfully!");
    }
    
    private static Datacenter createDatacenter(CloudSim simulation, String name, int hostPes, int mips) {
        List<Host> hostList = new ArrayList<>();
        
        // Create PEs (Processing Elements)
        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < hostPes; i++) {
            peList.add(new PeSimple(mips));
        }
        
        // RAM, Storage, and Bandwidth values based on layer
        int ram = name.equals("Edge") ? 4096 : name.equals("Fog") ? 8192 : 16384; // MB
        int storage = name.equals("Edge") ? 10000 : name.equals("Fog") ? 50000 : 100000; // MB
        int bw = name.equals("Edge") ? 1000 : name.equals("Fog") ? 5000 : 10000; // Mbps
        
        // Create a Host
        Host host = new HostSimple(ram, bw, storage, peList);
        host.setVmScheduler(new VmSchedulerTimeShared());
        host.setRamProvisioner(new ResourceProvisionerSimple());
        host.setBwProvisioner(new ResourceProvisionerSimple());
        hostList.add(host);
        
        // Create a Datacenter
        Datacenter datacenter = new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());
        datacenter.setName(name + "Datacenter");
        return datacenter;
    }
    
    private static List<Vm> createVms(int numVms, int vmPes, int mips) {
        List<Vm> vmList = new ArrayList<>();
        
        for (int i = 0; i < numVms; i++) {
            // Create a VM
            Vm vm = new VmSimple(mips, vmPes);
            
            // Set RAM, Storage, and Bandwidth based on MIPS (to identify layer)
            if (mips == EDGE_MIPS) {
                vm.setRam(2048).setBw(1000).setSize(10000L);
            } else if (mips == FOG_MIPS) {
                vm.setRam(4096).setBw(5000).setSize(50000L);
            } else { // CLOUD_MIPS
                vm.setRam(8192).setBw(10000).setSize(100000L);
            }
            
            vm.setCloudletScheduler(new CloudletSchedulerTimeShared());
            vmList.add(vm);
        }
        
        return vmList;
    }
    
    private static List<Cloudlet> createCloudlets(int numCloudlets, long length, int pes) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        
        UtilizationModelDynamic utilizationCpu = new UtilizationModelDynamic(0.8);
        UtilizationModelDynamic utilizationRam = new UtilizationModelDynamic(0.4);
        UtilizationModelDynamic utilizationBw = new UtilizationModelDynamic(0.3);
        
        for (int i = 0; i < numCloudlets; i++) {
            // Create a Cloudlet
            Cloudlet cloudlet = new CloudletSimple(length, pes);
            cloudlet.setUtilizationModelCpu(utilizationCpu);
            cloudlet.setUtilizationModelRam(utilizationRam);
            cloudlet.setUtilizationModelBw(utilizationBw);
            cloudletList.add(cloudlet);
        }
        
        return cloudletList;
    }
    
    private static String getLayerFromVm(Vm vm) {
        double mips = vm.getMips();
        if (mips == EDGE_MIPS) {
            return "Edge";
        } else if (mips == FOG_MIPS) {
            return "Fog";
        } else {
            return "Cloud";
        }
    }
    
    private static double calculateCpuCost(Cloudlet cloudlet, String layer) {
        double costPerCpu = layer.equals("Edge") ? COST_PER_CPU_EDGE : 
                           layer.equals("Fog") ? COST_PER_CPU_FOG : COST_PER_CPU_CLOUD;
        
        return cloudlet.getActualCpuTime() * costPerCpu * cloudlet.getNumberOfPes() / 3600.0;
    }
    
    private static double calculateEnergyConsumption(Cloudlet cloudlet, String layer) {
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
