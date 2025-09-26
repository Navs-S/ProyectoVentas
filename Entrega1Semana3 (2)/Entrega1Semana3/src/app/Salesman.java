package app;

import java.util.ArrayList;
import java.util.List;

public class Salesman {
    public String typeDoc;
    public String docNumber;
    public String firstName;
    public String lastName;
    public List<Sale> sales;

    public Salesman(String typeDoc, String docNumber, String firstName, String lastName) {
        this.typeDoc = typeDoc;
        this.docNumber = docNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sales = new ArrayList<>();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public double getTotalRevenue(List<Product> products) {
        double total = 0;
        for (Sale sale : sales) {
            for (Product product : products) {
                if (product.id.equals(sale.productId)) {
                    total += product.price * sale.quantity;
                }
            }
        }
        return total;
    }
}