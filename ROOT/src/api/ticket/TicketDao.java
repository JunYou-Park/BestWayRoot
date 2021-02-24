package api.ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import db.DBConnector;

public class TicketDao {
	 
	private DBConnector dbConnector = DBConnector.getInstance();
	private Connection conn = null;
	private PreparedStatement pstm = null;
	private ResultSet rs = null;
	
	public JSONObject getUserTicket(String userEmail) {
		JSONObject json = new JSONObject();
		json.put("status", "400");
		json.put("msg", "식권 수량을 가져오지 못했습니다.");
		json.put("data", null);
		try {
			conn = dbConnector.getConnection();
			String query = "SELECT ticket_5000, ticket_4000, ticket_3500, ticket_2000 FROM ticket_info WHERE user_email = ?";
			pstm = this.conn.prepareStatement(query);
			pstm.setString(1, userEmail);
			rs = pstm.executeQuery();
			if(rs.next()) {
				JSONObject data = new JSONObject();
				JSONArray jsonArray = new JSONArray();
				
				json.put("status", "201");
				json.put("msg", "식권 수량입니다.");
				
				data.put("ticket_5000", rs.getInt("ticket_5000"));
				data.put("ticket_4000", rs.getInt("ticket_4000"));
				data.put("ticket_3500", rs.getInt("ticket_3500"));
				data.put("ticket_2000", rs.getInt("ticket_2000"));
				jsonArray.add(data);
				
				json.put("data", jsonArray);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return json;
	}
	 
}
