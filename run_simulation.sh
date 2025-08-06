#!/bin/bash

echo "====================================================================="
echo "Edge-Fog-Cloud Simulation with Enhanced Statistics"
echo "====================================================================="

# Create results directory if it doesn't exist
RESULTS_DIR="results"
if [ ! -d "$RESULTS_DIR" ]; then
    mkdir -p "$RESULTS_DIR"
    echo "Created results directory"
fi

# Generate timestamp for results file
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
RESULTS_FILE="$RESULTS_DIR/simulation_results_$TIMESTAMP.txt"

# Set Java memory options
JAVA_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC"

echo "Starting simulation with memory options: $JAVA_OPTS"
echo "Results will be saved to: $RESULTS_FILE"
echo "====================================================================="

# Run the simulation with a timeout of 5 minutes to prevent endless runs
export MAVEN_OPTS="$JAVA_OPTS"
export LOG_LEVEL=${LOG_LEVEL:-WARN}

# Redirect output to both console and file
timeout 300 mvn exec:java -Dexec.mainClass="org.edgefogcloud.test.OptimizedStatisticsRunner" \
    -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL \
    -Dexec.cleanupDaemonThreads=false | tee "$RESULTS_FILE"

# Check if the simulation was killed by timeout
if [ $? -eq 124 ]; then
    echo "====================================================================="
    echo "ERROR: Simulation exceeded the 5-minute timeout and was terminated."
    echo "Consider reducing the simulation parameters or increasing the timeout."
    echo "====================================================================="
    exit 1
fi

echo "====================================================================="
echo "Edge-Fog-Cloud simulation completed successfully."
echo "Results saved to: $RESULTS_FILE"
echo "====================================================================="
