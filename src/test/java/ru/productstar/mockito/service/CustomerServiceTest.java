package ru.productstar.mockito.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.model.Customer;
import ru.productstar.mockito.repository.CustomerRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    /**
     * Тест 1 - Получение покупателя "Ivan"
     * Проверки:
     * - очередность и точное количество вызовов каждого метода из CustomerRepository
     */
    @Test
    public void getOrCreate_existingCustomer_ivan_returnsExistingCustomer() {

        Customer ivan = new Customer("Ivan");
        ivan.setId(1);
        when(customerRepository.getByName("Ivan")).thenReturn(ivan);

        Customer result = customerService.getOrCreate("Ivan");

        assertEquals(ivan, result);

        InOrder inOrder = inOrder(customerRepository);

        inOrder.verify(customerRepository, times(1)).getByName("Ivan");

        verify(customerRepository, times(0)).add(any(Customer.class));

        verifyNoMoreInteractions(customerRepository);
    }

    /**
     * Тест 2 - Получение покупателя "Oleg"
     * Проверки:
     * - очередность и точное количество вызовов каждого метода из CustomerRepository
     * - в метод getOrCreate была передана строка "Oleg"
     */
    @Test
    public void getOrCreate_newCustomer_oleg_createsAndSavesCustomer() {

        when(customerRepository.getByName("Oleg")).thenReturn(null);

        Customer savedOleg = new Customer("Oleg");
        savedOleg.setId(4);
        when(customerRepository.add(any(Customer.class))).thenReturn(savedOleg);

        Customer result = customerService.getOrCreate("Oleg");

        assertEquals(savedOleg, result);
        assertEquals("Oleg", result.getName());

        InOrder inOrder = inOrder(customerRepository);


        inOrder.verify(customerRepository, times(1)).getByName("Oleg");

        inOrder.verify(customerRepository, times(1)).add(any(Customer.class));

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).add(captor.capture());
        assertEquals("Oleg", captor.getValue().getName(),
                "В метод add должен быть передан клиент с именем Oleg");

        verifyNoMoreInteractions(customerRepository);
    }

    /**
     * Тест 3 — граничный случай: пустая строка в качестве имени.
     * Проверки:
     * - сервис не падает и создаёт клиента с пустым именем;
     * - очередность и точное количество вызовов каждого метода из CustomerRepository.
     */
    @Test
    public void getOrCreate_emptyName_createsCustomerWithEmptyName() {

        when(customerRepository.getByName("")).thenReturn(null);

        Customer savedCustomer = new Customer("");
        savedCustomer.setId(3);
        when(customerRepository.add(any(Customer.class))).thenReturn(savedCustomer);


        Customer result = customerService.getOrCreate("");


        assertEquals("", result.getName());
        assertEquals(3, result.getId());


        InOrder inOrder = inOrder(customerRepository);
        inOrder.verify(customerRepository, times(1)).getByName("");
        inOrder.verify(customerRepository, times(1)).add(any(Customer.class));
        verifyNoMoreInteractions(customerRepository);
    }
}
