package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import dao.EventDaoImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.CartItem;
import model.Event;
import model.Model;
import util.DateValidator;
import util.ErrorHandler;
import util.ValidationUtils;


/**
 * The Class HomeController.
 */
public class HomeController implements Initializable {
    
    /** The welcome label. */
    @FXML private Label welcomeLabel;
    
    /** The event table. */
    @FXML private TableView<Event> eventTable;
    
    /** The eventname column. */
    @FXML private TableColumn<Event, String> eventnameColumn;
    
    /** The venue column. */
    @FXML private TableColumn<Event, String> venueColumn;
    
    /** The day of week column. */
    @FXML private TableColumn<Event, String> dayOfWeekColumn;
    
    /** The price column. */
    @FXML private TableColumn<Event, Double> priceColumn;
    
    /** The tickets sold column. */
    @FXML private TableColumn<Event, Integer> ticketsSoldColumn;
    
    /** The total tickets column. */
    @FXML private TableColumn<Event, Integer> totalTicketsColumn;
    
    /** The view cart button. */
    @FXML private Button viewCartButton;
    
    /** The add to cart button. */
    @FXML private Button addToCartButton;
    
    /** The view orders button. */
    @FXML private Button viewOrdersButton;
    
    /** The logout button. */
    @FXML private Button logoutButton;
    
    /** The cart count label. */
    @FXML private Label cartCountLabel;
    
    /** The view profile. */
    @FXML private MenuItem viewProfile;
    
    /** The update profile. */
    @FXML private MenuItem updateProfile;
    
    /** The root pane. */
    @FXML private StackPane rootPane;
    
    /** The bg image. */
    @FXML private ImageView bgImage;
    
    /** The event dao. */
    private EventDaoImpl eventDao;
    
    /** The model. */
    private Model model;
    
    /** The stage. */
    private Stage stage;

   
    /**
     * Instantiates a new home controller.
     *
     * @param stage the stage
     * @param model the model
     */
    // Constructor with parameters for manual instantiation
    public HomeController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
        try {
            this.eventDao = new EventDaoImpl();
        } catch (SQLException e) {
            ErrorHandler.handleError("Failed to initialize EventDao", e);
        }
    }

//    // Method to set stage and model after FXML loading
//    public void setStageAndModel(Stage stage, Model model) {
//        this.stage = stage;
//        this.model = model;
//        // Call update methods after model is set
//        updateView();
//        updateCartCount();
//        if (model != null && model.getCurrentUser() != null) {
//            welcomeLabel.setText("Welcome, " + model.getCurrentUser().getPreferredName() + "!");
//        }
//    }

    /**
 * Initialize.
 *
 * @param location the location
 * @param resources the resources
 */
