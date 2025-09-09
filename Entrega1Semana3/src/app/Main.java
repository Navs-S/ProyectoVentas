package app;

import java.util.*;

/**
 * Main class for registering product sales and generating a consolidated report.
 * This program allows the user to select a predefined salesman, input product sales manually,
 * and view a detailed sales report including totals per product and per salesman.
 */
public class Main {

    /**
     * Entry point of the program.
     * Displays available salesmen and products, allows manual input of sales,
     * and generates a consolidated report at the end.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Predefined list of products available for sale
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
            new Salesman("CC", "1022454063", "Ana", "Perez"),
            new Salesman("CC", "79523478", "Luis", "Mota"),
            new Salesman("CC", "45986215", "Marta", "Garcia")
        );

        Salesman selectedSalesman = null;

        // Loop until a valid salesman is selected
        while (selectedSalesman == null) {
            System.out.println("\nLista de vendedores:");
            System.out.printf("| %-22s | %-13s |%n", "Full Name", "Document");
            System.out.println("|------------------------|---------------|");
            for (Salesman s : salesmen) {
                System.out.printf("| %-22s | %-13s |%n", s.getFullName(), s.docNumber);
            }

            System.out.print("\nEscribe el nombre o numero de documento del vendedor: ");
            String input = scanner.nextLine().trim();

            // Search for salesman by full name or document number
            selectedSalesman = salesmen.stream()
                .filter(s -> s.getFullName().equalsIgnoreCase(input) || s.docNumber.equalsIgnoreCase(input))
                .findFirst()
                .orElse(null);

            if (selectedSalesman == null) {
                System.out.println("Vendedor no encontrado, por favor intenta de nuevo");
            }
        }

        System.out.println("\nRecording sales for: " + selectedSalesman.getFullName());

        // Display available products in table format
        System.out.println("\nLista de productos:");
        System.out.printf("| %-10s | %-25s | %-8s |%n", "Code", "Product Name", "Price");
        System.out.println("|------------|---------------------------|----------|");
        for (Product p : products) {
            System.out.printf("| %-10s | %-25s | %8.2f |%n", p.id, p.name, p.price);
        }

        // Loop to register sales until user types 'fin'
        while (true) {
            System.out.print("\nIngresa nombre o codigo del producto (o 'fin' para finalizar): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("fin")) break;

            // Search for product by code or name
            Product product = products.stream()
                .filter(p -> p.id.equalsIgnoreCase(input) || p.name.equalsIgnoreCase(input))
                .findFirst()
                .orElse(null);

            if (product == null) {
                System.out.println("Producto no encontrado. por favor intenta de nuevo.");
                continue;
            }

            System.out.print("Ingresa la cantidad vendida: ");
            int quantity;
            try {
                quantity = Integer.parseInt(scanner.nextLine().trim());
                if (quantity <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.out.println("Cantidad incorrecta, ingresa números positivos.");
                continue;
            }

            // Register the sale
            selectedSalesman.sales.add(new Sale(product.id, quantity));
            System.out.printf("Cantidad vendida: %s x%d%n", product.name, quantity);
        }

        // Generate and display the sales report
        runReport(salesmen, products);
    }

    /**
     * Generates a consolidated sales report for all salesmen.
     * Displays each salesman's details and the breakdown of products sold,
     * including total per product and total per salesman.
     */
    public static void runReport(List<Salesman> salesmen, List<Product> products) {
        System.out.println("\nReporte de consolidado de ventas");

        for (Salesman s : salesmen) {
            double totalSalesman = s.getTotalRevenue(products);
            if (totalSalesman == 0) continue;

            System.out.println("────────────────────────────────────────────────────────────────────────────────────");
            System.out.printf("Salesman: %-22s | Type: %-3s | Document: %-13s%n",
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
            System.out.printf("Total Venta: %.2f%n", totalGeneral);
            System.out.println("────────────────────────────────────────────────────────────────────────────────────\n");
        }
    }
}