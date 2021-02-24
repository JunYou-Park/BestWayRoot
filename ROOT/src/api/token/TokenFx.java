package api.token;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import util.crypto.AES256Cipher;
import util.db.DBConnector;

public class TokenFx {
	 
	private DBConnector dbConnector = DBConnector.getInstance();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	// 토큰을 생성해주는 기능
	public String generateToken(String userEmail) {
		
		Date nowTime = new Date();
		String now = dateFormat.format(nowTime);
		
		String token = "";
		
		try {
			token = enCrypt(userEmail+":"+now);
		} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("generateToken error: " + e.getMessage().toString());
		}
		return token;
	}
	
    // 토큰 생성시간을 꺼내기 위한 코드
    // 1. 토큰을 입력 받음
    // 2. 토큰을 디크립트하고 유저이메일과 생성일을 분리
    // 3. 생성일을 Date타입으로 바꿈
    // 4-1. 타입 변환이 성공한 경우 -> user_email, created_at이라는 값에 넣어서 jsonObject로 출력
    // 4-2. 타입 변환이 실패한 경우  -> 비어있는 jsonObject를 출력
    public JSONObject decodeAndObjToken(String cryptoToken) {
    	JSONObject json = new JSONObject();
		try {
			String token = deCrypt(cryptoToken);
			
			String userEmail = token.substring(0, token.indexOf(":"));
			String createdAt = token.substring(token.indexOf(":")+1);
			
			json.put("user_email", userEmail);
			json.put("created_at", createdAt);
			
		} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("getTokenData error: " + e.getMessage().toString());
		}
		
		return json;
		
    }
	
    
    private String deCrypt(String str) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    	AES256Cipher a256 = AES256Cipher.getInstance();
    	return a256.AES_Decode(str);
    }
    
    private String enCrypt(String str) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    	AES256Cipher a256 = AES256Cipher.getInstance();
    	return a256.AES_Encode(str);
    }
}
