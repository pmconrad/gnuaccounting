package dataLayer;

import java.sql.Connection;
import javax.persistence.EntityManager;

//   1. source http://www.tutorials.de/forum/java/268066-kleines-beispiel-zur-verwendung-der-derby-db-java-6-a.html
//      /**
//   2.
//       *
//   3.
//       */
//   4.
//      package de.tutorials;
//   5.
//       
//   6.
//      import java.sql.Connection;
//   7.
//      import java.sql.DriverManager;
//   8.
//      import java.sql.PreparedStatement;
//   9.
//      import java.sql.ResultSet;
//  10.
//      import java.sql.SQLException;
//  11.
//      import java.sql.Statement;
//  12.
//      import java.util.Properties;
//  13.
//       
//  14.
//      /**
//  15.
//       * @author Tom
//  16.
//       */
//  17.
//      public class DerbyEmbeddedDatabaseExample {
//  18.
//       
//  19.
//        /**
//  20.
//         * @param args
//  21.
//         */
//  22.
//        public static void main(String[] args) throws Exception {
//  23.
//          Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
//  24.
//          Properties properties = new Properties();
//  25.
//          properties.put("user", "user1");
//  26.
//          properties.put("password", "user1");
//  27.
//          Connection connection = DriverManager.getConnection("jdbc:derby:c:/TEMP/tutorialsDB;create=true", properties);
//  28.
//       
//  29.
//          createTableTestIfItDoesntExistYet(connection);
//  30.
//          populateTableTestIfItHasNotBeenPopulatedYet(connection);
//  31.
//          showContentsOfTableTest(connection);
//  32.
//         
//  33.
//          connection.close();
//  34.
//        }
//  35.
//       
//  36.
//        /**
//  37.
//         * @param connection
//  38.
//         * @throws SQLException
//  39.
//         */
//  40.
//        private static void showContentsOfTableTest(Connection connection) throws SQLException {
//  41.
//          Statement statement = connection.createStatement();
//  42.
//          ResultSet resultSet = statement.executeQuery("SELECT * FROM test");
//  43.
//          int columnCnt = resultSet.getMetaData().getColumnCount();
//  44.
//          boolean shouldCreateTable = true;
//  45.
//          while (resultSet.next() && shouldCreateTable) {
//  46.
//            for(int i = 1; i <= columnCnt;i++){
//  47.
//              System.out.print(resultSet.getString(i) +  " ");
//  48.
//            }
//  49.
//            System.out.println();
//  50.
//          }
//  51.
//          resultSet.close();
//  52.
//          statement.close();
//  53.
//        }
//  54.
//       
//  55.
//        private static void populateTableTestIfItHasNotBeenPopulatedYet(Connection connection) throws Exception {
//  56.
//       
//  57.
//          boolean shouldPopulateTable = true;
//  58.
//          Statement statement = connection.createStatement();
//  59.
//          ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM test");
//  60.
//          if (resultSet.next()) {
//  61.
//            shouldPopulateTable = resultSet.getInt(1) == 0;
//  62.
//          }
//  63.
//          resultSet.close();
//  64.
//          statement.close();
//  65.
//       
//  66.
//          if (shouldPopulateTable) {
//  67.
//            System.out.println("Populating Table test...");
//  68.
//            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO test VALUES (?,?)");
//  69.
//            String[] data = { "AAA", "BBB", "CCC", "DDD", "EEE" };
//  70.
//            for (int i = 0; i < data.length; i++) {
//  71.
//              preparedStatement.setInt(1, i);
//  72.
//              preparedStatement.setString(2, data[i]);
//  73.
//              preparedStatement.execute();
//  74.
//            }
//  75.
//            preparedStatement.close();
//  76.
//          }
//  77.
//        }
//  78.
//       
//  79.
//       
//  80.
//        private static void createTableTestIfItDoesntExistYet(Connection connection) throws Exception {
//  81.
//          ResultSet resultSet = connection.getMetaData().getTables("%", "%", "%", new String[] { "TABLE" });
//  82.
//          int columnCnt = resultSet.getMetaData().getColumnCount();
//  83.
//          boolean shouldCreateTable = true;
//  84.
//          while (resultSet.next() && shouldCreateTable) {
//  85.
//            if (resultSet.getString("TABLE_NAME").equalsIgnoreCase("TEST")) {
//  86.
//              shouldCreateTable = false;
//  87.
//            }
//  88.
//          }
//  89.
//          resultSet.close();
//  90.
//          if (shouldCreateTable) {
//  91.
//            System.out.println("Creating Table test...");
//  92.
//            Statement statement = connection.createStatement();
//  93.
//            statement.execute("create table test (id int not null, data varchar(32))");
//  94.
//            statement.close();
//  95.
//          }
//  96.
//        }
//  97.
//      }
public class DB {
	// this is the database connection
	static private Connection con = null;
	// this is the hibernate session
	private static EntityManager em;
	static private final String DBVersionString = "0.8.x"; //$NON-NLS-1$
	static private final double DBVersionDouble = 0.89; 

	/*
	 * DBVersionString and DBVersionDouble are the database version: if changes
	 * to the DB are introduced that break the DB, the numbers will be increased
	 * here
	 */

	public static Connection getConnection() {
		return con;
	}

	public static void setConnection(Connection con) {
		DB.con = con;
	}

	public static void setEntityManager(EntityManager em) {
		DB.em = em;
	}

	public static EntityManager getEntityManager() {
		return DB.em;
	}

	public static String getDBVersionString() {
		return DBVersionString;
	}

	public static double getDBVersionDouble() {
		return DBVersionDouble;
	}

}
