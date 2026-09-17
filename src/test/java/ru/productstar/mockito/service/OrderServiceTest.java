package ru.productstar.mockito.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.ProductNotFoundException;
import ru.productstar.mockito.model.Customer;
import ru.productstar.mockito.model.Delivery;
import ru.productstar.mockito.model.Order;
import ru.productstar.mockito.model.Product;
import ru.productstar.mockito.model.Stock;
import ru.productstar.mockito.model.Warehouse;
import ru.productstar.mockito.repository.OrderRepository;
import ru.productstar.mockito.repository.ProductRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    //Моки зависимостей

    @Mock
    private CustomerService customerService;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    // Сервис, который мы тестируем. Все 4 зависимости будут внедрены из моков выше.
    @InjectMocks
    private OrderService orderService;

    //Вспомогательные методы

    private Customer customer(String name, int id) {
        Customer c = new Customer(name);
        c.setId(id);
        return c;
    }

    private Product product(String name, int id) {
        Product p = new Product(name);
        p.setId(id);
        return p;
    }

    private Order order(Customer customer, int id) {
        Order o = new Order(customer);
        o.setId(id);
        return o;
    }

    private Warehouse warehouse(String name, int distance, int id) {
        Warehouse w = new Warehouse(name, distance);
        w.setId(id);
        return w;
    }

    private Stock stock(Product product, int price, int count) {
        return new Stock(product, price, count);
    }


    // Тест 1. Обычный случай: создание заказа для существующего клиента

    /**
     * Цель: проверить, что create() получает клиента через customerService
     * и передаёт его в orderRepository.create(), а также порядок и количество вызовов.
     */
    @Test
    public void create_existingCustomer_returnsNewOrder() {

        Customer ivan = customer("Ivan", 1);
        Order expectedOrder = order(ivan, 0);

        when(customerService.getOrCreate("Ivan")).thenReturn(ivan);          // stub
        when(orderRepository.create(ivan)).thenReturn(expectedOrder);        // stub


        Order result = orderService.create("Ivan");


        assertSame(expectedOrder, result, "Должен вернуться тот же заказ, что создал репозиторий");


        InOrder inOrder = inOrder(customerService, orderRepository);
        inOrder.verify(customerService, times(1)).getOrCreate("Ivan");
        inOrder.verify(orderRepository, times(1)).create(ivan);
        verifyNoMoreInteractions(customerService, orderRepository);
    }


    // Тест 2. Параметризованный тест: создание заказов для разных имён

    /**
     * Цель: убедиться, что имя клиента корректно передаётся в customerService
     * для разных входных данных. Демонстрируем @ParameterizedTest + @ValueSource.
     */
    @ParameterizedTest
    @ValueSource(strings = {"Oleg", "Petr", "Maria", "Alexey"})
    public void create_variousCustomerNames_createsOrderForEach(String name) {

        Customer c = customer(name, 42);
        Order expected = order(c, 1);
        when(customerService.getOrCreate(name)).thenReturn(c);
        when(orderRepository.create(c)).thenReturn(expected);


        Order result = orderService.create(name);


        assertNotNull(result);
        assertEquals(name, result.getCustomer().getName());
        verify(customerService, times(1)).getOrCreate(name);
        verify(orderRepository, times(1)).create(c);
    }


    // Тест 3. Обычный случай: успешное добавление товара (обычная доставка)

    /**
     * Цель: проверить, что addProduct:
     *  - вызывает warehouseService.findWarehouse(...) (при fastestDelivery=false),
     *  - берёт цену через warehouseService.getStock(...),
     *  - добавляет Delivery через orderRepository.addDelivery(...),
     *  - итоговая сумма заказа корректна.
     */
    @Test
    public void addProduct_sufficientStock_updatesTotal() throws ProductNotFoundException {

        Customer ivan = customer("Ivan", 1);
        Order ord = order(ivan, 0);

        Product phone = product("phone", 0);
        Warehouse wh = warehouse("Warehouse0", 30, 0);
        Stock phoneStock = stock(phone, 400, 5);

        when(warehouseService.findWarehouse("phone", 2)).thenReturn(wh);
        when(productRepository.getByName("phone")).thenReturn(phone);
        when(warehouseService.getStock(wh, "phone")).thenReturn(phoneStock);

        when(orderRepository.addDelivery(eq(0), any(Delivery.class))).thenAnswer(invocation -> {
            Delivery d = invocation.getArgument(1);
            ord.addDelivery(d);
            return ord;
        });


        Order result = orderService.addProduct(ord, "phone", 2, false);


        assertEquals(800L, result.getTotal(), "Итог: 2 * 400 = 800");
        assertEquals(1, result.getDeliveries().size());


        InOrder inOrder = inOrder(warehouseService, productRepository, orderRepository);
        inOrder.verify(warehouseService).findWarehouse("phone", 2);
        inOrder.verify(productRepository).getByName("phone");
        inOrder.verify(warehouseService).getStock(wh, "phone");
        inOrder.verify(orderRepository).addDelivery(eq(0), any(Delivery.class));
    }


    // Тест 4. Обработка ошибки: товара нет ни на одном складе

    /**
     * Цель: если warehouseService не нашёл склад, addProduct должен
     * выбросить ProductNotFoundException и не вызывать addDelivery.
     */
    @Test
    public void addProduct_productNotFound_throwsException() {
        // given
        Customer ivan = customer("Ivan", 1);
        Order ord = order(ivan, 0);
        when(warehouseService.findWarehouse("tv", 1)).thenReturn(null); // товара нет


        ProductNotFoundException ex = assertThrows(
                ProductNotFoundException.class,
                () -> orderService.addProduct(ord, "tv", 1, false)
        );
        assertTrue(ex.getMessage().contains("tv"));


        verify(orderRepository, never()).addDelivery(anyInt(), any(Delivery.class));
    }


    // Тест 5. Особый случай: "быстрая доставка"

    /**
     * Цель: при fastestDelivery=true должен вызываться
     * findClosestWarehouse, а не findWarehouse.
     */
    @Test
    public void addProduct_fastestDelivery_usesFindClosestWarehouse() throws ProductNotFoundException {
        // given
        Customer ivan = customer("Ivan", 1);
        Order ord = order(ivan, 0);

        Product laptop = product("laptop", 1);
        Warehouse closest = warehouse("Warehouse2", 5, 2);
        Stock laptopStock = stock(laptop, 450, 3);

        when(warehouseService.findClosestWarehouse("laptop", 1)).thenReturn(closest);
        when(warehouseService.getStock(closest, "laptop")).thenReturn(laptopStock);
        when(productRepository.getByName("laptop")).thenReturn(laptop);
        when(orderRepository.addDelivery(eq(0), any(Delivery.class))).thenReturn(ord);


        orderService.addProduct(ord, "laptop", 1, true);


        verify(warehouseService, times(1)).findClosestWarehouse("laptop", 1);
        verify(warehouseService, never()).findWarehouse(anyString(), anyInt());
    }


    // Тест 6. Симуляция внутренней ошибки через thenThrow

    /**
     * Цель: если нижележащий сервис склада бросает исключение,
     * оно должно пробрасываться наружу, а addDelivery не вызывается.
     */
    @Test
    public void addProduct_warehouseThrows_propagatesException() {

        Customer ivan = customer("Ivan", 1);
        Order ord = order(ivan, 0);

        Warehouse wh = warehouse("Warehouse0", 30, 0);
        when(warehouseService.findWarehouse("phone", 1)).thenReturn(wh);
        // симулируем сбой на уровне получения остатка
        when(warehouseService.getStock(wh, "phone"))
                .thenThrow(new RuntimeException("stock service unavailable"));


        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> orderService.addProduct(ord, "phone", 1, false)
        );
        assertEquals("stock service unavailable", ex.getMessage());

        verify(orderRepository, never()).addDelivery(anyInt(), any(Delivery.class));
    }


    // Тест 7. Демонстрация spy

    /**
     * Цель: показать работу со spy. Мы шпионим за реальным OrderRepository,
     * чтобы убедиться, что create(...) действительно был вызван.
     * Часть методов оставляем настоящими, часть — переопределяем при необходимости.
     */
    @Test
    public void create_withSpyOnRealOrderRepository_verifiesCall() {

        OrderRepository spyRepo = spy(new OrderRepository());


        OrderService serviceWithSpy = new OrderService(
                customerService, warehouseService, spyRepo, productRepository
        );

        Customer oleg = customer("Oleg", 1);
        Order realOrder = new Order(oleg);
        realOrder.setId(0);

        when(customerService.getOrCreate("Oleg")).thenReturn(oleg);

        doReturn(realOrder).when(spyRepo).create(oleg);


        Order result = serviceWithSpy.create("Oleg");


        assertSame(realOrder, result);
        verify(spyRepo, times(1)).create(oleg);
        verify(customerService, times(1)).getOrCreate("Oleg");
    }
}
