package ru.productstar.mockito;

public class ProductNotFoundException extends Exception {
    public ProductNotFoundException(String product) {
        super(product + " not found");
    }
}
