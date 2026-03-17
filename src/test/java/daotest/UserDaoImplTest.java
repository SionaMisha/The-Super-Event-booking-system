package java.daotest;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.UserDao;
import dao.UserDaoImpl;
import model.User;

public class UserDaoImplTest {
    private UserDao userDao;
    
    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl();
        try {
            userDao.setup(); // Setup test database
        } catch (SQLException e) {
            fail("Database setup failed: " + e.getMessage());
        }
    }
    
    @Test
    void testLoginSuccess() throws SQLException {
        // Arrange
        String username = "siona";
        String password = "sionam";
        User loggedInUser = userDao.getUser(username, password);
        
        // Assert
        assertNotNull(loggedInUser, "Login should return a user object");
        assertEquals(username, loggedInUser.getUsername(), "Username should match");
        assertEquals(password, loggedInUser.getPassword(), "Password should match");

    }
    
    @Test
    void testLoginAdminSuccess() throws SQLException {
        // Arrange
        String username = "admin";
        String password = "Admin321";
        
        User loggedInUser = userDao.getUser(username, password);
        
     
        // Assert
        assertNotNull(loggedInUser, "Login should return a user object");
        assertEquals(username, loggedInUser.getUsername(), "Username should match");
        assertEquals(password, loggedInUser.getPassword(), "Password should match");
    }
    
    @Test
    void testLoginWrongPassword() throws SQLException {
        // Arrange
        String username = "siona";
        String correctPassword = "sionam";
        String wrongPassword = "sionamm";
        
        User loggedInUser = userDao.getUser(username, wrongPassword);
        
        assertNull(loggedInUser, "The user login failed");
    }
    
}