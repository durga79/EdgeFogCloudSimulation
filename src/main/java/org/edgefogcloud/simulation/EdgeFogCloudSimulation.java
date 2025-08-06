package org.edgefogcloud.simulation;

import org.edgefogcloud.cloud.CloudDataCenter;
import org.edgefogcloud.data.DataGenerator;
import org.edgefogcloud.devices.IoTDevice;
import org.edgefogcloud.edge.EdgeNode;
import org.edgefogcloud.fog.FogNode;
import org.edgefogcloud.network.NetworkModel;
import org.edgefogcloud.utils.ConfigManager;
import org.edgefogcloud.utils.MetricsCollector;
import org.edgefogcloud.utils.ResultsVisualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// CloudSim Plus imports
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;

/**
 * Main simulation class for Edge-Fog-Cloud architecture in IoT environments
 * Based on the paper: "Performance Analysis of Edge-Fog-Cloud Architectures in the Internet of Things"
 * Integrated with CloudSim Plus 7.3.3 for simulation
 */
public class EdgeFogCloudSimulation {
    private static final Logger LOGGER = Logger.getLogger(EdgeFogCloudSimulation.class.getName());
    
    // Configuration and metrics
    private ConfigManager configManager;
    private MetricsCollector metricsCollector;
    private ResultsVisualizer resultsVisualizer;
    
    // Custom objects for our Edge-Fog-Cloud architecture
    private List<IoTDevice> iotDevices;
    private List<EdgeNode> edgeNodes;
    private List<FogNode> fogNodes;
    private CloudDataCenter cloudDataCenter;
    private NetworkModel networkModel;
    
    // CloudSim specific objects
    private List<org.cloudbus.cloudsim.datacenters.Datacenter> datacenters;
    private List<org.cloudbus.cloudsim.hosts.Host> hosts;
    private List<org.cloudbus.cloudsim.vms.Vm> vms;
    private List<org.cloudbus.cloudsim.cloudlets.Cloudlet> cloudlets;
    private org.cloudbus.cloudsim.brokers.DatacenterBroker broker;
    private org.cloudbus.cloudsim.core.CloudSim simulation;
    
    private int simulationTime; // in seconds
    
    /**
     * Constructor initializes the simulation environment
     */
    public EdgeFogCloudSimulation() {
        LOGGER.info("Initializing Edge-Fog-Cloud Simulation with CloudSim Plus...");
        this.configManager = new ConfigManager();
        this.metricsCollector = new MetricsCollector();
        this.resultsVisualizer = new ResultsVisualizer();
        
        // Initialize our custom objects for metrics
        this.iotDevices = new ArrayList<>();
        this.edgeNodes = new ArrayList<>();
        this.fogNodes = new ArrayList<>();
        
        // Initialize CloudSim Plus specific objects
        this.datacenters = new ArrayList<>();
        this.hosts = new ArrayList<>();
        this.vms = new ArrayList<>();
        this.cloudlets = new ArrayList<>();
        
        // Load simulation parameters
        this.simulationTime = configManager.getSimulationTime();
        
        // Initialize network model
        this.networkModel = new NetworkModel(configManager);
        
        // Initialize cloud data center
        this.cloudDataCenter = new CloudDataCenter(configManager);
    }
    
    /**
     * Sets up the simulation environment with both CloudSim Plus components
     * and our custom objects for detailed metrics
     */
    private void setupSimulation() {
        LOGGER.info("Setting up CloudSim Plus simulation environment...");
        
        // Initialize CloudSim Plus library
        this.simulation = new CloudSim();
        
        // Create broker to manage VM and cloudlet allocation
        this.broker = new DatacenterBrokerSimple(simulation);
        
        // Create datacenters for cloud, fog, and edge layers
        createCloudDatacenter();
        createFogDatacenter();
        createEdgeDatacenter();
        
        // Create VMs for each layer
        createCloudVMs();
        createFogVMs();
        createEdgeVMs();
        
        // Create cloudlets representing tasks
        createCloudlets();
        
        // Submit VMs and cloudlets to broker
        broker.submitVmList(vms);
        broker.submitCloudletList(cloudlets);
        
        // Create custom topology for our detailed metrics
        createCustomTopology();
        
        LOGGER.info("Simulation environment setup completed.");
    }
    
