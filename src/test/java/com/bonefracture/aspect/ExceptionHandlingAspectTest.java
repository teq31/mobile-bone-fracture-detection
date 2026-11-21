package com.bonefracture.aspect;

import com.bonefracture.config.ConfigurationException;
import com.bonefracture.service.ConfigurationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionHandlingAspectTest {

    @Test
    void serviceError_isWrappedInServiceException() {
        ConfigurationService service = new ConfigurationService();

        ServiceException thrown = assertThrows(ServiceException.class,
                () -> service.loadConfiguration("config/does-not-exist.properties"));

        assertEquals("ConfigurationService.loadConfiguration", thrown.getServiceOperation());
        assertTrue(thrown.getMessage().contains("ConfigurationService.loadConfiguration"));
    }

    @Test
    void originalCause_isPreserved() {
        ConfigurationService service = new ConfigurationService();

        ServiceException thrown = assertThrows(ServiceException.class,
                () -> service.loadConfiguration("config/does-not-exist.properties"));

        assertInstanceOf(ConfigurationException.class, thrown.getCause());
    }
}
