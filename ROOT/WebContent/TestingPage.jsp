<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<script type="text/javascript">
	function submit(email, pw){
/* 
		alert(email);
		alert(pw);
 */		
		$('signIn').submit();
	}
</script>
</head>
<body>
	로그인
	<br>
	<form name = "signIn" action = "/api/user/sign_in.do" method="post">
		<input id = "email" type='email' name = "email" placeholder="이메일을 입력하세요"/>
		<input id = "pw" type="password" name = "pw" placeholder="비밀번호를 입력하세요"/>
		<button onclick="submit(email.value, pw.value)">로그인</button>
	</form>
	<br>
	
	로그아웃
	<br>
	<form action="/api/user/logout.do">
		<input type='text' name = "access_token" value="WTRNoUmmXrjb5cGa8j83IplrEGJzhcsazFvIiexRpZvcnKgL1kbw+/h8HWi6FW2q"/>
		<input type="submit" value="이동"> 
	</form>
	<br>
	
	회원탈퇴
	<br>
	<form action="/api/user/withdrawal.do">
		<input type='text' name = "access_token" value="WTRNoUmmXrjb5cGa8j83IplrEGJzhcsazFvIiexRpZvcnKgL1kbw+/h8HWi6FW2q"/>
		<input type="submit" value="이동"> 
	</form>
	<br>
	
	회원가입
	<br>
	<form action = "/api/user/sign_up.do" method="post">
		<input type="text" name="fullName" placeholder="이름을 입력하세요"/>
		<input type='email' name = "email" placeholder="이메일을 입력하세요"/>
		<input type="password" name = "pw" placeholder="비밀번호를 입력하세요"/>
		<input type="number" name = "phone_number" placeholder="휴대폰 번호를 입력하세요"/>
		<input type="text" name = "createAt" placeholder="생성날짜를입력하세요" value = "1613789421831"/>
		<input type= "submit" value="회원가입 "/>
	</form>
	<br>
	
	이메일 중복확인
	<br>
	<form action = "/api/user/dup_verify_email.do" method="post">
		<input type='email' name = "email" placeholder="이메일을 입력하세요"/>
		<input type= "submit" value="중복확인 "/>
	</form>
	<br>
	<br>
	
	이메일 인증
	<form action = "/api/user/authentication.do" method="get">
		<input type='text' name = "user" value="iQu6d1c2lqzbQq+tPH/4ZoTOqgaE08aF03BjsD5tlJ0="/>
		<input type= "submit" value="이메일 인증 "/>
	</form>
	<br>
	
	알림 리스트 확인
	<br>
	<form action="/api/fcm/get_notice.do">
		<input type="submit" value="이동"> 
	</form>
	<br>
	
	배너 리스트 확인
	<br>
	<form action="/api/event/get_banner.do">
		<input type="submit" value="이동"> 
	</form>
	<br>
	
	오늘메뉴 리스트 확인
	<br>
	<form action="/api/menu/get_today_menu.do">
		<input type='text' name = "today_menu_date" value="20210218124141"/>
		<input type="submit" value="이동"> 
	</form>
	<br>
	
	접근 토큰 갱신 확인
	<br>
	<form action="/api/user/update_access_token.do">
		<input type='text' name = "access_token" value="WTRNoUmmXrjb5cGa8j83IvJYARtM0LeqLHVcPlVszXtI1lTAqfZBJjyc/x8dE84p"/>
		<input type="submit" value="이동"> 
	</form>
	<br>
	
	유저 식권 확인
	<br>
	<form action="/api/user/get_user_ticket.do">
		<input type='text' name = "access_token" value="WTRNoUmmXrjb5cGa8j83InVzPPprtJ457ibV/DpHFrb8aL9TC0U9oIii6J+Q/270"/>
		<input type="submit" value="이동"> 
	</form>
	<br>
	
	유저 핀 코드 확인
	<br>
	<form action="/api/user/auth_user_pin_code.do">
		<input type='text' name = "access_token" value="iQu6d1c2lqzbQq+tPH/4Zg0CdLgvpLzhCLtAx52Zukp87Uyld/T3yOia1h6nIYnP"/><br>
		<input type='text' name = "en_pin_code" value="123456"/><br>
		<input type="submit" value="이동"> 
	</form>
	<br>
	
	유저 핀 코드 생성
	<br>
	<form action="/api/user/create_user_pin_code.do">
		<input type='text' name = "access_token" value="iQu6d1c2lqzbQq+tPH/4ZqU0droxLTMLhCC+GhT5pvn0zyEu3g1BeC2XVxfxG4/A"/><br>
		<input type='text' name = "en_pin_code" value="scU5ZeX4Q%2B1V0UG2XgGOWg%3D%3D%0A"/><br>
		<input type="submit" value="이동"> 
	</form>
	<br>
	
	유저 핀 코드 수정
	<br>
	<form action="/api/user/update_user_pin_code.do">
		<input type='text' name = "access_token" value="iQu6d1c2lqzbQq+tPH/4Zoh0nRHC+V5bElDC3hdHfxnnah0bGfg6nL+tDyr642cZ"/><br>
		<input type='text' name = "en_pin_code" value="scU5ZeX4Q%2B1V0UG2XgGOWg%3D%3D%0A"/><br>
		<input type="submit" value="이동"> 
	</form>
	<br>
	
	
	유저 접근토큰 값(테스트)
	<br>
	<form action="/test/api/user/get.do">
		<input type='text' name = "access_token" value="iQu6d1c2lqzbQq+tPH/4ZmrED7VZDlV3w2+dTzb965GDRvkU62TMi8pXgoEwDfPZ"/><br>
		<input type="submit" value="이동"> 
	</form>
	<br>
	
	유저 토큰 생성(테스트)
	<br>
	<form action="/test/api/user/update_user_access_token.do">
		<input type='text' name = "user_email" value="jypjun12@gmail.com"/><br>
		<input type="submit" value="이동"> 
	</form>
	<br>
	
</body>
</html>