    /**
     * Creates the Cloud datacenter with high-performance hosts
     */
    private void createCloudDatacenter() {
        LOGGER.info("Creating Cloud datacenter...");
        
        // Create PEs (Processing Elements, i.e., CPU cores)
        List<org.cloudbus.cloudsim.resources.Pe> peList = new ArrayList<>();
        int cloudPes = configManager.getCloudHostPes();
        int mips = configManager.getCloudHostMips();
        for (int i = 0; i < cloudPes; i++) {
            peList.add(new org.cloudbus.cloudsim.resources.PeSimple(mips));
        }
        
        int hostId = 0;
        long ram = 65536; // 64 GB
        long storage = 10000000; // 10 TB
        long bw = 100000; // 100 Gbps
        
        org.cloudbus.cloudsim.hosts.Host host = new org.cloudbus.cloudsim.hosts.HostSimple(ram, bw, storage, peList);
        host.setId(hostId);
        host.setVmScheduler(new org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared());
        
        // Create a list to hold hosts
        List<org.cloudbus.cloudsim.hosts.Host> hostList = new ArrayList<>();
        hostList.add(host);
        
        // Create Datacenter
        org.cloudbus.cloudsim.datacenters.Datacenter datacenter = new org.cloudbus.cloudsim.datacenters.DatacenterSimple(simulation, hostList);
        datacenter.setName("CloudDatacenter");
        
        // Set datacenter characteristics
        datacenter.getCharacteristics()
            .setCostPerSecond(3.0)
            .setCostPerMem(0.05)
            .setCostPerStorage(0.001)
            .setCostPerBw(0.1);
        
        datacenters.add(datacenter);
        hosts.add(host);
        
        // Create our custom Cloud datacenter for metrics
        cloudDataCenter = new CloudDataCenter(configManager);
        
        LOGGER.info("Cloud datacenter created with " + hostList.size() + " hosts");
    }
    
