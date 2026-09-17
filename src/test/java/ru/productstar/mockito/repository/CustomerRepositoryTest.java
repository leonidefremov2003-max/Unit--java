package ru.productstar.mockito.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.model.Customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerRepositoryTest{

    @Test
    public void getByNameTest() {
        CustomerRepository cr = InitRepository.getInstance().getCustomerRepository();

        assertEquals(3, cr.size());
        assertNotNull(cr.getByName("Ivan"));
        assertNull(cr.getByName("Max"));
    }

    @Test
    public void mockGetByNameTest() {
        CustomerRepository cr = mock(CustomerRepository.class);
        when(cr.getByName("Max")).thenReturn(new Customer("Max"));

        assertEquals(0, cr.size());
        assertNotNull(cr.getByName("Max"));

        when(cr.getByName(startsWith("Alex"))).then(invocationOnMock -> new Customer(invocationOnMock.getArgument(0)));
        assertNotNull(cr.getByName("Alex"));
        assertNotNull(cr.getByName("Alexey"));
        assertEquals("Alex", cr.getByName("Alex").getName());
        assertEquals("Alexey", cr.getByName("Alexey").getName());
    }
}
