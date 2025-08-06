package org.edgefogcloud.fog;

import org.cloudbus.cloudsim.power.models.PowerModelHostSimple;

/**
 * Custom power model for fog devices
 * Updated to use CloudSim Plus 6.4.3 API
 */
public class FogLinearPowerModel extends PowerModelHostSimple {
    private double maxPower;
    private double staticPower;
    
    /**
     * Creates a new linear power model for fog devices
     * 
     * @param maxPower the maximum power consumed by the host in Watts (W)
     * @param staticPower the static power consumed by the host in Watts (W)
     */
    public FogLinearPowerModel(double maxPower, double staticPower) {
        super(maxPower, staticPower);
        this.maxPower = maxPower;
        this.staticPower = staticPower;
    }
    
    @Override
    public double getPower(double utilization) throws IllegalArgumentException {
        if (utilization < 0 || utilization > 1) {
            throw new IllegalArgumentException("Utilization must be between 0 and 1");
        }
        return staticPower + (maxPower - staticPower) * utilization;
    }
}