    /**
     * Creates the Fog datacenter with medium-performance hosts
     */
    private void createFogDatacenter() {
        LOGGER.info("Creating Fog datacenter...");
        
        // Create PEs (Processing Elements, i.e., CPU cores)
        List<org.cloudbus.cloudsim.resources.Pe> peList = new ArrayList<>();
        int fogPes = configManager.getFogHostPes();
        int mips = configManager.getFogHostMips();
        for (int i = 0; i < fogPes; i++) {
            peList.add(new org.cloudbus.cloudsim.resources.PeSimple(mips));
        }
        
        // Create Host
        int hostId = 1; // Use a different ID than cloud host
        long ram = configManager.getFogHostRam(); // in MB
        long storage = configManager.getFogHostStorage(); // in MB
        long bw = configManager.getFogHostBw(); // in Mbps
        
        org.cloudbus.cloudsim.hosts.Host host = new org.cloudbus.cloudsim.hosts.HostSimple(ram, bw, storage, peList);
        host.setId(hostId);
        host.setVmScheduler(new org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared());
                
        List<org.cloudbus.cloudsim.hosts.Host> hostList = new ArrayList<>();
        hostList.add(host);
        
        // Create Datacenter
        org.cloudbus.cloudsim.datacenters.Datacenter datacenter = new org.cloudbus.cloudsim.datacenters.DatacenterSimple(simulation, hostList);
        datacenter.setName("FogDatacenter");
        
        // Set datacenter characteristics
        datacenter.getCharacteristics()
            .setCostPerSecond(0.005) // Lower cost than cloud
            .setCostPerMem(0.002)
            .setCostPerStorage(0.0005)
            .setCostPerBw(0.0005);
        
        datacenters.add(datacenter);
        hosts.add(host);
        LOGGER.info("Fog Datacenter created with " + fogPes + " PEs, " + ram + "MB RAM");
        
        // Create our custom Fog nodes for metrics
        int numFogNodes = configManager.getNumFogNodes();
        for (int i = 0; i < numFogNodes; i++) {
            FogNode fogNode = new FogNode("Fog-Node-" + i, configManager);
            fogNodes.add(fogNode);
        }
        LOGGER.info("Created " + numFogNodes + " custom Fog nodes for metrics");
    }

/**
 * Creates Edge datacenter with low-performance hosts
 */
private void createEdgeDatacenter() {
    LOGGER.info("Creating Edge datacenter...");

    // Create PEs (Processing Elements, i.e., CPU cores)
    List<org.cloudbus.cloudsim.resources.Pe> peList = new ArrayList<>();
    int edgePes = configManager.getEdgeHostPes();
    int mips = configManager.getEdgeHostMips();
    for (int i = 0; i < edgePes; i++) {
        peList.add(new org.cloudbus.cloudsim.resources.PeSimple(mips));
    }

    // Create Host
    int hostId = 2; // Use a different ID than cloud and fog hosts
    long ram = configManager.getEdgeHostRam(); // in MB
    long storage = configManager.getEdgeHostStorage(); // in MB
    long bw = configManager.getEdgeHostBw(); // in Mbps

    org.cloudbus.cloudsim.hosts.Host host = new org.cloudbus.cloudsim.hosts.HostSimple(ram, bw, storage, peList);
    host.setId(hostId);
    host.setVmScheduler(new org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared());

    List<org.cloudbus.cloudsim.hosts.Host> hostList = new ArrayList<>();
    hostList.add(host);

    // Create Datacenter
    org.cloudbus.cloudsim.datacenters.Datacenter datacenter = new org.cloudbus.cloudsim.datacenters.DatacenterSimple(simulation, hostList);
    datacenter.setName("EdgeDatacenter");
    
    // Set datacenter characteristics
    datacenter.getCharacteristics()
        .setCostPerSecond(0.003) // Lower cost than fog
        .setCostPerMem(0.001)
        .setCostPerStorage(0.0003)
        .setCostPerBw(0.0002);
    
    datacenters.add(datacenter);
    hosts.add(host);
    LOGGER.info("Edge Datacenter created with " + edgePes + " PEs, " + ram + "MB RAM");

    // Create our custom Edge nodes for metrics
    int numEdgeNodes = configManager.getNumEdgeNodes();
    for (int i = 0; i < numEdgeNodes; i++) {
        EdgeNode edgeNode = new EdgeNode("Edge-Node-" + i, configManager);
        edgeNodes.add(edgeNode);
        
        // Create IoT devices connected to this edge node
        int devicesPerEdge = configManager.getNumIoTDevices() / configManager.getNumEdgeNodes();
        for (int j = 0; j < devicesPerEdge; j++) {
            int deviceId = i * devicesPerEdge + j;
            IoTDevice device = new IoTDevice("IoT-Device-" + deviceId, configManager);
            device.setAssignedEdgeNode(edgeNode);
            edgeNode.addAssignedDevice(device);
            iotDevices.add(device);
        }
    }
    
    LOGGER.info("Edge datacenter created with " + numEdgeNodes + " nodes");
}
    
    /**
     * Creates VMs for Cloud layer
     */
    private void createCloudVMs() {
        LOGGER.info("Creating Cloud VMs...");
        
        // Cloud VM characteristics
        int vmId = 0;
        int mips = 50000; // High MIPS for cloud
        long size = 100000; // 100 GB VM image size
        long ram = 16384; // 16 GB RAM
        long bw = 10000; // 10 Gbps
        int pesNumber = 8; // 8 CPU cores
        
        // Create Cloud VM with CloudSim Plus
        org.cloudbus.cloudsim.vms.Vm cloudVm = new org.cloudbus.cloudsim.vms.VmSimple(vmId, mips, pesNumber)
            .setRam(ram)
            .setBw(bw)
            .setSize(size)
            .setCloudletScheduler(new org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared());
        
        vms.add(cloudVm);
        broker.submitVm(cloudVm);
        
        LOGGER.info("Created Cloud VM with ID: " + vmId + ", MIPS: " + mips + ", Cores: " + pesNumber);
    }
    
