package ru.productstar.mockito.model;

public class Delivery {
    private Product product;
    private Warehouse warehouse;
    private int price;
    private int count;

    public Delivery(Product product, Warehouse warehouse, int price, int count) {
        this.product = product;
        this.warehouse = warehouse;
        this.price = price;
        this.count = count;
    }

    public Product getProduct() {
        return product;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public int getCount() {
        return count;
    }

    public int getPrice() {
        return price;
    }
}
