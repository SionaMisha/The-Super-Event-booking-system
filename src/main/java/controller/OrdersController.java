package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Model;
import model.Order;
import model.OrderItem;
import util.ErrorHandler;
import util.ValidationUtils;


/**
 * The Class OrdersController.
 */
public class OrdersController implements Initializable {
    
    /** The orders table. */
    @FXML private TableView<Order> ordersTable;
    
    /** The order id column. */
    @FXML private TableColumn<Order, String> orderIdColumn;
    
    /** The order date column. */
    @FXML private TableColumn<Order, String> orderDateColumn;
    
    /** The event summary column. */
    @FXML private TableColumn<Order, String> eventSummaryColumn;
    
    /** The total price column. */
    @FXML private TableColumn<Order, Double> totalPriceColumn;
   // @FXML private TableColumn<Order, String> confirmationCodeColumn;

    /** The order items table. */
   @FXML private TableView<OrderItem> orderItemsTable;
    
    /** The event name column. */
    @FXML private TableColumn<OrderItem, String> eventNameColumn;
    
    /** The quantity column. */
    @FXML private TableColumn<OrderItem, Integer> quantityColumn;
    
    /** The price column. */
    @FXML private TableColumn<OrderItem, Double> priceColumn;
    
    /** The total column. */
    @FXML private TableColumn<OrderItem, Double> totalColumn;

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
     * Instantiates a new orders controller.
     *
     * @param stage the stage
     * @param model the model
     */
    public OrdersController(Stage stage, Model model) {
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
    public void initialize(URL location, ResourceBundle resources) {
    	model = Model.getInstance();
    	
    	bgImage.fitWidthProperty().bind(rootPane.widthProperty());
        bgImage.fitHeightProperty().bind(rootPane.heightProperty());

        // Setup orders table columns
        orderIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                ValidationUtils.formatOrderNumber(cellData.getValue().getOrderId())
            ));
        //Date and Time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        orderDateColumn.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getOrderDate().toLocalDateTime().format(formatter)
        ));

        //orderDateColumn.setCellValueFactory(cellData -> 
          //  new SimpleStringProperty(cellData.getValue().getOrderDate().toString()));
     // Summarize event names with seats: e.g. "Concert x 2, Magic Show x 1"
        eventSummaryColumn.setCellValueFactory(cellData -> {
            StringBuilder summary = new StringBuilder();
            for (OrderItem item : cellData.getValue().getItems()) {
                if (summary.length() > 0) summary.append(", ");
                summary.append(item.getEventName()).append(" x ").append(item.getQuantity());
            }
            return new SimpleStringProperty(summary.toString());
        });
       
       //Total Price 
        totalPriceColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getTotalPrice()).asObject());
        
//        confirmationCodeColumn.setCellValueFactory(cellData -> 
//            new SimpleStringProperty(cellData.getValue().getConfirmationCode()));

        refreshOrders();
    }

    /**
     * Refresh orders.
     */
    private void refreshOrders() {
    	List<Order> orders = model.getUserOrders();
    	orders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate())); // Most recent first
    	ordersTable.setItems(FXCollections.observableArrayList(orders));
//        List<Order> orders = model.getUserOrders(); // assumed to be sorted oldest to newest
//        Collections.reverse(orders); // make newest first
//        ordersTable.setItems(FXCollections.observableArrayList(orders));
    }


    /**
     * Builds the event summary.
     *
     * @param items the items
     * @return the string
     */
    private String buildEventSummary(List<OrderItem> items) {
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : items) {
            sb.append(item.getEventName())
              .append(" (")
              .append(item.getQuantity())
              .append("), ");
        }
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2); // Remove last comma
        }
        return sb.toString();
    }
    
    /**
     * Show order items.
     *
     * @param order the order
     */
    private void showOrderItems(Order order) {
        ObservableList<OrderItem> items = FXCollections.observableArrayList(order.getItems());
        orderItemsTable.setItems(items);
    }

    /**
     * Handle back.
     */
    @FXML
    private void handleBack() {
        try {
			
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
            HomeController homeController = new HomeController(stage, model);
            loader.setController(homeController);
            Parent root = loader.load();
            homeController.showStage(root);
            
        } catch (IOException e) {
            ErrorHandler.handleError("Failed to load home view", e);
        }
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
     * Show stage.
     *
     * @param root the root
     */
    public void showStage(Parent root) {
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Orders - The Super Event");
        stage.setMaximized(true);
        stage.show();
    }
} 