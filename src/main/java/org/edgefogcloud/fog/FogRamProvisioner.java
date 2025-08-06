package org.edgefogcloud.fog;

import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Ram;

/**
 * RAM provisioner for fog devices
 * Updated to use CloudSim Plus 6.4.3 API
 */
public class FogRamProvisioner extends ResourceProvisionerSimple {
    
    public FogRamProvisioner(long ram) {
        super(new Ram(ram));
    }
}
