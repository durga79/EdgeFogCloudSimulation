@echo off

echo =====================================================================
echo Edge-Fog-Cloud Simulation with Enhanced Statistics
echo =====================================================================

REM Create directories if they don't exist
if not exist results mkdir results
if not exist resources mkdir resources

REM Check if config file exists, create if not
if not exist resources\config.properties (
    echo Creating configuration file with realistic values...
    (
        echo # Simulation parameters
        echo simulation.time=3600
        echo num.iot_devices=50
        echo num.edge_nodes=5
        echo num.fog_nodes=2
        echo.
        echo # Network parameters
        echo network.wireless_latency=10.0
        echo network.wireless_bandwidth=54.0
        echo network.edge_to_fog_latency=15.0
        echo network.fog_to_cloud_latency=50.0
        echo.
        echo # Edge node parameters
        echo edge.processing_capacity=2000.0
        echo edge.storage_capacity=10240.0
        echo edge.energy_consumption=50.0
        echo edge.filtering_ratio=0.6
        echo.
        echo # Fog node parameters
        echo fog.processing_capacity=8000.0
        echo fog.storage_capacity=102400.0
        echo fog.energy_consumption=200.0
        echo fog.bandwidth=100.0
        echo fog.aggregation_ratio=0.5
        echo.
        echo # Cloud parameters
        echo cloud.processing_capacity=50000.0
        echo cloud.storage_capacity=1048576.0
        echo cloud.energy_consumption=1000.0
        echo cloud.bandwidth=1000.0
        echo.
        echo # Host parameters
        echo cloud.host.pes=16
        echo cloud.host.mips=10000
        echo cloud.host.ram=65536
        echo cloud.host.storage=1048576
        echo cloud.host.bw=10000
        echo.
        echo fog.host.pes=8
        echo fog.host.mips=5000
        echo fog.host.ram=32768
        echo fog.host.storage=524288
        echo fog.host.bw=5000
        echo.
        echo edge.host.pes=4
        echo edge.host.mips=2000
        echo edge.host.ram=8192
        echo edge.host.storage=102400
        echo edge.host.bw=1000
        echo.
        echo # Cost parameters ($$ per hour^)
        echo cost.cpu=0.10
        echo cost.ram=0.05
        echo cost.storage=0.01
        echo cost.bandwidth=0.02
    ) > resources\config.properties
    echo Configuration file created.
)

REM Set Java memory options
set MAVEN_OPTS=-Xmx2g -XX:MaxMetaspaceSize=512m

REM Generate timestamp for results file
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set TIMESTAMP=%datetime:~0,8%_%datetime:~8,6%
set RESULTS_FILE=results\simulation_results_%TIMESTAMP%.txt

REM Set Java memory options
set MAVEN_OPTS=-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC

echo Starting simulation with memory options: %MAVEN_OPTS%
echo Results will be saved to: %RESULTS_FILE%
echo =====================================================================

REM Run the simulation
call mvn exec:java -Dexec.mainClass="org.edgefogcloud.test.OptimizedStatisticsRunner" -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN -Dexec.cleanupDaemonThreads=false > %RESULTS_FILE%

echo =====================================================================
echo Edge-Fog-Cloud simulation completed successfully.
echo Results saved to: %RESULTS_FILE%
echo =====================================================================
pause
