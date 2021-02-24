package api.user;

import api.token.TokenDao;
import api.token.TokenFx;
import org.json.simple.JSONObject;

public class UserDao {
    private UserFx userFx = new UserFx();
    private TokenFx tokenFx = new TokenFx();
    private TokenDao tokenDao = new TokenDao();
    private PinCodeFx pinCodeFx = new PinCodeFx();
    
    public UserDao() {}
    
    // 회원가입 (json에 담긴 user_email, user_pw를 파라미터로 받는다.)
    // 1. 유저 이메일 사용가능 유무 확인
    // 2. 유저 테이블에 저장
    // 3. 로그인 확인 테이블에 저장
    // 4. 식권 테이블에 저장
    public JSONObject signUp(JSONObject userJson) {
    	JSONObject json = new JSONObject();
    	
    	String userEmail = userJson.get("user_email").toString();
    	
    	// 1-1. 유저 이메일 사용 가능
    	if(userFx.dupVerifyEmail(userEmail)) {
    		
    		// 2. 유저 테이블에 저장
    		if(userFx.addUserInfo(userJson)) {	
    			
    			// 갱신토큰 생성
    			userJson.put("user_refresh_token", tokenFx.generateToken(userEmail));
    			
    			// 3. 로그인 확인 테이블에 저장
    			if(userFx.addSignInfo(userJson)) {
    				
    				// 4. 식권 테이블에 저장
    				if(userFx.addTicketInfo(userJson)) {
    					
    					json.put("status", "200");
    	        		json.put("msg", "회원가입에 성공하였습니다.");
    					
    				}
    				
    				// 4. 식권 테이블에 저장 실패 -> 리턴
    				else {
    					
    					// 저장한 데이터 롤백
    					userFx.withdrawal(userJson);
    					
    					json.put("status", "400");
    	        		json.put("msg", "회원가입에 실패하였습니다.");
    				}
    			}
    			// 3. 로그인 확인 테이블에 저장 실패 -> 리턴
    			else {
    				
    				// 저장한 데이터 롤백
    				userFx.withdrawal(userJson);
    				
    				json.put("status", "400");
            		json.put("msg", "회원가입에 실패하였습니다.");
            		
    			}
    		}
    		
    		// 2. 유저 테이블에 저장 실패 -> 리턴
    		else {
    			
    			json.put("status", "400");
        		json.put("msg", "회원가입에 실패하였습니다.");
        		
    		}
    	}
    	
    	// 1-2. 유저 이메일 사용 불가능 -> 리턴
    	else {
    		json.put("status", "400");
    		json.put("msg", "이미 사용중인 이메일입니다.");
    	}
    	
    	return json;
    }
    

    // 로그인 status 값
    // 400 -> 로그인 실패 
    // 201 -> 로그인 성공
    // 1. 유저의 계정 유무, 비밀번호 일치, 인증 상태를 확인한다.
    // 2. 갱신 토큰을 확인하고 접근 토큰이 기존에 존재하더라도 갱신해주고 기존 토큰을 만료 시킨다.
    
    public JSONObject signIn(JSONObject userJson) {
    	JSONObject json = new JSONObject();
    	
    	json = userFx.getSignInfo(userJson);
    	
    	String signInfoStatus = json.get("status").toString();
    	String userRefreshToken = json.get("user_refresh_token").toString(); 
    	if(signInfoStatus.contentEquals("201")) {
    		json.clear();
    		json = tokenDao.updateAllToken(userJson);
    		json.put("msg", "로그인에 성공하였습니다.");
    	}
    	
    	return json;
    }
    
    // 유저 인증 상태를 가져오는 기능
    public JSONObject getUserAuth(JSONObject tokenJson) {
    	JSONObject json = new JSONObject();
    	
    	String accessToken = tokenJson.get("access_token").toString();
    	tokenJson = tokenFx.decodeAndObjToken(accessToken);
    	
    	String userAuth = userFx.getUserAuth(tokenJson);
    	
    	if(userAuth.equals("")) {
    		json.put("status", "400");
    		json.put("", userAuth)
    	}
    	else {
    		json.put("status", "");
    	}
    	
    	
    	
    	return json;
    }
    
    
    // 핀 코드를 처음 생성하는 경우
    public JSONObject createUserPinCode(JSONObject userJson) {
    	JSONObject json = new JSONObject();
    
    	// 핀 코드 생성
    	if(pinCodeFx.updateUserPinCode(userJson)) {
    		
    	}
    	else {
    		
    	}
    	return json;
    }
   
}