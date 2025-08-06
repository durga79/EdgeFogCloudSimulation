package org.edgefogcloud.fog;

import org.edgefogcloud.cloud.CloudDataCenter;
import org.edgefogcloud.data.DataPacket;
import org.edgefogcloud.edge.EdgeNode;
import org.edgefogcloud.utils.ConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents a Fog Node that aggregates and processes data from Edge nodes
 * before forwarding to the Cloud
 */
public class FogNode {
    private static final Logger LOGGER = Logger.getLogger(FogNode.class.getName());
    
    private String nodeId;
    private List<EdgeNode> assignedEdgeNodes;
    private CloudDataCenter cloudDataCenter;
    private ConfigManager configManager;
    
    // Fog node characteristics
    private double processingCapacity; // in MIPS
    private double storageCapacity; // in GB
    private double energyConsumption; // in W
    private double bandwidth; // in Mbps
    
    // Data processing parameters
    private double aggregationRatio; // data size reduction due to aggregation
    private Map<String, List<DataPacket>> edgeDataBuffer;
    
    // Metrics
    private int totalPacketsReceived;
    private int totalPacketsProcessed;
    private int totalPacketsForwarded;
    private double totalProcessingTime; // in ms
    private double totalEnergyConsumed; // in Wh
    private double totalBandwidthUsed; // in MB
    
    public FogNode(String nodeId, ConfigManager configManager) {
        this.nodeId = nodeId;
        this.configManager = configManager;
        this.assignedEdgeNodes = new ArrayList<>();
        this.edgeDataBuffer = new HashMap<>();
        
        // Initialize fog node characteristics
        this.processingCapacity = configManager.getFogNodeProcessingCapacity();
        this.storageCapacity = configManager.getFogNodeStorageCapacity();
        this.energyConsumption = configManager.getFogNodeEnergyConsumption();
        this.bandwidth = configManager.getFogNodeBandwidth();
        this.aggregationRatio = configManager.getFogAggregationRatio();
        
        // Initialize metrics
        this.totalPacketsReceived = 0;
        this.totalPacketsProcessed = 0;
        this.totalPacketsForwarded = 0;
        this.totalProcessingTime = 0.0;
        this.totalEnergyConsumed = 0.0;
        this.totalBandwidthUsed = 0.0;
        
        LOGGER.fine("Created Fog Node: " + nodeId);
    }
    
    public void addEdgeNode(EdgeNode edgeNode) {
        assignedEdgeNodes.add(edgeNode);
        edgeDataBuffer.put(edgeNode.getNodeId(), new ArrayList<>());
        LOGGER.fine("Edge Node " + edgeNode.getNodeId() + " assigned to Fog Node " + nodeId);
    }
    
    /**
     * Alternative method name for addEdgeNode to maintain compatibility
     * @param edgeNode The edge node to assign to this fog node
     */
    public void addAssignedEdgeNode(EdgeNode edgeNode) {
        addEdgeNode(edgeNode);
    }
    
    public void receiveData(List<DataPacket> dataPackets, EdgeNode sourceEdge) {
        // Store received data packets in buffer
        String edgeId = sourceEdge.getNodeId();
        if (edgeDataBuffer.containsKey(edgeId)) {
            edgeDataBuffer.get(edgeId).addAll(dataPackets);
            totalPacketsReceived += dataPackets.size();
            
            // Calculate bandwidth usage
            int totalDataSize = 0;
            for (DataPacket packet : dataPackets) {
                totalDataSize += packet.getSize();
            }
            totalBandwidthUsed += totalDataSize / (1024.0 * 1024.0); // Convert to MB
            
            LOGGER.fine("Fog Node " + nodeId + " received " + dataPackets.size() + 
                    " data packets from Edge Node " + edgeId);
        } else {
            LOGGER.warning("Fog Node " + nodeId + " received data from unassigned Edge Node " + edgeId);
        }
    }
    
