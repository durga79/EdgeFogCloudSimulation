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
 * An optimized simulation runner with enhanced statistics for Edge-Fog-Cloud architecture
 * Combines the successful OptimizedSimpleRunner with detailed statistics
 */
public class OptimizedStatisticsRunner {
    
    public static void main(String[] args) {
        System.out.println("Starting Optimized Edge-Fog-Cloud Simulation with Enhanced Statistics...");
        
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
        
        // Create VM lists for each layer
        List<Vm> edgeVms = new ArrayList<>();
        edgeVms.add(edgeVm1);
        edgeVms.add(edgeVm2);
        
        List<Vm> fogVms = new ArrayList<>();
        fogVms.add(fogVm1);
        fogVms.add(fogVm2);
        
        List<Vm> cloudVms = new ArrayList<>();
        cloudVms.add(cloudVm);
        
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
        
        // Use our SimulationStatistics utility to print enhanced statistics
        SimulationStatistics.printEnhancedStatistics(
            edgeCloudlets, fogCloudlets, cloudCloudlets,
            finishedCloudlets, 
            edgeVms, fogVms, cloudVms,
            edgeDatacenter, fogDatacenter, cloudDatacenter
        );
        
        System.out.println("\nOptimized simulation with enhanced statistics finished successfully!");
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
