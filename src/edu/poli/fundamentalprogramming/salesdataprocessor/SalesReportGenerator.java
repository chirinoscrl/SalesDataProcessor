package edu.poli.fundamentalprogramming.salesdataprocessor;

import java.io.*;
import java.util.*;

/**
 * Class responsible for generating sales reports based on files created by GenerateInfoFiles.
 * This class reads the generated sales, products, and salespeople data files to create
 * comprehensive reports including vendor revenue summaries.
 */
public class SalesReportGenerator {

    // Directory structure constants (same as GenerateInfoFiles for consistency)
    private static final String DATA_DIR = "data";
    private static final String INPUT_DIR = DATA_DIR + "/input";
    private static final String SALESPEOPLE_DIR = INPUT_DIR + "/salespeople";
    private static final String OUTPUT_DIR = DATA_DIR + "/output";

    // File path constants
    private static final String PRODUCTS_FILE_PATH = INPUT_DIR + "/products.csv";
    private static final String SALESPEOPLE_FILE_PATH = INPUT_DIR + "/salespeople.csv";
    private static final String VENDOR_REPORT_FILE_PATH = OUTPUT_DIR + "/sales_report.csv";
    private static final String PRODUCTS_REPORT_FILE_PATH = OUTPUT_DIR + "/products_report.csv";

    // Data structures to store information from input files
    private static final Map<String, String> salespeopleMap = new HashMap<>();
    private static final Map<Integer, Integer> productsMap = new HashMap<>();
    private static final Map<Integer, String> productNamesMap = new HashMap<>();
    private static final Map<String, Double> vendorRevenueMap = new HashMap<>();
    private static final Map<Integer, Integer> productQuantityTotals = new HashMap<>();

