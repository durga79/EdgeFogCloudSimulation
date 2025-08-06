package org.edgefogcloud.network;

import org.edgefogcloud.utils.ConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Models network characteristics and communication between different layers
 * in the Edge-Fog-Cloud architecture
 */
public class NetworkModel {
    private static final Logger LOGGER = Logger.getLogger(NetworkModel.class.getName());
    
    private ConfigManager configManager;
    private Random random;
    
    // Network characteristics
    private double wirelessBaseLatency; // in ms
    private double wirelessBandwidth;   // in KB/s
    private double edgeToFogLatency;    // in ms
    private double fogToCloudLatency;   // in ms
    
    // Network conditions simulation
    private Map<String, Double> linkQualityFactors; // Quality factor for each link type (0.0-1.0)
    private Map<String, Double> congestionFactors;  // Congestion factor for each link type (1.0+)
    
    public NetworkModel(ConfigManager configManager) {
        this.configManager = configManager;
        this.random = new Random();
        
        // Initialize network characteristics from configuration
        this.wirelessBaseLatency = configManager.getWirelessBaseLatency();
        this.wirelessBandwidth = configManager.getWirelessBandwidth();
        this.edgeToFogLatency = configManager.getEdgeToFogLatency();
        this.fogToCloudLatency = configManager.getFogToCloudLatency();
        
        // Initialize link quality and congestion factors
        initializeLinkFactors();
        
        LOGGER.info("Network model initialized");
    }
    
    private void initializeLinkFactors() {
        linkQualityFactors = new HashMap<>();
        congestionFactors = new HashMap<>();
        
        // Initialize with default values
        // Link quality: 1.0 = perfect, 0.0 = completely degraded
        linkQualityFactors.put("IoT-to-Edge", 0.95); // WiFi/BLE/etc.
        linkQualityFactors.put("Edge-to-Fog", 0.98); // Wired/Wireless backhaul
        linkQualityFactors.put("Fog-to-Cloud", 0.99); // Fiber/high-speed connection
        
        // Congestion factor: 1.0 = no congestion, higher values = more congestion
        congestionFactors.put("IoT-to-Edge", 1.2);
        congestionFactors.put("Edge-to-Fog", 1.1);
        congestionFactors.put("Fog-to-Cloud", 1.05);
    }
    
    /**
     * Calculates transmission latency between IoT device and Edge node
     * 
     * @param packetSize Size of the data packet in bytes
     * @return Transmission latency in milliseconds
     */
    public double calculateIoTToEdgeLatency(int packetSize) {
        // Base latency + transmission time + jitter
        double transmissionTime = packetSize / wirelessBandwidth;
        double qualityFactor = linkQualityFactors.get("IoT-to-Edge");
        double congestionFactor = congestionFactors.get("IoT-to-Edge");
        double jitter = generateJitter(5.0); // Up to 5ms of jitter
        
        // Adjust latency based on link quality and congestion
        double adjustedLatency = (wirelessBaseLatency + transmissionTime) * (1.0 / qualityFactor) * congestionFactor;
        
        return adjustedLatency + jitter;
    }
    
    /**
     * Calculates transmission latency between Edge node and Fog node
     * 
     * @param packetSize Size of the data packet in bytes
     * @return Transmission latency in milliseconds
     */
    public double calculateEdgeToFogLatency(int packetSize) {
        // Edge to Fog uses higher bandwidth, so calculate based on that
        double fogBandwidth = configManager.getFogNodeBandwidth() * 1024.0 / 8.0; // Convert Mbps to KB/s
        double transmissionTime = packetSize / fogBandwidth;
        double qualityFactor = linkQualityFactors.get("Edge-to-Fog");
        double congestionFactor = congestionFactors.get("Edge-to-Fog");
        double jitter = generateJitter(3.0); // Up to 3ms of jitter
        
        // Adjust latency based on link quality and congestion
        double adjustedLatency = (edgeToFogLatency + transmissionTime) * (1.0 / qualityFactor) * congestionFactor;
        
        return adjustedLatency + jitter;
    }
    
    /**
     * Calculates transmission latency between Fog node and Cloud
     * 
     * @param packetSize Size of the data packet in bytes
     * @return Transmission latency in milliseconds
     */
    public double calculateFogToCloudLatency(int packetSize) {
        // Fog to Cloud uses very high bandwidth
        double cloudBandwidth = configManager.getCloudBandwidth() * 1024.0 * 1024.0 / 8.0; // Convert Gbps to KB/s
        double transmissionTime = packetSize / cloudBandwidth;
        double qualityFactor = linkQualityFactors.get("Fog-to-Cloud");
        double congestionFactor = congestionFactors.get("Fog-to-Cloud");
        double jitter = generateJitter(2.0); // Up to 2ms of jitter
        
        // Adjust latency based on link quality and congestion
        double adjustedLatency = (fogToCloudLatency + transmissionTime) * (1.0 / qualityFactor) * congestionFactor;
        
        return adjustedLatency + jitter;
    }
    
    /**
     * Simulates network conditions changing over time
     * Call this method periodically to update network conditions
     * 
     * @param currentTime Current simulation time
     */
    public void updateNetworkConditions(int currentTime) {
        // Update link quality factors with small random variations
        for (String linkType : linkQualityFactors.keySet()) {
            double currentQuality = linkQualityFactors.get(linkType);
            double variation = (random.nextDouble() - 0.5) * 0.1; // -0.05 to +0.05 variation
            double newQuality = Math.min(1.0, Math.max(0.5, currentQuality + variation)); // Keep between 0.5 and 1.0
            linkQualityFactors.put(linkType, newQuality);
        }
        
        // Update congestion factors with small random variations
        for (String linkType : congestionFactors.keySet()) {
            double currentCongestion = congestionFactors.get(linkType);
            double variation = (random.nextDouble() - 0.3) * 0.2; // More likely to decrease than increase
            double newCongestion = Math.max(1.0, currentCongestion + variation); // Minimum congestion factor is 1.0
            congestionFactors.put(linkType, newCongestion);
        }
        
        // Simulate periodic network congestion
        if (currentTime % 300 == 0) { // Every 5 minutes of simulation time
            LOGGER.info("Network congestion spike at time " + currentTime);
            congestionFactors.put("IoT-to-Edge", congestionFactors.get("IoT-to-Edge") * 1.5);
            congestionFactors.put("Edge-to-Fog", congestionFactors.get("Edge-to-Fog") * 1.3);
            congestionFactors.put("Fog-to-Cloud", congestionFactors.get("Fog-to-Cloud") * 1.2);
        }
    }
    
    private double generateJitter(double maxJitter) {
        return random.nextDouble() * maxJitter;
    }
    
    // Getters for network characteristics
    public double getWirelessBaseLatency() {
        return wirelessBaseLatency;
    }
    
    public double getWirelessBandwidth() {
        return wirelessBandwidth;
    }
    
    public double getEdgeToFogLatency() {
        return edgeToFogLatency;
    }
    
    public double getFogToCloudLatency() {
        return fogToCloudLatency;
    }
    
    public Map<String, Double> getLinkQualityFactors() {
        return new HashMap<>(linkQualityFactors);
    }
    
    public Map<String, Double> getCongestionFactors() {
        return new HashMap<>(congestionFactors);
    }
}
