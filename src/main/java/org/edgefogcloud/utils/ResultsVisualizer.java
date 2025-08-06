package org.edgefogcloud.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates visualizations of simulation results
 */
public class ResultsVisualizer {
    private static final Logger LOGGER = Logger.getLogger(ResultsVisualizer.class.getName());
    
    private static final String RESULTS_DIRECTORY = "results";
    private static final int CHART_WIDTH = 800;
    private static final int CHART_HEIGHT = 600;
    
    public ResultsVisualizer() {
        // Create results directory if it doesn't exist
        File resultsDir = new File(RESULTS_DIRECTORY);
        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
            LOGGER.info("Created results directory: " + resultsDir.getAbsolutePath());
        }
    }
    
    public void generateLatencyGraph(MetricsCollector metricsCollector) {
        try {
            // Create dataset for latency over time
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries series = new XYSeries("End-to-End Latency");
            
            // Sort time points for proper visualization
            Map<Integer, Double> sortedLatencyData = new TreeMap<>(metricsCollector.getLatencyByTime());
            
            for (Map.Entry<Integer, Double> entry : sortedLatencyData.entrySet()) {
                series.add(entry.getKey(), entry.getValue());
            }
            
            dataset.addSeries(series);
            
            // Create chart
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "End-to-End Latency Over Time",
                    "Simulation Time (s)",
                    "Latency (ms)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            
            // Save chart to file
            File latencyChart = new File(RESULTS_DIRECTORY + "/latency_chart.png");
            ChartUtils.saveChartAsPNG(latencyChart, chart, CHART_WIDTH, CHART_HEIGHT);
            
            LOGGER.info("Latency chart generated: " + latencyChart.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating latency chart", e);
        }
    }
    
    public void generateDataReductionGraph(MetricsCollector metricsCollector) {
        try {
            // Create dataset for data reduction by layer
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            // Get packets processed at each layer
            Map<String, Integer> packetsByLayer = metricsCollector.getPacketsByLayer();
            int totalPackets = packetsByLayer.get("IoT"); // Total packets generated
            
            // Calculate percentage of packets processed at each layer
            double edgePercentage = (double) packetsByLayer.get("Edge") / totalPackets * 100;
            double fogPercentage = (double) packetsByLayer.get("Fog") / totalPackets * 100;
            double cloudPercentage = (double) packetsByLayer.get("Cloud") / totalPackets * 100;
            
            dataset.addValue(edgePercentage, "Packets Processed", "Edge");
            dataset.addValue(fogPercentage, "Packets Processed", "Fog");
            dataset.addValue(cloudPercentage, "Packets Processed", "Cloud");
            
            // Create chart
            JFreeChart chart = ChartFactory.createBarChart(
                    "Data Processing Distribution by Layer",
                    "Layer",
                    "Percentage of Total Packets (%)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            
            // Save chart to file
            File dataReductionChart = new File(RESULTS_DIRECTORY + "/data_reduction_chart.png");
            ChartUtils.saveChartAsPNG(dataReductionChart, chart, CHART_WIDTH, CHART_HEIGHT);
            
            LOGGER.info("Data reduction chart generated: " + dataReductionChart.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating data reduction chart", e);
        }
    }
    
    public void generateEnergyConsumptionGraph(MetricsCollector metricsCollector) {
        try {
            // Create dataset for energy consumption by layer
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            // Get energy consumption by layer
            Map<String, Double> energyByLayer = metricsCollector.getEnergyConsumptionByLayer();
            
            dataset.addValue(energyByLayer.get("IoT"), "Energy Consumption (Wh)", "IoT");
            dataset.addValue(energyByLayer.get("Edge"), "Energy Consumption (Wh)", "Edge");
            dataset.addValue(energyByLayer.get("Fog"), "Energy Consumption (Wh)", "Fog");
            dataset.addValue(energyByLayer.get("Cloud"), "Energy Consumption (Wh)", "Cloud");
            
            // Create chart
            JFreeChart chart = ChartFactory.createBarChart(
                    "Energy Consumption by Layer",
                    "Layer",
                    "Energy Consumption (Wh)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            
            // Save chart to file
            File energyChart = new File(RESULTS_DIRECTORY + "/energy_consumption_chart.png");
            ChartUtils.saveChartAsPNG(energyChart, chart, CHART_WIDTH, CHART_HEIGHT);
            
            LOGGER.info("Energy consumption chart generated: " + energyChart.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating energy consumption chart", e);
        }
    }
    
    public void generateBandwidthUsageGraph(MetricsCollector metricsCollector) {
        try {
            // Create dataset for bandwidth usage over time
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries series = new XYSeries("Bandwidth Usage");
            
            // Sort time points for proper visualization
            Map<Integer, Double> sortedBandwidthData = new TreeMap<>(metricsCollector.getBandwidthUsageByTime());
            
            for (Map.Entry<Integer, Double> entry : sortedBandwidthData.entrySet()) {
                series.add(entry.getKey(), entry.getValue());
            }
            
            dataset.addSeries(series);
            
            // Create chart
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Bandwidth Usage Over Time",
                    "Simulation Time (s)",
                    "Bandwidth Usage (MB)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            
            // Save chart to file
            File bandwidthChart = new File(RESULTS_DIRECTORY + "/bandwidth_usage_chart.png");
            ChartUtils.saveChartAsPNG(bandwidthChart, chart, CHART_WIDTH, CHART_HEIGHT);
            
            LOGGER.info("Bandwidth usage chart generated: " + bandwidthChart.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating bandwidth usage chart", e);
        }
    }
    
    /**
     * Generates a graph showing the distribution of processing tasks across Edge, Fog, and Cloud layers
     * @param metricsCollector The metrics collector containing simulation data
     */
    public void generateProcessingDistributionGraph(MetricsCollector metricsCollector) {
        try {
            // Create dataset for processing distribution by layer
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            // Get processing times at each layer
            Map<String, Double> processingTimeByLayer = metricsCollector.getProcessingTimeByLayer();
            double totalProcessingTime = 0.0;
            for (double time : processingTimeByLayer.values()) {
                totalProcessingTime += time;
            }
            
            // Calculate percentage of processing time at each layer
            double edgePercentage = processingTimeByLayer.getOrDefault("Edge", 0.0) / totalProcessingTime * 100;
            double fogPercentage = processingTimeByLayer.getOrDefault("Fog", 0.0) / totalProcessingTime * 100;
            double cloudPercentage = processingTimeByLayer.getOrDefault("Cloud", 0.0) / totalProcessingTime * 100;
            
            dataset.addValue(edgePercentage, "Processing Time", "Edge");
            dataset.addValue(fogPercentage, "Processing Time", "Fog");
            dataset.addValue(cloudPercentage, "Processing Time", "Cloud");
            
            // Create chart
            JFreeChart chart = ChartFactory.createBarChart(
                    "Processing Distribution by Layer",
                    "Layer",
                    "Percentage of Total Processing Time (%)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            
            // Save chart to file
            File processingChart = new File(RESULTS_DIRECTORY + "/processing_distribution_chart.png");
            ChartUtils.saveChartAsPNG(processingChart, chart, CHART_WIDTH, CHART_HEIGHT);
            
            LOGGER.info("Processing distribution chart generated: " + processingChart.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating processing distribution chart", e);
        }
    }
}
