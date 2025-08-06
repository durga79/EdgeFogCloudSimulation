package org.edgefogcloud.data;

/**
 * Represents a data packet in the Edge-Fog-Cloud architecture
 * This is the basic unit of data transfer between different layers
 */
public class DataPacket {
    private String sourceId;      // ID of the source device/node
    private int timestamp;        // Time when the data was generated
    private int size;             // Size of the data in bytes
    private String dataType;      // Type of data (e.g., SENSOR_DATA, IMAGE_DATA)
    private String processingStatus; // Current processing status of the data
    
    public DataPacket(String sourceId, int timestamp, int size, String dataType, String processingStatus) {
        this.sourceId = sourceId;
        this.timestamp = timestamp;
        this.size = size;
        this.dataType = dataType;
        this.processingStatus = processingStatus;
    }
    
    // Getters
    public String getSourceId() {
        return sourceId;
    }
    
    public int getTimestamp() {
        return timestamp;
    }
    
    public int getSize() {
        return size;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public String getProcessingStatus() {
        return processingStatus;
    }
    
    // Setters
    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    @Override
    public String toString() {
        return "DataPacket{" +
                "sourceId='" + sourceId + '\'' +
                ", timestamp=" + timestamp +
                ", size=" + size + " bytes" +
                ", dataType='" + dataType + '\'' +
                ", processingStatus='" + processingStatus + '\'' +
                '}';
    }
}