    public void processData(int currentTime) {
        LOGGER.fine("Fog Node " + nodeId + " processing data at time " + currentTime);
        
        List<DataPacket> aggregatedPackets = new ArrayList<>();
        Map<String, List<DataPacket>> dataByType = new HashMap<>();
        
        // Process data from each edge node's buffer
        for (String edgeId : edgeDataBuffer.keySet()) {
            List<DataPacket> edgeBuffer = edgeDataBuffer.get(edgeId);
            
            // Group packets by data type for aggregation
            for (DataPacket packet : edgeBuffer) {
                String dataType = packet.getDataType();
                if (!dataByType.containsKey(dataType)) {
                    dataByType.put(dataType, new ArrayList<>());
                }
                dataByType.get(dataType).add(packet);
                totalPacketsProcessed++;
            }
            
            // Clear the buffer after processing
            edgeBuffer.clear();
        }
        
        // Aggregate data by type
        for (String dataType : dataByType.keySet()) {
            List<DataPacket> packetsOfType = dataByType.get(dataType);
            
            // Only aggregate if we have multiple packets of the same type
            if (packetsOfType.size() > 1) {
                DataPacket aggregatedPacket = aggregatePackets(packetsOfType, dataType);
                aggregatedPackets.add(aggregatedPacket);
            } else if (packetsOfType.size() == 1) {
                // If only one packet, just process it without aggregation
                DataPacket processedPacket = processPacket(packetsOfType.get(0));
                aggregatedPackets.add(processedPacket);
            }
        }
        
        // Forward aggregated packets to cloud
        forwardAggregatedData(aggregatedPackets);
    }
    
    private DataPacket aggregatePackets(List<DataPacket> packets, String dataType) {
        // Calculate total size of all packets
        int totalSize = 0;
        for (DataPacket packet : packets) {
            totalSize += packet.getSize();
        }
        
        // Apply aggregation to reduce data size
        int aggregatedSize = (int) (totalSize * aggregationRatio);
        
        // Calculate processing time for aggregation
        double processingTime = calculateProcessingTime(totalSize);
        totalProcessingTime += processingTime;
        
        // Update energy consumption
        double energyUsed = calculateEnergyConsumption(processingTime);
        totalEnergyConsumed += energyUsed;
        
        // Create aggregated packet
        return new DataPacket(
                nodeId,
                packets.get(0).getTimestamp(), // Use timestamp of first packet
                aggregatedSize,
                dataType,
                "FOG_AGGREGATED"
        );
    }
    
    private DataPacket processPacket(DataPacket originalPacket) {
        // Simulate processing time
        double processingTime = calculateProcessingTime(originalPacket.getSize());
        totalProcessingTime += processingTime;
        
        // Update energy consumption
        double energyUsed = calculateEnergyConsumption(processingTime);
        totalEnergyConsumed += energyUsed;
        
        // Create processed packet (with slightly reduced size)
        return new DataPacket(
                originalPacket.getSourceId(),
                originalPacket.getTimestamp(),
                (int)(originalPacket.getSize() * 0.9), // Reduce size by 10%
                originalPacket.getDataType(),
                "FOG_PROCESSED"
        );
    }
    
    private double calculateProcessingTime(int dataSize) {
        // Simple processing time model: time = data size / processing capacity
        return dataSize / processingCapacity;
    }
    
    private double calculateEnergyConsumption(double processingTime) {
        // Energy consumption model: E = power * time
        // Convert processing time from ms to hours
        return energyConsumption * (processingTime / 3600000.0);
    }
    
    private void forwardAggregatedData(List<DataPacket> aggregatedPackets) {
        if (cloudDataCenter != null && !aggregatedPackets.isEmpty()) {
            cloudDataCenter.receiveData(aggregatedPackets, this);
            totalPacketsForwarded += aggregatedPackets.size();
            
            // Calculate bandwidth usage for forwarding to cloud
            int totalDataSize = 0;
            for (DataPacket packet : aggregatedPackets) {
                totalDataSize += packet.getSize();
            }
            totalBandwidthUsed += totalDataSize / (1024.0 * 1024.0); // Convert to MB
            
            LOGGER.fine("Fog Node " + nodeId + " forwarded " + aggregatedPackets.size() + 
                    " aggregated packets to Cloud");
        }
    }
    
    // Getters and setters
    public String getNodeId() {
        return nodeId;
    }
    
    public void setCloudDataCenter(CloudDataCenter cloudDataCenter) {
        this.cloudDataCenter = cloudDataCenter;
    }
    
    public List<EdgeNode> getAssignedEdgeNodes() {
        return new ArrayList<>(assignedEdgeNodes);
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
    
    public double getTotalBandwidthUsed() {
        return totalBandwidthUsed;
    }
    
    public double getDataReductionRatio() {
        if (totalPacketsReceived == 0) {
            return 0.0;
        }
        return 1.0 - ((double) totalPacketsForwarded / totalPacketsReceived);
    }
}
