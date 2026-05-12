package com.yebur.backendorderly.appconfig;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Stub — implemented after AppConfigService is created in Plan 12-02")
class AppConfigServiceTest {

    @Test
    void getOrCreateConfig_returnsDefaultsWhenEmpty() {}

    @Test
    void updateConfig_setsAllFields() {}

    @Test
    void updateConfig_nullPassword_keepsPreviousEncryptedPassword() {}
}
