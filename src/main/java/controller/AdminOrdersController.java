package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.Order;
import model.OrderItem;
import util.ErrorHandler;


/**
 * The Class AdminOrdersController.
 */
public class AdminOrdersController {
    
    /** The orders table. */
    @FXML private TableView<Order> ordersTable;
    
    /** The order id column. */
    @FXML private TableColumn<Order, String> orderIdColumn;
    
    /** The username column. */
    @FXML private TableColumn<Order, String> usernameColumn;
    
    /** The date column. */
    @FXML private TableColumn<Order, String> dateColumn;
    
    /** The total column. */
    @FXML private TableColumn<Order, Double> totalColumn;
    
    /** The details column. */
    @FXML private TableColumn<Order, Void> detailsColumn;
    
    /** The back button. */
    @FXML private Button backButton;
    
    /** The model. */
    private Model model;
    
    /** The stage. */
    private Stage stage;
    
    /** The formatter. */
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    
    /**
     * Instantiates a new admin orders controller.
     *
     * @param stage the stage
     * @param model the model
     */
    public AdminOrdersController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }
    
    /**
     * Initialize.
     */
    @FXML
    public void initialize() {
        if (!model.isAdmin()) {
            ErrorHandler.showWarning("Access denied. Admin privileges required.");
            handleBack();
            return;
        }
        
        // Setup table columns
        orderIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("%04d", cellData.getValue().getOrderId())));
        usernameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsername()));
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getOrderDate().toLocalDateTime().format(formatter)));
        totalColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());
            
        // Add details button column
        detailsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button detailsButton = new Button("View Details");
            
            {
                detailsButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showOrderDetails(order);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);
                }
            }
        });
        
        // Setup back button
        backButton.setOnAction(e -> handleBack());
        
        refreshOrders();
    }
    
    /**
     * Refresh orders.
     */
    private void refreshOrders() {
        try {
            List<Order> orders = model.getAllOrders();
            ordersTable.setItems(FXCollections.observableArrayList(orders));
        } catch (SQLException e) {
            ErrorHandler.handleError("Failed to load orders", e);
        }
    }
    
    /**
     * Show order details.
     *
     * @param order the order
     */
    private void showOrderDetails(Order order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Details");
        dialog.setHeaderText("Order #" + String.format("%04d", order.getId()));
        
        VBox content = new VBox(10);
        content.getChildren().add(new Label("Username: " + order.getUsername()));
        content.getChildren().add(new Label("Order Date: " + order.getOrderDate().toLocalDateTime().format(formatter)));
        content.getChildren().add(new Label("Total: $" + String.format("%.2f", order.getTotal())));
        
        // Add order items
        content.getChildren().add(new Label("\nOrder Items:"));
        for (OrderItem item : order.getItems()) {
            HBox itemBox = new HBox(10);
            itemBox.getChildren().addAll(
                new Label(item.getEventName()),
                new Label("x" + item.getQuantity()),
                new Label("$" + String.format("%.2f", item.getPrice()))
            );
            content.getChildren().add(itemBox);
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    /**
     * Handle back.
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminDashboardView.fxml"));
            AdminDashboardController controller = new AdminDashboardController(stage, model);
            loader.setController(controller);
            Parent root = loader.load();
            controller.showStage(root);
        } catch (IOException e) {
            ErrorHandler.handleError("Failed to return to dashboard", e);
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
        stage.setTitle("Admin Orders - The Super Event");
        stage.show();
    }
} 