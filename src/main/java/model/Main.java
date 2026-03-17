package model;
import java.io.IOException;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import controller.LoginController;


/**
 * The Class Main.
 */
public class Main extends Application {
	
	/** The model. */
	private Model model;  //holds an instance of Model class that manages the current user and provides access to DAO for user related database operations.

	/**
	 * Inits the.
	 */
	@Override
	public void init() {   //initializes Model, which connects to the database and sets up DAO's
		//model = new Model(); 
	}

	/**
	 * Start.
	 *
	 * @param primaryStage the primary stage
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			//model = new Model().getInstance(); 
			model = Model.getInstance();
			model.setup();   //sets up the database tables using UserDaoImpl.setup() and makes sure user table exists before login/signup
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));   //loads the FXML UI layout
			
			// Customize controller instance
			LoginController loginController = new LoginController(primaryStage, model); // sets loginController an injects model and stage

			loader.setController(loginController);
			StackPane root = loader.load();
			  // Set the scene
	        Scene scene = new Scene(root);   //Creates new scene using loaded layout
	        primaryStage.setScene(scene);

	        // Make the window maximized
	        primaryStage.setMaximized(true);  // This ensures the app opens in full screen
	        primaryStage.setTitle("The Super Event");
	        primaryStage.show();
		
	        
			
		} catch (IOException | SQLException | RuntimeException e) {
			Scene scene = new Scene(new Label(e.getMessage()), 200, 100);
			primaryStage.setTitle("Error");
			primaryStage.setScene(scene);
			primaryStage.show();
		}
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		launch(args);  //Launches the JavaFX app
	}
}
