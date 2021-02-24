package test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import util.crypto.AES256Cipher;

public class CryptoTest {

	public static void main(String[] args) {
		
		String enPinCode = "scU5ZeX4Q%2B1V0UG2XgGOWg%3D%3D%0A";
		
		try {
			String urlPinCode = URLDecoder.decode(enPinCode, "UTF-8");
			String pincode = deCrypt(urlPinCode);
			System.out.println("pinCode: " + pincode);
		} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			;
			System.out.println("error: " + e.getMessage().toString());
		}

		
		String userAuth = "100";
		System.out.println("userAuth: " + userAuth);
		char[] array = userAuth.toCharArray();
		array[1] = '1';
		String updatedUserAuth = new String(array);
		
		System.out.println("updatedUserAuth: " + updatedUserAuth);
		
		String token = "{\"token\":[{\"date\":\"2021-02-22 14:59:41.682\",\"email\":\"jypjun12@gmail.com\"}]}";
		
		try {
			String enToken = enCrypt(token);
			System.out.println("enToken: " + enToken);
			String deToken = deCrypt("f5ukzCP94PLdxz1AMAzBcxqD5ae1Eg924qKpWV2oIICcOOtIV+C/zFmSUaz1G3ZHQhCPf5pna5ex 9WpWuQfxdVyfkDqD+R8GSRjsHdyT2qqBbXU+Fgyjd48JA1dQewkd");
			System.out.println("deToken: " + deToken);
			JSONParser jsonParser = new JSONParser();
			JSONObject json = (JSONObject) jsonParser.parse(deToken);
			
			JSONArray jsArray = new JSONArray();
			jsArray = (JSONArray) json.get("token");
			
			System.out.println("json: " + json);
			System.out.println("jsArray: " + jsArray);
			
			String user_email = ((JSONObject) jsArray.get(0)).get("user_email").toString();
			String created_at = ((JSONObject) jsArray.get(0)).get("created_at").toString();
			
			System.out.println("user_email: " + user_email);
			System.out.println("created_at: " + created_at);
			
			
		} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("enToken error: " + e.getMessage().toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	 
    private static String deCrypt(String str) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    	AES256Cipher a256 = AES256Cipher.getInstance();
    	return a256.AES_Decode(str);
    }
    
    private static String enCrypt(String str) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    	AES256Cipher a256 = AES256Cipher.getInstance();
    	return a256.AES_Encode(str);
    }
	
}
