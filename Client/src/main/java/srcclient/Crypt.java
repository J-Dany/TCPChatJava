package srcclient;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

public class Crypt 
{
    private static KeyPairGenerator keyPairGenerator;

    public static KeyPair getKeyPairGenerator()
    {
        return keyPairGenerator.generateKeyPair();
    }

    public static String encrypt (String content, Key pubKey)
    {
        try
        {
            byte[] buff = content.getBytes();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] contentBuff = cipher.doFinal(buff);
    
            return Base64.getEncoder().encodeToString(contentBuff);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static String decrypt (String content, Key privKey)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privKey);
            byte[] contentBuff = Base64.getDecoder().decode(content.getBytes());
            byte[] buffDecrypted = cipher.doFinal(contentBuff);

            return new String(Base64.getDecoder().decode(buffDecrypted));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static String encodeKey (Key key)
    {
        byte[] buff = key.getEncoded();
        return Base64.getEncoder().encodeToString(buff);
    }

    public static PublicKey decodePublicKey (String key)
    {
        try
        {
            byte[] buff = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(buff);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    
            return keyFactory.generatePublic(spec);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static PrivateKey decodePrivateKey(String key)
    {
        try
        {
            byte[] buff = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buff);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            
            return keyFactory.generatePrivate(keySpec);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static void initialize()
    {
        try
        {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");

            keyPairGenerator.initialize(3072);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}