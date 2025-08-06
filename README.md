# Edge-Fog-Cloud Simulation with Enhanced Statistics

This project implements a proof-of-concept simulation based on the IEEE paper "Performance Analysis of Edge-Fog-Cloud Architectures in the Internet of Things" (2020/2021). The implementation demonstrates a three-tier architecture for processing IoT data with efficient distribution of computational tasks across edge devices, fog nodes, and cloud infrastructure. The simulation provides comprehensive statistics on resource utilization, energy consumption, network traffic, response times, and cost analysis.

## Project Overview

This simulation implements the key concepts from the IEEE paper "Performance Analysis of Edge-Fog-Cloud Architectures in the Internet of Things" (2020/2021) with the following features:

- Three-tier hierarchical architecture with Edge, Fog, and Cloud layers
- Separate datacenters with different resource capacities for each layer
- Dedicated brokers for each layer to manage resource allocation and scheduling
- Cloudlet distribution across all three layers with appropriate VM assignments
- Guaranteed 100% cloudlet completion through optimized resource allocation
- VM destruction delays to ensure proper resource cleanup

The project analyzes performance metrics across the three layers including:
- Processing distribution and completion rates
- Cost analysis with detailed breakdown by resource type
- Energy consumption estimates
- Network traffic analysis
- Response time and latency measurements

## Key Features

## Running the Simulation

This project provides simple scripts to run the optimized Edge-Fog-Cloud simulation with enhanced statistics:

### For Linux/Mac:

```bash
./run_simulation.sh
```

### For Windows:

```batch
run_simulation.bat
```

### Requirements:

- Java 11 or higher
- Maven 3.6 or higher
- At least 2GB RAM (configured in the run scripts)

The simulation is configured with optimal parameters to ensure 100% cloudlet completion across all layers (Edge, Fog, Cloud) with realistic resource allocation, VM destruction delays, and scheduling.

## Key Features

As described in the IEEE paper "Performance Analysis of Edge-Fog-Cloud Architectures in the Internet of Things", this simulation implements:

- **Multi-tier Architecture**: Complete Edge-Fog-Cloud hierarchy with distinct resource profiles
- **Performance Analysis**: Comprehensive metrics collection and analysis across all three layers
- **Resource Management**: Optimized VM and cloudlet allocation with guaranteed task completion
- **Cost Modeling**: Detailed cost calculations based on resource usage (CPU, memory, storage, bandwidth)
- **Energy Efficiency**: Energy consumption estimates for each layer of the architecture
- **Network Performance**: Traffic analysis and latency measurements between layers
- **Response Time Analysis**: Detailed statistics on processing times across the architecture

## Requirements

- Java 11 or higher
- Maven for dependency management

## Setup Instructions

1. Ensure Java 11+ and Maven are installed on your system
2. Clone or download this repository
3. Navigate to the project directory:
   ```
   cd /path/to/EdgeFogCloudSimulation
   ```
4. Install dependencies:
   ```
   mvn clean install
   ```
5. Run the simulation using the provided script:
   ```
   # For Linux/Mac
   ./run_simulation.sh
   
   # For Windows
   run_simulation.bat
   ```
   
   The simulation results will be saved to the `results` directory with a timestamped filename.

## Project Structure

```
EdgeFogCloudSimulation/
├── src/main/java/org/edgefogcloud/
│   └── test/
│       ├── OptimizedSimpleRunner.java       # Basic optimized simulation runner
│       ├── OptimizedStatisticsRunner.java   # Enhanced statistics simulation runner
│       ├── EnhancedSimulationRunner.java    # Original enhanced simulation runner
│       └── SimulationStatistics.java        # Statistics utility class
├── resources/         # Configuration files
├── results/           # Output data and metrics
├── backup_scripts/    # Backup of old simulation scripts
├── run_simulation.sh  # Main simulation script for Linux/Mac
└── run_simulation.bat # Main simulation script for Windows
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

This implementation is based on the IEEE paper:

"Performance Analysis of Edge-Fog-Cloud Architectures in the Internet of Things" (IEEE, 2020/2021)

## Dependencies

- CloudSim Plus 6.4.3
- Java Standard Libraries
- Maven Build System

## Simulation Configuration

The simulation is configured with:
- Small cloudlet lengths and reasonable VM destruction delays to ensure cloudlet completion
- Minimal logging with WARN level to reduce console clutter
- JVM options set for 2GB heap, metaspace size, and G1GC for performance
- 5-minute timeout to prevent endless runs
- Results saved to timestamped files in the `results` directory
