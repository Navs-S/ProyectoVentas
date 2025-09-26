package app;

import java.text.Normalizer;
import java.util.*;

/**
 * Main class for managing product sales and generating sales reports.
 * This program allows users to select a salesman, record multiple product sales,
 * and generate either individual or consolidated sales reports.
 */
public class Main {

    /**
     * Entry point of the program.
     * Handles salesman selection, product sales input, and report generation.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Predefined product catalog
        List<Product> products = Arrays.asList(
            new Product("2536", "Tornillo", 500),
            new Product("9852", "Regla", 1200),
            new Product("9825", "Arbol", 5000),
            new Product("3325", "Tabla", 500),
            new Product("9854", "Cargador", 1200),
            new Product("7825", "Vidrio", 5000)
        );

        // Predefined list of salesmen
        List<Salesman> salesmen = Arrays.asList(
            new Salesman("CC", "1022454063", "Ana", "Pérez"),
            new Salesman("CC", "79523478", "Luis", "Mota"),
            new Salesman("CC", "45986215", "Marta", "García")
        );

        System.out.println("Bienvenido al registro diario de ventas.");

        boolean continueSales = true;

        // Loop to allow multiple salesmen to register sales
        while (continueSales) {
            Salesman selectedSalesman = null;

            // Prompt user to select a salesman by name or document number
            while (selectedSalesman == null) {
                System.out.println("\nAvailable Salesmen:");
                System.out.printf("| %-22s | %-13s |%n", "Full Name", "Document");
                System.out.println("|------------------------|---------------|");
                for (Salesman s : salesmen) {
                    System.out.printf("| %-22s | %-13s |%n", s.getFullName(), s.docNumber);
                }

                System.out.print("\nIngrese el nombre o número de documento del vendedor: ");
                String input = normalize(scanner.nextLine().trim());

                selectedSalesman = salesmen.stream()
                    .filter(s -> normalize(s.getFullName()).equalsIgnoreCase(input) || normalize(s.docNumber).equalsIgnoreCase(input))
                    .findFirst()
                    .orElse(null);

                if (selectedSalesman == null) {
                    System.out.println("Vendedor no encontrado, por favor intentalo de nuevo.");
                }
            }

            System.out.println("\nRecording sales for: " + selectedSalesman.getFullName());

            // Display product catalog
            System.out.println("\nAvailable Products:");
            System.out.printf("| %-10s | %-25s | %-8s |%n", "Code", "Product Name", "Price");
            System.out.println("|------------|---------------------------|----------|");
            for (Product p : products) {
                System.out.printf("| %-10s | %-25s | %8.2f |%n", p.id, p.name, p.price);
            }

            // Loop to record product sales
            while (true) {
                System.out.print("\nIngresa el nombre o código del producto (en caso contrario puedes escribir 'fin' para finalizar): ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("fin")) break;

                Product product = products.stream()
                    .filter(p -> normalize(p.id).equalsIgnoreCase(normalize(input)) || normalize(p.name).equalsIgnoreCase(normalize(input)))
                    .findFirst()
                    .orElse(null);

                if (product == null) {
                    System.out.println("Producto no encontrado, ¿puedes intentarlo nuevamente?.");
                    continue;
                }

                System.out.print("Ingresa las cantidades vendida: ");
                int quantity;
                try {
                    quantity = Integer.parseInt(scanner.nextLine().trim());
                    if (quantity <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    System.out.println("Cantidad no valida, recuerda que solo acepto números positivos.");
                    continue;
                }

                // Check if the product was already sold by this salesman
                Optional<Sale> existingSale = selectedSalesman.sales.stream()
                    .filter(s -> s.productId.equals(product.id))
                    .findFirst();

                if (existingSale.isPresent()) {
                    existingSale.get().quantity += quantity;
                } else {
                    selectedSalesman.sales.add(new Sale(product.id, quantity));
                }

                System.out.printf("Venta guardada: %s x%d%n", product.name, quantity);
            }

            System.out.print("\nTe gustaria registrar ventas para otro vendedor? (y/n): ");
            continueSales = scanner.nextLine().trim().equalsIgnoreCase("y");
        }

        // Prompt for report type
        System.out.print("\n¿Quieres generar un reporte individual de ventas? (y/n): ");
        boolean individualReport = scanner.nextLine().trim().equalsIgnoreCase("y");

        if (individualReport) {
            for (Salesman s : salesmen) {
                if (!s.sales.isEmpty()) {
                    runReport(Collections.singletonList(s), products);
                }
            }
        } else {
            runReport(salesmen, products);
        }

        // Calculate total revenue for the day
        double totalDay = salesmen.stream()
            .mapToDouble(s -> s.getTotalRevenue(products))
            .sum();

        System.out.printf("\nSuper, el dia de hoy vendiste en total: %.2f pesos%n", totalDay);
        System.out.println("Hemos terminado el día, nos vemos mañana!");
    }

    /**
     * Generates a sales report for the given list of salesmen.
     * Displays product breakdown and total revenue per salesman.
     *
     * @param salesmen List of salesmen to include in the report.
     * @param products Product catalog used for price lookup.
     */
    public static void runReport(List<Salesman> salesmen, List<Product> products) {
        System.out.println("\nReporte de consolidado de ventas");

        for (Salesman s : salesmen) {
            double totalSalesman = s.getTotalRevenue(products);
            if (totalSalesman == 0) continue;

            System.out.println("────────────────────────────────────────────────────────────────────────────────────");
            System.out.printf("Vendedor: %-22s | Type: %-3s | Documento: %-13s%n",
                              s.getFullName(), s.typeDoc, s.docNumber);
            System.out.println("────────────────────────────────────────────────────────────────────────────────────");
            System.out.printf("| %-10s | %-25s | %-8s | %-8s | %-10s |%n",
                              "Code", "Description", "Price", "Quantity", "Total");
            System.out.println("|------------|---------------------------|----------|----------|------------|");

            double totalGeneral = 0;

            for (Sale sale : s.sales) {
                Product p = products.stream()
                    .filter(prod -> prod.id.equals(sale.productId))
                    .findFirst()
                    .orElse(null);

                if (p != null) {
                    double totalProduct = p.price * sale.quantity;
                    totalGeneral += totalProduct;
                    System.out.printf("| %-10s | %-25s | %8.2f | %8d | %10.2f |%n",
                                      p.id, p.name, p.price, sale.quantity, totalProduct);
                }
            }

            System.out.println("────────────────────────────────────────────────────────────────────────────────────");
            System.out.printf("Total Ventas: %.2f%n", totalGeneral);
            System.out.println("────────────────────────────────────────────────────────────────────────────────────\n");
        }
    }

    /**
     * Normalizes a string by removing accents and converting to lowercase.
     * Useful for case-insensitive and accent-insensitive comparisons.
     *
     * @param input The string to normalize.
     * @return Normalized string.
     */
    public static String normalize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                         .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                         .toLowerCase();
    }
}