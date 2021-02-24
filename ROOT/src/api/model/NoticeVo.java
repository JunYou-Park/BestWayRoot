package api.model;

import java.sql.Date;
import java.sql.Timestamp;

public class NoticeVo {
	int noti_id;
	String noti_type;
	String noti_title;
	String noti_content;
	Timestamp noti_create_at;
	
	public NoticeVo() {}
	
	public NoticeVo(int noti_id, String noti_type, String noti_title, String noti_content, Timestamp noti_create_at) {
		super();
		this.noti_id = noti_id;
		this.noti_type = noti_type;
		this.noti_title = noti_title;
		this.noti_content = noti_content;
		this.noti_create_at = noti_create_at;
	}
	
	public int getNoti_id() {
		return noti_id;
	}
	public void setNoti_id(int noti_id) {
		this.noti_id = noti_id;
	}
	public String getNoti_type() {
		return noti_type;
	}
	public void setNoti_type(String noti_type) {
		this.noti_type = noti_type;
	}
	public String getNoti_title() {
		return noti_title;
	}
	public void setNoti_title(String noti_title) {
		this.noti_title = noti_title;
	}
	public String getNoti_content() {
		return noti_content;
	}
	public void setNoti_content(String noti_content) {
		this.noti_content = noti_content;
	}
	public Timestamp getNoti_create_at() {
		return noti_create_at;
	}
	public void setNoti_create_at(Timestamp noti_create_at) {
		this.noti_create_at = noti_create_at;
	}

	@Override
	public String toString() {
		return "NoticeVo [noti_id=" + noti_id + ", noti_type=" + noti_type + ", noti_title=" + noti_title
				+ ", noti_content=" + noti_content + ", noti_create_at=" + noti_create_at + "]";
	}
	
	
	
}
