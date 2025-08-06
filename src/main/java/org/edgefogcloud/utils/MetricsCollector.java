package org.edgefogcloud.utils;

import org.edgefogcloud.cloud.CloudDataCenter;
import org.edgefogcloud.devices.IoTDevice;
import org.edgefogcloud.edge.EdgeNode;
import org.edgefogcloud.fog.FogNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Collects and analyzes performance metrics from the Edge-Fog-Cloud simulation
 */
public class MetricsCollector {
    private static final Logger LOGGER = Logger.getLogger(MetricsCollector.class.getName());
    
    // Time series metrics
    private Map<Integer, Double> latencyByTime;
    private Map<Integer, Double> energyConsumptionByTime;
    private Map<Integer, Double> bandwidthUsageByTime;
    private Map<Integer, Double> dataReductionByTime;
    
    // Aggregate metrics
    private double averageEndToEndLatency;
    private double totalEnergyConsumption;
    private double totalBandwidthUsage;
    private double overallDataReductionRatio;
    
    // Layer-specific metrics
    private Map<String, Double> processingTimeByLayer;
    private Map<String, Double> energyConsumptionByLayer;
    private Map<String, Integer> packetsByLayer;
    
    public MetricsCollector() {
        // Initialize metrics collections
        latencyByTime = new HashMap<>();
        energyConsumptionByTime = new HashMap<>();
        bandwidthUsageByTime = new HashMap<>();
        dataReductionByTime = new HashMap<>();
        
        processingTimeByLayer = new HashMap<>();
        energyConsumptionByLayer = new HashMap<>();
        packetsByLayer = new HashMap<>();
        
        // Initialize layer-specific metrics
        processingTimeByLayer.put("IoT", 0.0);
        processingTimeByLayer.put("Edge", 0.0);
        processingTimeByLayer.put("Fog", 0.0);
        processingTimeByLayer.put("Cloud", 0.0);
        
        energyConsumptionByLayer.put("IoT", 0.0);
        energyConsumptionByLayer.put("Edge", 0.0);
        energyConsumptionByLayer.put("Fog", 0.0);
        energyConsumptionByLayer.put("Cloud", 0.0);
        
        packetsByLayer.put("IoT", 0);
        packetsByLayer.put("Edge", 0);
        packetsByLayer.put("Fog", 0);
        packetsByLayer.put("Cloud", 0);
    }
    
    public void collectMetrics(int currentTime, List<IoTDevice> iotDevices, List<EdgeNode> edgeNodes, 
                              List<FogNode> fogNodes, CloudDataCenter cloudDataCenter) {
        // Calculate metrics for current time step
        double currentLatency = calculateCurrentLatency(iotDevices, edgeNodes, fogNodes, cloudDataCenter);
        double currentEnergyConsumption = calculateCurrentEnergyConsumption(iotDevices, edgeNodes, fogNodes, cloudDataCenter);
        double currentBandwidthUsage = calculateCurrentBandwidthUsage(fogNodes, cloudDataCenter);
        double currentDataReduction = calculateCurrentDataReduction(edgeNodes, fogNodes);
        
        // Store metrics for current time step
        latencyByTime.put(currentTime, currentLatency);
        energyConsumptionByTime.put(currentTime, currentEnergyConsumption);
        bandwidthUsageByTime.put(currentTime, currentBandwidthUsage);
        dataReductionByTime.put(currentTime, currentDataReduction);
        
        // Update layer-specific metrics
        updateLayerMetrics(iotDevices, edgeNodes, fogNodes, cloudDataCenter);
        
        LOGGER.fine("Metrics collected for time " + currentTime);
    }
    
    private double calculateCurrentLatency(List<IoTDevice> iotDevices, List<EdgeNode> edgeNodes, 
                                         List<FogNode> fogNodes, CloudDataCenter cloudDataCenter) {
        // Calculate average latency across all devices and nodes
        double totalLatency = 0.0;
        int count = 0;
        
        // IoT device transmission latency
        for (IoTDevice device : iotDevices) {
            if (device.getTotalPacketsTransmitted() > 0) {
                totalLatency += device.getAverageTransmissionLatency();
                count++;
            }
        }
        
        // Edge processing latency
        for (EdgeNode edge : edgeNodes) {
            if (edge.getTotalPacketsProcessed() > 0) {
                totalLatency += edge.getTotalProcessingTime() / edge.getTotalPacketsProcessed();
                count++;
            }
        }
        
        // Fog processing latency
        for (FogNode fog : fogNodes) {
            if (fog.getTotalPacketsProcessed() > 0) {
                totalLatency += fog.getTotalProcessingTime() / fog.getTotalPacketsProcessed();
                count++;
            }
        }
        
        // Cloud processing latency
        if (cloudDataCenter.getTotalPacketsProcessed() > 0) {
            totalLatency += cloudDataCenter.getTotalProcessingTime() / cloudDataCenter.getTotalPacketsProcessed();
            count++;
        }
        
        return count > 0 ? totalLatency / count : 0.0;
    }
    
