package org.edgefogcloud.fog;

import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.FileStorage;
import org.cloudbus.cloudsim.resources.SanStorage;
import org.cloudbus.cloudsim.resources.DatacenterStorage;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;

/**
 * Scheduler for fog devices using CloudSim Plus 6.4.3 API
 */
public class FogDeviceScheduler extends DatacenterSimple {
    
    /**
     * Creates a new fog device scheduler
     * 
     * @param simulation the CloudSim Plus simulation instance
     * @param hostList the list of hosts for this datacenter
     * @param vmAllocationPolicy the VM allocation policy
     */
    public FogDeviceScheduler(CloudSim simulation, List<? extends Host> hostList, 
                             VmAllocationPolicy vmAllocationPolicy) {
        super(simulation, hostList, vmAllocationPolicy);
        setName("FogDeviceScheduler_" + getId());
    }
    
    /**
     * Creates a new fog device scheduler with storage
     * 
     * @param simulation the CloudSim Plus simulation instance
     * @param hostList the list of hosts for this datacenter
     * @param vmAllocationPolicy the VM allocation policy
     * @param storageList the list of storage devices
     */
    public FogDeviceScheduler(CloudSim simulation, List<? extends Host> hostList, 
                             VmAllocationPolicy vmAllocationPolicy, List<FileStorage> storageList) {
        super(simulation, hostList, vmAllocationPolicy);
        setName("FogDeviceScheduler_" + getId());
        // Create a DatacenterStorage with the storage list
        DatacenterStorage storage = new DatacenterStorage();
        for (FileStorage fs : storageList) {
            if (fs instanceof SanStorage) {
                // In CloudSim Plus 6.4.3, we need to use the storage list differently
                storage.getStorageList().add((SanStorage)fs);
            }
        }
        setDatacenterStorage(storage);
    }
}
