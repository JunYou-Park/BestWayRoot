package api.menu;

import api.model.MenuVo;
import db.DBConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public class MenuDao {
    private DBConnector dbConnector = DBConnector.getInstance();
    
    private MenuVo menuVo;

    public MenuDao() {}
   
    public ArrayList<MenuVo> getToDayMenuList(Date toDayMenuDate) {
    	Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
    	
        ArrayList<Integer> idList = new ArrayList();
        ArrayList<MenuVo> list = new ArrayList();
        java.sql.Date sqlDate = new java.sql.Date(toDayMenuDate.getTime());
        System.out.println("sqlDate: " + sqlDate);
        String query = "SELECT today_menu_items FROM today_menu_info WHERE today_menu_date = ?";

        try {
            conn = this.dbConnector.getConnection();
            pstm = conn.prepareStatement(query);
            pstm.setDate(1, sqlDate);
            rs = pstm.executeQuery();
            if (rs.next()) {
                idList = getMenuIdList(idList, rs.getString("today_menu_items"));
                String query2 = "SELECT * FROM menu_info WHERE menu_id = ? ";
                pstm = conn.prepareStatement(query2);

                for(int i = 0; i < idList.size(); ++i) {
                    pstm.setInt(1, (Integer)idList.get(i));
                    rs = pstm.executeQuery();

                    while(rs.next()) {
                        this.menuVo = new MenuVo(rs.getInt("menu_id"), rs.getString("menu_name"), rs.getString("menu_summary"), rs.getInt("menu_price"), rs.getString("menu_thumb"), rs.getTimestamp("menu_create_at"));
                        list.add(this.menuVo);
                    }
                }
            }
        } catch (SQLException var12) {
            var12.printStackTrace();
        } catch (Exception var13) {
            var13.printStackTrace();
        } finally {
            this.dbConnector.freeConnection(conn, pstm, rs);
        }

        return list;
    }

    private ArrayList<Integer> getMenuIdList(ArrayList<Integer> list, String idString) {
        StringBuilder sb = new StringBuilder(idString);
        if (!idString.equals("")) {
            if (idString.contains(",")) {
                list.add(Integer.parseInt(sb.substring(0, sb.indexOf(",")).toString()));
                sb.replace(0, sb.indexOf(",") + 1, "");
                System.out.println("sb: " + sb);
            } else {
                list.add(Integer.parseInt(sb.toString()));
                sb.replace(0, sb.length(), "");
            }

            System.out.println("sb: " + sb);
            return this.getMenuIdList(list, sb.toString());
        } else {
            return list;
        }
    }
}
