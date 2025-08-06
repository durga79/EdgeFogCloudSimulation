package org.edgefogcloud.test;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.datacenters.Datacenter;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A utility class to calculate and display enhanced statistics for Edge-Fog-Cloud simulations
 */
public class SimulationStatistics {
    
    /**
     * Calculates and prints enhanced statistics for the simulation
     * 
     * @param edgeCloudlets List of edge cloudlets
     * @param fogCloudlets List of fog cloudlets
     * @param cloudCloudlets List of cloud cloudlets
     * @param finishedCloudlets List of all finished cloudlets
     * @param edgeVms List of edge VMs
     * @param fogVms List of fog VMs
     * @param cloudVms List of cloud VMs
     * @param edgeDatacenter Edge datacenter
     * @param fogDatacenter Fog datacenter
     * @param cloudDatacenter Cloud datacenter
     */
    public static void printEnhancedStatistics(
            List<Cloudlet> edgeCloudlets, List<Cloudlet> fogCloudlets, List<Cloudlet> cloudCloudlets,
            List<Cloudlet> finishedCloudlets, 
            List<Vm> edgeVms, List<Vm> fogVms, List<Vm> cloudVms,
            Datacenter edgeDatacenter, Datacenter fogDatacenter, Datacenter cloudDatacenter) {
        
        // Count finished cloudlets by layer
        int edgeFinished = countFinishedCloudlets(edgeCloudlets, finishedCloudlets);
        int fogFinished = countFinishedCloudlets(fogCloudlets, finishedCloudlets);
        int cloudFinished = countFinishedCloudlets(cloudCloudlets, finishedCloudlets);
        
        // Calculate costs
        double edgeCostPerSec = 0.003;
        double fogCostPerSec = 0.005;
        double cloudCostPerSec = 0.01;
        
        double edgeCost = calculateDetailedCost(edgeCloudlets, finishedCloudlets, edgeCostPerSec);
        double fogCost = calculateDetailedCost(fogCloudlets, finishedCloudlets, fogCostPerSec);
        double cloudCost = calculateDetailedCost(cloudCloudlets, finishedCloudlets, cloudCostPerSec);
        
        // Print basic statistics
        System.out.println("\n=== Enhanced Simulation Statistics ===");
        
        // Processing distribution
        System.out.println("\n=== Processing Distribution ===");
        System.out.println("Edge Layer: " + edgeFinished + " of " + edgeCloudlets.size() + " cloudlets finished (" + 
                String.format("%.1f%%", (double)edgeFinished/edgeCloudlets.size()*100) + ")");
        System.out.println("Fog Layer: " + fogFinished + " of " + fogCloudlets.size() + " cloudlets finished (" + 
                String.format("%.1f%%", (double)fogFinished/fogCloudlets.size()*100) + ")");
        System.out.println("Cloud Layer: " + cloudFinished + " of " + cloudCloudlets.size() + " cloudlets finished (" + 
                String.format("%.1f%%", (double)cloudFinished/cloudCloudlets.size()*100) + ")");
        System.out.println("Total: " + finishedCloudlets.size() + " of " + 
                (edgeCloudlets.size() + fogCloudlets.size() + cloudCloudlets.size()) + 
                " cloudlets finished (" + 
                String.format("%.1f%%", (double)finishedCloudlets.size()/(edgeCloudlets.size() + fogCloudlets.size() + cloudCloudlets.size())*100) + ")");
        
        // Detailed cost analysis
        System.out.println("\n=== Detailed Cost Analysis ===");
        System.out.printf("Edge Layer: $%.4f (%.1f%% of total)%n", 
                edgeCost, (edgeCost/(edgeCost + fogCost + cloudCost))*100);
        System.out.printf("Fog Layer: $%.4f (%.1f%% of total)%n", 
                fogCost, (fogCost/(edgeCost + fogCost + cloudCost))*100);
        System.out.printf("Cloud Layer: $%.4f (%.1f%% of total)%n", 
                cloudCost, (cloudCost/(edgeCost + fogCost + cloudCost))*100);
        System.out.printf("Total Cost: $%.4f%n", edgeCost + fogCost + cloudCost);
        
        // Cost breakdown
        System.out.println("\n=== Cost Breakdown ===");
        System.out.println("Layer | Processing Cost | Memory Cost | Storage Cost | Bandwidth Cost | Total");
        System.out.println("------|----------------|-------------|--------------|----------------|------");
        printCostBreakdown("Edge", edgeCostPerSec, edgeFinished);
        printCostBreakdown("Fog", fogCostPerSec, fogFinished);
        printCostBreakdown("Cloud", cloudCostPerSec, cloudFinished);
        
        // Resource utilization
        System.out.println("\n=== Resource Utilization ===");
        printResourceUtilization(edgeVms, "Edge");
        printResourceUtilization(fogVms, "Fog");
        printResourceUtilization(cloudVms, "Cloud");
        
        // Estimated energy consumption
        System.out.println("\n=== Estimated Energy Consumption ===");
        double edgeEnergy = estimateEnergyConsumption(edgeVms, 0.1); // kWh
        double fogEnergy = estimateEnergyConsumption(fogVms, 0.2);   // kWh
        double cloudEnergy = estimateEnergyConsumption(cloudVms, 0.5); // kWh
        System.out.printf("Edge Layer: %.4f kWh%n", edgeEnergy);
        System.out.printf("Fog Layer: %.4f kWh%n", fogEnergy);
        System.out.printf("Cloud Layer: %.4f kWh%n", cloudEnergy);
        System.out.printf("Total Energy: %.4f kWh%n", edgeEnergy + fogEnergy + cloudEnergy);
        
        // Network traffic analysis
        System.out.println("\n=== Network Traffic Analysis ===");
        double edgeTraffic = estimateNetworkTraffic(edgeCloudlets, finishedCloudlets);
        double fogTraffic = estimateNetworkTraffic(fogCloudlets, finishedCloudlets);
        double cloudTraffic = estimateNetworkTraffic(cloudCloudlets, finishedCloudlets);
        System.out.printf("Edge Layer: %.2f MB%n", edgeTraffic);
        System.out.printf("Fog Layer: %.2f MB%n", fogTraffic);
        System.out.printf("Cloud Layer: %.2f MB%n", cloudTraffic);
        System.out.printf("Total Traffic: %.2f MB%n", edgeTraffic + fogTraffic + cloudTraffic);
        
        // Response time analysis
        System.out.println("\n=== Response Time Analysis ===");
        printResponseTimeAnalysis(edgeCloudlets, finishedCloudlets, "Edge");
        printResponseTimeAnalysis(fogCloudlets, finishedCloudlets, "Fog");
        printResponseTimeAnalysis(cloudCloudlets, finishedCloudlets, "Cloud");
        
        // Latency metrics
        System.out.println("\n=== Latency Metrics ===");
        System.out.println("Edge-to-User Latency: 5-15 ms (estimated)");
        System.out.println("Edge-to-Fog Latency: 10-30 ms (estimated)");
        System.out.println("Fog-to-Cloud Latency: 50-100 ms (estimated)");
        
        // Save all statistics to file
        saveEnhancedStatisticsToFile(
            edgeCloudlets, fogCloudlets, cloudCloudlets, finishedCloudlets,
            edgeVms, fogVms, cloudVms, 
            edgeFinished, fogFinished, cloudFinished,
            edgeCost, fogCost, cloudCost,
            edgeEnergy, fogEnergy, cloudEnergy,
            edgeTraffic, fogTraffic, cloudTraffic
        );
    }
    
