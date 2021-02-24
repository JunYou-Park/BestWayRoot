package api.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.json.simple.JSONObject;

import api.model.UserVo;
import util.converter.Converter;
import util.db.DBConnector;

public class UserFx {
	
	private Converter converter = new Converter();
	
	private DBConnector dbConnector = DBConnector.getInstance();
    private UserVo userVO;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // user_info 테이블 추가(회원가입)
    public boolean addUserInfo(JSONObject userJson) {
    	boolean result = false;
    	Connection conn = null;
    	PreparedStatement pstm = null;
    	
    	String userEmail = userJson.get("user_email").toString();
    	String userPw = userJson.get("user_pw").toString();
    	String userFullName = userJson.get("user_full_name").toString();
    	String userPhoneNumber = userJson.get("user_phone_number").toString();
    	String userCreateTimeString = userJson.get("user_create_time").toString();
    	Timestamp userCreateTime = converter.StringtoTimestamp(userCreateTimeString);
    	
    	String query = "INSERT INTO user_info(user_email, user_pw, user_full_name, user_phone_number, user_create_time) VALUES(?, ?, ?, ?, ?)";
    	try {
    		conn = dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
            pstm.setString(1, userEmail);
            pstm.setString(2, userPw);
            pstm.setString(3, userFullName);
            pstm.setString(4, userPhoneNumber);
            pstm.setTimestamp(5, userCreateTime);
            result = pstm.executeUpdate() == 1;
    	}
    	catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.dbConnector.freeConnection(conn, pstm);
        }
    	return result;
    }
    
    // sign_info 테이블 추가
    public boolean addSignInfo(JSONObject userJson) {
    	String userEmail = userJson.get("user_email").toString();
    	String userPw = userJson.get("user_pw").toString();
    	String userRefreshToken = userJson.get("user_refresh_token").toString();
    	boolean result = false;
    	Connection conn = null;
        PreparedStatement pstm = null;
        String query = "INSERT INTO sign_info(user_email, user_pw, user_refresh_token) VALUES(?, ?, ?)";
        
    	try {
    		conn = dbConnector.getConnection();
            pstm = conn.prepareStatement(query);
            pstm.setString(1, userEmail);
            pstm.setString(2, userPw);
            pstm.setString(3, userRefreshToken);
            result = pstm.executeUpdate() == 1;
    	}
    	catch (SQLException e) {
    		e.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	finally {
            this.dbConnector.freeConnection(conn, pstm);
        }
    	return result;
    }
    
    // ticket_info 테이블 추가
    public boolean addTicketInfo(JSONObject userJson) {
    	String userEmail = userJson.get("user_email").toString();
    	boolean result = false;
    	Connection conn = null;
        PreparedStatement pstm = null;
    	String query = "INSERT INTO ticket_info(user_email, ticket_5000, ticket_4000, ticket_3500, ticket_2000) VALUES(?, 0, 0, 0, 0)";
    	
    	try {
    		conn = dbConnector.getConnection();
            pstm = conn.prepareStatement(query);
            pstm.setString(1, userEmail);
            result = pstm.executeUpdate() == 1;
    	}
    	catch (SQLException e) {
    		e.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	finally {
            this.dbConnector.freeConnection(conn, pstm);
        }
    	
    	return result;
    }
    
 // 이메일 인증 로직
    // 1.이메일 인증 상태 확인
    // 1-1.이메일 인증이된 경우 -> 세션이 만료라는 메시지 출력
    // 1-2.이메일 인증이 안된 경우 -> 이메일 인증 로직 실행 -> 인증 컬럼 업데이트 -> 인증 완료 메시지 출력
    // 이메일 인증 상태 확인
    public boolean authenticationEmailCheck(String email) {
    	boolean result = false;
    	Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
     	String query = "SELECT user_auth FROM sign_info WHERE user_email = ?";
     	
    	try {
           	conn = this.dbConnector.getConnection();
          
           	pstm = conn.prepareStatement(query);
           	pstm.setString(1, email);
           	rs = pstm.executeQuery();
           	if(rs.next()) {
           		int userAuth = rs.getInt("user_auth");
           		System.out.println("authenticationEmailCheck userAuth: " + userAuth);
           		if(userAuth == 1) result = true;
           		else result = false;
           	}

    	}
    	catch(SQLException e) {
    		e.printStackTrace();
    	}catch (Exception e) {
            e.printStackTrace();
        } finally {
        	this.dbConnector.freeConnection(conn, pstm, rs);
        }
    	System.out.println("authenticationEmailCheck result: " + result);
    
    	return result;
    }
    
 // 이메일 인증
    public boolean authenticationEmail(String email) {
    	boolean result = false;
    	Connection conn = null;
        PreparedStatement pstm = null;
       
    	try {
           	conn = this.dbConnector.getConnection();
           	String query = "UPDATE sign_info SET user_auth = 1 WHERE user_email = ?";
           	pstm = conn.prepareStatement(query);
           	pstm.setString(1, email);
           	result = pstm.executeUpdate() == 1;
    	}
    	catch(SQLException e) {
    		e.printStackTrace();
    	}catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.dbConnector.freeConnection(conn, pstm);
        }
    	System.out.println("authenticationEmail: " + result);
    	return result;
    }
    
    // 이메일 중복 확인
    // 유저테이블과 탈퇴유저테이블을 병합(UNION)해서 확인
    public boolean dupVerifyEmail(String userEmail) {
    	boolean result = false;
    	Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        String query = "SELECT user_email FROM user_info WHERE user_email = ? UNION SELECT user_email FROM withdrawal_user_info WHERE user_email = ?";
    	try {
    		conn = this.dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, userEmail);
    		pstm.setString(2, userEmail);
    		rs = pstm.executeQuery();
    		// 유저 이메일이 없으면 true로 반환해야하기 때문에 !를 사용함
    		result = !rs.next();
    		System.out.println(result);
    	}
    	catch(SQLException e) {
    		e.printStackTrace();
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	finally {
    		this.dbConnector.freeConnection(conn, pstm, rs);
    	}
    	
    	return result;
    }
    
 // 회원탈퇴
    // 유저테이블에 데이터를 지운다
    // 탈퇴유저테이블에 데이터를 넣는다.
    public boolean withdrawal(JSONObject userJson) {
    	String userEmail = userJson.get("user_email").toString();
    	boolean result = false;
    	Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
         
        String query = "DELETE FROM user_info WHERE user_email = ?";
    	try {
    		conn = dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, userEmail);
    		result = pstm.executeUpdate() == 1;
    		
    	}
    	catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.dbConnector.freeConnection(conn, pstm, rs);
        }
    	
    	return result;
    }
    
    // 탈퇴유저테이블에 데이터 저장
    public boolean addWithDrawal(JSONObject userJson) {
    	String userEmail = userJson.get("user_email").toString();
    	
    	boolean result = false;
    	Connection conn = null;
        PreparedStatement pstm = null;
        
        String query = "INSERT INTO withdrawal_user_info(user_email, created_at) VALUE(? ,now())";
    	try {
    		conn = dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, userEmail);
    		int update = pstm.executeUpdate();
    		result = update == 1;
    	}
    	catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.dbConnector.freeConnection(conn, pstm);
        }
    	
    	return result;
    }
    
    // 로그인하기 위해 사용되는 데이터 가져오는 기능
    // 이메일 인증 데이터 형식
    // 000 -> 로그인 불가능 (이메일 인증 필요)
    // 100 -> 로그인 가능 (핀 코드 입력 필요)
    // 110 -> 로그인 가능 (휴대폰 인증 필요)
    // 111 -> 로그인 가능 (모든 인증 완료)
    public JSONObject getSignInfo(JSONObject userJson) {
    	JSONObject json = new JSONObject();
    	
    	String userEmail = userJson.get("user_email").toString();
    	String userPw = userJson.get("user_pw").toString();
    	
    	Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
    	String query = "SELECT user_pw, user_auth, user_refresh_token, user_access_token FROM sign_info WHERE user_email = ? ";
    	
    	try {
    		conn = dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, userEmail);
    		rs = pstm.executeQuery();
            if (rs.next()) { // 이메일이 존재 => 유효한
            	
            	String userAuth = rs.getString("user_auth");
            	
            	// 이메일 인증이 필요
            	if(userAuth.contentEquals("000")) {
            		json.put("status", "400");
                	json.put("msg", "이메일 인증이 필요합니다.");
            	}
            	
            	else {
            		String getUserPw = rs.getString("user_pw");
            		
            		// 비밀번호가 다른 경우
            		if(!userPw.contentEquals(getUserPw)) {
            			json.put("status", "400");
                    	json.put("msg", "이메일 또는 비밀번호를 확인해주세요.");
            		}
            		
            		// 비밀번호가 같은 경우 -> 갱신 토큰 값을 반환
            		else {
            			json.put("status", "201");
                    	json.put("msg", "존재하는 계정입니다.");
                    	json.put("user_refresh_token", rs.getString("user_refresh_token"));
            		}
            	}
            	
            }
            
            // 계정이 존재하지 않는 경우
            else {
            	json.put("status", "400");
            	json.put("msg", "존재하지 않는 계정입니다.");
            }
    	}
    	catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.dbConnector.freeConnection(conn, pstm);
        }
    	
    	
    	return json;
    }
    
    // 유저의 인증 상태를 가져오는 기능
    public String getUserAuth(JSONObject userJson) {
    	String userEmail = userJson.get("user_email").toString();
    	
    	System.out.println("getUserAuth userEmail: " + userEmail);
    	Connection conn = null;
        PreparedStatement pstm = null;
    	ResultSet rs = null;
    	
    	String userAuth = "";
    	
    	String query = "SELECT user_auth FROM sign_info WHERE user_email = ?";
    	try {
    		conn = this.dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, userEmail);
    		rs = pstm.executeQuery();
    		if(rs.next()) {
    			userAuth = rs.getString("user_auth");
    		}
    		
    	}
    	catch(SQLException e) {
    		System.out.println("getUserAuth: " + e.getMessage().toString());
    	}
    	catch(Exception e) {
    		System.out.println("getUserAuth: " + e.getMessage().toString());
    		
    	}
    	finally {
    		this.dbConnector.freeConnection(conn, pstm, rs);
    	}
    	
    	return userAuth;
    }
    
    public String updateUserAuth(JSONObject userJson) {
    	String userEmail = userJson.get("user_email").toString();
    	String userAuth = userJson.get("user_auth").toString();
    	
    	System.out.println("updateUserAuth");
    	Connection conn = null;
        PreparedStatement pstm = null;
    	
    	String query = "UPDATE sign_info SET user_auth = ? WHERE user_email = ?";
    	char[] array = userAuth.toCharArray();
		array[1] = '1';
		String updatedUserAuth = new String(array);
		System.out.println("updatedUserAuth: " + updatedUserAuth);
		try {
    		conn = this.dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, updatedUserAuth);
    		pstm.setString(2, userEmail);
    		if(pstm.executeUpdate() != 1) {
    			userAuth = null;
    		}
    	}
		catch(SQLException e) {
    		System.out.println("updateUserAuth: " + e.getMessage().toString());
    		userAuth = null;
    	}
    	catch(Exception e) {
    		System.out.println("updateUserAuth: " + e.getMessage().toString());
    		userAuth = null;
    	}
    	finally {
    		this.dbConnector.freeConnection(conn, pstm);
    	}
    	
    	return userAuth;
    }
    
}
