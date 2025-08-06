package org.edgefogcloud.edge;

import org.edgefogcloud.data.DataPacket;
import org.edgefogcloud.devices.IoTDevice;
import org.edgefogcloud.fog.FogNode;
import org.edgefogcloud.utils.ConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents an Edge Node that processes data from IoT devices and forwards filtered data to Fog nodes
 */
public class EdgeNode {
    private static final Logger LOGGER = Logger.getLogger(EdgeNode.class.getName());
    
    private String nodeId;
    private List<IoTDevice> assignedDevices;
    private FogNode assignedFogNode;
    private ConfigManager configManager;
    
    // Edge node characteristics
    private double processingCapacity; // in MIPS
    private double storageCapacity; // in MB
    private double energyConsumption; // in W
    
    // Data processing parameters
    private double filteringRatio; // percentage of data filtered out at edge
    private Map<String, List<DataPacket>> deviceDataBuffer;
    
    // Metrics
    private int totalPacketsReceived;
    private int totalPacketsProcessed;
    private int totalPacketsForwarded;
    private double totalProcessingTime; // in ms
    private double totalEnergyConsumed; // in Wh
    
    public EdgeNode(String nodeId, ConfigManager configManager) {
        this.nodeId = nodeId;
        this.configManager = configManager;
        this.assignedDevices = new ArrayList<>();
        this.deviceDataBuffer = new HashMap<>();
        
        // Initialize edge node characteristics
        this.processingCapacity = configManager.getEdgeNodeProcessingCapacity();
        this.storageCapacity = configManager.getEdgeNodeStorageCapacity();
        this.energyConsumption = configManager.getEdgeNodeEnergyConsumption();
        this.filteringRatio = configManager.getEdgeFilteringRatio();
        
        // Initialize metrics
        this.totalPacketsReceived = 0;
        this.totalPacketsProcessed = 0;
        this.totalPacketsForwarded = 0;
        this.totalProcessingTime = 0.0;
        this.totalEnergyConsumed = 0.0;
        
        LOGGER.fine("Created Edge Node: " + nodeId);
    }
    
    public void addIoTDevice(IoTDevice device) {
        assignedDevices.add(device);
        deviceDataBuffer.put(device.getDeviceId(), new ArrayList<>());
        LOGGER.fine("IoT Device " + device.getDeviceId() + " assigned to Edge Node " + nodeId);
    }
    
    /**
     * Alternative method name for addIoTDevice to maintain compatibility
     * @param device The IoT device to assign to this edge node
     */
    public void addAssignedDevice(IoTDevice device) {
        addIoTDevice(device);
    }
    
    public void receiveData(DataPacket dataPacket, IoTDevice sourceDevice) {
        // Store received data packet in buffer
        String deviceId = sourceDevice.getDeviceId();
        if (deviceDataBuffer.containsKey(deviceId)) {
            deviceDataBuffer.get(deviceId).add(dataPacket);
            totalPacketsReceived++;
            LOGGER.fine("Edge Node " + nodeId + " received data packet from " + deviceId);
        } else {
            LOGGER.warning("Edge Node " + nodeId + " received data from unassigned device " + deviceId);
        }
    }
    
    public void processData(int currentTime) {
        LOGGER.fine("Edge Node " + nodeId + " processing data at time " + currentTime);
        
        List<DataPacket> processedPackets = new ArrayList<>();
        
        // Process data from each device's buffer
        for (String deviceId : deviceDataBuffer.keySet()) {
            List<DataPacket> deviceBuffer = deviceDataBuffer.get(deviceId);
            
            // Process all packets in the buffer
            for (DataPacket packet : deviceBuffer) {
                // Apply edge filtering and processing
                if (shouldProcessPacket(packet)) {
                    DataPacket processedPacket = processPacket(packet);
                    processedPackets.add(processedPacket);
                    totalPacketsProcessed++;
                }
            }
            
            // Clear the buffer after processing
            deviceBuffer.clear();
        }
        
        // Forward processed packets to fog node
        forwardProcessedData(processedPackets);
    }
    
    private boolean shouldProcessPacket(DataPacket packet) {
        // Implement edge filtering logic
        // For simplicity, we use a probabilistic approach based on filtering ratio
        return Math.random() > filteringRatio;
    }
    
    private DataPacket processPacket(DataPacket originalPacket) {
        // Simulate processing time based on packet size and processing capacity
        double processingTime = calculateProcessingTime(originalPacket.getSize());
        totalProcessingTime += processingTime;
        
        // Update energy consumption
        double energyUsed = calculateEnergyConsumption(processingTime);
        totalEnergyConsumed += energyUsed;
        
        // Create processed packet (with reduced size due to edge processing)
        DataPacket processedPacket = new DataPacket(
                originalPacket.getSourceId(),
                originalPacket.getTimestamp(),
                (int)(originalPacket.getSize() * 0.7), // Reduce size by 30%
                originalPacket.getDataType(),
                "EDGE_PROCESSED"
        );
        
        return processedPacket;
    }
    
    private double calculateProcessingTime(int packetSize) {
        // Simple processing time model: time = packet size / processing capacity
        return packetSize / processingCapacity;
    }
    
    private double calculateEnergyConsumption(double processingTime) {
        // Energy consumption model: E = power * time
        // Convert processing time from ms to hours
        return energyConsumption * (processingTime / 3600000.0);
    }
    
    private void forwardProcessedData(List<DataPacket> processedPackets) {
        if (assignedFogNode != null && !processedPackets.isEmpty()) {
            assignedFogNode.receiveData(processedPackets, this);
            totalPacketsForwarded += processedPackets.size();
            LOGGER.fine("Edge Node " + nodeId + " forwarded " + processedPackets.size() + " packets to Fog Node");
        }
    }
    
    // Getters and setters
    public String getNodeId() {
        return nodeId;
    }
    
    public void setAssignedFogNode(FogNode fogNode) {
        this.assignedFogNode = fogNode;
    }
    
    public FogNode getAssignedFogNode() {
        return assignedFogNode;
    }
    
    public List<IoTDevice> getAssignedDevices() {
        return new ArrayList<>(assignedDevices);
    }
    
    public int getTotalPacketsReceived() {
        return totalPacketsReceived;
    }
    
    public int getTotalPacketsProcessed() {
        return totalPacketsProcessed;
    }
    
    public int getTotalPacketsForwarded() {
        return totalPacketsForwarded;
    }
    
    public double getTotalProcessingTime() {
        return totalProcessingTime;
    }
    
    public double getTotalEnergyConsumed() {
        return totalEnergyConsumed;
    }
    
    public double getFilteringRatio() {
        return filteringRatio;
    }
    
    public double getDataReductionRatio() {
        if (totalPacketsReceived == 0) {
            return 0.0;
        }
        return 1.0 - ((double) totalPacketsForwarded / totalPacketsReceived);
    }
}