    private static void printCostBreakdown(String layer, double costPerSec, int finishedCloudlets) {
        double processingCost = costPerSec * finishedCloudlets * 0.5;
        double memoryCost = costPerSec * finishedCloudlets * 0.2;
        double storageCost = costPerSec * finishedCloudlets * 0.1;
        double bwCost = costPerSec * finishedCloudlets * 0.2;
        double totalCost = processingCost + memoryCost + storageCost + bwCost;
        
        System.out.printf("%-6s| $%-14.4f| $%-11.4f| $%-12.4f| $%-14.4f| $%.4f%n",
                layer, processingCost, memoryCost, storageCost, bwCost, totalCost);
    }
    
    private static void printResponseTimeAnalysis(List<Cloudlet> layerCloudlets, List<Cloudlet> finishedCloudlets, String layer) {
        double totalResponseTime = 0;
        int count = 0;
        double minResponseTime = Double.MAX_VALUE;
        double maxResponseTime = 0;
        
        for (Cloudlet layerCloudlet : layerCloudlets) {
            for (Cloudlet finishedCloudlet : finishedCloudlets) {
                if (layerCloudlet.getId() == finishedCloudlet.getId()) {
                    double responseTime = finishedCloudlet.getFinishTime() - finishedCloudlet.getExecStartTime();
                    totalResponseTime += responseTime;
                    count++;
                    
                    if (responseTime < minResponseTime) {
                        minResponseTime = responseTime;
                    }
                    
                    if (responseTime > maxResponseTime) {
                        maxResponseTime = responseTime;
                    }
                    
                    break;
                }
            }
        }
        
        if (count > 0) {
            double avgResponseTime = totalResponseTime / count;
            System.out.printf("%s Layer - Avg: %.4f sec, Min: %.4f sec, Max: %.4f sec%n", 
                    layer, avgResponseTime, minResponseTime, maxResponseTime);
        } else {
            System.out.printf("%s Layer - No completed cloudlets%n", layer);
        }
    }
    
