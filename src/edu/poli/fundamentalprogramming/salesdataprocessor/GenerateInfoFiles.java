package edu.poli.fundamentalprogramming.salesdataprocessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main class for generating test files for sales data processing.
 * This class creates pseudo-random data files that will serve as input
 * for the main program. It generates three types of files:
 * 1. Sales files for each salesperson
 * 2. A product information file
 * 3. A salesperson information file
 * <p>
 * First deliverable for the Fundamental Programming Concepts module.
 */
public class GenerateInfoFiles {

    // Directory structure constants
    private static final String DATA_DIR = "data";
    private static final String INPUT_DIR = DATA_DIR + "/input";
    private static final String SALESPEOPLE_DIR = INPUT_DIR + "/salespeople";
    private static final String OUTPUT_DIR = DATA_DIR + "/output";

    // File path constants
    private static final String PRODUCTS_FILE_PATH = INPUT_DIR + "/products.csv";
    private static final String SALESPEOPLE_FILE_PATH = INPUT_DIR + "/salespeople.csv";
    private static final String SALES_FILE_PREFIX = SALESPEOPLE_DIR + "/sales_";

    // Random instance for generating pseudo-random data
    private static final Random random = new Random();

    // Arrays for random data generation
    private static final String[] DOCUMENT_TYPES = {"CC", "CE", "NIT", "TI"};
    private static final String[] FIRST_NAMES = {
        "Alejandro", "Carlos", "Diego", "Eduardo", "Fernando", "Gabriel", "Héctor", "Ignacio", "Javier", "Luis",
        "María", "Ana", "Paula", "Sofía", "Valentina", "Natalia", "Isabella", "Camila", "Lucía", "Daniela"
    };
    private static final String[] LAST_NAMES = {
        "García", "Rodríguez", "Martínez", "Hernández", "López", "González", "Pérez", "Sánchez", "Ramírez", "Torres",
        "Flores", "Rivera", "Gómez", "Díaz", "Reyes", "Morales", "Cruz", "Ortiz", "Gutiérrez", "Mendoza"
    };
    private static final String[] PRODUCT_NAMES = {
        "Soccer Ball", "Tennis Racket", "Running Shoes", "Mountain Bike", "Weights", "Dumbbells",
        "Sports T-shirt", "Training Shorts", "Boxing Gloves", "Yoga Mat",
        "Baseball Bat", "Basketball", "Roller Skates", "Golf Club", "Swimsuit",
        "Sports Cap", "Water Bottle", "Technical Socks", "Swimming Goggles", "Reflective Vest"
    };

    // Lists to store generated information for use between methods
    private static List<String[]> salespeopleInfo = new ArrayList<>();
    private static List<Integer[]> productsInfo = new ArrayList<>();

    /**
     * Main method that executes the file generation process.
     * Creates test files with pseudo-random data for sales, products, and salespeople.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            // Clear old files to ensure a clean start
            clearOldFiles();

            // Create necessary directories
            createDirectoryStructure();

            // Define the number of entities to generate
            int numberOfSalespeople = 5;
            int numberOfProducts = 10;

            // Generate salespeople information file
            boolean salesPeopleFileCreated = createSalesManInfoFile(numberOfSalespeople);

            if (!salesPeopleFileCreated) {
                throw new Exception("Failed to create salespeople information file");
            }

            // Generate products information file
            boolean productsFileCreated = createProductsFile(numberOfProducts);
            if (!productsFileCreated) {
                throw new Exception("Failed to create products information file");
            }

            // Generate individual sales files for each salesperson using the generated information
            for (String[] salespersonData : salespeopleInfo) {
                String documentType = salespersonData[0];
                long documentNumber = Long.parseLong(salespersonData[1]);
                String fullName = salespersonData[2] + " " + salespersonData[3];

                // Random number of sales per salesperson (between 5 and 15)
                int salesCount = 5 + random.nextInt(11);

                boolean salesFileCreated = createSalesMenFile(salesCount, fullName, documentNumber, documentType);
                if (!salesFileCreated) {
                    throw new Exception("Failed to create sales file for " + fullName);
                }
            }

            System.out.println("All files generated successfully!");
            System.out.println("Files are located in:");
            System.out.println("- Salespeople info: " + SALESPEOPLE_FILE_PATH);
            System.out.println("- Products info: " + PRODUCTS_FILE_PATH);
            System.out.println("- Sales files: " + SALESPEOPLE_DIR + "/");

        } catch (Exception e) {
            System.err.println("Error generating files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates the directory structure needed for the application.
     *
     * @throws IOException if directories cannot be created
     */
    private static void createDirectoryStructure() throws IOException {
        if (ensureDirectoryExists(DATA_DIR)) {
            throw new IOException("Failed to create data directory");
        }
        if (ensureDirectoryExists(INPUT_DIR)) {
            throw new IOException("Failed to create input directory");
        }
        if (ensureDirectoryExists(OUTPUT_DIR)) {
            throw new IOException("Failed to create output directory");
        }
        if (ensureDirectoryExists(SALESPEOPLE_DIR)) {
            throw new IOException("Failed to create salespeople directory");
        }
    }

