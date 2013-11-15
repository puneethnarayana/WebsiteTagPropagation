package com.iiitb.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import com.iiitb.wtp.DocumentVector;
import com.iiitb.wtp.Global;
import com.iiitb.wtp.Parser;
import com.iiitb.wtp.Probability;
import com.iiitb.wtp.URLConnect;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
/**
 * 
 * @author Sindhu Priyadarshini
 *
 */
public class SeedHandler {

	ArrayList<String> urlList = new ArrayList<String>();
	Map<String, DocumentVector> tagDV = new HashMap<>();
	Probability prob = new Probability();
	Parser parse = new Parser();
	int totalRecords = 0;

	//MySQL connection string
	Connection conn = null;
	String dbUrl = "jdbc:mysql://localhost:3306/";
	String dbName = "wtpdb";
	String driver = "com.mysql.jdbc.Driver";
	String userName = "root";
	String password = "root";

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getUrlList() {
		return urlList;
	}

	public void setUrlList(ArrayList<String> urlList) {
		this.urlList = urlList;
	}

	// Add content to urlList
	public void addUrlList(String url) {
		urlList.add(url);
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	// Constructor: connects to database
	public SeedHandler() throws SQLException {
		connectDatabase();
		
	}

	// Connects to the database
	private void connectDatabase() {

		try {
			Class.forName(getDriver()).newInstance();
			conn = DriverManager.getConnection(getDbUrl() + getDbName(),
					getUserName(), getPassword());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The crawler is started with seed data that is present in the urlList
	 * and calculate the amount of time it takes to crawl and propagate the tags
	 */
	public void mainCrawler() {

		try {

			for (String list : urlList) {
				System.out.println(list + " list");
			}

			long startTime = System.currentTimeMillis();

			URLConnect connector = new URLConnect();
			connector.connect(urlList, tagDV);

			long endTime = System.currentTimeMillis();

			long TotalTime = ((endTime - startTime) / 1000);

			double TotalT = (double) TotalTime / 60;
			System.out.println("Total Time taken in minutes " + TotalT);

			queryHITS();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} 

	}

	// Closing the database.
	public void closeDatabase(Connection conn) throws SQLException {
		conn.close();
	}

	// Loads the urlList variable the the seeds(URLs) from database.
	public void loadData() throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;

		stmt = conn.createStatement();
		System.out.println("Started loading the data from database!");

		if (stmt.execute("SELECT * FROM seed_list;")) {
			rs = stmt.getResultSet();
		} else {
			System.err.println("select failed");
		}
		try {
			while (rs.next()) {
				urlList.add(rs.getString(2));
				String[] tags = rs.getString(3).split(",");
				for (String string : tags) {

					tagDV.put(string, parse.getWikiDV(string));
				}
			}
		} catch (ClientProtocolException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	/**
	 *  The Final Output of the suggested websites.
	 * @throws SQLException
	 */
	public void queryHITS() throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		String query = "";
		stmt = conn.createStatement();
		System.out.println("Output");

		query = "select * from HITS";

		if (stmt.execute(query)) {
			rs = stmt.getResultSet();
		} else {
			System.err.println("select failed");
		}
	
		int sno = 1;
		while (rs.next()) {
			System.out.println(sno + " " + rs.getString(2) + " "
					+ rs.getString(3));
			sno++;
		}
	}

	// Querying from seed list to serialise along with the crawled data
	public Map<String, String> querySeedData() throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		String query = "";
		Map<String, String> seedMap = new HashMap<>();
		stmt = conn.createStatement();
		System.out.println("Querying the database");

		query = "select url, tag from seed_list";

		if (stmt.execute(query)) {
			rs = stmt.getResultSet();
		} else {
			System.err.println("select failed");
		}

		while (rs.next()) {
			seedMap.put(rs.getString(1), rs.getString(2));
		}

		return seedMap;

	}

	// Inserts the seed(URL)'s into database
	public void insertData(String userUrl, String tag) throws SQLException {
		try {
			ResultSet rs = null;
			Statement stmt = conn.createStatement();
			String selectQuery = "select tag from seed_list where url = \""
					+ userUrl + "\"";
			if (stmt.execute(selectQuery)) {
				rs = stmt.getResultSet();
			} else {
				System.err.println("select failed");
			}
			String tagDB = "";
			while (rs.next()) {
				tagDB = tagDB + rs.getString(1);
			}
			String tagUpdate = "";
			if (tagDB.equalsIgnoreCase(tag)) {
				tagUpdate = tagDB;
			} else {
				tagUpdate = tagDB + ", " + tag;
			}
			String sql = "INSERT INTO seed_list(url, tag) VALUES(?,?) on duplicate key update tag = \""
					+ tagUpdate + "\"";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userUrl);
			pstmt.setString(2, tag);
			pstmt.executeUpdate();

			if (!tagDB.equals("")) {
				String[] tags = tagUpdate.split(",");
				for (String string : tags) {
					tagDV.put(string, parse.getWikiDV(string));
				}
			} else {
				String[] tags = tag.split(",");
				for (String string : tags) {
					tagDV.put(string, parse.getWikiDV(string));
				}
			}

			// Also keep the url in the arraylist
			if (!urlList.contains(userUrl))
				urlList.add(userUrl);
		
		} catch (MySQLIntegrityConstraintViolationException e) {
			e.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}


	/**
	 * Inserts the HITS data into database - output
	 * @param userUrl
	 * @param tag
	 * @throws SQLException
	 */
	public void insertHITSData(String userUrl, String tag) throws SQLException {

		try {
			String sql = "INSERT INTO HITS(url, tag) VALUES(?, ?) on duplicate key update tag = \""
					+ tag + "\"";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userUrl);
			pstmt.setString(2, tag);
			pstmt.executeUpdate();
		} catch (MySQLIntegrityConstraintViolationException e) {
			e.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * serialise the webgraph object into a file
	 * 
	 * @param objectToSerialize
	 */
	public void serializeJavaObjectToFile(Object objectToSerialize) {
		try {

			FileOutputStream fos = new FileOutputStream(Global.fileName);
			System.out.println(Global.fileName);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(objectToSerialize);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * To deserialize the object from the file
	 * 
	 * @return
	 */
	public Object deSerializeJavaObjectFromFile() {
		Object deSerializedObject = null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			File file = new File(Global.fileName);
			if (file.exists()) {
				fis = new FileInputStream(Global.fileName);
				ois = new ObjectInputStream(fis);

				deSerializedObject = ois.readObject();
			} else
				return new Object();

			System.out.println("........DeSerialized the Object.......");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return deSerializedObject;

	}

}
