package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.CartItem;
import model.Model;
import model.Order;
import util.DateValidator;
import util.ErrorHandler;
import util.ValidationUtils;


/**
 * The Class CartController.
 */
public class CartController implements Initializable {

    /** The cart table. */
    @FXML private TableView<CartItem> cartTable;
    
    /** The event name column. */
    @FXML private TableColumn<CartItem, String> eventNameColumn;
    
    /** The venue column. */
    @FXML private TableColumn<CartItem, String> venueColumn;
    
    /** The day of week column. */
    @FXML private TableColumn<CartItem, String> dayOfWeekColumn;
    
    /** The price column. */
    @FXML private TableColumn<CartItem, Double> priceColumn;
    
    /** The quantity column. */
    @FXML private TableColumn<CartItem, Integer> quantityColumn;
    
    /** The total column. */
    @FXML private TableColumn<CartItem, Double> totalColumn;
    
    /** The action column. */
    @FXML private TableColumn<CartItem, Void> actionColumn;

    /** The total label. */
    @FXML private Label totalLabel;
    
    /** The confirmation code field. */
    @FXML private TextField confirmationCodeField;
    
    /** The checkout button. */
    @FXML private Button checkoutButton;
    
    /** The back button. */
    @FXML private Button backButton;
    
    /** The export button. */
    @FXML private Button exportButton;
    
    /** The root pane. */
    @FXML private StackPane rootPane;
	
	/** The bg image. */
	@FXML private ImageView bgImage;

    /** The stage. */
    private Stage stage;
    
    /** The model. */
    private Model model;

    /**
     * Instantiates a new cart controller.
     *
     * @param stage the stage
     * @param model the model
     */
    public CartController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    /**
     * Initialize.
     *
     * @param url the url
     * @param resourceBundle the resource bundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (model == null) {
            model = Model.getInstance();
        }
        
        bgImage.fitWidthProperty().bind(rootPane.widthProperty());
        bgImage.fitHeightProperty().bind(rootPane.heightProperty());
        
        cartTable.setEditable(true); // Enable inline editing
        
        eventNameColumn.setCellValueFactory(cellData -> 
            cellData.getValue().getEvent().eventNameProperty());
        venueColumn.setCellValueFactory(cellData -> 
            cellData.getValue().getEvent().venueProperty());
        dayOfWeekColumn.setCellValueFactory(cellData -> 
            cellData.getValue().getEvent().dayOfWeekProperty());
        priceColumn.setCellValueFactory(cellData -> 
            cellData.getValue().getEvent().priceProperty().asObject());
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalColumn.setCellValueFactory(data -> {
            CartItem item = data.getValue();
            double total = item.getQuantity() * item.getEvent().getPrice();
            return new SimpleDoubleProperty(total).asObject();
        });
        // Add remove button column
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");
            {
                removeButton.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    model.removeFromCart(item.getEvent());
                    refreshCart();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });
        // Allow user to edit quantity
        quantityColumn.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 100, 1);
            {
                spinner.setEditable(true);
                spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (isEditing()) {
                        CartItem item = getTableView().getItems().get(getIndex());
                        try {
                            model.updateCartItem(item.getEvent(), newVal);
                            updateTotal();
                        } catch (SQLException e) {
                            ErrorHandler.showWarning(e.getMessage());
                            spinner.getValueFactory().setValue(oldVal);
                        }
                    }
                });
            }
            
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    spinner.getValueFactory().setValue(quantity);
                    setGraphic(spinner);
                }
            }
        });

        // Add context menu for remove
        cartTable.setRowFactory(tv -> {
            TableRow<CartItem> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();

            MenuItem removeItem = new MenuItem("Remove from Cart");
            removeItem.setOnAction(e -> {
                CartItem item = row.getItem();
                if (item != null) {
                    model.removeFromCart(item.getEvent());
                    refreshCart();
                }
            });

            menu.getItems().add(removeItem);
            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(menu)
            );
            return row;
        });
        System.out.println("CartController initialized");
        refreshCart();
    }

    /**
     * Refresh cart.
     */
    private void refreshCart() {
        try {
            if (model == null || model.getCurrentUser() == null) {
                ErrorHandler.showWarning("Please log in to view your cart");
                handleBack(null);
                return;
            }
            
            ObservableList<CartItem> cartItems = FXCollections.observableArrayList(model.getCart());
            cartTable.setItems(cartItems);
            updateTotal();
        } catch (IllegalStateException e) {
            ErrorHandler.showWarning(e.getMessage());
            handleBack(null);
        } catch (Exception e) {
            ErrorHandler.handleError("Failed to load cart", e);
        }
    }

