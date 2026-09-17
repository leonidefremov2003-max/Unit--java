package ru.productstar.mockito.repository;

import com.sun.source.tree.WhileLoopTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.model.Warehouse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.geq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WarehouseRepositoryTest {

    @Test
    public void getByIdTest() {
        WarehouseRepository whr = InitRepository.getInstance().getWarehouseRepository();

        assertEquals(3, whr.size());

        Warehouse wh0 = whr.getById(0);
        assertEquals("Warehouse0", wh0.getName());
        assertEquals(40, wh0.getStocks().get(2).getPrice());
    }

    @Test
    public void mockGetByIdTest() {
        WarehouseRepository whr = spy(InitRepository.getInstance().getWarehouseRepository());
        when(whr.size()).thenReturn(1);
        assertEquals(1, whr.size());

        Warehouse wh0 = whr.getById(0);
        assertEquals("Warehouse0", wh0.getName());
        assertEquals(40, wh0.getStocks().get(2).getPrice());

        when(whr.getById(geq(1))).thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class, () -> whr.getById(1));
        assertThrows(RuntimeException.class, () -> whr.getById(5));
    }
}
