package util.crypto;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;


public class AES256Cipher {
	private static volatile AES256Cipher INSTANCE;

    final static String secretKey = "comairetefacruomyapplicationbest"; //32byte
    static byte[] IV = new byte[] {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};//16byte

    public static AES256Cipher getInstance() {
        if (INSTANCE == null) {
            synchronized (AES256Cipher.class) {
                if (INSTANCE == null)
                    INSTANCE = new AES256Cipher();
            }
        }
        return INSTANCE;
    }

    private AES256Cipher() {}

    //암호화
    public static String AES_Encode(String str) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    	String transformation = "AES/CBC/PKCS5Padding";
    	byte[] textByte = str.getBytes("UTF-8");
    	IvParameterSpec ivSpec = new IvParameterSpec(IV);
    	SecretKeySpec newKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
    	Cipher cipher = Cipher.getInstance(transformation);
    	cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
    	return Base64.encodeToString(cipher.doFinal(textByte), 0);
    }

    //복호화
    public static String AES_Decode(String str) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    	String transformation = "AES/CBC/PKCS5Padding";
    	
    	byte[] textByte = Base64.decode(str, 0);
    	IvParameterSpec ivSpec = new IvParameterSpec(IV);
    	SecretKeySpec newKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
    	
    	Cipher cipher = Cipher.getInstance(transformation);
    	cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
    	return new String(cipher.doFinal(textByte), Charset.forName("UTF-8"));
    }
}