    /**
     * Creates VMs for Fog nodes
     */
    private void createFogVMs() {
        LOGGER.info("Creating Fog VMs...");
        
        // Fog VM characteristics
        int startVmId = vms.size();
        int mips = 20000; // Medium MIPS for fog
        long size = 50000; // 50 GB VM image size
        long ram = 8192; // 8 GB RAM
        long bw = 1000; // 1 Gbps
        int pesNumber = 4; // 4 CPU cores
        
        // Create Fog VMs
        for (int i = 0; i < fogNodes.size(); i++) {
            int vmId = startVmId + i;
            org.cloudbus.cloudsim.vms.Vm fogVm = new org.cloudbus.cloudsim.vms.VmSimple(vmId, mips, pesNumber)
                .setRam(ram)
                .setBw(bw)
                .setSize(size)
                .setCloudletScheduler(new org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared());
            
            vms.add(fogVm);
            broker.submitVm(fogVm);
        }
        
        LOGGER.info("Created " + fogNodes.size() + " Fog VMs");
    }
    
    /**
     * Creates VMs for Edge nodes
     */
    private void createEdgeVMs() {
        LOGGER.info("Creating Edge VMs...");
        
        // Edge VM characteristics
        int startVmId = vms.size();
        int mips = 8000; // Lower MIPS for edge
        long size = 20000; // 20 GB VM image size
        long ram = 4096; // 4 GB RAM
        long bw = 100; // 100 Mbps
        int pesNumber = 2; // 2 CPU cores
        
        // Create Edge VMs
        for (int i = 0; i < edgeNodes.size(); i++) {
            int vmId = startVmId + i;
            org.cloudbus.cloudsim.vms.Vm edgeVm = new org.cloudbus.cloudsim.vms.VmSimple(vmId, mips, pesNumber)
                .setRam(ram)
                .setBw(bw)
                .setSize(size)
                .setCloudletScheduler(new org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared());
            
            vms.add(edgeVm);
            broker.submitVm(edgeVm);
        }
        
        LOGGER.info("Created " + edgeNodes.size() + " Edge VMs");
    }
    
