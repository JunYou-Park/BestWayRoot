package api;

import api.event.EventDao;
import api.fcm.FcmDao;
import api.menu.MenuDao;
import api.model.BannerVo;
import api.model.MenuVo;
import api.model.NoticeVo;
import api.model.UserVo;
import api.ticket.TicketDao;
import api.user.UserDao;
import crypto.AES256Cipher;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

// 통신 구문 참조
// https://sanghaklee.tistory.com/61


// status 200번대 => 서버와 통신 OK
// status: 200 -> 받아올 데이터는 없는 경우
// status: 201 -> 데이터를 받는 경우
// status: 202 -> 데이터를 받고 아직 서버와 통신이 남은 경우
// status: 204 -> 세션이 만료된 상태

// status 400번대 => 서버와 통신 OK, 유효하지 않는 상태
// status: 400 -> 클라이언트의 요청이 유효하지 않음 (받아올 데이터가 없음)

// status: 500 -> 서버 자체에서 오류 발생

@WebServlet({"/api/*"})
public class Controller extends HttpServlet {
    private UserDao userDao;
    private FcmDao fcmDao;
    private EventDao eventDao;
    private MenuDao menuDao;
    private TicketDao ticketDao;
    private JSONObject json = new JSONObject();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public Controller() {}

    public void init() throws ServletException {
        this.userDao = new UserDao();
        this.fcmDao = new FcmDao();
        this.eventDao = new EventDao();
        this.menuDao = new MenuDao();
        this.ticketDao = new TicketDao();
    }

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	String action = req.getPathInfo();
        PrintWriter out = resp.getWriter();
        this.json.clear();
        System.out.println(action);
        String toDayMenuDate;
        String deEmail;
        
        System.out.println(action);
        
        if (action.equals("/user/sign_in.do")) {
            String email = req.getParameter("email");
            String pw = req.getParameter("pw");
            out.println(this.userDao.signIn(email, pw));
           
        } 
        else if (action.contentEquals("/user/sign_up.do")) {
        	String fullName = req.getParameter("fullName");
            String email = req.getParameter("email");
            String pw = req.getParameter("pw");
            long createAt = Long.parseLong(req.getParameter("createAt"));
            String phoneNumber = req.getParameter("phone_number");
            Timestamp timestamp = new Timestamp(createAt);
            try {
            	String refreshToken = enCrypt(email+ ":" + timestamp.toString());
	            String pwHashed = BCrypt.hashpw(pw, BCrypt.gensalt(15));
	            
	            UserVo userVo = new UserVo(email, pwHashed, fullName, phoneNumber, timestamp);
	            if(this.userDao.dupVerifyEmail(email)) {
	            	 if (this.userDao.signUp(userVo, refreshToken)) {
	                     this.json.put("status", "200");
	                     this.json.put("msg", "회원가입에 성공하였습니다.");
	                 }
	                 else {
	                 	this.json.put("status", "400");
	                 	this.json.put("msg", "회원가입에 실패하였습니다.");
	                 }
	            }
	            else {
	            	this.json.put("status", "400");
	             	this.json.put("msg", "사용중인 아이디입니다.");
	            }
			} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException
					| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
					| BadPaddingException e1) {
				this.json.put("status", "400");
             	this.json.put("msg", e1.getMessage().toString());
			}
            
