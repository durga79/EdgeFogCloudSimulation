package org.edgefogcloud.fog;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;

import java.util.List;

/**
 * Host with utilization history for fog devices
 * Updated to use CloudSim Plus 7.3.3 API
 */
public class FogHostUtilizationHistory extends HostSimple {
    
    private double schedulingInterval;
    
    /**
     * Creates a fog host with utilization history tracking
     * 
     * @param ram the RAM capacity in Megabytes
     * @param bw the Bandwidth capacity in Megabits/s
     * @param storage the Storage capacity in Megabytes
     * @param peList the host's Processing Elements list
     * @param schedulingInterval the scheduling interval for the host
     */
    public FogHostUtilizationHistory(long ram, long bw, long storage, List<Pe> peList, double schedulingInterval) {
        super(ram, bw, storage, peList);
        this.schedulingInterval = schedulingInterval;
        setVmScheduler(new VmSchedulerTimeShared());
        enableUtilizationStats();
    }
    
    /**
     * Creates a fog host with utilization history tracking and a power model
     * 
     * @param ram the RAM capacity in Megabytes
     * @param bw the Bandwidth capacity in Megabits/s
     * @param storage the Storage capacity in Megabytes
     * @param peList the host's Processing Elements list
     * @param vmScheduler the VM scheduler
     * @param powerModel the power model
     * @param schedulingInterval the scheduling interval for the host
     */
    public FogHostUtilizationHistory(long ram, long bw, long storage, List<Pe> peList, 
                                    VmScheduler vmScheduler, PowerModelHost powerModel, double schedulingInterval) {
        super(ram, bw, storage, peList);
        setVmScheduler(vmScheduler);
        setPowerModel(powerModel);
        this.schedulingInterval = schedulingInterval;
        enableUtilizationStats();
    }
    
    /**
     * Gets the scheduling interval for this host
     * 
     * @return the scheduling interval
     */
    public double getSchedulingInterval() {
        return schedulingInterval;
    }
}
