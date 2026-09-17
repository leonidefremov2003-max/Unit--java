package ru.productstar.mockito.repository;

public class InitRepository {
    private static InitRepository instance;

    private final ProductRepository productRepository = new ProductRepository();
    private final CustomerRepository customerRepository = new CustomerRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    private final WarehouseRepository warehouseRepository = new WarehouseRepository(productRepository);

    private InitRepository() {
    }

    public static InitRepository getInstance() {
        if (instance == null) {
            instance = new InitRepository();
        }
        return instance;
    }

    public WarehouseRepository getWarehouseRepository() {
        return warehouseRepository;
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public CustomerRepository getCustomerRepository() {
        return customerRepository;
    }

    public OrderRepository getOrderRepository() {
        return orderRepository;
    }
}