@Override
    public void initialize(URL location, ResourceBundle resources) {
        // setup table columns
    	model = Model.getInstance();
    	updateView();
    	
    	if (model != null && model.getCurrentUser() != null) {
            welcomeLabel.setText("Welcome, " + model.getCurrentUser().getPreferredName()); //displays welcome message once logged in
        }

    	
        eventnameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        venueColumn.setCellValueFactory(new PropertyValueFactory<>("venue"));
        dayOfWeekColumn.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        ticketsSoldColumn.setCellValueFactory(new PropertyValueFactory<>("tickets_sold"));
        totalTicketsColumn.setCellValueFactory(new PropertyValueFactory<>("total_tickets"));

        // Bind background image
        if (bgImage != null && rootPane != null) {
            bgImage.fitWidthProperty().bind(rootPane.widthProperty());
            bgImage.fitHeightProperty().bind(rootPane.heightProperty());
        }

        // Setup menu actions
        if (viewProfile != null) {
            viewProfile.setOnAction(e -> handleViewProfile(e));
        }
        if (updateProfile != null) {
            updateProfile.setOnAction(e -> handleUpdateProfile(e));
        }
        if (logoutButton != null) {
            logoutButton.setOnAction(e -> handleLogout(e));
        }
    }

    /**
     * Update view.
     */
    private void updateView() {
        //if (model == null) return; // Guard clause
        
        try {
            eventDao.setup();  // Ensure table exists
            List<Event> events = eventDao.getEvents();

            if (events.isEmpty()) {
                // Load from file only if DB is empty
                ArrayList<Event> fileEvents = eventDao.readEventsFromFile("events.dat");
                eventDao.loadEvents(fileEvents);
                events = eventDao.getEvents();  // use these to show in table
                System.out.println("Events file loaded to table");
            }

            ObservableList<Event> observableEvents = FXCollections.observableArrayList(events);
            eventTable.setItems(observableEvents);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update cart count.
     */
    private void updateCartCount() {
        if (model == null || cartCountLabel == null) return; // Guard clause
        
        int totalItems = model.getCart().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        cartCountLabel.setText("(" + totalItems + ")");
    }
    
    /**
     * Handle view profile.
     *
     * @param event the event
     */
    @FXML
    private void handleViewProfile(ActionEvent event) {
        System.out.println("View profile clicked");
    }
    
    
    /**
     * Handle update profile.
     *
     * @param event the event
     */
    @FXML
    private void handleUpdateProfile(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Profile");
        dialog.setHeaderText("Update your profile information");

        // Create the custom dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField currentPassword = new PasswordField();
        PasswordField newPassword = new PasswordField();
        PasswordField confirmPassword = new PasswordField();

        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(currentPassword, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPassword, 1, 1);
        grid.add(new Label("Confirm New Password:"), 0, 2);
        grid.add(confirmPassword, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Add buttons to the dialog
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Show the dialog and wait for user input
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == updateButtonType) {
            String currentPwd = currentPassword.getText();
            String newPwd = newPassword.getText();
            String confirmPwd = confirmPassword.getText();

            // Validate inputs
            if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "All fields are required");
                return;
            }

            if (!newPwd.equals(confirmPwd)) {
                showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match");
                return;
            }

            if (!ValidationUtils.validatePassword(newPwd)) {
                showAlert(Alert.AlertType.ERROR, "Error", ValidationUtils.getPasswordRequirements());
                return;
            }

            try {
                // Verify current password
                if (!model.validateCurrentPassword(model.getCurrentUser().getUsername(), currentPwd)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect");
                    return;
                }

                // Update password
                model.changePassword(newPwd);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password: " + e.getMessage());
            }
        }
    }
    
    
    /**
     * Handle logout.
     *
     * @param event the event
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Clear current user and cart
            model.setCurrentUser(null);
            
            // Load login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            LoginController loginController = new LoginController(stage, model);
            loader.setController(loginController);
            
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login - The Super Event");
            stage.setMaximized(true);
        } catch (IOException e) {
            ErrorHandler.handleError("Failed to logout", e);
        }
    }

	
    /**
     * Handle cart view.
     *
     * @param event the event
     */
    @FXML
    private void handleCartView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CartView.fxml"));
            CartController cartController = new CartController(stage, model);
            loader.setController(cartController);
            Parent root = loader.load();
            cartController.showStage(root);
            
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open Cart").showAndWait();
        }
    }
    
    
    
    
    /**
     * Handle add to cart.
     *
     * @param event the event
     */
    @FXML
    private void handleAddToCart(ActionEvent event) {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an event first.");
            return;
        }
        
        // Validate event booking day BEFORE prompting for quantity
        String eventDay = selectedEvent.getDayOfWeek(); // e.g., "Monday"
        if (!DateValidator.isValidBookingDay(eventDay)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Booking Date",
                      "You cannot book events scheduled before today. " +
                      "This event is on: " + eventDay);
            return;
        }
        

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Enter quantity for " + selectedEvent.getEventName());
        dialog.setContentText("Quantity:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int quantity = Integer.parseInt(result.get());
                if (quantity <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Quantity must be positive.");
                    return;
                }
                int available = selectedEvent.getAvailableTickets();
                if (quantity > available) {
                    showAlert(Alert.AlertType.WARNING, "Not enough tickets",
                              "Only " + available + " tickets are available.");
                    return;
                }
                model.addOrUpdateItem(selectedEvent, quantity);
                updateCartCount();
                showAlert(Alert.AlertType.INFORMATION, "Added to Cart", 
                         quantity + " tickets added for " + selectedEvent.getEventName());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number.");
            }	
        }
    }
    
	
    /**
     * Handle view orders.
     *
     * @param event the event
     */
    @FXML
    private void handleViewOrders(ActionEvent event) {
        try {
        	URL fxmlUrl = getClass().getResource("/view/OrdersView.fxml");
        	System.out.println("FXML URL: " + fxmlUrl);

        	FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OrdersView.fxml"));
            OrdersController orderController = new OrdersController(stage, model);
            loader.setController(orderController);
            Parent root = loader.load();
        	orderController.showStage(root);  // display HomeView
        
        } catch (IOException e) {
        	e.printStackTrace();  // add this
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open orders: " + e.getMessage());
        }
    }

    /**
     * Show alert.
     *
     * @param type the type
     * @param title the title
     * @param message the message
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show stage.
     *
     * @param root the root
     */
    public void showStage(Parent root) {
        Scene scene = new Scene(root); 
        stage.setScene(scene);
        stage.setTitle("Home - The Super Event");
        stage.setMaximized(true);
        stage.show();
        
        // Ensure background image scales with window
        bgImage.fitWidthProperty().bind(rootPane.widthProperty());
        bgImage.fitHeightProperty().bind(rootPane.heightProperty());
    }
}