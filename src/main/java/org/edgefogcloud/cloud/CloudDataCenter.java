package org.edgefogcloud.cloud;

import org.edgefogcloud.data.DataPacket;
import org.edgefogcloud.fog.FogNode;
import org.edgefogcloud.utils.ConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents a Cloud Data Center that receives data from Fog nodes
 * and performs complex analytics and long-term storage
 */
public class CloudDataCenter {
    private static final Logger LOGGER = Logger.getLogger(CloudDataCenter.class.getName());
    
    private String datacenterId;
    private List<FogNode> connectedFogNodes;
    private ConfigManager configManager;
    
    // Cloud characteristics
    private double processingCapacity; // in MIPS
    private double storageCapacity; // in TB
    private double energyConsumption; // in W
    private double bandwidth; // in Gbps
    
    // Data storage and analytics
    private Map<String, List<DataPacket>> dataStore;
    
    // Metrics
    private int totalPacketsReceived;
    private int totalPacketsProcessed;
    private double totalProcessingTime; // in ms
    private double totalEnergyConsumed; // in Wh
    private double totalBandwidthUsed; // in GB
    
    public CloudDataCenter(ConfigManager configManager) {
        this.datacenterId = "Cloud-DataCenter";
        this.configManager = configManager;
        this.connectedFogNodes = new ArrayList<>();
        this.dataStore = new HashMap<>();
        
        // Initialize cloud characteristics
        this.processingCapacity = configManager.getCloudProcessingCapacity();
        this.storageCapacity = configManager.getCloudStorageCapacity();
        this.energyConsumption = configManager.getCloudEnergyConsumption();
        this.bandwidth = configManager.getCloudBandwidth();
        
        // Initialize metrics
        this.totalPacketsReceived = 0;
        this.totalPacketsProcessed = 0;
        this.totalProcessingTime = 0.0;
        this.totalEnergyConsumed = 0.0;
        this.totalBandwidthUsed = 0.0;
        
        LOGGER.info("Created Cloud Data Center: " + datacenterId);
    }
    
    public void addFogNode(FogNode fogNode) {
        connectedFogNodes.add(fogNode);
        LOGGER.fine("Fog Node " + fogNode.getNodeId() + " connected to Cloud Data Center");
    }
    
    public void receiveData(List<DataPacket> dataPackets, FogNode sourceFog) {
        // Store received data packets by data type
        for (DataPacket packet : dataPackets) {
            String dataType = packet.getDataType();
            if (!dataStore.containsKey(dataType)) {
                dataStore.put(dataType, new ArrayList<>());
            }
            dataStore.get(dataType).add(packet);
        }
        
        totalPacketsReceived += dataPackets.size();
        
        // Calculate bandwidth usage
        int totalDataSize = 0;
        for (DataPacket packet : dataPackets) {
            totalDataSize += packet.getSize();
        }
        totalBandwidthUsed += totalDataSize / (1024.0 * 1024.0 * 1024.0); // Convert to GB
        
        LOGGER.fine("Cloud Data Center received " + dataPackets.size() + 
                " data packets from Fog Node " + sourceFog.getNodeId());
    }
    
    public void processData(int currentTime) {
        LOGGER.fine("Cloud Data Center processing data at time " + currentTime);
        
        // Process data for each data type
        for (String dataType : dataStore.keySet()) {
            List<DataPacket> packetsOfType = dataStore.get(dataType);
            
            // Only process if we have new data
            if (!packetsOfType.isEmpty()) {
                // Perform complex analytics on the data
                performAnalytics(packetsOfType, dataType, currentTime);
                
                // Clear processed packets to simulate storage
                // In a real system, we would store the processed results
                packetsOfType.clear();
            }
        }
    }
    
    private void performAnalytics(List<DataPacket> packets, String dataType, int currentTime) {
        // Calculate total size of all packets
        int totalSize = 0;
        for (DataPacket packet : packets) {
            totalSize += packet.getSize();
        }
        
        // Simulate complex analytics processing time
        double processingTime = calculateProcessingTime(totalSize, dataType);
        totalProcessingTime += processingTime;
        totalPacketsProcessed += packets.size();
        
        // Update energy consumption
        double energyUsed = calculateEnergyConsumption(processingTime);
        totalEnergyConsumed += energyUsed;
        
        LOGGER.fine("Cloud Data Center performed analytics on " + packets.size() + 
                " packets of type " + dataType + " at time " + currentTime);
    }
    
    private double calculateProcessingTime(int dataSize, String dataType) {
        // Different data types may require different processing complexity
        double complexityFactor = 1.0;
        
        switch (dataType) {
            case "SENSOR_DATA":
                complexityFactor = 1.0;
                break;
            case "IMAGE_DATA":
                complexityFactor = 2.5;
                break;
            case "VIDEO_DATA":
                complexityFactor = 5.0;
                break;
            case "AUDIO_DATA":
                complexityFactor = 1.5;
                break;
            default:
                complexityFactor = 1.0;
        }
        
        // Processing time model: time = (data size * complexity) / processing capacity
        return (dataSize * complexityFactor) / processingCapacity;
    }
    
    private double calculateEnergyConsumption(double processingTime) {
        // Energy consumption model: E = power * time
        // Convert processing time from ms to hours
        return energyConsumption * (processingTime / 3600000.0);
    }
    
    // Getters and metrics
    public String getDatacenterId() {
        return datacenterId;
    }
    
    public List<FogNode> getConnectedFogNodes() {
        return new ArrayList<>(connectedFogNodes);
    }
    
    public int getTotalPacketsReceived() {
        return totalPacketsReceived;
    }
    
    public int getTotalPacketsProcessed() {
        return totalPacketsProcessed;
    }
    
    public double getTotalProcessingTime() {
        return totalProcessingTime;
    }
    
    public double getTotalEnergyConsumed() {
        return totalEnergyConsumed;
    }
    
    public double getTotalBandwidthUsed() {
        return totalBandwidthUsed;
    }
    
    public Map<String, Integer> getDataTypeDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        
        for (String dataType : dataStore.keySet()) {
            distribution.put(dataType, dataStore.get(dataType).size());
        }
        
        return distribution;
    }
}
