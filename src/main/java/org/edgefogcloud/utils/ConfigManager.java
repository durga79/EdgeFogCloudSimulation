package org.edgefogcloud.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages configuration parameters for the Edge-Fog-Cloud simulation
 */
public class ConfigManager {
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    
    private Properties properties;
    private Random random;
    
    // Default configuration values - extremely lightweight for testing
    private static final int DEFAULT_SIMULATION_TIME = 60; // Just 1 minute in seconds
    private static final int DEFAULT_NUM_IOT_DEVICES = 3; // Minimal number of devices
    private static final int DEFAULT_NUM_EDGE_NODES = 1;  // Just one edge node
    private static final int DEFAULT_NUM_FOG_NODES = 1;   // Just one fog node
    
    // IoT device types
    private static final String[] DEVICE_TYPES = {"SENSOR", "ACTUATOR", "SMARTPHONE", "WEARABLE"};
    
    public ConfigManager() {
        properties = new Properties();
        random = new Random();
        
        // Try to load configuration from file
        try {
            properties.load(new FileInputStream("resources/config.properties"));
            LOGGER.info("Configuration loaded from file");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not load configuration file. Using default values.", e);
            setDefaultProperties();
        }
    }
    
    private void setDefaultProperties() {
        // Simulation parameters
        properties.setProperty("simulation.time", String.valueOf(DEFAULT_SIMULATION_TIME));
        properties.setProperty("simulation.num_iot_devices", String.valueOf(DEFAULT_NUM_IOT_DEVICES));
        properties.setProperty("simulation.num_edge_nodes", String.valueOf(DEFAULT_NUM_EDGE_NODES));
        properties.setProperty("simulation.num_fog_nodes", String.valueOf(DEFAULT_NUM_FOG_NODES));
        
        // Network parameters
        properties.setProperty("network.wireless.base_latency", "10.0"); // ms
        properties.setProperty("network.wireless.bandwidth", "1000.0"); // KB/s (1 MB/s)
        properties.setProperty("network.edge_to_fog.latency", "20.0"); // ms
        properties.setProperty("network.fog_to_cloud.latency", "50.0"); // ms
        
        // Edge node parameters
        properties.setProperty("edge.processing_capacity", "1000.0"); // MIPS
        properties.setProperty("edge.storage_capacity", "1024.0"); // MB (1 GB)
        properties.setProperty("edge.energy_consumption", "50.0"); // W
        properties.setProperty("edge.filtering_ratio", "0.6"); // 60% of data filtered at edge
        
        // Fog node parameters
        properties.setProperty("fog.processing_capacity", "5000.0"); // MIPS
        properties.setProperty("fog.storage_capacity", "102400.0"); // MB (100 GB)
        properties.setProperty("fog.energy_consumption", "200.0"); // W
        properties.setProperty("fog.bandwidth", "100.0"); // Mbps
        properties.setProperty("fog.aggregation_ratio", "0.5"); // 50% size reduction due to aggregation
        
        // Cloud parameters
        properties.setProperty("cloud.processing_capacity", "50000.0"); // MIPS
        properties.setProperty("cloud.storage_capacity", "1024.0"); // TB (1 PB)
        properties.setProperty("cloud.energy_consumption", "1000.0"); // W
        properties.setProperty("cloud.bandwidth", "10.0"); // Gbps
        
        LOGGER.info("Default configuration values set");
    }
    
    // Simulation parameters
    public int getSimulationTime() {
        return Integer.parseInt(properties.getProperty("simulation.time", String.valueOf(DEFAULT_SIMULATION_TIME)));
    }
    
    public int getNumIoTDevices() {
        return Integer.parseInt(properties.getProperty("simulation.num_iot_devices", String.valueOf(DEFAULT_NUM_IOT_DEVICES)));
    }
    
    public int getNumEdgeNodes() {
        return Integer.parseInt(properties.getProperty("simulation.num_edge_nodes", String.valueOf(DEFAULT_NUM_EDGE_NODES)));
    }
    
    public int getNumFogNodes() {
        return Integer.parseInt(properties.getProperty("simulation.num_fog_nodes", String.valueOf(DEFAULT_NUM_FOG_NODES)));
    }
    
    // Network parameters
    public double getWirelessBaseLatency() {
        return Double.parseDouble(properties.getProperty("network.wireless.base_latency", "10.0"));
    }
    
    public double getWirelessBandwidth() {
        return Double.parseDouble(properties.getProperty("network.wireless.bandwidth", "1000.0"));
    }
    