    /**
     * Creates cloudlets representing tasks at different layers
     */
    private void createCloudlets() {
        LOGGER.info("Creating cloudlets (tasks)...");
        
        // Create utilization model for cloudlets
        org.cloudbus.cloudsim.utilizationmodels.UtilizationModel utilizationModel = 
            new org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull();
        
        // Create cloudlets for IoT data processing
        int cloudletId = 0;
        
        // Assign VMs for each layer
        org.cloudbus.cloudsim.vms.Vm cloudVm = vms.get(0); // First VM is cloud VM
        List<org.cloudbus.cloudsim.vms.Vm> fogVms = new ArrayList<>();
        List<org.cloudbus.cloudsim.vms.Vm> edgeVms = new ArrayList<>();
        
        // Separate VMs by layer
        int fogVmCount = fogNodes.size();
        int edgeVmCount = edgeNodes.size();
        
        for (int i = 1; i <= fogVmCount; i++) {
            fogVms.add(vms.get(i));
        }
        
        for (int i = 1 + fogVmCount; i < 1 + fogVmCount + edgeVmCount; i++) {
            edgeVms.add(vms.get(i));
        }
        
        for (int i = 0; i < iotDevices.size(); i++) {
            IoTDevice device = iotDevices.get(i);
            EdgeNode assignedEdge = device.getAssignedEdgeNode();
            int edgeIndex = edgeNodes.indexOf(assignedEdge);
            int fogIndex = edgeIndex % fogNodes.size(); // Simple assignment strategy
            
            org.cloudbus.cloudsim.vms.Vm edgeVm = edgeVms.get(edgeIndex);
            org.cloudbus.cloudsim.vms.Vm fogVm = fogVms.get(fogIndex);
            
            // Data generation cloudlet (IoT device)
            int pesNumber = 1;
            long length = 1000; // Instructions
            long fileSize = 1000; // Input file size (bytes)
            long outputSize = 2000; // Output file size (bytes)
            
            org.cloudbus.cloudsim.cloudlets.Cloudlet dataGenCloudlet = 
                new org.cloudbus.cloudsim.cloudlets.CloudletSimple(cloudletId++, length, pesNumber)
                    .setFileSize(fileSize)
                    .setOutputSize(outputSize)
                    .setUtilizationModelCpu(utilizationModel)
                    .setUtilizationModelRam(utilizationModel)
                    .setUtilizationModelBw(utilizationModel);
            
            cloudlets.add(dataGenCloudlet);
            broker.bindCloudletToVm(dataGenCloudlet, edgeVm);
            
            // Data filtering cloudlet (Edge)
            pesNumber = 2;
            length = 3000; // More instructions for filtering
            fileSize = 2000; // Input file size (bytes)
            outputSize = 1000; // Output file size (bytes)
            
            org.cloudbus.cloudsim.cloudlets.Cloudlet filterCloudlet = 
                new org.cloudbus.cloudsim.cloudlets.CloudletSimple(cloudletId++, length, pesNumber)
                    .setFileSize(fileSize)
                    .setOutputSize(outputSize)
                    .setUtilizationModelCpu(utilizationModel)
                    .setUtilizationModelRam(utilizationModel)
                    .setUtilizationModelBw(utilizationModel);
            
            cloudlets.add(filterCloudlet);
            broker.bindCloudletToVm(filterCloudlet, edgeVm);
            
            // Data processing cloudlet (Fog)
            pesNumber = 4;
            length = 5000; // More instructions for processing
            fileSize = 1000; // Input file size (bytes)
            outputSize = 500; // Output file size (bytes)
            
            org.cloudbus.cloudsim.cloudlets.Cloudlet processCloudlet = 
                new org.cloudbus.cloudsim.cloudlets.CloudletSimple(cloudletId++, length, pesNumber)
                    .setFileSize(fileSize)
                    .setOutputSize(outputSize)
                    .setUtilizationModelCpu(utilizationModel)
                    .setUtilizationModelRam(utilizationModel)
                    .setUtilizationModelBw(utilizationModel);
            
            cloudlets.add(processCloudlet);
            broker.bindCloudletToVm(processCloudlet, fogVm);
            
            // Data analytics cloudlet (Cloud)
            pesNumber = 8;
            length = 10000; // Most instructions for analytics
            fileSize = 500; // Input file size (bytes)
            outputSize = 200; // Output file size (bytes)
            
            org.cloudbus.cloudsim.cloudlets.Cloudlet analyticsCloudlet = 
                new org.cloudbus.cloudsim.cloudlets.CloudletSimple(cloudletId++, length, pesNumber)
                    .setFileSize(fileSize)
                    .setOutputSize(outputSize)
                    .setUtilizationModelCpu(utilizationModel)
                    .setUtilizationModelRam(utilizationModel)
                    .setUtilizationModelBw(utilizationModel);
            
            cloudlets.add(analyticsCloudlet);
            broker.bindCloudletToVm(analyticsCloudlet, cloudVm);
        }
        
        LOGGER.info("Created " + cloudlets.size() + " cloudlets");
    }
    
