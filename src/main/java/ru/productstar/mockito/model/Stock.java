package ru.productstar.mockito.model;

public class Stock {
    private Product product;
    private int price;
    private int count;

    public Stock(Product product, int price, int count) {
        this.product = product;
        this.price = price;
        this.count = count;
    }

    public Product getProduct() {
        return product;
    }

    public int getPrice() {
        return price;
    }

    public int getCount() {
        return count;
    }
}
