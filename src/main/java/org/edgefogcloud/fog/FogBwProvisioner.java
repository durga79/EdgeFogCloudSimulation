package org.edgefogcloud.fog;

import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Bandwidth;

/**
 * Bandwidth provisioner for fog devices
 * Updated to use CloudSim Plus 6.4.3 API
 */
public class FogBwProvisioner extends ResourceProvisionerSimple {
    
    public FogBwProvisioner(long bw) {
        super(new Bandwidth(bw));
    }
}
