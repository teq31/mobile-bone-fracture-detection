package com.bonefracture.aspect;

import com.bonefracture.config.Language;
import com.bonefracture.service.ConfigurationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationAuditAspectTest {

    private String testConfigPath() {
        return getClass().getClassLoader()
                .getResource("test-application.properties")
                .getPath();
    }

    @Test
    void successfulLoad_isNotAlteredByAudit() {
        ConfigurationService service = new ConfigurationService();
        service.loadConfiguration(testConfigPath());

        assertEquals(Language.EN, service.getDefaultLanguage());
    }

    @Test
    void failedLoad_stillPropagates() {
        ConfigurationService service = new ConfigurationService();

        assertThrows(ServiceException.class,
                () -> service.loadConfiguration("config/does-not-exist.properties"));
    }
}
