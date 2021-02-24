package api.user;

import java.io.UnsupportedEncodingException;
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

import crypto.AES256Cipher;
import db.DBConnector;

public class RefreshTokenFx {

	private TokenFx tokenFx = new TokenFx();
	private DBConnector dbConnector = DBConnector.getInstance();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 갱신토큰의 만료 시간을 확인하기 위한 코드
    // 1. 토큰 생성일을 입력 받음
    // 2-1. (현재 시간 - 생성 시간)이 30일 보다 큰 경우 -> false를 출력
    // 2-2. (현재 시간 - 생성 시간)이 30일 보다 작은 경우 -> true를 출력
    public boolean checkRefreshToken(String createdAt) {
    	boolean result = false;
    	try {
    		Date tokenCreateDate = dateFormat.parse(createdAt);
    		Date now = new Date();
    		if(now.getTime() - tokenCreateDate.getTime() > (1000L * 60L * 60L * 24L * 30L)) { // refreshToken expired (over 30 days)
    			System.out.println("refreshToken expired (over 30 days)");
    		}
    		else {
    			System.out.println("refreshToken valid");
    			result = true;
    		}
    	}
    	catch (ParseException e) {
 			System.out.println("checkRefreshToken error: " + e.getMessage().toString());
 		}
    	return result;
    }

   
 // 갱신토큰이 있는지 확인하는 기능
    public boolean isValidRefreshToken(String userEmail, String cryptoRefreshToken) {
    	boolean result = false;
    	Connection conn = null;
    	PreparedStatement pstm = null;
    	ResultSet rs = null;
    	String query = "SELECT user_refresh_token FROM sign_info WHERE user_email = ?";
    	
    	try {
    		conn = dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, userEmail);
    		rs = pstm.executeQuery();
    		if(rs.next()) {
    			result = rs.getString("user_refresh_token").equals(cryptoRefreshToken);
    		}
    	}
    	catch(SQLException e) {
    		System.out.println("isValidRefreshToken sql error: " + e.getMessage().toString());
    	}
    	catch(Exception e) {
    		System.out.println("isValidRefreshToken error: " + e.getMessage().toString());
    	}
    	finally {
    		this.dbConnector.freeConnection(conn, pstm, rs);
    	}
    	return result;
    }
    
    
 // 갱신토큰을 불러오는 기능
    public String getRefreshToken(String userEmail) {
    	String refreshToken = "";
    	Connection conn = null;
    	PreparedStatement pstm = null;
    	ResultSet rs = null;
    	
    	String query = "SELECT user_refresh_token FROM sign_info WHERE user_email = ?";
    	try {
    		conn = dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, userEmail);
    		rs = pstm.executeQuery();
    		while(rs.next()) {
    			refreshToken = rs.getString("user_refresh_token");
    		}
    	}
    	catch(SQLException e) {
    		System.out.println("getRefreshToken sql error: " + e.getMessage().toString());
    	}
    	catch(Exception e) {
    		System.out.println("getRefreshToken error: " + e.getMessage().toString());
    	}
    	finally {
    		this.dbConnector.freeConnection(conn, pstm, rs);
    	}
    	return refreshToken;
    }
    
    
// 	갱신토큰을 갱신해주는 기능
    public String updateRefreshToken(String userEmail) {
    	Connection conn = null;
    	PreparedStatement pstm = null;
    	String query = "UPDATE sign_info SET user_refresh_token = ? WHERE user_email = ?";
    	
    	// 갱신토큰을 생성
    	String refreshToken = tokenFx.generateToken(userEmail);
    	if(refreshToken.length()>0) {
    		try {
        		conn = dbConnector.getConnection();
        		pstm = conn.prepareStatement(query);
        		pstm.setString(1, refreshToken);
        		pstm.setString(2, userEmail);
        		
        		// 갱신토큰 갱신이 제대로 진행되지 않은 경우 ""로 초기화
        		if(pstm.executeUpdate() != 1){
        			refreshToken = "";
        		}
        	}
        	catch(SQLException e) {
        		System.out.println("getRefreshToken sql error: " + e.getMessage().toString());
        	}
        	catch(Exception e) {
        		System.out.println("getRefreshToken error: " + e.getMessage().toString());
        	}
        	finally {
        		this.dbConnector.freeConnection(conn, pstm);
        	}
    	}
    	
    	return refreshToken;
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
