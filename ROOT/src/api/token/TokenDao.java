package api.token;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TokenDao {
	
	private AccessTokenDao accessTokenDao = new AccessTokenDao();
	private RefreshTokenDao refreshTokenDao = new RefreshTokenDao();
	private TokenFx tokenFx = new TokenFx();
	
	
	public JSONObject isValidAccessToken(JSONObject tokenJson) {
		return accessTokenDao.checkAccessToken(tokenJson);
	}
	
	// 1. 접근토큰이 만료되어 프론트단에 갱신토큰을 요청 (공통)
	// 2. 갱신토큰을 받아 갱신토큰의 유효성을 확인 (공통)
	
	// 3-1-1. 갱신토큰이 유효한 경우
	// 3-1-2. 접근토큰을 갱신해줌
	
	// 3-2-1. 갱신토큰이 만료된 경우
	// 3-2-2. 갱신토큰을 갱신해줌
	// 3-2-3. 접근토큰을 갱신해줌
	
	// 3-2-4. 갱신토큰 갱신 실패 시 실패값 리턴
	
	// 4. 리턴값에 갱신, 접근토큰을 반환(공통)
	
	public JSONObject updateAllToken(JSONObject tokenJson) {
		JSONObject json = new JSONObject();
		
		// 1,2번 과정		
		json = refreshTokenDao.checkRefreshToken(tokenJson);
		
		String refreshStatus = json.get("status").toString();
		String userEmail = json.get("user_email").toString();
		
		switch(refreshStatus) {
		
		// 3-1-1번 과정 (갱신토큰 유효)
			case "201":
				json.clear();
				
				// 3-1-2번 과정 (접근토큰 갱신)
				json = accessTokenDao.updateAccessToken(userEmail);
				String accessUpdateStatus = json.get("status").toString();
				
				if(accessUpdateStatus.contentEquals("201")) {
					// 4번 과정 (접근토큰, 갱신토큰을 반환)
					JSONObject tmpJson = new JSONObject();
					JSONObject newTokenJson = new JSONObject();
					JSONArray jsArray = new JSONArray();
					
					String accessToken = json.get("access_token").toString();
					String refreshToken = tokenJson.get("refresh_token").toString();
					
					tokenJson.put("refresh_token", refreshToken);
					tokenJson.put("access_token", accessToken);
					
					tmpJson.put("token", newTokenJson);
					jsArray.add(tmpJson);
					
					json.clear();
					
					json.put("status", "201");
					json.put("msg", "토큰이 갱신되었습니다.");
					json.put("data", jsArray);
				
				}
				
				System.out.println("updateAllToken 200: " + json);
				break;
				
		// 3-2-1번 과정 (갱신토큰 만료)
			case "202":
				json.clear();
				
				// 3-2-2번 과정(갱신토큰 갱신)
				json = refreshTokenDao.updateRefreshToken(userEmail);
				String refreshUpdateStatus = json.get("status").toString();
				
				// 3-2-3번 과정 (접근토큰 갱신)
				if(refreshUpdateStatus.contentEquals("201")) {  
					JSONObject tmpJson = new JSONObject();
					JSONObject newTokenJson = new JSONObject();
					JSONArray jsArray = new JSONArray();
					String newRefreshToken = json.get("refresh_token").toString();
					
					json.clear();
					
					json = accessTokenDao.updateAccessToken(userEmail);
					String accessUpdateStatus2 = json.get("status").toString();
					
					if(accessUpdateStatus2.contentEquals("201")) {
						// 4번 과정 (접근토큰, 갱신토큰을 반환)
						String accessToken = json.get("access_token").toString();
					
						newTokenJson.put("refresh_token", newRefreshToken);
						newTokenJson.put("access_token", accessToken);
						
						tmpJson.put("token", newTokenJson);
						
						jsArray.add(tmpJson);
						
						json.clear();
						
						json.put("status", "201");
						json.put("msg", "토큰이 갱신되었습니다.");
						json.put("data", jsArray);
					}
					else {
						json.clear();
						
						tokenJson.put("refresh_token", newRefreshToken);
						jsArray.add(tokenJson);
						
						json.put("status", "401");
						json.put("msg", "갱신토큰만 갱신되고 접근토큰은 갱신되지 않았습니다.");
						json.put("data", jsArray);
					}
					
				}
				break;
				
			default:
				break;
		}
		return json;
	}
	
	
	
}