    private static double estimateEnergyConsumption(List<Vm> vms, double energyFactorPerVm) {
        // Simple estimation based on VM count and a factor
        return vms.size() * energyFactorPerVm;
    }
    
    private static double estimateNetworkTraffic(List<Cloudlet> layerCloudlets, List<Cloudlet> finishedCloudlets) {
        double totalTraffic = 0;
        
        for (Cloudlet layerCloudlet : layerCloudlets) {
            for (Cloudlet finishedCloudlet : finishedCloudlets) {
                if (layerCloudlet.getId() == finishedCloudlet.getId()) {
                    // Estimate traffic based on cloudlet file size and output size
                    totalTraffic += (finishedCloudlet.getFileSize() + finishedCloudlet.getOutputSize()) / (1024.0 * 1024.0); // Convert to MB
                    break;
                }
            }
        }
        
        return totalTraffic;
    }
    
    private static void printResourceUtilization(List<Vm> vms, String layer) {
        double totalCpuUtilization = 0;
        double totalRamUtilization = 0;
        double totalBwUtilization = 0;
        double peakCpuUtilization = 0;
        double peakRamUtilization = 0;
        double peakBwUtilization = 0;
        
        for (Vm vm : vms) {
            double cpuUtil = vm.getCpuPercentUtilization() * 100;
            double ramUtil = vm.getRam().getPercentUtilization() * 100;
            double bwUtil = vm.getBw().getPercentUtilization() * 100;
            
            totalCpuUtilization += cpuUtil;
            totalRamUtilization += ramUtil;
            totalBwUtilization += bwUtil;
            
            peakCpuUtilization = Math.max(peakCpuUtilization, cpuUtil);
            peakRamUtilization = Math.max(peakRamUtilization, ramUtil);
            peakBwUtilization = Math.max(peakBwUtilization, bwUtil);
        }
        
        if (!vms.isEmpty()) {
            System.out.printf("%s Layer - Avg CPU: %.2f%%, Peak CPU: %.2f%%, Avg RAM: %.2f%%, Peak RAM: %.2f%%, Avg BW: %.2f%%, Peak BW: %.2f%%%n", 
                layer,
                totalCpuUtilization / vms.size(),
                peakCpuUtilization,
                totalRamUtilization / vms.size(),
                peakRamUtilization,
                totalBwUtilization / vms.size(),
                peakBwUtilization);
        }
    }
    
    private static double calculateDetailedCost(List<Cloudlet> layerCloudlets, List<Cloudlet> finishedCloudlets, double costPerSec) {
        double totalCost = 0;
        
        for (Cloudlet layerCloudlet : layerCloudlets) {
            for (Cloudlet finishedCloudlet : finishedCloudlets) {
                if (layerCloudlet.getId() == finishedCloudlet.getId()) {
                    // Calculate cost based on execution time
                    double executionTime = finishedCloudlet.getActualCpuTime();
                    totalCost += executionTime * costPerSec;
                    break;
                }
            }
        }
        
        return totalCost;
    }
    
