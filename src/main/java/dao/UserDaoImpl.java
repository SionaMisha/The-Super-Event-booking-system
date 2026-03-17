//Implements actual logic for storing and retrieving users from SQLite.
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.User;
import util.PasswordUtil;

public class UserDaoImpl implements UserDao {
	private final String TABLE_NAME = "users";

	public UserDaoImpl() {
	}

	//creates users table if doesnt exist and is used to ensure if table is ready
	@Override
	public void setup() throws SQLException {
		try (Connection connection = Database.getConnection();
				Statement stmt = connection.createStatement();) {
			String sql ="CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + "username VARCHAR(10) NOT NULL PRIMARY KEY, "
                    + "password VARCHAR(8) NOT NULL, "
                    + "preferredName VARCHAR(10) NOT NULL"
                    + ")";
			stmt.executeUpdate(sql);
		} 
		System.out.println("User table created");
	}
	
// Fetches a user from the database matching the provided credentials. used during login and if found, returns a User object mif not return null
	@Override
	public User getUser(String username, String password) throws SQLException {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE username = ? AND password = ?";
		try (Connection connection = Database.getConnection(); 
				PreparedStatement stmt = connection.prepareStatement(sql);) {
			stmt.setString(1, username);
			stmt.setString(2, PasswordUtil.encrypt(password));
			//stmt.setString(3,preferredName);
			
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					User user = new User();
					user.setUsername(rs.getString("username"));
					user.setPassword(rs.getString("password"));
					user.setPreferredName(rs.getString("preferredName"));
					return user;
				}
				return null;
			} 
		}
	}
// Inserts a new user record into users table and is used in signup
	@Override
	public User createUser(String username, String password, String preferredName) throws SQLException {
		String sql = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?)";
		try (Connection connection = Database.getConnection();
				PreparedStatement stmt = connection.prepareStatement(sql);) {
			stmt.setString(1, username);
			stmt.setString(2, password);
			stmt.setString(3, preferredName);

			stmt.executeUpdate();
			return new User(username, password,preferredName);
		} 
	}
	
	@Override
	public void changePassword(String username, String newPassword) throws SQLException {
		String encryptedPassword = PasswordUtil.encrypt(newPassword);
		String sql = "UPDATE users SET password = ? WHERE username = ?";
		try (Connection connection = Database.getConnection();
				PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, encryptedPassword);
			stmt.setString(2, username);
			stmt.executeUpdate();
		}
	}
}
