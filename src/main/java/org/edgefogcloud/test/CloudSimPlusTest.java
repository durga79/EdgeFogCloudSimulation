package org.edgefogcloud.test;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudbus.cloudsim.power.models.PowerModelHostSimple;

/**
 * A simple test class to verify the correct package structure in CloudSim Plus 6.4.3
 */
public class CloudSimPlusTest {
    
    public static void main(String[] args) {
        System.out.println("Testing CloudSim Plus 7.3.3 package structure");
        
        // Create CloudSim instance
        CloudSim simulation = new CloudSim();
        System.out.println("CloudSim instance created: " + simulation);
        
        // Create ResourceProvisionerSimple instance
        ResourceProvisionerSimple resourceProvisioner = new ResourceProvisionerSimple();
        System.out.println("ResourceProvisionerSimple instance created: " + resourceProvisioner);
        
        // Create VmSchedulerTimeShared instance
        VmSchedulerTimeShared vmScheduler = new VmSchedulerTimeShared();
        System.out.println("VmSchedulerTimeShared instance created: " + vmScheduler);
        
        // Create PowerModelHostSimple instance
        PowerModelHostSimple powerModel = new PowerModelHostSimple(1000, 300);
        System.out.println("PowerModelHostSimple instance created: " + powerModel);
        
        System.out.println("All CloudSim Plus 6.4.3 classes loaded successfully!");
    }
}
