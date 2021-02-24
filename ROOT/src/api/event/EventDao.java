package api.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import api.model.BannerVo;
import db.DBConnector;

public class EventDao {
	
	private DBConnector dbConnector = DBConnector.getInstance();
	private Connection conn = null;
	private PreparedStatement pstm = null;
	private ResultSet rs = null;
	private BannerVo bannerVo;
	
	public ArrayList<BannerVo> getBanners(){
		ArrayList<BannerVo> list = new ArrayList<BannerVo>();
		String query = "SELECT * FROM banner_info";
		try {
			this.conn = this.dbConnector.getConnection();
			this.pstm = this.conn.prepareStatement(query);
			this.rs = this.pstm.executeQuery();
			while(rs.next()) {
				list.add(new BannerVo(rs.getInt("banner_id"), rs.getString("banner_thumb"), rs.getTimestamp("banner_create_at")));
			}
		}
		catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.dbConnector.freeConnection(this.conn, this.pstm, this.rs);
        }
		
		return list;
	}
}
