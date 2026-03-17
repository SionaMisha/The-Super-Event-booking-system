//Represents a user of the system (someone who signs up or logs in).
package model;

public class User {
	private String username;
	private String password;
	private String preferredName;


	public User() {
	}
	
	public User(String username, String password, String preferredName) {
		this.username = username;
		this.password = password;
		this.preferredName = preferredName;
	}

	//Allow access and updates to user details. Used during login, signup, and personalization.
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	public String getPreferredName() {
		return preferredName;
	}

	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}
}