    private double calculateCurrentEnergyConsumption(List<IoTDevice> iotDevices, List<EdgeNode> edgeNodes, 
                                                  List<FogNode> fogNodes, CloudDataCenter cloudDataCenter) {
        double totalEnergy = 0.0;
        
        // IoT device energy consumption (in mWh, convert to Wh)
        for (IoTDevice device : iotDevices) {
            totalEnergy += device.getTotalEnergyConsumed() / 1000.0;
        }
        
        // Edge node energy consumption (in Wh)
        for (EdgeNode edge : edgeNodes) {
            totalEnergy += edge.getTotalEnergyConsumed();
        }
        
        // Fog node energy consumption (in Wh)
        for (FogNode fog : fogNodes) {
            totalEnergy += fog.getTotalEnergyConsumed();
        }
        
        // Cloud energy consumption (in Wh)
        totalEnergy += cloudDataCenter.getTotalEnergyConsumed();
        
        return totalEnergy;
    }
    
    private double calculateCurrentBandwidthUsage(List<FogNode> fogNodes, CloudDataCenter cloudDataCenter) {
        double totalBandwidth = 0.0;
        
        // Fog node bandwidth usage (in MB)
        for (FogNode fog : fogNodes) {
            totalBandwidth += fog.getTotalBandwidthUsed();
        }
        
        // Cloud bandwidth usage (in GB, convert to MB)
        totalBandwidth += cloudDataCenter.getTotalBandwidthUsed() * 1024.0;
        
        return totalBandwidth;
    }
    
    private double calculateCurrentDataReduction(List<EdgeNode> edgeNodes, List<FogNode> fogNodes) {
        double totalReduction = 0.0;
        int count = 0;
        
        // Edge node data reduction
        for (EdgeNode edge : edgeNodes) {
            if (edge.getTotalPacketsReceived() > 0) {
                totalReduction += edge.getDataReductionRatio();
                count++;
            }
        }
        
        // Fog node data reduction
        for (FogNode fog : fogNodes) {
            if (fog.getTotalPacketsReceived() > 0) {
                totalReduction += fog.getDataReductionRatio();
                count++;
            }
        }
        
        return count > 0 ? totalReduction / count : 0.0;
    }
    
    private void updateLayerMetrics(List<IoTDevice> iotDevices, List<EdgeNode> edgeNodes, 
                                  List<FogNode> fogNodes, CloudDataCenter cloudDataCenter) {
        // IoT layer metrics
        double iotEnergy = 0.0;
        int iotPackets = 0;
        for (IoTDevice device : iotDevices) {
            iotEnergy += device.getTotalEnergyConsumed() / 1000.0; // Convert mWh to Wh
            iotPackets += device.getTotalPacketsGenerated();
        }
        energyConsumptionByLayer.put("IoT", energyConsumptionByLayer.get("IoT") + iotEnergy);
        packetsByLayer.put("IoT", iotPackets);
        
        // Edge layer metrics
        double edgeProcessingTime = 0.0;
        double edgeEnergy = 0.0;
        int edgePackets = 0;
        for (EdgeNode edge : edgeNodes) {
            edgeProcessingTime += edge.getTotalProcessingTime();
            edgeEnergy += edge.getTotalEnergyConsumed();
            edgePackets += edge.getTotalPacketsProcessed();
        }
        processingTimeByLayer.put("Edge", processingTimeByLayer.get("Edge") + edgeProcessingTime);
        energyConsumptionByLayer.put("Edge", energyConsumptionByLayer.get("Edge") + edgeEnergy);
        packetsByLayer.put("Edge", edgePackets);
        
        // Fog layer metrics
        double fogProcessingTime = 0.0;
        double fogEnergy = 0.0;
        int fogPackets = 0;
        for (FogNode fog : fogNodes) {
            fogProcessingTime += fog.getTotalProcessingTime();
            fogEnergy += fog.getTotalEnergyConsumed();
            fogPackets += fog.getTotalPacketsProcessed();
        }
        processingTimeByLayer.put("Fog", processingTimeByLayer.get("Fog") + fogProcessingTime);
        energyConsumptionByLayer.put("Fog", energyConsumptionByLayer.get("Fog") + fogEnergy);
        packetsByLayer.put("Fog", fogPackets);
        
        // Cloud layer metrics
        processingTimeByLayer.put("Cloud", processingTimeByLayer.get("Cloud") + cloudDataCenter.getTotalProcessingTime());
        energyConsumptionByLayer.put("Cloud", energyConsumptionByLayer.get("Cloud") + cloudDataCenter.getTotalEnergyConsumed());
        packetsByLayer.put("Cloud", cloudDataCenter.getTotalPacketsProcessed());
    }
    