    /**
     * Update total.
     */
    private void updateTotal() {
        try {
            if (model != null && model.getCurrentUser() != null) {
                totalLabel.setText(String.format("Total: $%.2f", model.getCartTotal()));
            }
        } catch (Exception e) {
            ErrorHandler.handleError("Failed to update total", e);
        }
    }

    /**
     * Handle back.
     *
     * @param event the event
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
            HomeController homeController = new HomeController(stage, model);
            loader.setController(homeController);
            Parent root = loader.load();
            
            // Show the home view with dashboard
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Home - The Super Event");
            stage.setMaximized(true);
            stage.show();
            
        } catch (IOException e) {
            ErrorHandler.handleError("Failed to load home view", e);
        }
    }

    /**
     * Handle checkout.
     *
     * @param event the event
     */
    @FXML
    private void handleCheckout(ActionEvent event) {
        if (model.getCart().isEmpty()) {
            ErrorHandler.showWarning("Your cart is empty");
            return;
        }
        
        // Validate all items in cart for date restrictions
        for (CartItem item : model.getCart()) {
            try {
                if (!DateValidator.isValidBookingDay(item.getEvent().getDayOfWeek())) {
                    ErrorHandler.showWarning("Cannot book events for past days or beyond one week from today");
                    return;
                }
            } catch (Exception e) {
                ErrorHandler.handleError("Validation error", e);
                return;
            }
        }
        
        // Show total price confirmation
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Purchase");
        confirmDialog.setHeaderText("Total Amount: $" + String.format("%.2f", model.getCartTotal()));
        confirmDialog.setContentText("Do you want to proceed with the purchase?");
        
        if (confirmDialog.showAndWait().get() != ButtonType.OK) {
            return;
        }
        
        // Show confirmation code dialog
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Checkout");
        dialog.setHeaderText("Enter 6-digit confirmation code");
        dialog.setContentText("Code:");
        
        dialog.showAndWait().ifPresent(code -> {
            try {
                // Validate 6-digit confirmation code
                if (!ValidationUtils.isValidConfirmationCode(code)) {
                    ErrorHandler.showWarning("Invalid confirmation code. Please enter exactly 6 digits (e.g., 230134)");
                    return;
                }
                
                // Process checkout
                Order order = model.checkout(code);
                
                // Show success message with formatted order number
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Order Successful");
                successAlert.setHeaderText("Order #" + ValidationUtils.formatOrderNumber(order.getOrderId()));
                successAlert.setContentText("Your order has been placed successfully!");
                successAlert.showAndWait();
                
                // Navigate back to home view
                handleBack(event);
                
            } catch (SQLException e) {
                ErrorHandler.handleError("Checkout failed", e);
            }
        });
    }

    /**
     * Handle export.
     */
    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Orders");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                model.exportOrders(file.getAbsolutePath());
                ErrorHandler.showInfo("Orders exported successfully!");
            } catch (IOException e) {
                ErrorHandler.handleError("Failed to export orders", e);
            }
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
        stage.setTitle("Your Cart - The Super Event");
        stage.setMaximized(true);
        stage.show();
    }
}
