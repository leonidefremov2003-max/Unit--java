package ru.productstar.mockito.repository;

import ru.productstar.mockito.model.Stock;
import ru.productstar.mockito.model.Warehouse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarehouseRepository {
    private Map<Integer, Warehouse> warehouses = new HashMap<>();

    public WarehouseRepository(ProductRepository productRepository) {
        Warehouse wh0 = new Warehouse("Warehouse0", 30);
        wh0.addStock(new Stock(productRepository.getByName("phone"), 400, 5));
        wh0.addStock(new Stock(productRepository.getByName("laptop"), 900, 3));
        wh0.addStock(new Stock(productRepository.getByName("keyboard"), 40, 10));
        add(wh0);

        Warehouse wh1 = new Warehouse("Warehouse1", 20);
        wh1.addStock(new Stock(productRepository.getByName("phone"), 380, 2));
        wh1.addStock(new Stock(productRepository.getByName("laptop"), 850, 1));
        wh1.addStock(new Stock(productRepository.getByName("monitor"), 300, 8));
        wh1.addStock(new Stock(productRepository.getByName("keyboard"), 40, 15));
        add(wh1);

        Warehouse wh2 = new Warehouse("Warehouse2", 5);
        wh2.addStock(new Stock(productRepository.getByName("phone"), 450, 3));
        wh2.addStock(new Stock(productRepository.getByName("printer"), 200, 4));
        wh2.addStock(new Stock(productRepository.getByName("keyboard"), 40, 15));
        add(wh2);
    }

    public Warehouse add(Warehouse warehouse) {
        warehouse.setId(warehouses.size());
        return this.warehouses.put(warehouse.getId(), warehouse);
    }

    public Warehouse getById(int id) {
        return warehouses.get(id);
    }

    public List<Warehouse> all() {
        return warehouses.values().stream().toList();
    }

    public int size() {
        return warehouses.size();
    }
}
