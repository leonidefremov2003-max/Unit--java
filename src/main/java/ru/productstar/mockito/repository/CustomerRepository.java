package ru.productstar.mockito.repository;

import ru.productstar.mockito.model.Customer;

import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {
    private List<Customer> customers = new ArrayList<>();

    public CustomerRepository() {
        add(new Customer("Ivan"));
        add(new Customer("Petr"));
        add(new Customer("Alex"));
    }

    public Customer add(Customer customer) {
        customer.setId(customers.size());
        customers.add(customer);
        return customer;
    }

    public Customer getByName(String name) {
        return customers.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    public List<Customer> all() {
        return customers;
    }

    public int size() {
        return customers.size();
    }
}
