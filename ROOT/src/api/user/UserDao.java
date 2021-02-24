package api.user;

import api.model.UserVo;
import crypto.AES256Cipher;
import db.DBConnector;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

public class UserDao {
    private DBConnector dbConnector = DBConnector.getInstance();
    private UserVo userVO;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public UserDao() {}

    // 로그인 status 값
    // 400 -> 로그인 실패 
    // 201 -> 로그인 성공
    // 202 -> 로그인은 성공했지만 세션을 끊을 건지 리턴 (통신이 남아 있음)
    
    // 이메일 인증 데이터 형식
    // 000 -> 로그인 불가능 (이메일 인증 필요)
    // 100 -> 로그인 가능 (핀 코드 입력 필요)
    // 110 -> 로그인 가능 (휴대폰 인증 필요)
    // 111 -> 로그인 가능 (모든 인증 완료)
    public JSONObject signIn(String id, String pw) {
    	JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        
        String query = "SELECT user_pw, user_auth, user_refresh_token, user_access_token FROM sign_info WHERE user_email = ? ";
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
        	conn = this.dbConnector.getConnection();
       	    pstm = conn.prepareStatement(query);
            pstm.setString(1, id);
            rs = pstm.executeQuery();
            if (rs.next()) { // 이메일이 존재 => 유효한
            	String userAuth = rs.getString("user_auth");
            	if(!userAuth.equals("000")) { 
            		// 이메일이 인증된 상태
            		if(BCrypt.checkpw(pw, rs.getString("user_pw"))) { // 비밀번호 일치
            			
            			// 암호화된 접근토큰
            			String cryptoAccessToken = rs.getString("user_access_token");
            			
            			// 암호화된 갱신토큰
            			String cryptoRefreshToken = rs.getString("user_refresh_token");
            			
            			Timestamp nowTimestamp = new Timestamp(new Date().getTime());
            			
            			System.out.println("접근토큰: " + cryptoAccessToken);
            			
            			// 아이디(이메일)값과 현재 시간(타임스템프)을 ":"를 기준으로 붙이고 암호화
            			String newCryptoAccessToken = enCrypt(id+":"+nowTimestamp.toString());
            			
            			// 암호화된 갱신토큰을 주고 토큰 생성일을 리턴 받았을 때 null이 아닌 경우
            			// 갱신토큰 생성일을 확인해야한다.
            			Date refreshTokenCreateDate = getTokenCreateTime(cryptoRefreshToken);
            			if(refreshTokenCreateDate!=null) { 
            				// 갱신토큰이 유효한 경우
            				if(checkRefreshToken(refreshTokenCreateDate)) {
            					// 접근 토큰이 없는 경우
            					if(cryptoAccessToken==null || cryptoAccessToken.isEmpty()) {
            						// 접근토큰을 업데이트
                    				if(updateToken(id, "user_access_token", newCryptoAccessToken)) { // 접근토큰 업데이트 성공
                						// 갱신토큰과 접근토큰을 리턴
                    					// 갱신토큰의 경우 만료된 상태가 아니였으니 리턴 값으로 사용가능 (cryptoRefreshToken)
                    					// 접근토큰의 경우 바로 위에서 업데이트 하기위해 생성했기 때문에 리턴값으로 사용 가능 (accessTokenValue)
                    					JSONObject data = new JSONObject();
                    					
                    					data.put("refresh_token", cryptoRefreshToken);
                    					data.put("access_token", newCryptoAccessToken);
                    					data.put("user_auth", userAuth);
                    					
                    					jsonArray.add(data);
                    					
                    					json.put("status", "201");
                    			        json.put("msg", "로그인에 성공하였습니다.");
                    			        json.put("data", jsonArray);
                    					
                					}
                    				// 접근토큰 업데이트 실패
                    				else {  
                    					json.put("status", "400");
                        				json.put("msg", "잘못된 접근토큰 값입니다.");
                        				json.put("data", null);
                    				}
            					}
            					// 접근토큰이 있는 경우 사용자에게 현재 로그인 세션을 끊을건지 확인
            					else {
            						JSONObject data = new JSONObject();
                					
                					data.put("refresh_token", cryptoRefreshToken);
                					data.put("access_token", newCryptoAccessToken);
                					data.put("user_auth", userAuth);
                					
                					jsonArray.add(data);
                					
                					json.put("status", "202");
                			        json.put("msg", "기존에 로그인된 상태입니다. 기존 로그인을 상태를 해제하시겠습니까?");
                			        json.put("data", jsonArray);
            					}
            				}
            				// 갱신토큰이 만료된 경우 -> 갱신이 필요
            				else {
            					// 아이디(이메일)값과 현재 시간(타임스템프)을 ":"를 기준으로 붙이고 암호화
            					// 갱신토큰을 업데이트
                				String newCryptoRefreshToken = enCrypt(id+":"+nowTimestamp.toString());
                				
                				// 갱신토큰 업데이트 성공한 경우
            					if(updateToken(id, "user_refresh_token", newCryptoRefreshToken)) {
            						// 접근 토큰이 없는 경우
            						if(cryptoAccessToken==null) {
            							
                    					// 갱신토큰을 업데이트
            							if(updateToken(id, "user_access_token", newCryptoAccessToken)) { // 접근토큰 업데이트 성공
                    						// 갱신토큰과 접근토큰을 리턴
                        					// 갱신토큰의 경우 만료된 상태가 아니였으니 리턴 값으로 사용가능 (cryptoRefreshToken)
                        					// 접근토큰의 경우 바로 위에서 업데이트 하기위해 생성했기 때문에 리턴값으로 사용 가능 (accessTokenValue)
                        					JSONObject data = new JSONObject();
                        					
                        					data.put("refresh_token", newCryptoRefreshToken);
                        					data.put("access_token", newCryptoAccessToken);
                        					data.put("user_auth", userAuth);
                        					
                        					jsonArray.add(data);
                        					
                        					json.put("status", "201");
                        			        json.put("msg", "로그인에 성공하였습니다.");
                        			        json.put("data", jsonArray);
                        					
                    					}
                        				// 접근토큰 업데이트 실패
                        				else {  
                        					json.put("status", "400");
                            				json.put("msg", "잘못된 접근토큰 값입니다.");
                        				}
            						}
            						else {
            							JSONObject data = new JSONObject();
                    					data.put("refresh_token", newCryptoRefreshToken);
                    					data.put("access_token", newCryptoAccessToken);
                    					data.put("user_auth", userAuth);
                    					
                    					jsonArray.add(data);
                    					
                    					json.put("status", "202");
                    			        json.put("msg", "기존에 로그인된 상태입니다. 기존 로그인을 상태를 해제하시겠습니까?");
                    			        json.put("data", jsonArray);
            						}
            					}
            					// 갱신토큰 업데이트를 실패한 경우 
            					else {
            						json.put("status", "400");
                    				json.put("msg", "잘못된 갱신토큰 값입니다.");
            					}
            				}
            			}
            			// 암호화된 갱신토큰을 주고 토큰 생성일을 리턴 받았을 때 null인 경우 (회원을 탈퇴한 경우)
            			// 로그인 실패 상태값을 리턴
            			else {
            				// 데이터에 null값을 넣음
            				json.put("status", "400");
            				json.put("msg", "계정이 만료되었습니다.");
            			}
            
            		}
            		// 패스워드 불일치
            		else { 
            			System.out.println("user_pw invalid");
            	    	json.put("status", "400");
            	        json.put("msg", "아이디 또는 비밀번호를 확인해주세요.");
            		}
            	}
            	// 이메일 미인증 상태
            	else { 
            		System.out.println("email authentication invalid");
            		json.put("status", "400");
        	        json.put("msg", "이메일 인증을 진행해주세요.");
            	}
            }
            else {
            	json.put("status", "400");
    	        json.put("msg", "존재하지 않는 계정입니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.dbConnector.freeConnection(conn, pstm, rs);
        }

        return json;
    }
    

    public boolean signUp(UserVo userVo, String refreshToken) {
        boolean result = false;
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        String query = "INSERT INTO user_info(user_email, user_pw, user_full_name, user_phone_number, user_create_time) VALUES(?, ?, ?, ?, ?)";
        try {
        	System.out.println("signUp: UserVo=" + userVo.toString());
        	conn = this.dbConnector.getConnection();
            
            pstm = conn.prepareStatement(query);
            pstm.setString(1, userVo.getEmail());
            pstm.setString(2, userVo.getPw());
            pstm.setString(3, userVo.getFullName());
            pstm.setString(4, userVo.getPhoneNumber());
            pstm.setTimestamp(5, userVo.getCreateTime());
            int update = pstm.executeUpdate();
            result = update == 1;
            if(result) {
            	// TODO 트리거로 돌려야하는 구문인데, 실력 부족으로 인해 서버에서 호출
            	addSignInfo(userVo.getEmail(), userVo.getPw(), refreshToken);
            	addTicketInfo(userVo.getEmail());
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.dbConnector.freeConnection(conn, pstm, rs);
        }

        return result;
    }
    
    // 회원탈퇴
    // 유저테이블에 데이터를 지운다
    // 탈퇴유저테이블에 데이터를 넣는다.
    public boolean withdrawal(String userEmail) {
    	boolean result = false;
    	Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
         
        String query = "DELETE FROM user_info WHERE user_email = ?";
    	try {
    		conn = dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, userEmail);
    		int update = pstm.executeUpdate();
    		if(update == 1) {
    			if(addWithDrawal(userEmail)) {
    				result = true;
    			}
    		}
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
    private boolean addWithDrawal(String userEmail) {
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
    
    public boolean updateToken(String userEmail, String tokenType, String tokenValue) {
    	boolean result = false;
    	Connection conn = null;
        PreparedStatement pstm = null;
        
        String query = "UPDATE sign_info SET " + tokenType + " = ? WHERE user_email = ?";
    	try {
    		conn = this.dbConnector.getConnection();
    		
            pstm = conn.prepareStatement(query);
            pstm.setString(1, tokenValue);
            pstm.setString(2, userEmail);
            int update = pstm.executeUpdate();
            result = update == 1;
            
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
    
    private void addSignInfo(String email, String pw, String refreshToken) {
    	Connection conn = null;
        PreparedStatement pstm = null;
        String query = "INSERT INTO sign_info(user_email, user_pw, user_refresh_token) VALUES(?, ?, ?)";
        
    	try {
            pstm = conn.prepareStatement(query);
            pstm.setString(1, email);
            pstm.setString(2, pw);
            pstm.setString(3, refreshToken);
            pstm.executeUpdate();
    	}
    	catch (SQLException e) {
    		e.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	finally {
            this.dbConnector.freeConnection(conn, pstm);
        }
    }
    
    private void addTicketInfo(String email) {
    	Connection conn = null;
        PreparedStatement pstm = null;
    	String query = "INSERT INTO ticket_info(user_email, ticket_5000, ticket_4000, ticket_3500, ticket_2000) VALUES(?, 0, 0, 0, 0)";
    	
    	try {
            pstm = conn.prepareStatement(query);
            pstm.setString(1, email);
            pstm.executeUpdate();
    	}
    	catch (SQLException e) {
    		e.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	finally {
            this.dbConnector.freeConnection(conn, pstm);
        }
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
    public int authenticationEmail(String email) {
    	int result = 0;
    	Connection conn = null;
        PreparedStatement pstm = null;
       
    	try {
           	conn = this.dbConnector.getConnection();
           	String query = "UPDATE sign_info SET user_auth = 1 WHERE user_email = ?";
           	pstm = conn.prepareStatement(query);
           	pstm.setString(1, email);
           	result = pstm.executeUpdate();
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
    public boolean dupVerifyEmail(String email) {
    	boolean result = false;
    	Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        
        String query = "SELECT user_email FROM user_info WHERE user_email = ? UNION SELECT user_email FROM withdrawal_user_info WHERE user_email = ?";
    	try {
    		conn = this.dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, email);
    		pstm.setString(2, email);
    		rs = pstm.executeQuery();
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
    
    // 접근토큰의 만료 시간을 확인하기 위한 코드
    // 1. 토큰 생성일을 입력 받음
    // 2-1. (현재 시간 - 생성 시간)이 30분 보다 큰 경우 -> false를 출력
    // 2-2. (현재 시간 - 생성 시간)이 30분 보다 작은 경우 -> true를 출력
    public boolean checkAccessToken(Date tokenCreateDate)  {
    	Date now = new Date();
    	if(now.getTime() - tokenCreateDate.getTime() > (1000L * 60L * 30L)) { // accessToken expired (over 30 min)
    		System.out.println("accessToken expired (over 30 min)");
			return false;
		}
		else {
			System.out.println("accessToken valid");
			return true;
		}
    }
    
    // 갱신토큰의 만료 시간을 확인하기 위한 코드
    // 1. 토큰 생성일을 입력 받음
    // 2-1. (현재 시간 - 생성 시간)이 30일 보다 큰 경우 -> false를 출력
    // 2-2. (현재 시간 - 생성 시간)이 30일 보다 작은 경우 -> true를 출력
    public boolean checkRefreshToken(Date tokenCreateDate) {
    	Date now = new Date();
    	if(now.getTime() - tokenCreateDate.getTime() > (1000L * 60L * 60L * 24L * 30L)) { // refreshToken expired (over 30 days)
			System.out.println("refreshToken expired (over 30 days)");
			return false;
		}
		else {
			System.out.println("refreshToken valid");
			return true;
		}
    }
    
    // AccessToken이 존재하는지 확인
    public boolean certAccessToken(String cryptoAccessToken) {
    	boolean result = false;
    	Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        
        String query = "SELECT * FROM sign_info WHERE user_access_token = ?";
        
    	try {
    		conn = this.dbConnector.getConnection();
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, cryptoAccessToken);
    		rs = pstm.executeQuery();
    		result = rs.next();
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
    
    // 토큰 생성시간을 꺼내기 위한 코드
    // 1. 토큰을 입력 받음
    // 2. 토큰을 디크립트하고 토큰 생성일을 분리해냄
    // 3. 생성일을 Date타입으로 바꿈
    // 4-1. 타입 변환이 셩공한 경우 -> 생성일을 출력
    // 4-2. 타입 변환이 실패한 경우  -> null을 출력
    public Date getTokenCreateTime(String crytoToken) {
    	try {
    		String token = deCrypt(crytoToken);
    		String tokenCreateTime = token.substring(token.indexOf(":")+1);
    		Date tokenCreateDate = this.dateFormat.parse(tokenCreateTime);
    		return tokenCreateDate;
    	}
    	catch(Exception e) {
    		System.out.println("getTokenCreateTime: " + e.getMessage().toString());
    		return null;
    	}
    }
    
    // 유저의 핀 코드 검증
    public boolean authUserPinCode(String userEmail, String pinCode) {
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
				result = BCrypt.checkpw(pinCode, pinCodeHash);
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
    
 // 유저의 핀 코드 생성
    public JSONObject createUserPinCode(String pincode, String userEmail) {
    	System.out.println("createUserPinCode");
    	
    	// 중간에 쿼리가 성공했는지 파악하기 위해 사용하는 flag
    	boolean flag = false;
    	
    	Connection conn = null;
        PreparedStatement pstm = null;
    	
    	JSONObject json = new JSONObject();
    	json.put("status", "400");
        json.put("msg", "핀 코드 생성에 실패했습니다.");
    	String result = null;
    	
    	String query = "UPDATE user_info SET user_pin_code = ? WHERE user_email = ?";
    	
    	try {
    		conn = this.dbConnector.getConnection();	
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, pincode);
    		pstm.setString(2, userEmail);
    		flag = pstm.executeUpdate() == 1;
    	}
    	catch(SQLException e) {
    		System.out.println("createUserPinCode: " + e.getMessage().toString());
    	}
    	catch(Exception e) {
    		System.out.println("createUserPinCode: " + e.getMessage().toString());
    	}
    	finally {
    		this.dbConnector.freeConnection(conn, pstm);
    	}
    	
    	if(flag) {
    		
    		result = updateUserAuth(userEmail);
    		System.out.println("result: " + result);
			
    		if(result!=null) {
				JSONArray jsonArray = new JSONArray();
				JSONObject data = new JSONObject();
				
				data.put("user_auth", result);
				jsonArray.add(data);
				
				json.put("status", "201");
		        json.put("msg", "핀 코드가 생성되었습니다.");
		        json.put("data", jsonArray);
			}
    	}
    	
    	return json;
    }
    
    public String getUserAuth(String userEmail) {
    	System.out.println("getUserAuth userEmail: " + userEmail);
    	Connection conn = null;
        PreparedStatement pstm = null;
    	ResultSet rs = null;
    	
    	String userAuth = null;
    	
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
    
    public String updateUserAuth(String userEmail) {
    	System.out.println("updateUserAuth");
    	Connection conn = null;
        PreparedStatement pstm = null;
    	String userAuth = getUserAuth(userEmail);
    	
    	String query = "UPDATE sign_info SET user_auth = ? WHERE user_email = ?";
    	if(userAuth!=null) {
    		
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
    	}
    	
    	return userAuth;
    }
    
    
 // 유저의 핀 코드 업데이트
    public boolean updateUserPinCode(String pincode, String userEmail) {
    	System.out.println("updateUserPinCode");
    	Connection conn = null;
        PreparedStatement pstm = null;
    	
    	boolean result = false;
    	
    	String query = "UPDATE user_info SET user_pin_code = ? WHERE user_email = ?";
    	try {
    		conn = this.dbConnector.getConnection();
    	
    		pstm = conn.prepareStatement(query);
    		pstm.setString(1, pincode);
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
    
    private String deCrypt(String str) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    	AES256Cipher a256 = AES256Cipher.getInstance();
    	return a256.AES_Decode(str);
    }
    
    private String enCrypt(String str) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    	AES256Cipher a256 = AES256Cipher.getInstance();
    	return a256.AES_Encode(str);
    }
}