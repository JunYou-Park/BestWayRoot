package api.fcm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import api.model.NoticeVo;
import db.DBConnector;

public class FcmDao {
	private DBConnector dbConnector = DBConnector.getInstance();
	private Connection conn = null;
	private PreparedStatement pstm = null;
	private ResultSet rs = null;
	private NoticeVo noticeVo;
	
	public FcmDao() {}
	
	public ArrayList<NoticeVo> getNotifications() {
		ArrayList<NoticeVo> list = new ArrayList<>();
		try {
			conn = this.dbConnector.getConnection();
			String query = "SELECT * FROM noti_info";
			pstm = this.conn.prepareStatement(query);
			rs = this.pstm.executeQuery();
			while(rs.next()) {
				noticeVo = new NoticeVo(rs.getInt("noti_id"), rs.getString("noti_type"), rs.getString("noti_title"), rs.getString("noti_content"), rs.getTimestamp("noti_create_at"));
				list.add(noticeVo);
				System.out.println(noticeVo.toString());
			}
			
		}catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.dbConnector.freeConnection(this.conn, this.pstm, this.rs);
        }
		
		return list;
		
	}
}
