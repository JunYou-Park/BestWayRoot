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

import util.crypto.AES256Cipher;
import util.db.DBConnector;

public class AccessTokenFx {
	
	private TokenFx tokenFx = new TokenFx();
	private DBConnector dbConnector = DBConnector.getInstance();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// 접근토큰의 만료 시간을 확인하기 위한 코드
    // 1. 토큰 생성일을 입력 받음
    // 2-1. (현재 시간 - 생성 시간)이 30분 보다 큰 경우 -> false를 출력
    // 2-2. (현재 시간 - 생성 시간)이 30분 보다 작은 경우 -> true를 출력
    public boolean checkAccessToken(String createdAt)  {
    	boolean result = false;
		try {
			Date tokenCreateDate = dateFormat.parse(createdAt);
			Date now = new Date();
	    	if(now.getTime() - tokenCreateDate.getTime() > (1000L * 60L * 30L)) { // accessToken expired (over 30 min)
	    		System.out.println("accessToken expired (over 30 min)");
			}
			else {
				System.out.println("accessToken valid");
				result = true;
			}
		} catch (ParseException e) {
			System.out.println("checkAccessToken error: " + e.getMessage().toString());
		}
    	return result;
    }
	
    
 // 접근토큰이 있는지 확인하는 기능
    public boolean isValidAccessToken(String userEmail, String cryptoAccessToken) {
    	boolean result = false;
    	Connection conn = null;
    	PreparedStatement pstm = null;
    	ResultSet rs = null;
    	String query = "SELECT user_access_token FROM sign_info WHERE user_email = ?";
    	
    	try {
    		conn = dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, userEmail);
    		rs = pstm.executeQuery();
    		if(rs.next()) {
    			String userAccessToken = rs.getString("user_access_token");
    			result = userAccessToken.equals(cryptoAccessToken);
    		}
    	}
    	catch(SQLException e) {
    		System.out.println("isValidAccessToken sql error: " + e.getMessage().toString());
    	}
    	catch(Exception e) {
    		System.out.println("isValidAccessToken error: " + e.getMessage().toString());
    	}
    	finally {
    		this.dbConnector.freeConnection(conn, pstm, rs);
    	}
    	return result;
    }
    
    // 접근토큰을 불러오는 기능
    public String getAccessToken(String userEmail) {
    	String accessToken = "";
    	Connection conn = null;
    	PreparedStatement pstm = null;
    	ResultSet rs = null;
    	
    	String query = "SELECT user_access_token FROM sign_info WHERE user_email = ?";
    	try {
    		conn = dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, userEmail);
    		rs = pstm.executeQuery();
    		while(rs.next()) {
    			accessToken = rs.getString("user_access_token");
    		}
    	}
    	catch(SQLException e) {
    		System.out.println("getAccessToken sql error: " + e.getMessage().toString());
    	}
    	catch(Exception e) {
    		System.out.println("getAccessToken error: " + e.getMessage().toString());
    	}
    	finally {
    		this.dbConnector.freeConnection(conn, pstm, rs);
    	}
    	return accessToken;
    }
    
// 	접근토큰을 갱신해주는 기능
    public String updateAccessToken(String userEmail) {
    	Connection conn = null;
    	PreparedStatement pstm = null;
    	String query = "UPDATE sign_info SET user_access_token = ? WHERE user_email = ?";
    	
    	// 접근토큰을 생성
    	String accessToken = tokenFx.generateToken(userEmail);
    	
    	// 토큰이 정상적으로 생성된 경우
    	if(accessToken.length()>0) {
    		try {
        		conn = dbConnector.getConnection();
        		pstm = conn.prepareStatement(query);
        		pstm.setString(1, accessToken);
        		pstm.setString(2, userEmail);
        		
        		// 접근토큰 갱신이 제대로 진행되지 않은 경우 ""로 초기화
        		if(pstm.executeUpdate() != 1){
        			accessToken = "";
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
    	
    	return accessToken;
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
