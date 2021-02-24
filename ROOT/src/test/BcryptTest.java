package test;

import org.mindrot.jbcrypt.BCrypt;

public class BcryptTest {

	public static void main(String[] args) {
		String pw = "test123!";
		String wrong = "123456789";
		// 위 비밀번호의 BCrypt 알고리즘 해쉬 생성
		// passwordHashed 변수는 실제 데이터베이스에 저장될 60바이트의 문자열이 된다.
//		String passwordHashed = BCrypt.hashpw(pw, BCrypt.gensalt());

		// 위 문장은 아래와 같다. 숫자가 높아질수록 해쉬를 생성하고 검증하는 시간은 느려진다.
		// 즉, 보안이 우수해진다. 하지만 그만큼 응답 시간이 느려지기 때문에 적절한 숫자를 선정해야 한다. 기본값은 10이다.
		String passwordHashed = BCrypt.hashpw(pw, BCrypt.gensalt(15));
		System.out.println("passwordHashed: " + passwordHashed);
		
		// 생성된 해쉬를 원래 비밀번호로 검증한다. 맞을 경우 true를 반환한다.
		// 주로 회원 로그인 로직에서 사용된다.
		boolean isValidPassword = BCrypt.checkpw(pw, "$2a$15$zHHZ3TLTBrRhhZWjH0dn7.lCnankBz2auVXG6Ii3nMyUuVVPxc42e");
		
		System.out.println(isValidPassword);
	}

}
