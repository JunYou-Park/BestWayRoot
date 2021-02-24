package util.converter;

import java.sql.Timestamp;

public class Converter {
	
	// 숫자로만 이루어진 String 값이 들어와야한다.
	public Timestamp StringtoTimestamp(String time) {
		long createAt = Long.parseLong(time);
		return new Timestamp(createAt);
	}
}