    /**
     * Main method that executes the sales report generation process.
     * First generates the necessary input files, then processes them to create reports.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            // First, generate all the necessary input files
            System.out.println("Generating input files...");
            GenerateInfoFiles.generateAllFiles();

            System.out.println("\nStarting sales report generation...");

            // Load data from input files
            loadSalespeopleData();
            loadProductsData();
            processSalesFiles();

            // Generate and save the vendor report
            generateVendorReport();
            // Generate and save the product report
            generateProductReport();

            System.out.println("Sales report generated successfully!");
            System.out.println("Vendor report saved to: " + VENDOR_REPORT_FILE_PATH);
            System.out.println("Product report saved to: " + PRODUCTS_REPORT_FILE_PATH);

        } catch (Exception e) {
            System.err.println("Error generating sales report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads salespeople information from the salespeople.csv file.
     * Maps document type and number to the full name for easy lookup.
     *
     * @throws IOException if the file cannot be read
     */
    private static void loadSalespeopleData() throws IOException {
        System.out.println("Loading salespeople data...");

        File file = new File(SALESPEOPLE_FILE_PATH);
        if (!file.exists()) {
            throw new FileNotFoundException("Salespeople file not found: " + SALESPEOPLE_FILE_PATH);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 4) {
                    String documentType = parts[0];
                    String documentNumber = parts[1];
                    String firstName = parts[2];
                    String lastName = parts[3];
                    String fullName = firstName + " " + lastName;
                    String documentKey = documentType + "_" + documentNumber;

                    salespeopleMap.put(documentKey, fullName);
                }
            }
        }

        System.out.println("Loaded " + salespeopleMap.size() + " salespeople records");
    }

    /**
     * Loads product information from the products.csv file.
     * Maps product ID to price for revenue calculations.
     *
     * @throws IOException if the file cannot be read
     */
    private static void loadProductsData() throws IOException {
        System.out.println("Loading products data...");

        File file = new File(PRODUCTS_FILE_PATH);
        if (!file.exists()) {
            throw new FileNotFoundException("Products file not found: " + PRODUCTS_FILE_PATH);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    int productId = Integer.parseInt(parts[0]);
                    String productName = parts[1];
                    int price = Integer.parseInt(parts[2]);
                    productsMap.put(productId, price);
                    productNamesMap.put(productId, productName);
                }
            }
        }

        System.out.println("Loaded " + productsMap.size() + " product records");
    }

    /**
     * Processes all sales files in the salespeople directory.
     * Calculates total revenue for each vendor based on their sales.
     *
     * @throws IOException if sales files cannot be read
     */
    private static void processSalesFiles() throws IOException {
        System.out.println("Processing sales files...");

        File salespeopleDir = new File(SALESPEOPLE_DIR);
        if (!salespeopleDir.exists() || !salespeopleDir.isDirectory()) {
            throw new FileNotFoundException("Salespeople directory not found: " + SALESPEOPLE_DIR);
        }

        File[] salesFiles = salespeopleDir.listFiles((dir, name) -> name.startsWith("sales_") && name.endsWith(".csv"));
        if (salesFiles == null || salesFiles.length == 0) {
            throw new FileNotFoundException("No sales files found in: " + SALESPEOPLE_DIR);
        }

        for (File salesFile : salesFiles) {
            processSalesFile(salesFile);
        }

        System.out.println("Processed " + salesFiles.length + " sales files");
    }

    /**
     * Processes an individual sales file and accumulates revenue for the vendor.
     *
     * @param salesFile The sales file to process
     * @throws IOException if the file cannot be read
     */
    private static void processSalesFile(File salesFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(salesFile))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.err.println("Warning: Empty sales file: " + salesFile.getName());
                return;
            }

            // Parse header to get vendor information
            String[] headerParts = headerLine.split(";");
            if (headerParts.length < 2) {
                System.err.println("Warning: Invalid header format in file: " + salesFile.getName());
                return;
            }

            String documentType = headerParts[0];
            String documentNumber = headerParts[1];
            String documentKey = documentType + "_" + documentNumber;

            String vendorName = salespeopleMap.get(documentKey);
            if (vendorName == null) {
                System.err.println("Warning: Vendor not found for document: " + documentKey);
                return;
            }

            // Process sales records
            String line;
            double totalRevenue = 0;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 2) {
                    try {
                        int productId = Integer.parseInt(parts[0]);
                        int quantitySold = Integer.parseInt(parts[1]);

                        // Accumulate product total quantities
                        productQuantityTotals.put(productId, productQuantityTotals.getOrDefault(productId, 0) + quantitySold);

                        Integer productPrice = productsMap.get(productId);
                        if (productPrice != null) {
                            totalRevenue += productPrice * quantitySold;
                        } else {
                            System.err.println("Warning: Product not found: " + productId);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Invalid number format in line: " + line);
                    }
                }
            }

            // Accumulate revenue for this vendor
            vendorRevenueMap.put(vendorName, vendorRevenueMap.getOrDefault(vendorName, 0.0) + totalRevenue);
        }
    }

    /**
     * Generates the vendor revenue report and saves it to a CSV file.
     * The report is sorted by total revenue in descending order.
     *
     * @throws IOException if the output file cannot be written
     */
    private static void generateVendorReport() throws IOException {
        System.out.println("Generating vendor report...");

        // Ensure output directory exists
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Sort vendors by revenue (descending order)
        List<Map.Entry<String, Double>> sortedVendors = new ArrayList<>(vendorRevenueMap.entrySet());
        sortedVendors.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));

        // Write the report to the CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VENDOR_REPORT_FILE_PATH))) {
            for (Map.Entry<String, Double> entry : sortedVendors) {
                String vendorName = entry.getKey();
                double totalRevenue = entry.getValue();

                // Format revenue as integer (no decimal places)
                writer.write(vendorName + ";" + String.format("%.0f", totalRevenue));
                writer.newLine();
            }
        }

        System.out.println("Vendor report contains " + sortedVendors.size() + " vendors");

        // Display summary on the console
        System.out.println("\n=== VENDOR REVENUE REPORT ===");
        for (Map.Entry<String, Double> entry : sortedVendors) {
            System.out.printf("%s: $%.0f%n", entry.getKey(), entry.getValue());
        }
    }

    /**
     * Generates the product sales report and saves it to a CSV file.
     * Format per line: ProductName;TotalQuantitySold
     * Sorted by TotalQuantitySold in descending order.
     *
     * @throws IOException if the output file cannot be written
     */
    private static void generateProductReport() throws IOException {
        System.out.println("Generating products report...");

        // Ensure output directory exists
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Prepare and sort products by quantity sold (descending)
        List<Map.Entry<Integer, Integer>> sortedProducts = new ArrayList<>(productQuantityTotals.entrySet());
        sortedProducts.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PRODUCTS_REPORT_FILE_PATH))) {
            for (Map.Entry<Integer, Integer> entry : sortedProducts) {
                int productId = entry.getKey();
                int totalQty = entry.getValue();
                String productName = productNamesMap.getOrDefault(productId, String.valueOf(productId));
                writer.write(productName + ";" + totalQty);
                writer.newLine();
            }
        }

        System.out.println("Products report contains " + sortedProducts.size() + " products");

        // Optional console summary
        System.out.println("\n=== PRODUCT SALES REPORT ===");
        for (Map.Entry<Integer, Integer> entry : sortedProducts) {
            String productName = productNamesMap.getOrDefault(entry.getKey(), String.valueOf(entry.getKey()));
            System.out.printf("%s: %d units%n", productName, entry.getValue());
        }
    }
}
