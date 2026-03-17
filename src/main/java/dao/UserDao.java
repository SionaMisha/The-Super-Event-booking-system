//Defines an interface for user authentication and registration operations.
package dao;

import java.sql.SQLException;

import model.User;

/**
 * A data access object (DAO) is a pattern that provides an abstract interface 
 * to a database or other persistence mechanism. 
 * the DAO maps application calls to the persistence layer and provides some specific data operations 
 * without exposing details of the database. 
 */
public interface UserDao {
	void setup() throws SQLException; //Creates users table
	User getUser(String username, String password) throws SQLException;  //Login
	User createUser(String username, String password, String preferredName) throws SQLException;  //Register a new user
	void changePassword(String username, String newPassword) throws SQLException;
}
