package ru.productstar.mockito.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InitRepositoryTest {

    @Test
    public void mockInitTest() {
        try (MockedStatic mock = mockStatic(InitRepository.class)) {
            assertNull(InitRepository.getInstance());

            mock.when(InitRepository::getInstance).thenCallRealMethod();
            assertNotNull(InitRepository.getInstance());
        }
    }

    @Test
    public void mockDeepInitTest() {
        try (MockedStatic mock = mockStatic(InitRepository.class, RETURNS_DEEP_STUBS)) {
            when(InitRepository.getInstance().getCustomerRepository().size()).thenReturn(5);

            assertEquals(5, InitRepository.getInstance().getCustomerRepository().size());
        }
    }
}
