package api.user;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import api.token.TokenFx;
import util.crypto.AES256Cipher;
import util.db.DBConnector;

public class PinCodeFx {

	private DBConnector dbConnector = DBConnector.getInstance();
    
    // 유저의 핀 코드 검증
    public boolean authUserPinCode(JSONObject userJson) {
    	String userEmail = userJson.get("user_email").toString();
    	String userPinCode = userJson.get("user_pin_code").toString();
    	
    	boolean result = false;
    	Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        String query = "SELECT user_pin_code FROM user_info WHERE user_email = ?";
        
    	try {
    		conn = this.dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, userEmail);
    		rs = pstm.executeQuery();
    		if(rs.next()) {
				String pinCodeHash = rs.getString("user_pin_code");
				result = BCrypt.checkpw(userPinCode, pinCodeHash);
    		}
    	}
    	catch(SQLException e) {
    		System.out.println("authUserPinCode: " + e.getMessage().toString());
    	}
    	catch(Exception e) {
    		System.out.println("authUserPinCode: " + e.getMessage().toString());
    		
    	}
    	finally {
    		this.dbConnector.freeConnection(conn, pstm, rs);
    	}
    	return result;
    }
    
    
 // 유저의 핀 코드 업데이트
    public boolean updateUserPinCode(JSONObject userJson) {
    	String userEmail = userJson.get("user_email").toString();
    	String userPinCode = userJson.get("user_pin_code").toString();
    	
    	System.out.println("updateUserPinCode");
    	Connection conn = null;
        PreparedStatement pstm = null;
    	
    	boolean result = false;
    	
    	String query = "UPDATE user_info SET user_pin_code = ? WHERE user_email = ?";
    	try {
    		conn = this.dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, userPinCode);
    		pstm.setString(2, userEmail);
    		result = pstm.executeUpdate() == 1;
    	}
    	catch(SQLException e) {
    		System.out.println("updateUserPinCode: " + e.getMessage().toString());
    	}
    	catch(Exception e) {
    		System.out.println("updateUserPinCode: " + e.getMessage().toString());
    	}
    	return result;
    }
	
}
