package ru.productstar.mockito.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private Customer customer;
    private List<Delivery> deliveries = new ArrayList<>();
    private long total;

    public Order(Customer customer) {
        this.customer = customer;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long addDelivery(Delivery delivery) {
        deliveries.add(delivery);
        total = total + delivery.getPrice() * delivery.getCount();
        return total;
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public long getTotal() {
        return total;
    }
}
