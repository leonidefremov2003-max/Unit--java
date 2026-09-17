package ru.productstar.mockito.model;

import java.util.ArrayList;
import java.util.List;

public class Warehouse {
    private int id;
    private String name;
    private int distance;
    private List<Stock> stocks = new ArrayList<>();

    public Warehouse() {
    }

    public Warehouse(String name, int distance) {
        this.name = name;
        this.distance = distance;
    }

    public boolean addStock(Stock stock) {
        return stocks.add(stock);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDistance() {
        return distance;
    }

    public List<Stock> getStocks() {
        return stocks;
    }
}