    /**
     * Creates a sales file for a salesperson with pseudo-random data.
     * The file format is:
     * DocumentType;DocumentNumber
     * ProductID1;QuantitySold1;
     * ProductID2;QuantitySold2;
     * ...
     *
     * @param randomSalesCount Number of random sales to generate
     * @param name Name of the salesperson (not used in the file but for naming)
     * @param id ID number of the salesperson
     * @param documentType Type of ID document (CC, CE, NIT, etc.)
     * @return true if the file was created successfully, false otherwise
     */
	public static boolean createSalesMenFile(int randomSalesCount, String name, long id, String documentType) {
	    // Validate that product information is available
	    if (productsInfo.isEmpty()) {
	        System.err.println("Error: No product information available. Cannot generate sales file for " + name);
	        return false;
	    }

	    // Create a unique filename based on salesperson's information
	    String fileName = SALES_FILE_PREFIX + documentType + "_" + id + ".csv";

	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
	        // Write the header line with salesperson identification
	        writer.write(documentType + ";" + id);
	        writer.newLine();

	        // Generate random sales records using real products
	        for (int i = 0; i < randomSalesCount; i++) {
	            Integer[] productInfo = productsInfo.get(random.nextInt(productsInfo.size()));
	            int productId = productInfo[0];
	            int quantitySold = 1 + random.nextInt(20);   // Random quantity between 1-20

	            writer.write(productId + ";" + quantitySold);
	            writer.newLine();
	        }

	        return true;
	    } catch (IOException e) {
	        System.err.println("Error creating sales file: " + e.getMessage());
	        return false;
	    }
	}


    /**
     * Creates a file with pseudo-random product information.
     * The file format is:
     * ProductID1;ProductName1;PricePerUnit1
     * ProductID2;ProductName2;PricePerUnit2
     * ...
     *
     * @param productsCount Number of products to generate
     * @return true if file was created successfully, false otherwise
     */
    public static boolean createProductsFile(int productsCount) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PRODUCTS_FILE_PATH))) {
            // Clear the products list to ensure no duplicate data from previous runs
            productsInfo.clear();

            // Generate random products entries
            for (int i = 0; i < productsCount; i++) {
                // Generate a unique product ID based on the product name index
                int productId = random.nextInt(PRODUCT_NAMES.length);

                // Select a product name from the predefined list
                String productName = PRODUCT_NAMES[i % PRODUCT_NAMES.length];

                // Append an index to the name if duplicates are possible
                if (productsCount > PRODUCT_NAMES.length && i >= PRODUCT_NAMES.length) {
                    productName += " " + (i / PRODUCT_NAMES.length + 1);
                }

                // Generate a random price between 1,000 and 100,000
                int productPrice = 1000 + random.nextInt(100000 - 1000 + 1);

                // Save the product data in the list for later use
                productsInfo.add(new Integer[]{productId, productPrice});

                writer.write(productId + ";" + productName + ";" + productPrice);
                writer.newLine();
            }

            return true;
        } catch (IOException e) {
            System.err.println("Error creating products file: " + e.getMessage());
            return false;
        }
    }


    /**
     * Creates a file with pseudo-random salesperson information.
     * The file format is:
     * DocumentType1;DocumentNumber1;FirstName1;LastName1
     * DocumentType2;DocumentNumber2;FirstName2;LastName2
     * ...
     *
     * @param salesmanCount Number of salespeople to generate
     * @return true if file was created successfully, false otherwise
     */
    public static boolean createSalesManInfoFile(int salesmanCount) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SALESPEOPLE_FILE_PATH))) {
            // Clear the salespeople list before generating it again
            salespeopleInfo.clear();

            // Generate random salesperson entries
            for (int i = 0; i < salesmanCount; i++) {
                String documentType = DOCUMENT_TYPES[random.nextInt(DOCUMENT_TYPES.length)];
                long documentNumber = 1000000000L + random.nextInt(900000000);
                String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];

                // Save the salesperson data in the list for later use
                salespeopleInfo.add(new String[]{documentType, String.valueOf(documentNumber), firstName, lastName});

                writer.write(documentType + ";" + documentNumber + ";" + firstName + ";" + lastName);
                writer.newLine();
            }

            return true;
        } catch (IOException e) {
            System.err.println("Error creating salespeople file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ensures that the output directory exists.
     * Creates the directory if it doesn't exist.
     *
     * @param directoryPath Path to the directory
     * @return true if the directory exists or was created successfully, false otherwise
     */
    private static boolean ensureDirectoryExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            return !directory.mkdirs();
        }
        return !directory.isDirectory();
    }

    /**
     * Clears old files from the predefined directories used in the application.
     * 
     * This method ensures that no leftover files from previous runs interfere 
     * with the current execution by performing the following actions:
     * - Deletes all files within the input directory.
     * - Deletes all files within the salespeople directory.
     * - Deletes all files within the output directory.
     * 
     * The directories themselves are not deleted, only their contents are cleared.
     * Uses the `deleteFilesInDirectory` method to perform the deletions.
     */
    private static void clearOldFiles() {
        // Delete files in the input directory
        deleteFilesInDirectory(new File(INPUT_DIR));

        // Delete files in the salespeople directory
        deleteFilesInDirectory(new File(SALESPEOPLE_DIR));

        // Delete files in the output directory
        deleteFilesInDirectory(new File(OUTPUT_DIR));
    }

    /**
     * Deletes all files in the specified directory.
     *
     * If the provided directory exists and is a valid directory, this method iterates
     * through all the files in the directory and deletes them. It does not delete
     * subdirectories or their contents.
     *
     * If a file cannot be deleted, an error message is printed to the standard error output.
     *
     * @param directory The directory whose files are to be deleted. Should be a valid directory object.
     */
    private static void deleteFilesInDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        if (!file.delete()) {
                            System.err.println("Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

}
