package ru.productstar.mockito.repository;

import ru.productstar.mockito.model.*;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private List<Order> orders = new ArrayList<>();

    public Order create(Customer customer) {
        Order order = new Order(customer);
        order.setId(orders.size());
        orders.add(order);
        return order;
    }

    public Order addDelivery(int orderId, Delivery delivery) {
        Order order = orders.get(orderId);
        order.addDelivery(delivery);
        return order;
    }

    public List<Order> all() {
        return orders;
    }

    public int size() {
        return orders.size();
    }
}
