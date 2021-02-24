package api.user;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TokenDao {
	
	private AccessTokenDao accessTokenDao = new AccessTokenDao();
	private RefreshTokenDao refreshTokenDao = new RefreshTokenDao();
	
	// 접근토큰이 만료되어 프로트단에 갱신토큰을 요청 (공통)
	// 갱신토큰을 받아 갱신토큰의 유효성을 확인 (공통)
	
	// 갱신토큰이 만료된 경우 자동으로 갱신해줌
	// 접근토큰을 갱신해줌
	
	// 갱신토큰이 유효한 경우
	// 접근토큰을 갱신해줌
	
	// 리턴값에 갱신, 접근토큰을 반환(공통)
	
	
	
}
