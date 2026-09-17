package ru.productstar.mockito.service;

import ru.productstar.mockito.model.Stock;
import ru.productstar.mockito.model.Warehouse;
import ru.productstar.mockito.repository.WarehouseRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WarehouseService {

    private WarehouseRepository warehouseRepository;

    public WarehouseService() {
    }

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public Stock getStock(Warehouse wh, String productName) {
        for (Stock s : wh.getStocks()) {
            if(s.getProduct().getName().equals(productName)) {
                return s;
            }
        }
        return null;
    }

    public Warehouse findWarehouse(String productName, int count) {
        List<Warehouse> whs = findWarehouses(productName, count);
        if (!whs.isEmpty()) {
            return whs.get(0);
        } else {
            return null;
        }
    }

    public Warehouse findClosestWarehouse(String productName, int count) {
        List<Warehouse> whs = findWarehouses(productName, count);
        if (!whs.isEmpty() && whs.size() > 1) {
            return whs.stream().min(Comparator.comparingInt(Warehouse::getDistance)).get();
        } else if (!whs.isEmpty()) {
            return whs.get(0);
        } else {
            return null;
        }
    }

    private List<Warehouse> findWarehouses(String productName, int count) {
        List<Warehouse> whs = new ArrayList<>();
        for (Warehouse wh : warehouseRepository.all()) {
            for(Stock s : wh.getStocks()) {
                if(s.getProduct().getName().equals(productName) && s.getCount() >= count) {
                    whs.add(wh);
                }
            }
        }
        return whs;
    }
}
