import java.sql.*;
public class FileFormatParameterValidation {
  public static void main(String[] args) throws Exception {
    String url = System.getenv("SNOWFLAKE_URL");
    String user = System.getenv("SNOWFLAKE_USER");
    String password = System.getenv("SNOWFLAKE_PASSWORD");
    try (Connection conn = DriverManager.getConnection(url, user, password)) {
      // Check if FILE_FORMATS table exists in INFORMATION_SCHEMA
      System.out.println("=== FILE FORMAT PARAMETERS DISCOVERY ===");
      try (PreparedStatement stmt = conn.prepareStatement("DESCRIBE TABLE INFORMATION_SCHEMA.FILE_FORMATS")) {
        ResultSet rs = stmt.executeQuery();
        System.out.println("Available columns in INFORMATION_SCHEMA.FILE_FORMATS:");
        while (rs.next()) {
          System.out.println(rs.getString("name") + " - " + rs.getString("type"));
        }
      } catch (SQLException e) {
        System.out.println("FILE_FORMATS table not found in INFORMATION_SCHEMA: " + e.getMessage());
      }
    }
  }
}
