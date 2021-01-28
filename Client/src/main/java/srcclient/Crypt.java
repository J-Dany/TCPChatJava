package srcclient;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

public class Crypt 
{
    private static KeyPairGenerator keyPairGenerator;

    private static KeyPair keys;

    public static String encrypt (String content, Key pubKey)
    {
        try
        {
            byte[] buff = content.getBytes();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] contentBuff = cipher.doFinal(buff);
            byte[] codedContent = Base64.getEncoder().encode(contentBuff);
    
            return new String (codedContent, 0, codedContent.length, "UTF-8");
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
            byte[] buffDecrypted = cipher.update(contentBuff);

            return new String(buffDecrypted, 0, buffDecrypted.length, "UTF-8");
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

    /**
     * Ritorna la chiave privata
     * @return PrivateKey 
     */
    public static PrivateKey getPrivateKey()
    {
        return keys.getPrivate();
    }

    /**
     * Ritorna la chiave pubblica
     * @return PublicKey
     */
    public static PublicKey getPublicKey ()
    {
        return keys.getPublic();
    }

    /**
     * Diversamente da {@code getPublicKey()} questa
     * funziona ritorna la chiave pubblica codificata
     * in Base64
     * @return String, la chiave pubblica codificata in Base64
     */
    public static String getCodPubKey()
    {
        return Base64.getEncoder().encodeToString(keys.getPublic().getEncoded());
    }

    /**
     * Inizializza il generatore di chiavi e la
     * coppia di chiavi
     */
    public static void initialize()
    {
        try
        {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");

            keyPairGenerator.initialize(4096);

            keys = keyPairGenerator.genKeyPair();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}