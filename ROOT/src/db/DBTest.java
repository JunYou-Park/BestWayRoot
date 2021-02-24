//package db;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import org.json.simple.JSONObject;
//
//@WebServlet({"/db"})
//public class DBTest extends HttpServlet {
//    private DBConnector dbConnection;
//    Connection conn = null;
//    PreparedStatement pstm = null;
//    ResultSet rs = null;
//
//    public DBTest() {
//    }
//
//    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        PrintWriter out = resp.getWriter();
//        this.dbConnection = DBConnector.getInstance();
//
//        try {
//            this.conn = this.dbConnection.getConnection();
//            String query = "select * from test";
//            this.pstm = this.conn.prepareStatement(query);
//            this.rs = this.pstm.executeQuery();
//            StringBuffer sb = new StringBuffer();
//
//            while(this.rs.next()) {
//                String no = this.rs.getString("no");
//                String title = this.rs.getString("title");
//                JSONObject json = new JSONObject();
//                json.put(no, title);
//                sb.append(json);
//                sb.append("<br>");
//            }
//
//            out.println("<html><body>" + sb + "</body></html>");
//        } catch (SQLException var13) {
//            var13.printStackTrace();
//        } catch (Exception var14) {
//            var14.printStackTrace();
//        } finally {
//            this.dbConnection.freeConnection(this.conn, this.pstm, this.rs);
//        }
//
//    }
//}