            out.println(this.json);
           
        }
        else if(action.contentEquals("/user/dup_verify_email.do")) {
        	String email = req.getParameter("email");
        	this.json.put("status", this.userDao.dupVerifyEmail(email));
        	out.println(this.json);
        }
        else if(action.contentEquals("/user/authentication.do")) {
        	String enEmail = req.getParameter("user");
        	try {
				deEmail = deCrypt(enEmail);
				// 유저 이메일이 아직 인증이 안된 경우
    			if(!userDao.authenticationEmailCheck(deEmail)) { 
    				// 유저 이메일이 인증되서 업데이트된 경우
    				if(userDao.authenticationEmail(deEmail)==1) {
            			out.print("<html><head><script>alert('인증에 성공했습니다.'); window.open('', '_self', ''); window.close(); </script></head></html>");
            		}
    				// 유저 이메일이 인증되서 업데이트가 되지 않은 경우
            		else {
            			out.print("<html><head><script>alert('인증에 실패했습니다.'); window.open('', '_self', ''); window.close(); </script></head></html>");
            		}
    			}
    			// 유저 이메일이 아직 인증이 안된 경우
    			else {
            		out.print("<html><head><script>alert('페이지가 만료되었습니다.'); window.open('', '_self', ''); window.close(); </script></head></html>");
            	}
			} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException
					| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
					| BadPaddingException e) {
				out.print("<html><head><script>alert('" + e.getMessage().toString() +"'); window.open('', '_self', ''); window.close(); </script></head></html>");
			}
        	   
        }
        
        else if(action.contentEquals("/fcm/get_notice.do")) {        
        	
        	ArrayList<NoticeVo> list = this.fcmDao.getNotifications();
        	JSONArray jsonArray = new JSONArray();
        	for(NoticeVo noticeVo: list) {
        		JSONObject json = new JSONObject();
        		json.put("noti_id", noticeVo.getNoti_id());
        		json.put("noti_type", noticeVo.getNoti_type());
        		json.put("noti_title", noticeVo.getNoti_title());
        		json.put("noti_content", noticeVo.getNoti_content());
        		json.put("noti_create_at", this.dateFormat.format(noticeVo.getNoti_create_at()));
        		jsonArray.add(json);
        	}
        	this.json.put("status", "201");
        	this.json.put("msg", "success");
        	this.json.put("data", jsonArray);
        	out.println(this.json);
        }
        
        else if(action.contentEquals("/event/get_banner.do")){
        	ArrayList<BannerVo> list = eventDao.getBanners();
        	JSONArray jsonArray = new JSONArray();
        	for(BannerVo bannerVo: list) {
        		JSONObject json = new JSONObject();
        		json.put("banner_id", bannerVo.getBannerId());
        		json.put("banner_thumb", bannerVo.getBannerThumb());
        		json.put("banner_create_at", this.dateFormat.format(bannerVo.getBannerCreateAt()));
          		jsonArray.add(json);
        	}
        	this.json.put("status", "201");
        	this.json.put("msg", "success");
        	this.json.put("data", jsonArray);
        	out.println(this.json);
        }
        else if (action.contentEquals("/menu/get_today_menu.do")) {
            toDayMenuDate = req.getParameter("today_menu_date");
            Date parsedDate = new Date();

            try {
                parsedDate = this.dateFormat.parse(toDayMenuDate);
            } catch (Exception var13) {
                System.out.println(var13.getMessage().toString());
            }

            ArrayList<MenuVo> list = this.menuDao.getToDayMenuList(parsedDate);
            JSONArray jsonArray = new JSONArray();
            Iterator var32 = list.iterator();

            while(var32.hasNext()) {
                MenuVo menuVo = (MenuVo)var32.next();
                JSONObject json = new JSONObject();
                json.put("menu_id", menuVo.getMenu_id());
                json.put("menu_name", menuVo.getMenu_name());
                json.put("menu_summary", menuVo.getMenu_summary());
                json.put("menu_price", menuVo.getMenu_price());
                json.put("menu_thumb", menuVo.getMenu_thumb());
                json.put("menu_create_at", this.dateFormat.format(menuVo.getMenu_create_at()));
                jsonArray.add(json);
            }
            this.json.put("status", "201");
            this.json.put("msg", "success");
            this.json.put("data", jsonArray);
            out.println(this.json);
        }
        // 다른 기기에서 로그인한 경우 접근토큰을 바꿔준다.
        else if(action.contentEquals("/user/update_access_token.do")) {
        	String cryptoAccessToken = req.getParameter("access_token");
			try {
				String accessToken = deCrypt(cryptoAccessToken);
				System.out.println("accessToken: " + accessToken);
				String userEmail = accessToken.substring(0, accessToken.indexOf(":"));
	        	if(userDao.updateToken(userEmail, "user_access_token", cryptoAccessToken)) {
	        		this.json.put("status", "200");
	        		this.json.put("msg", "접근토큰이 갱신되었습니다.");
	        	}
			} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException
					| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
					| BadPaddingException e) {
				this.json.put("status", "400");
	            this.json.put("msg", e.getMessage().toString());
			}
			out.println(this.json);
        }
       
        // 로그아웃 API
        else if(action.contentEquals("/user/logout.do")) {
        	String cryptoAccessToken = req.getParameter("access_token");
			try {
				String accessToken = deCrypt(cryptoAccessToken);
				System.out.println("accessToken: " + accessToken);
				String userEmail = accessToken.substring(0, accessToken.indexOf(":"));
				
				// 로그아웃한 경우 접근토큰을 null로 입력한다.
				if(userDao.updateToken(userEmail, "user_access_token", "")) {
	        		this.json.put("status", "200");
	        		this.json.put("msg", "로그아웃되었습니다.");
	        	}
			} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException
					| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
					| BadPaddingException e) {
				this.json.put("status", "400");
	            this.json.put("msg", e.getMessage().toString());
			}
			out.println(this.json);
        }
        // 회원탈퇴 API
        else if(action.contentEquals("/user/withdrawal.do")) {
        	String cryptoAccessToken = req.getParameter("access_token");
        	// 접근 토큰 존재하는지 확인
        	// 접근 토큰이 존재
        	if(userDao.certAccessToken(cryptoAccessToken)) {
        		try {
					String accessToken = deCrypt(cryptoAccessToken);
					String userEmail = accessToken.substring(0, accessToken.indexOf(":"));
					if(userDao.withdrawal(userEmail)) {
						this.json.put("status", "200");
						this.json.put("msg", "회원탈퇴가 처리되었습니다.");
					}
		        	
				} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException
						| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
						| BadPaddingException e) {
					this.json.put("status", "400");
		            this.json.put("msg", e.getMessage().toString());
				}
        	}
        	// 접근 토큰이 존재하지 않는 경우 -> 다른 기기에서 로그인을 한 상태
        	else {
        		this.json.put("status", "204");
	            this.json.put("msg", "다른 기기에서 로그인 되어 세션이 만료되었습니다.");
        	}
        	
        	out.println(this.json);
        	
        }
        
        // 유저의 식권 양을 가져오는 API
        else if (action.contentEquals("/user/get_user_ticket.do")) {
        	String cryptoAccessToken = req.getParameter("access_token");
        	// 접근 토큰 존재하는지 확인
        	// 접근 토큰이 존재
        	if(userDao.certAccessToken(cryptoAccessToken)) {
        		// 접근 토큰이 유효한지 확인
        		Date accessTokenCreateDate = userDao.getTokenCreateTime(cryptoAccessToken);
        		// 접근 토큰이 없는 경우
        		if(accessTokenCreateDate==null) {
        			this.json.put("status", "400");
    	            this.json.put("msg", "접근토큰이 유효하지 않습니다.");
        		}
        		else {
        			try {
    					String accessToken = deCrypt(cryptoAccessToken);
    					String userEmail = accessToken.substring(0, accessToken.indexOf(":"));
    					
    					// 접근 토큰이 유효한 경우
            			if(userDao.checkAccessToken(accessTokenCreateDate)) {
            				// 이메일을 이용해 식권의 수량을 가져온다.
            				this.json = ticketDao.getUserTicket(userEmail);
            			}
            			// 접근 토큰이 유효하지 않은 경우 -> 갱신해줘야 함
            			else {
            				Timestamp nowTimestamp = new Timestamp(new Date().getTime());
            				// 아이디(이메일)값과 현재 시간(타임스템프)을 ":"를 기준으로 붙이고 암호화
                			String newCryptoAccessToken = enCrypt(userEmail+":"+nowTimestamp.toString());
                			
                			// 접근토큰 업데이트에 성공한 경우
                			if(userDao.updateToken(userEmail, "user_access_token", newCryptoAccessToken)) {
                				this.json = ticketDao.getUserTicket(userEmail);
                			}
                			// 접근토큰 업데이트에 실패한 경우
                			else {
                				this.json.put("status", "400");
            		            this.json.put("msg", "접근토큰을 갱신하지 못했습니다.");
                			}
            			}
    		        	
    				} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException
    						| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
    						| BadPaddingException e) {
    					this.json.put("status", "400");
    		            this.json.put("msg", e.getMessage().toString());
    				}
        			
        		}
        	}
        	// 접근 토큰이 존재하지 않는 경우 -> 다른 기기에서 로그인을 한 상태
        	else {
        		this.json.put("status", "204");
	            this.json.put("msg", "다른 기기에서 로그인 되어 세션이 만료되었습니다.");
        	}
        	out.println(this.json);
        }
        
        
     // 유저의 핀 코드 확인하는 API
        else if (action.contentEquals("/user/auth_user_pin_code.do")) {
        	String enPinCode = req.getParameter("en_pin_code");
        	String cryptoAccessToken = req.getParameter("access_token");
        	
        	
        	try {
        		String urlPinCode = URLDecoder.decode(enPinCode, "UTF-8");
				String pinCode = deCrypt(urlPinCode);
				
				// 접근 토큰 존재하는지 확인
	        	// 접근 토큰이 존재
	        	if(userDao.certAccessToken(cryptoAccessToken)) {
	        		// 접근 토큰이 유효한지 확인
	        		Date accessTokenCreateDate = userDao.getTokenCreateTime(cryptoAccessToken);
	        		// 접근 토큰이 없는 경우
	        		if(accessTokenCreateDate==null) {
	        			this.json.put("status", "400");
	    	            this.json.put("msg", "접근토큰이 유효하지 않습니다.");
	        		}
	        		else {
	        			try {
	    					String accessToken = deCrypt(cryptoAccessToken);
	    					String userEmail = accessToken.substring(0, accessToken.indexOf(":"));
	    					
	    					// 접근 토큰이 유효한 경우
	            			if(userDao.checkAccessToken(accessTokenCreateDate)) {
	            				// 유저의 핀 코드인증 상태를 리턴
	            				if(userDao.authUserPinCode(userEmail, pinCode)) {
	            					this.json.put("status", "200");
	        	    	            this.json.put("msg", "핀 코드가 인증되었습니다.");
	            				}
	            				else {
	            					this.json.put("status", "400");
	        	    	            this.json.put("msg", "잘못된 핀 코드입니다.");
	            				}
	            			}
	            			// 접근 토큰이 유효하지 않은 경우 -> 갱신해줘야 함
	            			else {
	            				Timestamp nowTimestamp = new Timestamp(new Date().getTime());
	            				// 아이디(이메일)값과 현재 시간(타임스템프)을 ":"를 기준으로 붙이고 암호화
	                			String newCryptoAccessToken = enCrypt(userEmail+":"+nowTimestamp.toString());
	                			
	                			// 접근토큰 업데이트에 성공한 경우
	                			if(userDao.updateToken(userEmail, "user_access_token", newCryptoAccessToken)) {
	                				// 유저의 핀 코드인증 상태를 리턴
	                				if(userDao.authUserPinCode(userEmail, pinCode)) {
		            					this.json.put("status", "200");
		        	    	            this.json.put("msg", "핀 코드가 인증되었습니다.");
		            				}
	                				else {
		            					this.json.put("status", "400");
		        	    	            this.json.put("msg", "잘못된 핀 코드입니다.");
		            				}
	                			}
	                			// 접근토큰 업데이트에 실패한 경우
	                			else {
	                				this.json.put("status", "400");
	            		            this.json.put("msg", "접근토큰을 갱신하지 못했습니다.");
	                			}
	            			}
	    		        	
	    				} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException
	    						| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
	    						| BadPaddingException e) {
	    					this.json.put("status", "400");
	    		            this.json.put("msg", e.getMessage().toString());
	    				}
	        			
	        		}
	        	}
	        	// 접근 토큰이 존재하지 않는 경우 -> 다른 기기에서 로그인을 한 상태
	        	else {
	        		this.json.put("status", "204");
		            this.json.put("msg", "다른 기기에서 로그인 되어 세션이 만료되었습니다.");
	        	}
				
			} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException
					| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
					| BadPaddingException e1) {
				this.json.put("status", "400");
	            this.json.put("msg", e1.getMessage().toString());
			}
        
        	out.println(this.json);
        }
        
     // 유저의 핀 코드를 생성하는 API
        else if (action.contentEquals("/user/create_user_pin_code.do")) {
        	String enPinCode = req.getParameter("en_pin_code");
        	String cryptoAccessToken = req.getParameter("access_token");
        	
			try {
				String urlPinCode = URLDecoder.decode(enPinCode, "UTF-8");
				String pinCode = deCrypt(urlPinCode);
				String pinCodeHashed = BCrypt.hashpw(pinCode, BCrypt.gensalt(15));
				
	        	// 접근 토큰 존재하는지 확인
	        	// 접근 토큰이 존재
	        	if(userDao.certAccessToken(cryptoAccessToken)) {
	        		// 접근 토큰이 유효한지 확인
	        		Date accessTokenCreateDate = userDao.getTokenCreateTime(cryptoAccessToken);
	        		// 접근 토큰이 없는 경우
	        		if(accessTokenCreateDate==null) {
	        			this.json.put("status", "400");
	    	            this.json.put("msg", "접근토큰이 유효하지 않습니다.");
	        		}
	        		else {
	        			try {
	    					String accessToken = deCrypt(cryptoAccessToken);
	    					String userEmail = accessToken.substring(0, accessToken.indexOf(":"));
	    					
	    					// 접근 토큰이 유효한 경우
	            			if(userDao.checkAccessToken(accessTokenCreateDate)) {
	            				// 유저의 핀 코드를 생성한다.
	            				this.json = userDao.createUserPinCode(pinCodeHashed, userEmail);
	            			}
	            			
	            			// 접근 토큰이 유효하지 않은 경우 -> 갱신해줘야 함
	            			else {
	            				Timestamp nowTimestamp = new Timestamp(new Date().getTime());
	            				// 아이디(이메일)값과 현재 시간(타임스템프)을 ":"를 기준으로 붙이고 암호화
	                			String newCryptoAccessToken = enCrypt(userEmail+":"+nowTimestamp.toString());
	                			
	                			// 접근토큰 업데이트에 성공한 경우
	                			if(userDao.updateToken(userEmail, "user_access_token", newCryptoAccessToken)) {
	                				// 유저의 핀 코드를 생성한다.
		            				this.json = userDao.createUserPinCode(pinCodeHashed, userEmail);
	                			}
	                			// 접근토큰 업데이트에 실패한 경우
	                			else {
	                				this.json.put("status", "400");
	            		            this.json.put("msg", "접근토큰을 갱신하지 못했습니다.");
	                			}
	            			}
	    		        	
	    				} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException
	    						| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
	    						| BadPaddingException e) {
	    					this.json.put("status", "400");
	    		            this.json.put("msg", e.getMessage().toString());
	    				}
	        			
	        		}
	        	}
	        	// 접근 토큰이 존재하지 않는 경우 -> 다른 기기에서 로그인을 한 상태
	        	else {
	        		this.json.put("status", "204");
	                this.json.put("msg", "다른 기기에서 로그인 되어 세션이 만료되었습니다.");
	        	}
				
			} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException
					| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
					| BadPaddingException e1) {
				this.json.put("status", "400");
	            this.json.put("msg", e1.getMessage().toString());
			}
        	
            
        	out.println(this.json);
        }
        
        
        // 유저의 핀 코드를 수정하는 API
        else if (action.contentEquals("/user/update_user_pin_code.do")) {
        	String cryptoAccessToken = req.getParameter("access_token");
        	String enPinCode = req.getParameter("en_pin_code");
			try {
				String urlPinCode = URLDecoder.decode(enPinCode, "UTF-8");
				String pinCode = deCrypt(urlPinCode);
				String pinCodeHashed = BCrypt.hashpw(pinCode, BCrypt.gensalt(15));
	        	// 접근 토큰 존재하는지 확인
	        	// 접근 토큰이 존재
	        	if(userDao.certAccessToken(cryptoAccessToken)) {
	        		// 접근 토큰이 유효한지 확인
	        		Date accessTokenCreateDate = userDao.getTokenCreateTime(cryptoAccessToken);
	        		// 접근 토큰이 없는 경우
	        		if(accessTokenCreateDate==null) {
	        			this.json.put("status", "400");
	    	            this.json.put("msg", "접근토큰이 유효하지 않습니다.");
	        		}
	        		else {
	        			try {
	    					String accessToken = deCrypt(cryptoAccessToken);
	    					String userEmail = accessToken.substring(0, accessToken.indexOf(":"));
	    					
	    					// 접근 토큰이 유효한 경우
	            			if(userDao.checkAccessToken(accessTokenCreateDate)) {
	            				// 유저의 핀 코드 수정한다.
	            				if(userDao.updateUserPinCode(pinCodeHashed, userEmail)) {
	            					this.json.put("status", "200");
	            		            this.json.put("msg", "핀 코드가 수정되었습니다.");
	            				}
	            			}
	            			
	            			// 접근 토큰이 유효하지 않은 경우 -> 갱신해줘야 함
	            			else {
	            				Timestamp nowTimestamp = new Timestamp(new Date().getTime());
	            				// 아이디(이메일)값과 현재 시간(타임스템프)을 ":"를 기준으로 붙이고 암호화
	                			String newCryptoAccessToken = enCrypt(userEmail+":"+nowTimestamp.toString());
	                			
	                			// 접근토큰 업데이트에 성공한 경우
	                			if(userDao.updateToken(userEmail, "user_access_token", newCryptoAccessToken)) {
	                				// 유저의 핀 코드 수정한다.
	                				if(userDao.updateUserPinCode(pinCodeHashed, userEmail)) {
	                					this.json.put("status", "200");
	                		            this.json.put("msg", "핀 코드가 수정되었습니다.");
	                				}
	                			}
	                			// 접근토큰 업데이트에 실패한 경우
	                			else {
	                				this.json.put("status", "400");
	            		            this.json.put("msg", "접근토큰을 갱신하지 못했습니다.");
	                			}
	            			}
	    		        	
	    				} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException
	    						| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
	    						| BadPaddingException e) {
	    					this.json.put("status", "400");
	    		            this.json.put("msg", e.getMessage().toString());
	    				}
	        			
	        		}
	        	}
	        	// 접근 토큰이 존재하지 않는 경우 -> 다른 기기에서 로그인을 한 상태
	        	else {
	        		this.json.put("status", "204");
	                this.json.put("msg", "다른 기기에서 로그인 되어 세션이 만료되었습니다.");
	        	}
				
			} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException
					| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
					| BadPaddingException e1) {
				this.json.put("status", "400");
	            this.json.put("msg", e1.getMessage().toString());
			}
        	
            
        	out.println(this.json);
        }
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