    /**
     * Creates custom topology for our detailed metrics collection
     */
    private void createCustomTopology() {
        LOGGER.info("Setting up custom topology for additional metrics...");
        
        // Connect edge nodes to fog nodes (round-robin)
        for (int i = 0; i < edgeNodes.size(); i++) {
            int fogIndex = i % fogNodes.size();
            FogNode targetFog = fogNodes.get(fogIndex);
            EdgeNode edgeNode = edgeNodes.get(i);
            edgeNode.setAssignedFogNode(targetFog);
            targetFog.addAssignedEdgeNode(edgeNode);
        }
        
        // All fog nodes connect to the cloud data center
        for (FogNode fogNode : fogNodes) {
            fogNode.setCloudDataCenter(cloudDataCenter);
            cloudDataCenter.addFogNode(fogNode);
        }
    }
    
    /**
     * Runs the simulation using CloudSim Plus and our custom objects
     */
    public void runSimulation() {
        LOGGER.info("Starting simulation...");
        
        try {
            // Start the CloudSim Plus simulation
            LOGGER.info("Starting CloudSim Plus simulation...");
            broker.getSimulation().start();
            
            // Print the CloudSim Plus results
            List<org.cloudbus.cloudsim.cloudlets.Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
            new org.cloudsimplus.builders.tables.CloudletsTableBuilder(finishedCloudlets).build();
            
            // Run our custom simulation logic for additional metrics
            runCustomSimulation();
            
            LOGGER.info("Simulation completed successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error running simulation: " + e.getMessage(), e);
        }
    }
    
    /**
     * Runs our custom simulation logic for detailed metrics collection
     */
    private void runCustomSimulation() {
        LOGGER.info("Running custom simulation for " + simulationTime + " seconds...");
        
        // Initialize data generators for each IoT device
        for (IoTDevice device : iotDevices) {
            device.initializeDataGenerator();
        }
        
        // Main simulation loop
        for (int currentTime = 0; currentTime < simulationTime; currentTime++) {
            LOGGER.fine("Simulation time: " + currentTime + " seconds");
            
            // Generate data from IoT devices
            for (IoTDevice device : iotDevices) {
                device.generateData(currentTime);
            }
            
            // Process data at edge nodes
            for (EdgeNode edgeNode : edgeNodes) {
                edgeNode.processData(currentTime);
            }
            
            // Process data at fog nodes
            for (FogNode fogNode : fogNodes) {
                fogNode.processData(currentTime);
            }
            
            // Process data at cloud
            cloudDataCenter.processData(currentTime);
            
            // Collect metrics for this time step
            metricsCollector.collectMetrics(currentTime, iotDevices, edgeNodes, fogNodes, cloudDataCenter);
        }
        
        LOGGER.info("Custom simulation completed.");
    }
    
    /**
     * Analyzes the simulation results and generates visualizations
     */
    public void analyzeResults() {
        LOGGER.info("Analyzing simulation results...");
        
        // Calculate and display metrics
        metricsCollector.calculateAggregateMetrics();
        metricsCollector.displayMetrics();
        
        // Generate visualizations
        resultsVisualizer.generateLatencyGraph(metricsCollector);
        resultsVisualizer.generateDataReductionGraph(metricsCollector);
        resultsVisualizer.generateEnergyConsumptionGraph(metricsCollector);
        resultsVisualizer.generateBandwidthUsageGraph(metricsCollector);
        resultsVisualizer.generateProcessingDistributionGraph(metricsCollector);
        
        LOGGER.info("Results analysis completed.");
    }
    
    /**
     * Main method to run the simulation
     */
    public static void main(String[] args) {
        try {
            LOGGER.info("Starting Edge-Fog-Cloud Architecture Simulation using CloudSim Plus 7.3.3");
            
            // Create our simulation instance
            EdgeFogCloudSimulation edgeFogCloudSim = new EdgeFogCloudSimulation();
            
            // Setup the simulation environment
            LOGGER.info("Setting up simulation environment...");
            edgeFogCloudSim.setupSimulation();
            
            // Run the simulation
            LOGGER.info("Running simulation...");
            edgeFogCloudSim.runSimulation();
            
            // Analyze results
            LOGGER.info("Analyzing results...");
            edgeFogCloudSim.analyzeResults();
            
            LOGGER.info("Simulation completed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during simulation", e);
        }
    }
}