    private static int countFinishedCloudlets(List<Cloudlet> layerCloudlets, List<Cloudlet> finishedCloudlets) {
        int count = 0;
        for (Cloudlet layerCloudlet : layerCloudlets) {
            for (Cloudlet finishedCloudlet : finishedCloudlets) {
                if (layerCloudlet.getId() == finishedCloudlet.getId()) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }
    
    private static void saveEnhancedStatisticsToFile(
            List<Cloudlet> edgeCloudlets, List<Cloudlet> fogCloudlets, List<Cloudlet> cloudCloudlets,
            List<Cloudlet> finishedCloudlets, 
            List<Vm> edgeVms, List<Vm> fogVms, List<Vm> cloudVms,
            int edgeFinished, int fogFinished, int cloudFinished,
            double edgeCost, double fogCost, double cloudCost,
            double edgeEnergy, double fogEnergy, double cloudEnergy,
            double edgeTraffic, double fogTraffic, double cloudTraffic) {
        
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "results/enhanced_statistics_" + timestamp + ".txt";
            PrintWriter writer = new PrintWriter(new FileWriter(filename));
            
            writer.println("=== Enhanced Edge-Fog-Cloud Simulation Statistics ===");
            writer.println("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            // Processing distribution
            writer.println("\n=== Processing Distribution ===");
            writer.println("Edge Layer: " + edgeFinished + " of " + edgeCloudlets.size() + " cloudlets finished (" + 
                    String.format("%.1f%%", (double)edgeFinished/edgeCloudlets.size()*100) + ")");
            writer.println("Fog Layer: " + fogFinished + " of " + fogCloudlets.size() + " cloudlets finished (" + 
                    String.format("%.1f%%", (double)fogFinished/fogCloudlets.size()*100) + ")");
            writer.println("Cloud Layer: " + cloudFinished + " of " + cloudCloudlets.size() + " cloudlets finished (" + 
                    String.format("%.1f%%", (double)cloudFinished/cloudCloudlets.size()*100) + ")");
            writer.println("Total: " + finishedCloudlets.size() + " of " + 
                    (edgeCloudlets.size() + fogCloudlets.size() + cloudCloudlets.size()) + 
                    " cloudlets finished (" + 
                    String.format("%.1f%%", (double)finishedCloudlets.size()/(edgeCloudlets.size() + fogCloudlets.size() + cloudCloudlets.size())*100) + ")");
            
            // Detailed cost analysis
            writer.println("\n=== Detailed Cost Analysis ===");
            writer.printf("Edge Layer: $%.4f (%.1f%% of total)%n", 
                    edgeCost, (edgeCost/(edgeCost + fogCost + cloudCost))*100);
            writer.printf("Fog Layer: $%.4f (%.1f%% of total)%n", 
                    fogCost, (fogCost/(edgeCost + fogCost + cloudCost))*100);
            writer.printf("Cloud Layer: $%.4f (%.1f%% of total)%n", 
                    cloudCost, (cloudCost/(edgeCost + fogCost + cloudCost))*100);
            writer.printf("Total Cost: $%.4f%n", edgeCost + fogCost + cloudCost);
            
            // Cost breakdown
            writer.println("\n=== Cost Breakdown ===");
            writer.println("Layer | Processing Cost | Memory Cost | Storage Cost | Bandwidth Cost | Total");
            writer.println("------|----------------|-------------|--------------|----------------|------");
            writeCostBreakdown(writer, "Edge", 0.003, edgeFinished);
            writeCostBreakdown(writer, "Fog", 0.005, fogFinished);
            writeCostBreakdown(writer, "Cloud", 0.01, cloudFinished);
            
            // Resource utilization summary
            writer.println("\n=== Resource Utilization Summary ===");
            writeResourceUtilizationSummary(writer, edgeVms, "Edge");
            writeResourceUtilizationSummary(writer, fogVms, "Fog");
            writeResourceUtilizationSummary(writer, cloudVms, "Cloud");
            
            // Energy consumption
            writer.println("\n=== Estimated Energy Consumption ===");
            writer.printf("Edge Layer: %.4f kWh%n", edgeEnergy);
            writer.printf("Fog Layer: %.4f kWh%n", fogEnergy);
            writer.printf("Cloud Layer: %.4f kWh%n", cloudEnergy);
            writer.printf("Total Energy: %.4f kWh%n", edgeEnergy + fogEnergy + cloudEnergy);
            
            // Network traffic
            writer.println("\n=== Network Traffic Analysis ===");
            writer.printf("Edge Layer: %.2f MB%n", edgeTraffic);
            writer.printf("Fog Layer: %.2f MB%n", fogTraffic);
            writer.printf("Cloud Layer: %.2f MB%n", cloudTraffic);
            writer.printf("Total Traffic: %.2f MB%n", edgeTraffic + fogTraffic + cloudTraffic);
            
            // Response time analysis
            writer.println("\n=== Response Time Analysis ===");
            writeResponseTimeAnalysis(writer, edgeCloudlets, finishedCloudlets, "Edge");
            writeResponseTimeAnalysis(writer, fogCloudlets, finishedCloudlets, "Fog");
            writeResponseTimeAnalysis(writer, cloudCloudlets, finishedCloudlets, "Cloud");
            
            // Latency metrics
            writer.println("\n=== Latency Metrics ===");
            writer.println("Edge-to-User Latency: 5-15 ms (estimated)");
            writer.println("Edge-to-Fog Latency: 10-30 ms (estimated)");
            writer.println("Fog-to-Cloud Latency: 50-100 ms (estimated)");
            
            // Detailed cloudlet results
            writer.println("\n=== Detailed Cloudlet Results ===");
            writer.println("Cloudlet ID | Status | Datacenter | VM ID | Length | PEs | Start Time | Finish Time | Execution Time");
            writer.println("------------|--------|------------|-------|--------|-----|------------|-------------|---------------");
            
            for (Cloudlet cloudlet : finishedCloudlets) {
                writer.printf("%-11d | %-6s | %-10d | %-5d | %-6d | %-3d | %-10.2f | %-11.2f | %-13.2f%n",
                        cloudlet.getId(),
                        "SUCCESS",
                        cloudlet.getVm().getHost().getDatacenter().getId(),
                        cloudlet.getVm().getId(),
                        cloudlet.getLength(),
                        cloudlet.getNumberOfPes(),
                        cloudlet.getExecStartTime(),
                        cloudlet.getFinishTime(),
                        cloudlet.getActualCpuTime());
            }
            
            writer.close();
            System.out.println("Enhanced statistics saved to " + filename);
        } catch (Exception e) {
            System.err.println("Error saving enhanced statistics to file: " + e.getMessage());
        }
    }
    
    private static void writeCostBreakdown(PrintWriter writer, String layer, double costPerSec, int finishedCloudlets) {
        double processingCost = costPerSec * finishedCloudlets * 0.5;
        double memoryCost = costPerSec * finishedCloudlets * 0.2;
        double storageCost = costPerSec * finishedCloudlets * 0.1;
        double bwCost = costPerSec * finishedCloudlets * 0.2;
        double totalCost = processingCost + memoryCost + storageCost + bwCost;
        
        writer.printf("%-6s| $%-14.4f| $%-11.4f| $%-12.4f| $%-14.4f| $%.4f%n",
                layer, processingCost, memoryCost, storageCost, bwCost, totalCost);
    }
    
    private static void writeResourceUtilizationSummary(PrintWriter writer, List<Vm> vms, String layer) {
        double totalCpuUtilization = 0;
        double totalRamUtilization = 0;
        double totalBwUtilization = 0;
        double peakCpuUtilization = 0;
        double peakRamUtilization = 0;
        double peakBwUtilization = 0;
        
        for (Vm vm : vms) {
            double cpuUtil = vm.getCpuPercentUtilization() * 100;
            double ramUtil = vm.getRam().getPercentUtilization() * 100;
            double bwUtil = vm.getBw().getPercentUtilization() * 100;
            
            totalCpuUtilization += cpuUtil;
            totalRamUtilization += ramUtil;
            totalBwUtilization += bwUtil;
            
            peakCpuUtilization = Math.max(peakCpuUtilization, cpuUtil);
            peakRamUtilization = Math.max(peakRamUtilization, ramUtil);
            peakBwUtilization = Math.max(peakBwUtilization, bwUtil);
        }
        
        if (!vms.isEmpty()) {
            writer.printf("%s Layer - Avg CPU: %.2f%%, Peak CPU: %.2f%%, Avg RAM: %.2f%%, Peak RAM: %.2f%%, Avg BW: %.2f%%, Peak BW: %.2f%%%n", 
                layer,
                totalCpuUtilization / vms.size(),
                peakCpuUtilization,
                totalRamUtilization / vms.size(),
                peakRamUtilization,
                totalBwUtilization / vms.size(),
                peakBwUtilization);
        }
    }
    
    private static void writeResponseTimeAnalysis(PrintWriter writer, List<Cloudlet> layerCloudlets, List<Cloudlet> finishedCloudlets, String layer) {
        double totalResponseTime = 0;
        int count = 0;
        double minResponseTime = Double.MAX_VALUE;
        double maxResponseTime = 0;
        
        for (Cloudlet layerCloudlet : layerCloudlets) {
            for (Cloudlet finishedCloudlet : finishedCloudlets) {
                if (layerCloudlet.getId() == finishedCloudlet.getId()) {
                    double responseTime = finishedCloudlet.getFinishTime() - finishedCloudlet.getExecStartTime();
                    totalResponseTime += responseTime;
                    count++;
                    
                    if (responseTime < minResponseTime) {
                        minResponseTime = responseTime;
                    }
                    
                    if (responseTime > maxResponseTime) {
                        maxResponseTime = responseTime;
                    }
                    
                    break;
                }
            }
        }
        
        if (count > 0) {
            double avgResponseTime = totalResponseTime / count;
            writer.printf("%s Layer - Avg: %.4f sec, Min: %.4f sec, Max: %.4f sec%n", 
                    layer, avgResponseTime, minResponseTime, maxResponseTime);
        } else {
            writer.printf("%s Layer - No completed cloudlets%n", layer);
        }
    }
}
