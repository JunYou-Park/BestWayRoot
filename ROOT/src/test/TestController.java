package test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import api.user.TokenDao;
import crypto.AES256Cipher;


@WebServlet({"/test/api/*"})
public class TestController extends HttpServlet {
	
    private TokenDao tokenDao;
    private JSONObject json = new JSONObject();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public TestController() {}

    public void init() throws ServletException {
        this.tokenDao= new TokenDao(); 
    }
    
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	String action = req.getPathInfo();
        PrintWriter out = resp.getWriter();
        System.out.println(action);
     
        if (action.equals("/user/get.do")) {
        	String cryptoAccessToken = req.getParameter("access_token");
        	out.println(tokenDao.checkAccessToken(cryptoAccessToken));
			
        } 
        
        else if(action.contentEquals("/user/update_user_access_token.do")) {
        	String userEmail = req.getParameter("user_email");
        	out.println(tokenDao.updateAccessToken(userEmail));
        }
    }

    private String deCrypt(String str) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    	AES256Cipher a256 = AES256Cipher.getInstance();
    	return a256.AES_Decode(str);
    }

}
