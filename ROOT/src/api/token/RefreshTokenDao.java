package api.token;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class RefreshTokenDao {

	private RefreshTokenFx refreshTokenFx = new RefreshTokenFx();
	private TokenFx tokenFx = new TokenFx();	

	
	// 갱신토큰이 만료인지 아닌지 확인
	public JSONObject checkRefreshToken(JSONObject tokenJson) {
		String cryptoRefreshToken = tokenJson.get("refresh_token").toString();
		JSONObject json = new JSONObject();
		
		JSONObject tmpJSONToken = tokenFx.decodeAndObjToken(cryptoRefreshToken);
		System.out.println("tmpJSONToken: " + tmpJSONToken);
		
		String userEmail = tmpJSONToken.get("user_email").toString();
		String createdAt = tmpJSONToken.get("created_at").toString();
		System.out.println("userEmail: " + userEmail + ",  createdAt: " +createdAt);
		
		// 갱신토큰이 존재하는 경우
		if(refreshTokenFx.isValidRefreshToken(userEmail, cryptoRefreshToken)) {
			
			// 갱신토큰의 기한이 유효한 경우
			if(refreshTokenFx.checkRefreshToken(createdAt)) {
				json.put("status", "201");
				json.put("msg", "정상적인 접근입니다.");
				json.put("userEmail", userEmail);
			}
			
			// 접근토큰의 기한이 유효하지 않는 경우
			else {
				json.put("status", "202");
				json.put("msg", "갱신 토큰이 만료되었습니다.");
				json.put("user_email", userEmail);
			}
		}
		
		// 갱신토큰이 존재하지 않는 경우
		else {
			json.put("status", "400");
			json.put("msg", "갱신토큰이 만료되었습니다. 관리자에게 문의해주세요.");
		}
		
		return json;
	}
	
	// 접근토큰 갱신 로직
	public JSONObject updateRefreshToken(String userEmail) {
		JSONObject json = new JSONObject();
		
		
		String refreshToken = refreshTokenFx.updateRefreshToken(userEmail);
		// 접근토큰 갱신이 성공한 경우
		if(refreshToken.length()>0) {
			
			json.put("status", "201");
			json.put("msg", "갱신토큰이 갱신되었습니다.");
			json.put("refresh_token", refreshToken);
			
			
		}
		// 접근토큰 갱신에 실패한 경우
		else {
			json.put("status", "400");
			json.put("msg", "갱신토큰 갱신에 실패하였습니다.");
		}
		
		return json;
	}
	
}
