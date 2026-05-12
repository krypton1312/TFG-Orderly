package com.yebur.backendorderly.appconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class AesEncryptionServiceTest {

    private AesEncryptionService service;

    @BeforeEach
    void setUp() {
        service = new AesEncryptionService();
        ReflectionTestUtils.setField(service, "encryptionKeyStr", "test-encryption-key-for-unit-tests");
        service.init();
    }

    @Test
    void encryptThenDecrypt_roundtrip() {
        String plaintext = "my_smtp_p@ssw0rd!";
        String encrypted = service.encrypt(plaintext);
        assertThat(encrypted).isNotNull().isNotEqualTo(plaintext);
        assertThat(service.decrypt(encrypted)).isEqualTo(plaintext);
    }

    @Test
    void encrypt_differentCallsProduceDifferentCiphertexts() {
        String enc1 = service.encrypt("same_password");
        String enc2 = service.encrypt("same_password");
        assertThat(enc1).isNotEqualTo(enc2);
    }

    @Test
    void encrypt_null_returnsNull() {
        assertThat(service.encrypt(null)).isNull();
    }

    @Test
    void decrypt_null_returnsNull() {
        assertThat(service.decrypt(null)).isNull();
    }
}
