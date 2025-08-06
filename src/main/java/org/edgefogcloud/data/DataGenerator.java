package org.edgefogcloud.data;

import org.edgefogcloud.utils.ConfigManager;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Generates simulated data packets for IoT devices
 */
public class DataGenerator {
    private static final Logger LOGGER = Logger.getLogger(DataGenerator.class.getName());
    
    private String deviceType;
    private ConfigManager configManager;
    private Random random;
    
    // Data type probabilities based on device type
    private double[] dataTypeProbabilities;
    private String[] dataTypes = {"SENSOR_DATA", "IMAGE_DATA", "VIDEO_DATA", "AUDIO_DATA", "TEXT_DATA"};
    
    public DataGenerator(String deviceType, ConfigManager configManager) {
        this.deviceType = deviceType;
        this.configManager = configManager;
        this.random = new Random();
        
        // Initialize data type probabilities based on device type
        initializeDataTypeProbabilities();
    }
    
    private void initializeDataTypeProbabilities() {
        dataTypeProbabilities = new double[dataTypes.length];
        
        switch (deviceType) {
            case "SENSOR":
                // Sensors mostly generate sensor data
                dataTypeProbabilities[0] = 0.9;  // SENSOR_DATA
                dataTypeProbabilities[1] = 0.05; // IMAGE_DATA
                dataTypeProbabilities[2] = 0.0;  // VIDEO_DATA
                dataTypeProbabilities[3] = 0.0;  // AUDIO_DATA
                dataTypeProbabilities[4] = 0.05; // TEXT_DATA
                break;
            case "ACTUATOR":
                // Actuators mostly generate sensor data and text data
                dataTypeProbabilities[0] = 0.7;  // SENSOR_DATA
                dataTypeProbabilities[1] = 0.0;  // IMAGE_DATA
                dataTypeProbabilities[2] = 0.0;  // VIDEO_DATA
                dataTypeProbabilities[3] = 0.0;  // AUDIO_DATA
                dataTypeProbabilities[4] = 0.3;  // TEXT_DATA
                break;
            case "SMARTPHONE":
                // Smartphones generate all types of data
                dataTypeProbabilities[0] = 0.2;  // SENSOR_DATA
                dataTypeProbabilities[1] = 0.2;  // IMAGE_DATA
                dataTypeProbabilities[2] = 0.2;  // VIDEO_DATA
                dataTypeProbabilities[3] = 0.2;  // AUDIO_DATA
                dataTypeProbabilities[4] = 0.2;  // TEXT_DATA
                break;
            case "WEARABLE":
                // Wearables mostly generate sensor data and some audio
                dataTypeProbabilities[0] = 0.7;  // SENSOR_DATA
                dataTypeProbabilities[1] = 0.1;  // IMAGE_DATA
                dataTypeProbabilities[2] = 0.0;  // VIDEO_DATA
                dataTypeProbabilities[3] = 0.15; // AUDIO_DATA
                dataTypeProbabilities[4] = 0.05; // TEXT_DATA
                break;
            default:
                // Default distribution
                dataTypeProbabilities[0] = 0.4;  // SENSOR_DATA
                dataTypeProbabilities[1] = 0.15; // IMAGE_DATA
                dataTypeProbabilities[2] = 0.15; // VIDEO_DATA
                dataTypeProbabilities[3] = 0.15; // AUDIO_DATA
                dataTypeProbabilities[4] = 0.15; // TEXT_DATA
        }
    }
    
    public DataPacket generateDataPacket(String sourceId, int timestamp) {
        // Determine data type based on probabilities
        String dataType = selectDataType();
        
        // Generate data size based on data type
        int dataSize = generateDataSize(dataType);
        
        // Create and return data packet
        DataPacket packet = new DataPacket(
                sourceId,
                timestamp,
                dataSize,
                dataType,
                "RAW"
        );
        
        return packet;
    }
    
    private String selectDataType() {
        double randomValue = random.nextDouble();
        double cumulativeProbability = 0.0;
        
        for (int i = 0; i < dataTypeProbabilities.length; i++) {
            cumulativeProbability += dataTypeProbabilities[i];
            if (randomValue <= cumulativeProbability) {
                return dataTypes[i];
            }
        }
        
        // Default to sensor data if something goes wrong
        return "SENSOR_DATA";
    }
    
    private int generateDataSize(String dataType) {
        // Generate data size based on data type (in bytes)
        switch (dataType) {
            case "SENSOR_DATA":
                // Sensor data is small, typically 10-100 bytes
                return 10 + random.nextInt(90);
            case "IMAGE_DATA":
                // Image data is medium-sized, typically 100KB-1MB
                return 100_000 + random.nextInt(900_000);
            case "VIDEO_DATA":
                // Video data is large, typically 1-10MB
                return 1_000_000 + random.nextInt(9_000_000);
            case "AUDIO_DATA":
                // Audio data is medium-sized, typically 50-500KB
                return 50_000 + random.nextInt(450_000);
            case "TEXT_DATA":
                // Text data is small, typically 100-5000 bytes
                return 100 + random.nextInt(4900);
            default:
                // Default size
                return 1000 + random.nextInt(9000);
        }
    }
}
