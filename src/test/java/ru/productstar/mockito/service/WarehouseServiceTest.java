package ru.productstar.mockito.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.model.Product;
import ru.productstar.mockito.model.Stock;
import ru.productstar.mockito.model.Warehouse;
import ru.productstar.mockito.repository.ProductRepository;
import ru.productstar.mockito.repository.WarehouseRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для WarehouseService.
 *
 * Все зависимости (WarehouseRepository, ProductRepository) подменяются моками,
 * чтобы не запускать реальные хранилища. Склады создаём вручную — это позволяет
 * контролировать состав товаров, цены и расстояния.
 */
@ExtendWith(MockitoExtension.class)
public class WarehouseServiceTest {

    // Мок репозитория складов — именно его будет использовать сервис
    @Mock
    private WarehouseRepository warehouseRepository;

    // Сам сервис, в который мы внедрим мок
    private WarehouseService warehouseService;

    // Данные, которые переиспользуем в тестах
    private Product phone;
    private Product laptop;
    private Product tv;

    private Warehouse wh0;
    private Warehouse wh1;
    private Warehouse wh2;

    /**
     * Подготовка общих данных перед каждым тестом:
     *   - создаём сервис с мок-репозиторием;
     *   - создаём три склада с разными расстояниями и остатками.
     *
     * Расклад по phone:
     *   wh0 (dist=30): 5 шт
     *   wh1 (dist=20): 2 шт
     *   wh2 (dist=5):  3 шт
     *
     * Расклад по laptop:
     *   wh0 (dist=30): 3 шт
     *   wh1 (dist=20): 1 шт
     *   wh2: нет
     */
    @BeforeEach
    void setUp() {
        warehouseService = new WarehouseService(warehouseRepository);

        phone = new Product("phone");
        phone.setId(0);
        laptop = new Product("laptop");
        laptop.setId(1);
        tv = new Product("tv");
        tv.setId(2);

        wh0 = new Warehouse("Warehouse0", 30);
        wh0.setId(0);
        wh0.addStock(new Stock(phone, 400, 5));
        wh0.addStock(new Stock(laptop, 900, 3));

        wh1 = new Warehouse("Warehouse1", 20);
        wh1.setId(1);
        wh1.addStock(new Stock(phone, 380, 2));
        wh1.addStock(new Stock(laptop, 850, 1));

        wh2 = new Warehouse("Warehouse2", 5);
        wh2.setId(2);
        wh2.addStock(new Stock(phone, 450, 3));
    }


    // Тест 1. Существующий товар с достаточным количеством

    /**
     * Проверяем, что findWarehouse находит первый склад, где есть товар
     * в нужном количестве.
     *
     * Порядок обхода: wh0, wh1, wh2.
     * Для phone и count=3 подходят wh0 (5 шт) и wh2 (3 шт). wh1 не подходит (2 шт).
     * Ожидаем, что вернётся wh0 — первый в списке.
     */
    @Test
    void findWarehouse_sufficientStock_returnsFirstMatchingWarehouse() {
        when(warehouseRepository.all()).thenReturn(List.of(wh0, wh1, wh2));

        Warehouse result = warehouseService.findWarehouse("phone", 3);

        assertNotNull(result);
        assertEquals(wh0, result, "Должен вернуться первый склад с достаточным количеством");
        verify(warehouseRepository, times(1)).all();
    }


    // Тест 2. Несуществующий товар

    /**
     * Если товар не найден ни на одном складе, метод должен вернуть null.
     */
    @Test
    void findWarehouse_productNotFound_returnsNull() {
        when(warehouseRepository.all()).thenReturn(List.of(wh0, wh1, wh2));

        Warehouse result = warehouseService.findWarehouse("tv", 1);

        assertNull(result, "Для несуществующего товара ожидаем null");
        verify(warehouseRepository, times(1)).all();
    }


    // Тест 3. Недостаточное количество товара

    /**
     * Если у товара недостаточно запаса ни на одном складе, метод возвращает null.
     * Например, phone запрашиваем 10 шт, а максимум есть только 5 на wh0.
     */
    @Test
    void findWarehouse_insufficientCount_returnsNull() {
        when(warehouseRepository.all()).thenReturn(List.of(wh0, wh1, wh2));

        Warehouse result = warehouseService.findWarehouse("phone", 10);

        assertNull(result, "Запас меньше запрошенного — ожидаем null");
        verify(warehouseRepository, times(1)).all();
    }


    // Тест 4. findClosestWarehouse — выбираем ближайший из нескольких

