package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.auth;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class EncryptionUtil {
    public static byte[] generateVerifyToken() {
        byte[] token = new byte[4];
        new SecureRandom().nextBytes(token);
        return token;
    }

    public static Cipher createAESCipher(int mode, byte[] sharedSecret) throws GeneralSecurityException {
        SecretKey key = new SecretKeySpec(sharedSecret, "AES");
        IvParameterSpec iv = new IvParameterSpec(sharedSecret);
        Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
        cipher.init(mode, key, iv);
        return cipher;
    }
}
