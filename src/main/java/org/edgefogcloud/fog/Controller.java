package org.edgefogcloud.fog;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;

/**
 * Controller for managing fog devices and application placement using CloudSim Plus 6.4.3
 */
public class Controller {
    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());
    
    private String name;
    private List<Datacenter> fogDatacenters;
    private DatacenterBroker broker;
    private CloudSim simulation;
    private Map<String, List<Vm>> fogVms;
    private Map<String, List<Cloudlet>> fogCloudlets;
    
    /**
     * Creates a new controller for managing fog devices using CloudSim Plus
     * 
     * @param name The name of the controller
     * @param fogDatacenters List of fog datacenters to manage
     * @param broker The datacenter broker
     * @param simulation The CloudSim Plus simulation instance
     */
    public Controller(String name, List<Datacenter> fogDatacenters, DatacenterBroker broker, CloudSim simulation) {
        this.name = name;
        this.fogDatacenters = fogDatacenters;
        this.broker = broker;
        this.simulation = simulation;
        this.fogVms = new HashMap<>();
        this.fogCloudlets = new HashMap<>();
        
        LOGGER.info("Created Controller: " + name);
    }
    
    /**
     * Submits VMs to the fog datacenters
     * 
     * @param vms List of VMs to submit
     */
    public void submitVms(List<Vm> vms) {
        broker.submitVmList(vms);
        LOGGER.info("Submitted " + vms.size() + " VMs to broker");
    }
    
    /**
     * Submits cloudlets to be executed on the fog VMs
     * 
     * @param cloudlets List of cloudlets to submit
     */
    public void submitCloudlets(List<Cloudlet> cloudlets) {
        broker.submitCloudletList(cloudlets);
        LOGGER.info("Submitted " + cloudlets.size() + " cloudlets to broker");
    }
    
    /**
     * Binds a cloudlet to a specific VM
     * 
     * @param cloudlet The cloudlet to bind
     * @param vm The VM to bind the cloudlet to
     */
    public void bindCloudletToVm(Cloudlet cloudlet, Vm vm) {
        broker.bindCloudletToVm(cloudlet, vm);
    }
    
    /**
     * Gets the name of the controller
     * 
     * @return The controller name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the broker managed by this controller
     * 
     * @return The datacenter broker
     */
    public DatacenterBroker getBroker() {
        return broker;
    }
}
