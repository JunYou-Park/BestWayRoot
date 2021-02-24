package api.user;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class AccessTokenDao {

	private TokenFx tokenFx = new TokenFx();
	private AccessTokenFx accessTokenFx = new AccessTokenFx();
	
	
	// 접근토큰이 들어오면 만료인지 아닌지 확인
		public JSONObject checkAccessToken(String cryptoAccessToken) {
			JSONObject json = new JSONObject();
			
			JSONObject tmpJSONToken = tokenFx.decodeAndObjToken(cryptoAccessToken);
			System.out.println("tmpJSONToken: " + tmpJSONToken);
			
			String userEmail = tmpJSONToken.get("user_email").toString();
			String createdAt = tmpJSONToken.get("created_at").toString();
			System.out.println("userEmail: " + userEmail + ",  createdAt: " +createdAt);
			
			// 접근토큰이 존재하는 경우
			if(accessTokenFx.isValidAccessToken(userEmail, cryptoAccessToken)) {
				
				// 접근토큰의 기한이 유효한 경우
				if(accessTokenFx.checkAccessToken(createdAt)) {
					json.put("status", "200");
					json.put("msg", "정상적인 접근입니다.");
				}
				
				// 접근토큰의 기한이 유효하지 않는 경우
				else {
					json.put("status", "202");
					json.put("msg", "접근 토큰이 만료되었습니다.");
				}
			}
			
			// 접근토큰이 존재하지 않는 경우
			else {
				json.put("status", "400");
				json.put("msg", "세션이 만료되었습니다.");
			}
			
			
			return json;
		}
		
		// 접근토큰 갱신 로직
		public JSONObject updateAccessToken(String userEmail) {
			JSONObject json = new JSONObject();
			JSONArray jsArray = new JSONArray();
			
			String accessToken = accessTokenFx.updateAccessToken(userEmail);
			// 접근토큰 갱신이 성공한 경우
			if(accessToken.length()>0) {
				
				json.put("access_token", accessToken);
				jsArray.add(json);
				
				json = new JSONObject();
				
				json.put("status", "201");
				json.put("msg", "접근토큰이 갱신되었습니다.");
				json.put("data", jsArray);
			}
			// 접근토큰 갱신에 실패한 경우
			else {
				json.put("status", "400");
				json.put("msg", "접근토큰 갱신에 실패하였습니다.");
			}
			
			return json;
		}
	
}