    public double getEdgeToFogLatency() {
        return Double.parseDouble(properties.getProperty("network.edge_to_fog.latency", "20.0"));
    }
    
    public double getFogToCloudLatency() {
        return Double.parseDouble(properties.getProperty("network.fog_to_cloud.latency", "50.0"));
    }
    
    // Edge node parameters
    public double getEdgeNodeProcessingCapacity() {
        return Double.parseDouble(properties.getProperty("edge.processing_capacity", "1000.0"));
    }
    
    public double getEdgeNodeStorageCapacity() {
        return Double.parseDouble(properties.getProperty("edge.storage_capacity", "1024.0"));
    }
    
    public double getEdgeNodeEnergyConsumption() {
        return Double.parseDouble(properties.getProperty("edge.energy_consumption", "50.0"));
    }
    
    public double getEdgeFilteringRatio() {
        return Double.parseDouble(properties.getProperty("edge.filtering_ratio", "0.6"));
    }
    
    // Fog node parameters
    public double getFogNodeProcessingCapacity() {
        return Double.parseDouble(properties.getProperty("fog.processing_capacity", "5000.0"));
    }
    
    public double getFogNodeStorageCapacity() {
        return Double.parseDouble(properties.getProperty("fog.storage_capacity", "102400.0"));
    }
    
    public double getFogNodeEnergyConsumption() {
        return Double.parseDouble(properties.getProperty("fog.energy_consumption", "200.0"));
    }
    
    public double getFogNodeBandwidth() {
        return Double.parseDouble(properties.getProperty("fog.bandwidth", "100.0"));
    }
    
    public double getFogAggregationRatio() {
        return Double.parseDouble(properties.getProperty("fog.aggregation_ratio", "0.5"));
    }
    
    // Cloud parameters
    public double getCloudProcessingCapacity() {
        return Double.parseDouble(properties.getProperty("cloud.processing_capacity", "50000.0"));
    }
    
    public double getCloudStorageCapacity() {
        return Double.parseDouble(properties.getProperty("cloud.storage_capacity", "1024.0"));
    }
    
    public double getCloudEnergyConsumption() {
        return Double.parseDouble(properties.getProperty("cloud.energy_consumption", "1000.0"));
    }
    
    public double getCloudBandwidth() {
        return Double.parseDouble(properties.getProperty("cloud.bandwidth", "10.0"));
    }
    
    // Helper methods
    public String getRandomIoTDeviceType() {
        int index = random.nextInt(DEVICE_TYPES.length);
        return DEVICE_TYPES[index];
    }
    
    // CloudSim Plus specific configuration methods
    public int getCloudHostPes() {
        return Integer.parseInt(properties.getProperty("cloud.host.pes", "16"));
    }
    
    public int getCloudHostMips() {
        return Integer.parseInt(properties.getProperty("cloud.host.mips", "10000"));
    }
    
    public int getCloudHostRam() {
        return Integer.parseInt(properties.getProperty("cloud.host.ram", "65536"));
    }
    
    public int getCloudHostStorage() {
        return Integer.parseInt(properties.getProperty("cloud.host.storage", "1000000"));
    }
    
    public int getCloudHostBw() {
        return Integer.parseInt(properties.getProperty("cloud.host.bw", "100000"));
    }
    
    public int getFogHostPes() {
        return Integer.parseInt(properties.getProperty("fog.host.pes", "8"));
    }
    
    public int getFogHostMips() {
        return Integer.parseInt(properties.getProperty("fog.host.mips", "5000"));
    }
    
    public int getFogHostRam() {
        return Integer.parseInt(properties.getProperty("fog.host.ram", "32768"));
    }
    
    public int getFogHostStorage() {
        return Integer.parseInt(properties.getProperty("fog.host.storage", "500000"));
    }
    
    public int getFogHostBw() {
        return Integer.parseInt(properties.getProperty("fog.host.bw", "50000"));
    }
    
    public int getEdgeHostPes() {
        return Integer.parseInt(properties.getProperty("edge.host.pes", "4"));
    }
    
    public int getEdgeHostMips() {
        return Integer.parseInt(properties.getProperty("edge.host.mips", "2000"));
    }
    
    public int getEdgeHostRam() {
        return Integer.parseInt(properties.getProperty("edge.host.ram", "8192"));
    }
    
    public int getEdgeHostStorage() {
        return Integer.parseInt(properties.getProperty("edge.host.storage", "100000"));
    }
    
    public int getEdgeHostBw() {
        return Integer.parseInt(properties.getProperty("edge.host.bw", "10000"));
    }
}
