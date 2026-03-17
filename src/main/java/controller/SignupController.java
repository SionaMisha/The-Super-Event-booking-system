package controller;

import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Model;
import model.User;

public class SignupController {
	@FXML private StackPane rootPane;
	@FXML private ImageView bgImage;
	
	@FXML
	private TextField username;
	@FXML
	private TextField password;
	@FXML
	private TextField preferredName;
	@FXML
	private Button createUser;
	@FXML
	private Button close;
	@FXML
	private Label status;
	
	private Stage stage;
	private Stage parentStage;
	private Model model;
	
	public SignupController(Stage parentStage, Model model) {
		this.stage = new Stage();
		this.parentStage = parentStage;
		this.model = model;
	}

	@FXML
	public void initialize() {
		// Bind the background image to fill the entire pane
		model = Model.getInstance();   // singleton access
	    bgImage.fitWidthProperty().bind(rootPane.widthProperty());
	    bgImage.fitHeightProperty().bind(rootPane.heightProperty());
	    
		createUser.setOnAction(event -> {
			if (!username.getText().isEmpty() && !password.getText().isEmpty() && !preferredName.getText().isEmpty()) {
				User user;
				try {
					user = model.getUserDao().createUser(username.getText(), password.getText(),preferredName.getText());
					if (user != null) {
						status.setText("Created " + user.getUsername());
						status.setTextFill(Color.GREEN);
					} else {
						status.setText("Cannot create user");
						status.setTextFill(Color.RED);
					}
				} catch (SQLException e) {
					status.setText(e.getMessage());
					status.setTextFill(Color.RED);
				}
				
			} else {
				status.setText("Empty username or password");
				status.setTextFill(Color.RED);
			}
		});

		close.setOnAction(event -> {
			stage.close();
			parentStage.show();
		});
	}


	
	public void showStage(Parent root) {
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setMaximized(true);
		stage.setTitle("Sign up - The Super Event");
		stage.show();
		
		// Ensure background image scales with window
		bgImage.fitWidthProperty().bind(rootPane.widthProperty());
		bgImage.fitHeightProperty().bind(rootPane.heightProperty());
	}
}
