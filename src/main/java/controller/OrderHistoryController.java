package controller;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Model;
import model.Order;
import model.OrderItem;
import util.ErrorHandler;
import util.ValidationUtils;


/**
 * The Class OrderHistoryController.
 */
public class OrderHistoryController {
    
    /** The order table. */
    @FXML private TableView<Order> orderTable;
    
    /** The order id column. */
    @FXML private TableColumn<Order, String> orderIdColumn;
    
    /** The date column. */
    @FXML private TableColumn<Order, String> dateColumn;
    
    /** The total column. */
    @FXML private TableColumn<Order, Double> totalColumn;
    
    /** The confirmation code column. */
    @FXML private TableColumn<Order, String> confirmationCodeColumn;
    
    /** The export button. */
    @FXML private Button exportButton;
    
    /** The back button. */
    @FXML private Button backButton;
    
    /** The root pane. */
    @FXML private StackPane rootPane;
    
    /** The model. */
    private Model model;
    
    /** The stage. */
    private Stage stage;
    
    /**
     * Instantiates a new order history controller.
     *
     * @param stage the stage
     * @param model the model
     */
    public OrderHistoryController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }
    
    /**
     * Initialize.
     */
    @FXML
    public void initialize() {
        // Setup table columns
        orderIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                ValidationUtils.formatOrderNumber(cellData.getValue().getOrderId())
            ));
            
        // Format date with proper time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getOrderDate().toLocalDateTime().format(formatter)
            ));
            
        totalColumn.setCellValueFactory(cellData -> 
            cellData.getValue().totalPriceProperty().asObject());
        confirmationCodeColumn.setCellValueFactory(cellData -> 
            cellData.getValue().confirmationCodeProperty());
        
        // Setup buttons
        exportButton.setOnAction(e -> handleExport());
        backButton.setOnAction(e -> handleBack());
        
        // Load orders and sort by date (most recent first)
        orderTable.getItems().setAll(model.getUserOrders());
        orderTable.getSortOrder().add(dateColumn);
        dateColumn.setSortType(TableColumn.SortType.DESCENDING);
        
        // Add context menu to view order details
        orderTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();
            
            MenuItem viewDetails = new MenuItem("View Order Details");
            viewDetails.setOnAction(e -> {
                Order order = row.getItem();
                if (order != null) {
                    showOrderDetails(order);
                }
            });
            
            menu.getItems().add(viewDetails);
            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(menu)
            );
            return row;
        });
    }
    
    /**
     * Handle export.
     */
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
     * Handle back.
     */
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
            HomeController homeController = new HomeController(stage, model);
            loader.setController(homeController);
            Parent root = loader.load();
            homeController.showStage(root);
        } catch (IOException e) {
            ErrorHandler.handleError("Failed to return to home", e);
        }
    }
    
    /**
     * Show order details.
     *
     * @param order the order
     */
    private void showOrderDetails(Order order) {
        StringBuilder details = new StringBuilder();
        details.append("Order #").append(ValidationUtils.formatOrderNumber(order.getOrderId())).append("\n");
        details.append("Date: ").append(order.getOrderDate().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))).append("\n");
        details.append("Confirmation Code: ").append(order.getConfirmationCode()).append("\n\n");
        details.append("Items:\n");
        
        for (OrderItem item : order.getItems()) {
            details.append("- ").append(item.getEvent().getEventName())
                  .append(" (").append(item.getQuantity()).append(" tickets)\n");
        }
        
        details.append("\nTotal: $").append(String.format("%.2f", order.getTotalPrice()));
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText(null);
        alert.setContentText(details.toString());
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
        stage.setMaximized(true);
        stage.setTitle("Order History - The Super Event");
        stage.show();
    }
} 