package org.eventplanner.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CryptoTest {

    @Test
    public void testEncryptDecrypt() throws Exception {
        var plainText = "secret text";
        var key = "secret-encryption-key";
        var salt = "secret-encryption-salt";

        var crypto = new Crypto(key, salt);

        var encryptedText = crypto.encrypt(plainText);
        assertNotEquals(encryptedText, plainText);

        var decryptedText = crypto.decrypt(encryptedText);
        assertEquals(decryptedText, plainText);
    }

    @Test
    public void testDifferentKeysHavingDifferentEncryptedValues() throws Exception {
        var plainText = "secret text";
        var key1 = "secret-encryption-key-1";
        var key2 = "secret-encryption-key-2";
        var salt = "secret-encryption-salt";

        var encryptedText1 = new Crypto(key1, salt).encrypt(plainText);
        var encryptedText2 = new Crypto(key2, salt).encrypt(plainText);
        assertNotEquals(encryptedText1, encryptedText2);
    }
}
