package api.model;

import java.sql.Date;
import java.sql.Timestamp;

public class UserVo {
    String email;
    String pw;
    String fullName;
    String phoneNumber;
    Timestamp createTime;

    public UserVo() {
    }

    public UserVo(String email, String pw, String fullName, String phoneNumber, Timestamp createTime) {
        this.email = email;
        this.pw = pw;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.createTime = createTime;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPw() {
        return this.pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Timestamp getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }


	@Override
	public String toString() {
		return "UserVo [email=" + email + ", pw=" + pw + ", fullName=" + fullName + ", phoneNumber=" + phoneNumber
				+ ", createTime=" + createTime;
	}
    
}