    public void calculateAggregateMetrics() {
        // Calculate average end-to-end latency
        double totalLatency = 0.0;
        for (Double latency : latencyByTime.values()) {
            totalLatency += latency;
        }
        averageEndToEndLatency = latencyByTime.size() > 0 ? totalLatency / latencyByTime.size() : 0.0;
        
        // Calculate total energy consumption
        totalEnergyConsumption = energyConsumptionByLayer.get("IoT") + 
                                energyConsumptionByLayer.get("Edge") + 
                                energyConsumptionByLayer.get("Fog") + 
                                energyConsumptionByLayer.get("Cloud");
        
        // Calculate total bandwidth usage
        totalBandwidthUsage = 0.0;
        for (Double bandwidth : bandwidthUsageByTime.values()) {
            totalBandwidthUsage += bandwidth;
        }
        
        // Calculate overall data reduction ratio
        double totalReduction = 0.0;
        for (Double reduction : dataReductionByTime.values()) {
            totalReduction += reduction;
        }
        overallDataReductionRatio = dataReductionByTime.size() > 0 ? totalReduction / dataReductionByTime.size() : 0.0;
        
        LOGGER.info("Aggregate metrics calculated");
    }
    
    public void displayMetrics() {
        System.out.println("\n=== SIMULATION RESULTS ===");
        
        // Display end-to-end latency
        System.out.println("\n--- Latency Metrics ---");
        System.out.printf("Average End-to-End Latency: %.2f ms\n", averageEndToEndLatency);
        
        // Display energy consumption
        System.out.println("\n--- Energy Consumption Metrics ---");
        System.out.printf("Total Energy Consumption: %.2f Wh\n", totalEnergyConsumption);
        System.out.println("Energy Consumption by Layer:");
        System.out.printf("  IoT Devices: %.2f Wh (%.1f%%)\n", 
                energyConsumptionByLayer.get("IoT"), 
                (energyConsumptionByLayer.get("IoT") / totalEnergyConsumption) * 100);
        System.out.printf("  Edge Nodes: %.2f Wh (%.1f%%)\n", 
                energyConsumptionByLayer.get("Edge"),
                (energyConsumptionByLayer.get("Edge") / totalEnergyConsumption) * 100);
        System.out.printf("  Fog Nodes: %.2f Wh (%.1f%%)\n", 
                energyConsumptionByLayer.get("Fog"),
                (energyConsumptionByLayer.get("Fog") / totalEnergyConsumption) * 100);
        System.out.printf("  Cloud: %.2f Wh (%.1f%%)\n", 
                energyConsumptionByLayer.get("Cloud"),
                (energyConsumptionByLayer.get("Cloud") / totalEnergyConsumption) * 100);
        
        // Display bandwidth usage
        System.out.println("\n--- Bandwidth Usage Metrics ---");
        System.out.printf("Total Bandwidth Usage: %.2f MB\n", totalBandwidthUsage);
        
        // Display data reduction
        System.out.println("\n--- Data Reduction Metrics ---");
        System.out.printf("Overall Data Reduction Ratio: %.2f%%\n", overallDataReductionRatio * 100);
        
        // Display processing distribution
        System.out.println("\n--- Processing Distribution ---");
        int totalPackets = packetsByLayer.get("IoT");
        System.out.printf("Total Data Packets Generated: %d\n", totalPackets);
        System.out.printf("Packets Processed at Edge: %d (%.1f%%)\n", 
                packetsByLayer.get("Edge"), 
                (double) packetsByLayer.get("Edge") / totalPackets * 100);
        System.out.printf("Packets Processed at Fog: %d (%.1f%%)\n", 
                packetsByLayer.get("Fog"),
                (double) packetsByLayer.get("Fog") / totalPackets * 100);
        System.out.printf("Packets Processed at Cloud: %d (%.1f%%)\n", 
                packetsByLayer.get("Cloud"),
                (double) packetsByLayer.get("Cloud") / totalPackets * 100);
        
        System.out.println("\n=========================");
    }
    
    // Getters for visualization
    public Map<Integer, Double> getLatencyByTime() {
        return new HashMap<>(latencyByTime);
    }
    
    public Map<Integer, Double> getEnergyConsumptionByTime() {
        return new HashMap<>(energyConsumptionByTime);
    }
    
    public Map<Integer, Double> getBandwidthUsageByTime() {
        return new HashMap<>(bandwidthUsageByTime);
    }
    
    public Map<Integer, Double> getDataReductionByTime() {
        return new HashMap<>(dataReductionByTime);
    }
    
    public Map<String, Double> getEnergyConsumptionByLayer() {
        return new HashMap<>(energyConsumptionByLayer);
    }
    
    public Map<String, Integer> getPacketsByLayer() {
        return new HashMap<>(packetsByLayer);
    }
    
    public double getAverageEndToEndLatency() {
        return averageEndToEndLatency;
    }
    
    public double getTotalEnergyConsumption() {
        return totalEnergyConsumption;
    }
    
    public double getTotalBandwidthUsage() {
        return totalBandwidthUsage;
    }
    
    public double getOverallDataReductionRatio() {
        return overallDataReductionRatio;
    }
    
    /**
     * Gets the processing time by layer map
     * @return Map containing processing time for each layer (IoT, Edge, Fog, Cloud)
     */
    public Map<String, Double> getProcessingTimeByLayer() {
        return new HashMap<>(processingTimeByLayer);
    }
}
