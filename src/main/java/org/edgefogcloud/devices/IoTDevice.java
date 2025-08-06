package org.edgefogcloud.devices;

import org.edgefogcloud.data.DataGenerator;
import org.edgefogcloud.data.DataPacket;
import org.edgefogcloud.edge.EdgeNode;
import org.edgefogcloud.utils.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents an IoT device that generates data and sends it to an edge node
 */
public class IoTDevice {
    private static final Logger LOGGER = Logger.getLogger(IoTDevice.class.getName());
    
    private String deviceId;
    private String deviceType;
    private DataGenerator dataGenerator;
    private EdgeNode assignedEdgeNode;
    private ConfigManager configManager;
    
    // Device characteristics
    private double batteryCapacity; // in mAh
    private double currentBatteryLevel; // in mAh
    private double processingPower; // in MIPS
    private double transmissionPower; // in mW
    private double dataGenerationRate; // packets per second
    
    // Metrics
    private int totalPacketsGenerated;
    private int totalPacketsTransmitted;
    private double totalEnergyConsumed; // in mWh
    private List<Double> transmissionLatencies; // in ms
    
    public IoTDevice(String deviceId, ConfigManager configManager) {
        this.deviceId = deviceId;
        this.configManager = configManager;
        this.deviceType = configManager.getRandomIoTDeviceType();
        this.totalPacketsGenerated = 0;
        this.totalPacketsTransmitted = 0;
        this.totalEnergyConsumed = 0.0;
        this.transmissionLatencies = new ArrayList<>();
        
        // Initialize device characteristics based on device type
        initializeDeviceCharacteristics();
        
        LOGGER.fine("Created IoT device: " + deviceId + " of type: " + deviceType);
    }
    
    private void initializeDeviceCharacteristics() {
        // Set device characteristics based on device type
        switch (deviceType) {
            case "SENSOR":
                this.batteryCapacity = 2000.0; // 2000 mAh
                this.processingPower = 100.0; // 100 MIPS
                this.transmissionPower = 50.0; // 50 mW
                this.dataGenerationRate = 1.0; // 1 packet per second
                break;
            case "ACTUATOR":
                this.batteryCapacity = 3000.0; // 3000 mAh
                this.processingPower = 200.0; // 200 MIPS
                this.transmissionPower = 100.0; // 100 mW
                this.dataGenerationRate = 0.5; // 0.5 packets per second
                break;
            case "SMARTPHONE":
                this.batteryCapacity = 4000.0; // 4000 mAh
                this.processingPower = 2000.0; // 2000 MIPS
                this.transmissionPower = 200.0; // 200 mW
                this.dataGenerationRate = 5.0; // 5 packets per second
                break;
            case "WEARABLE":
                this.batteryCapacity = 500.0; // 500 mAh
                this.processingPower = 500.0; // 500 MIPS
                this.transmissionPower = 30.0; // 30 mW
                this.dataGenerationRate = 2.0; // 2 packets per second
                break;
            default:
                this.batteryCapacity = 1000.0; // 1000 mAh
                this.processingPower = 100.0; // 100 MIPS
                this.transmissionPower = 50.0; // 50 mW
                this.dataGenerationRate = 1.0; // 1 packet per second
        }
        
        this.currentBatteryLevel = this.batteryCapacity;
    }
    
    public void initializeDataGenerator() {
        this.dataGenerator = new DataGenerator(deviceType, configManager);
    }
    
    public void generateData(int currentTime) {
        // Check if we should generate data at this time step based on data generation rate
        if (shouldGenerateData(currentTime)) {
            DataPacket dataPacket = dataGenerator.generateDataPacket(deviceId, currentTime);
            totalPacketsGenerated++;
            
            // Transmit data to edge node
            transmitData(dataPacket);
            
            // Update energy consumption
            updateEnergyConsumption(dataPacket.getSize());
            
            LOGGER.fine(deviceId + " generated and transmitted data packet at time " + currentTime);
        }
    }
    
    private boolean shouldGenerateData(int currentTime) {
        // Simple implementation: generate data based on data generation rate
        // For example, if rate is 0.5 packets per second, generate data every 2 seconds
        return currentTime % (1.0 / dataGenerationRate) < 1;
    }
    
    private void transmitData(DataPacket dataPacket) {
        if (assignedEdgeNode != null) {
            // Calculate transmission latency based on packet size and network conditions
            double latency = calculateTransmissionLatency(dataPacket.getSize());
            transmissionLatencies.add(latency);
            
            // Send data packet to edge node
            assignedEdgeNode.receiveData(dataPacket, this);
            totalPacketsTransmitted++;
        } else {
            LOGGER.warning(deviceId + " has no assigned edge node. Data packet discarded.");
        }
    }
    
    private double calculateTransmissionLatency(int packetSize) {
        // Simple latency model: latency = base latency + (packet size / bandwidth)
        double baseLatency = configManager.getWirelessBaseLatency(); // in ms
        double bandwidth = configManager.getWirelessBandwidth(); // in bytes per ms
        
        return baseLatency + (packetSize / bandwidth);
    }
    
    private void updateEnergyConsumption(int packetSize) {
        // Energy consumption model: E = transmission power * transmission time
        double transmissionTime = packetSize / configManager.getWirelessBandwidth(); // in ms
        double energyForTransmission = transmissionPower * transmissionTime / 3600.0; // convert to mWh
        
        totalEnergyConsumed += energyForTransmission;
        currentBatteryLevel -= energyForTransmission;
        
        if (currentBatteryLevel < 0) {
            currentBatteryLevel = 0;
            LOGGER.warning(deviceId + " battery depleted!");
        }
    }
    
    // Getters and setters
    public String getDeviceId() {
        return deviceId;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setAssignedEdgeNode(EdgeNode assignedEdgeNode) {
        this.assignedEdgeNode = assignedEdgeNode;
    }
    
    public EdgeNode getAssignedEdgeNode() {
        return assignedEdgeNode;
    }
    
    public int getTotalPacketsGenerated() {
        return totalPacketsGenerated;
    }
    
    public int getTotalPacketsTransmitted() {
        return totalPacketsTransmitted;
    }
    
    public double getTotalEnergyConsumed() {
        return totalEnergyConsumed;
    }
    
    public double getCurrentBatteryLevel() {
        return currentBatteryLevel;
    }
    
    public double getBatteryPercentage() {
        return (currentBatteryLevel / batteryCapacity) * 100.0;
    }
    
    public double getAverageTransmissionLatency() {
        if (transmissionLatencies.isEmpty()) {
            return 0.0;
        }
        
        double sum = 0.0;
        for (Double latency : transmissionLatencies) {
            sum += latency;
        }
        
        return sum / transmissionLatencies.size();
    }
}
