package com.yebur.backendorderly.appconfig;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Stub — implemented after AppConfigController is created in Plan 12-02")
class AppConfigControllerTest {

    @Test
    void getConfig_omitsSmtpPassword() {}

    @Test
    void putConfig_nullSmtpPassword_keepsExistingEncryptedPassword() {}

    @Test
    void postTestSmtp_returnsSuccessOrFailureShape() {}
}