    /**
     * Проверяем логику выбора ближайшего склада.
     * Для phone и count=3 подходят wh0 (dist=30) и wh2 (dist=5).
     * Ближайший — wh2.
     */
    @Test
    void findClosestWarehouse_multipleMatches_returnsClosest() {
        when(warehouseRepository.all()).thenReturn(List.of(wh0, wh1, wh2));

        Warehouse result = warehouseService.findClosestWarehouse("phone", 3);

        assertEquals(wh2, result, "wh2 ближе (5 < 30), значит должен вернуться он");
        verify(warehouseRepository, times(1)).all();
    }


    // Тест 5. findClosestWarehouse — подходящий склад ровно один

    /**
     * Если товар с достаточным количеством есть только на одном складе,
     * возвращаем его (без учёта расстояния).
     * Для laptop и count=3 подходит только wh0.
     */
    @Test
    void findClosestWarehouse_singleMatch_returnsThatWarehouse() {
        when(warehouseRepository.all()).thenReturn(List.of(wh0, wh1, wh2));

        Warehouse result = warehouseService.findClosestWarehouse("laptop", 3);

        assertEquals(wh0, result);
        verify(warehouseRepository, times(1)).all();
    }


    // Тест 6. findClosestWarehouse — товар не найден

    @Test
    void findClosestWarehouse_productNotFound_returnsNull() {
        when(warehouseRepository.all()).thenReturn(List.of(wh0, wh1, wh2));

        Warehouse result = warehouseService.findClosestWarehouse("tv", 1);

        assertNull(result);
        verify(warehouseRepository, times(1)).all();
    }


    // Тест 7. Граничный случай — нулевое количество

    /**
     * При count = 0 подходит любой склад с положительным остатком.
     * Ожидаем вернуть первый такой склад (wh0) в порядке обхода.
     */
    @Test
    void findWarehouse_zeroCount_returnsFirstWarehouseWithStock() {
        when(warehouseRepository.all()).thenReturn(List.of(wh0, wh1, wh2));

        Warehouse result = warehouseService.findWarehouse("phone", 0);

        assertEquals(wh0, result);
        verify(warehouseRepository, times(1)).all();
    }


    // Тест 8. Ошибочный случай — отрицательное количество

    /**
     * При отрицательном count любой склад с товаром подходит (count >= -1 всегда истинно).
     * Возвращается первый склад, где есть товар.
     *
     * Это пример того, что сервис не валидирует входные данные.
     * Если в будущем бизнес потребует проверки — тест придётся обновить.
     */
    @Test
    void findWarehouse_negativeCount_returnsFirstWarehouseWithStock() {
        when(warehouseRepository.all()).thenReturn(List.of(wh0, wh1, wh2));

        Warehouse result = warehouseService.findWarehouse("phone", -1);

        assertEquals(wh0, result);
        verify(warehouseRepository, times(1)).all();
    }


    // Тест 9. Порядок и количество вызовов

    /**
     * Проверяем, что сервис обращается к репозиторию ровно один раз
     * и делает это до вычисления результата — InOrder для контроля порядка.
     */
    @Test
    void findWarehouse_callsRepositoryExactlyOnce() {
        when(warehouseRepository.all()).thenReturn(List.of(wh0, wh1, wh2));

        warehouseService.findWarehouse("phone", 3);

        InOrder inOrder = inOrder(warehouseRepository);
        inOrder.verify(warehouseRepository, times(1)).all();
        verifyNoMoreInteractions(warehouseRepository);
    }


    // Тест 10. Spy + doReturn — работа со шпионом реального репозитория

    /**
     * Демонстрация spy(...) и doReturn(...).when(spy).method().
     *
     * Создаём реальный WarehouseRepository (с мок-ProductRepository в конструкторе,
     * чтобы не поднимать реальные данные), оборачиваем его в spy и подменяем
     * только метод all() — остальные методы работают по-настоящему.
     *
     * Такое комбинирование удобно, когда хочется протестировать "полу-реальную"
     * логику, оставив часть поведения настоящей.
     */
    @Test
    void findClosestWarehouse_withSpyOnRepository_returnsClosest() {
        // реальный репозиторий, но с мок-ProductRepository — чтобы конструктор не тянул настоящие данные
        ProductRepository productRepoMock = mock(ProductRepository.class);
        WarehouseRepository realRepository = new WarehouseRepository(productRepoMock);

        // шпион вокруг реального объекта
        WarehouseRepository spyRepository = spy(realRepository);

        // подменяем поведение только метода all() через doReturn
        doReturn(List.of(wh0, wh1, wh2)).when(spyRepository).all();

        // сервис с шпионом вместо чистого мока
        WarehouseService spyService = new WarehouseService(spyRepository);

        Warehouse result = spyService.findClosestWarehouse("phone", 3);

        assertEquals(wh2, result);
        verify(spyRepository, times(1)).all();
    }
}