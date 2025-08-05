# Edge-Fog-Cloud Architecture for IoT Data Processing

This project implements a proof-of-concept simulation based on the IEEE paper "Performance Analysis of Edge-Fog-Cloud Architectures in the Internet of Things" (2020/2021). The implementation demonstrates a three-tier architecture for processing IoT data with efficient distribution of computational tasks across edge devices, fog nodes, and cloud infrastructure.

## Project Overview

This simulation models:
- IoT devices generating data at the network edge
- Edge nodes performing initial data filtering and processing
- Fog nodes aggregating and processing intermediate data
- Cloud servers handling complex analytics and long-term storage

The project focuses on demonstrating how this hierarchical approach optimizes:
- Latency reduction through edge processing
- Bandwidth efficiency through data filtering
- Computational load distribution across the network
- Energy efficiency in IoT applications

## Key Features

## Running the Simulation

This project provides several scripts to run the simulation with different resource configurations:

### For Linux/Mac:

- `run_full_simulation.sh`: Runs the complete Edge-Fog-Cloud simulation with full resources
- `run_lightweight_simulation.sh`: Runs a minimal version of the simulation with reduced resource usage

### For Windows:

- `run_full_simulation.bat`: Runs the complete Edge-Fog-Cloud simulation with full resources
- `run_lightweight_simulation.bat`: Runs a minimal version of the simulation with reduced resource usage

### Requirements:

- Java 11 or higher
- Maven 3.6 or higher
- At least 2GB RAM for the full simulation (512MB for lightweight)

### Adjusting Simulation Parameters:

You can modify the simulation parameters in `src/main/java/org/edgefogcloud/utils/ConfigManager.java` to adjust:
- Number of IoT devices, edge nodes, and fog nodes
- Processing capacities and storage capabilities
- Network bandwidth and latency
- Energy consumption parameters
- Cost models

## Key Features

- Simulation of IoT devices with configurable data generation patterns
- Edge processing with customizable filtering algorithms
- Fog layer for intermediate data aggregation and processing
- Cloud computing simulation for complex analytics
- Performance metrics collection and visualization
- Wireless communication simulation between layers (WiFi, BLE, etc.)

## Requirements

- Java 11 or higher
- Maven for dependency management

## Setup Instructions

1. Ensure Java 11+ and Maven are installed on your system
2. Navigate to the project directory:
   ```
   cd /path/to/EdgeFogCloudSimulation
   ```
3. Install dependencies:
   ```
   mvn clean install
   ```
4. Run the simulation using the provided script:
   ```
   ./run_simulation.sh
   ```
   
   Or manually with:
   ```
   mvn exec:java -Dexec.mainClass="org.edgefogcloud.simulation.EdgeFogCloudSimulation"
   ```

## Project Structure

```
EdgeFogCloudSimulation/
├── src/main/java/org/edgefogcloud/
│   ├── devices/       # IoT device simulation
│   ├── edge/          # Edge processing logic
│   ├── fog/           # Fog node implementation
│   ├── cloud/         # Cloud processing
│   ├── network/       # Network communication simulation
│   ├── data/          # Data generation and models
│   ├── utils/         # Utility classes
│   └── simulation/    # Main simulation classes
├── resources/         # Configuration files
├── results/           # Output data and metrics
└── docs/              # Documentation
```

## Configuration

The simulation parameters can be configured in the `resources/config.properties` file:

### Simulation Parameters
- `simulation.time`: Duration of simulation in seconds
- `simulation.num_iot_devices`: Number of IoT devices to simulate
- `simulation.num_edge_nodes`: Number of edge nodes
- `simulation.num_fog_nodes`: Number of fog nodes

### Network Parameters
- `network.wireless.base_latency`: Base latency for wireless communication (ms)
- `network.wireless.bandwidth`: Bandwidth for wireless communication (KB/s)
- `network.edge_to_fog.latency`: Latency between edge and fog nodes (ms)
- `network.fog_to_cloud.latency`: Latency between fog and cloud (ms)

### Edge Node Parameters
- `edge.processing_capacity`: Processing capacity of edge nodes (MIPS)
- `edge.storage_capacity`: Storage capacity of edge nodes (MB)
- `edge.energy_consumption`: Energy consumption of edge nodes (W)
- `edge.filtering_ratio`: Percentage of data filtered at edge nodes

### Fog Node Parameters
- `fog.processing_capacity`: Processing capacity of fog nodes (MIPS)
- `fog.storage_capacity`: Storage capacity of fog nodes (MB)
- `fog.energy_consumption`: Energy consumption of fog nodes (W)
- `fog.bandwidth`: Bandwidth between fog nodes (Mbps)
- `fog.aggregation_ratio`: Data size reduction due to aggregation

### Cloud Parameters
- `cloud.processing_capacity`: Processing capacity of cloud (MIPS)
- `cloud.storage_capacity`: Storage capacity of cloud (TB)
- `cloud.energy_consumption`: Energy consumption of cloud (W)
- `cloud.bandwidth`: Bandwidth to cloud (Gbps)

## Results and Metrics

After running the simulation, results will be available in the `results/` directory:

- **Latency Chart**: Shows end-to-end latency over time
- **Data Reduction Chart**: Shows data processing distribution by layer
- **Energy Consumption Chart**: Shows energy usage by layer
- **Bandwidth Usage Chart**: Shows bandwidth usage over time

The console output will also display summary statistics including:
- Average end-to-end latency
- Total energy consumption by layer
- Total bandwidth usage
- Overall data reduction ratio

## Documentation

Additional documentation is available in the `docs/` directory:

- `implementation_details.md`: Detailed explanation of the implementation
- `project_report_template.md`: Template for creating your project report
- `architecture_diagram.txt`: ASCII representation of the system architecture

## References

This implementation is based on the paper:
"Performance Analysis of Edge-Fog-Cloud Architectures in the Internet of Things" (IEEE, 2020/2021)
# EdgeFogCloudSimulation
