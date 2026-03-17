package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Model;
import model.User;
import util.ErrorHandler;


/**
 * The Class LoginController.
 */
public class LoginController implements Initializable {
	//Links UI components via @FXML
	
	/** The root pane. */
	@FXML private StackPane rootPane;
	
	/** The bg image. */
	@FXML private ImageView bgImage;
	
	/** The name. */
	@FXML
	private TextField name;
	
	/** The password. */
	@FXML
	private PasswordField password;
	
	/** The preferred name. */
	@FXML
	private PasswordField preferredName;
	
	/** The message. */
	@FXML
	private Label message;
	
	/** The login. */
	@FXML
	private Button login;
	
	/** The signup. */
	@FXML
	private Button signup;

	/** The model. */
	private Model model;
	
	/** The stage. */
	private Stage stage;
	
	/**
	 * Instantiates a new login controller.
	 *
	 * @param stage the stage
	 * @param model the model
	 */
	public LoginController(Stage stage, Model model) {
		this.stage = stage;
		this.model = model;
	}
	
/**
 * Initialize.
 *
 * @param location the location
 * @param resources the resources
 */
@Override
	//@FXML
	public void initialize(URL location, ResourceBundle resources) {	
		// Bind image size to parent StackPane for fullscreen scaling
		model = Model.getInstance();
		bgImage.fitWidthProperty().bind(rootPane.widthProperty()); 
	    bgImage.fitHeightProperty().bind(rootPane.heightProperty());
		
		login.setOnAction(event -> {
			handleLogin();
		});
		
		signup.setOnAction(event -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SignupView.fxml"));
				
				// Customize controller instance
				SignupController signupController =  new SignupController(stage,model);

				loader.setController(signupController);

				Parent root = loader.load();
				
				signupController.showStage(root);
				
				message.setText("");
				name.clear();
				password.clear();
				
			} catch (IOException e) {
				message.setText(e.getMessage());
			}});
	}
	
	/**
	 * Handle login.
	 */
	private void handleLogin() {
		String username = name.getText().trim();
		String password = this.password.getText().trim();
		
		if (username.isEmpty() || password.isEmpty()) {
			ErrorHandler.showWarning("Please enter both username and password");
			return;
		}
		
		try {
			User user = model.login(username, password);
			if (user != null) {
				// Navigate based on user type
				if (model.isAdmin()) {
					// Load admin dashboard
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminDashboardView.fxml"));
					AdminDashboardController controller = new AdminDashboardController(stage, model);
					loader.setController(controller);
					Parent root = loader.load();
					controller.showStage(root);
				} else {
					// Load home view for regular users
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
					HomeController controller = new HomeController(stage, model);
					loader.setController(controller);
					Parent root = loader.load();
					controller.showStage(root);
				}
			} else {
				ErrorHandler.showWarning("Invalid username or password");
			}
		} catch (SQLException e) {
			ErrorHandler.handleError("Login failed", e);
		} catch (IOException e) {
			ErrorHandler.handleError("Failed to load view", e);
		}
	}
	
	/**
	 * Show stage.
	 *
	 * @param root the root
	 */
	public void showStage(Parent root) {
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setMaximized(true);
		stage.setTitle("Welcome - The Super Event");
		stage.show();
		
		// Ensure background image scales with window
		bgImage.fitWidthProperty().bind(rootPane.widthProperty());
		bgImage.fitHeightProperty().bind(rootPane.heightProperty());
	}
}

