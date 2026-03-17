package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Event;
import model.Model;
import util.ErrorHandler;


/**
 * The Class AdminDashboardController.
 */
public class AdminDashboardController {
	
	/** The events table. */
	@FXML
	private TableView<Event> eventsTable;
	
	/** The event name column. */
	@FXML
	private TableColumn<Event, String> eventNameColumn;
	
	/** The venue column. */
	@FXML
	private TableColumn<Event, String> venueColumn;
	
	/** The day column. */
	@FXML
	private TableColumn<Event, String> dayColumn;
	
	/** The price column. */
	@FXML
	private TableColumn<Event, Double> priceColumn;
	
	/** The capacity column. */
	@FXML
	private TableColumn<Event, Integer> capacityColumn;
	
	/** The status column. */
	@FXML
	private TableColumn<Event, String> statusColumn;
	
	/** The action column. */
	@FXML
	private TableColumn<Event, Void> actionColumn;

	/** The event name field. */
	@FXML
	private TextField eventNameField;
	
	/** The venue field. */
	@FXML
	private TextField venueField;
	
	/** The day combo box. */
	@FXML
	private ComboBox<String> dayComboBox;
	
	/** The price field. */
	@FXML
	private TextField priceField;
	
	/** The capacity field. */
	@FXML
	private TextField capacityField;
	
	/** The add event button. */
	@FXML
	private Button addEventButton;
	
	/** The view orders button. */
	@FXML
	private Button viewOrdersButton;
	
	/** The logout button. */
	@FXML
	private Button logoutButton;
	
	/** The root pane. */
	@FXML
	private StackPane rootPane;

	/** The background image. */
	@FXML private ImageView bgImage;

	/** The model. */
	private Model model;
	
	/** The stage. */
	private Stage stage;

	/**
	 * Instantiates a new admin dashboard controller.
	 *
	 * @param stage the stage
	 * @param model the model
	 */
	public AdminDashboardController(Stage stage, Model model) {
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
			handleLogout();
			return;
		}

		// Initialize day combo box
		dayComboBox.setItems(FXCollections.observableArrayList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));

		// Setup table columns
		eventNameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
		venueColumn.setCellValueFactory(new PropertyValueFactory<>("venue"));
		dayColumn.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
		priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
		capacityColumn.setCellValueFactory(new PropertyValueFactory<>("total_tickets"));
		statusColumn.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().isEnabled() ? "Enabled" : "Disabled"));

		// hide event name if already displayed earlier
		eventNameColumn.setCellFactory(column -> new TableCell<Event, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || item == null) {
					setText(null);
					return;
				}

				// Get the current row index
				int rowIndex = getIndex();
				Event currentEvent = getTableView().getItems().get(rowIndex);

				// Check if this is the first row or if the event name and venue are different
				// from the previous row
				if (rowIndex == 0 || !item.equals(getTableView().getItems().get(rowIndex - 1).getEventName())
						|| !currentEvent.getVenue().equals(getTableView().getItems().get(rowIndex - 1).getVenue())) {
					setText(item);
				} else {
					setText(""); // Empty text for duplicate event names
				}
			}
		});

		// hide venue if already displayed earlier
		venueColumn.setCellFactory(column -> new TableCell<Event, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || item == null) {
					setText(null);
					return;
				}

				// Get the current row index
				int rowIndex = getIndex();
				Event currentEvent = getTableView().getItems().get(rowIndex);

				// Check if this is the first row or if the event name and venue are different
				// from the previous row
				if (rowIndex == 0
						|| !currentEvent.getEventName()
								.equals(getTableView().getItems().get(rowIndex - 1).getEventName())
						|| !item.equals(getTableView().getItems().get(rowIndex - 1).getVenue())) {
					setText(item);
				} else {
					setText(""); // Empty text for duplicate venues
				}
			}
		});

		// Add action buttons column
		actionColumn.setCellFactory(col -> new TableCell<>() {
			private final Button editButton = new Button("Edit");
			private final Button toggleButton = new Button("Disable");
			private final Button deleteButton = new Button("Delete");
			private final HBox buttons = new HBox(5, editButton, toggleButton, deleteButton);

			{
				editButton.setOnAction(e -> {
					Event event = getTableView().getItems().get(getIndex());
					showEditDialog(event);
				});

				toggleButton.setOnAction(e -> {
					Event event = getTableView().getItems().get(getIndex());
					try {
						if (event.isEnabled()) {
							model.disableEvent(event.getId());
							toggleButton.setText("Enable");
						} else {
							model.enableEvent(event.getId());
							toggleButton.setText("Disable");
						}
						refreshEvents();
					} catch (SQLException ex) {
						ErrorHandler.handleError("Failed to toggle event status", ex);
					}
				});

				deleteButton.setOnAction(e -> {
					Event event = getTableView().getItems().get(getIndex());
					showDeleteConfirmation(event);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					Event event = getTableView().getItems().get(getIndex());
					toggleButton.setText(event.isEnabled() ? "Disable" : "Enable");
					setGraphic(buttons);
				}
			}
		});

		// Setup buttons
		addEventButton.setOnAction(e -> handleAddEvent());
		viewOrdersButton.setOnAction(e -> handleViewOrders());
		logoutButton.setOnAction(e -> handleLogout());

		refreshEvents();
	}

	/**
	 * Refresh events.
	 */
	private void refreshEvents() {
		try {
			List<Event> events = model.getAdminEvents();
			eventsTable.setItems(FXCollections.observableArrayList(events));
		} catch (SQLException e) {
			ErrorHandler.handleError("Failed to load events", e);
		}
	}

	/**
	 * Handle add event.
	 */
	private void handleAddEvent() {
		try {
			String name = eventNameField.getText().trim();
			String venue = venueField.getText().trim();
			String day = dayComboBox.getValue();
			double price = Double.parseDouble(priceField.getText().trim());
			int capacity = Integer.parseInt(capacityField.getText().trim());

			if (name.isEmpty() || venue.isEmpty() || day == null) {
				ErrorHandler.showWarning("Please fill in all fields");
				return;
			}

			Event event = new Event(0, name, venue, day, price, 0, capacity, true);
			model.addEvent(event);

			// Clear form
			eventNameField.clear();
			venueField.clear();
			dayComboBox.setValue(null);
			priceField.clear();
			capacityField.clear();

			refreshEvents();
			ErrorHandler.showInfo("Event added successfully");

		} catch (NumberFormatException e) {
			ErrorHandler.showWarning("Please enter valid numbers for price and capacity");
		} catch (SQLException e) {
			ErrorHandler.handleError("Failed to add event", e);
		}
	}

	/**
	 * Show edit dialog.
	 *
	 * @param event the event
	 */
	private void showEditDialog(Event event) {
		Dialog<Event> dialog = new Dialog<>();
		dialog.setTitle("Edit Event");
		dialog.setHeaderText("Edit Event Details");

		// Create the custom dialog content
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);

		TextField nameField = new TextField(event.getEventName());
		TextField venueField = new TextField(event.getVenue());
		ComboBox<String> dayBox = new ComboBox<>(
				FXCollections.observableArrayList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));
		dayBox.setValue(event.getDayOfWeek());
		TextField priceField = new TextField(String.valueOf(event.getPrice()));
		TextField capacityField = new TextField(String.valueOf(event.getTotal_tickets()));

		grid.add(new Label("Event Name:"), 0, 0);
		grid.add(nameField, 1, 0);
		grid.add(new Label("Venue:"), 0, 1);
		grid.add(venueField, 1, 1);
		grid.add(new Label("Day:"), 0, 2);
		grid.add(dayBox, 1, 2);
		grid.add(new Label("Price:"), 0, 3);
		grid.add(priceField, 1, 3);
		grid.add(new Label("Capacity:"), 0, 4);
		grid.add(capacityField, 1, 4);

		dialog.getDialogPane().setContent(grid);

		ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == saveButtonType) {
				try {
					Event updatedEvent = new Event(event.getId(), nameField.getText().trim(),
							venueField.getText().trim(), dayBox.getValue(),
							Double.parseDouble(priceField.getText().trim()), event.getTickets_sold(),
							Integer.parseInt(capacityField.getText().trim()), true);
					model.updateEvent(updatedEvent);
					refreshEvents();
					return updatedEvent;
				} catch (NumberFormatException e) {
					ErrorHandler.showWarning("Please enter valid numbers for price and capacity");
				} catch (SQLException e) {
					ErrorHandler.handleError("Failed to update event", e);
				}
			}
			return null;
		});

		dialog.showAndWait();
	}

	/**
	 * Show delete confirmation.
	 *
	 * @param event the event
	 */
	private void showDeleteConfirmation(Event event) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Delete Event");
		alert.setHeaderText("Delete Event: " + event.getEventName());
		alert.setContentText("Are you sure you want to delete this event? This action cannot be undone.");

		if (alert.showAndWait().get() == ButtonType.OK) {
			try {
				model.deleteEvent(event.getId());
				refreshEvents();
				ErrorHandler.showInfo("Event deleted successfully");
			} catch (SQLException e) {
				ErrorHandler.handleError("Failed to delete event", e);
			}
		}
	}

	/**
	 * Handle view orders.
	 */
	private void handleViewOrders() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminOrdersView.fxml"));
			AdminOrdersController controller = new AdminOrdersController(stage, model);
			loader.setController(controller);
			Parent root = loader.load();
			controller.showStage(root);
		} catch (IOException e) {
			ErrorHandler.handleError("Failed to load orders view", e);
		}
	}

	/**
	 * Handle logout.
	 */
	private void handleLogout() {
		try {
			model.setCurrentUser(null);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
			LoginController loginController = new LoginController(stage, model);
			loader.setController(loginController);
			Parent root = loader.load();
			loginController.showStage(root);
		} catch (IOException e) {
			ErrorHandler.handleError("Failed to return to login", e);
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
		stage.setTitle("Admin Dashboard - The Super Event");
		stage.show();
		
		// Ensure background image scales with window
		if (bgImage != null && rootPane != null) {
			bgImage.fitWidthProperty().bind(rootPane.widthProperty());
			bgImage.fitHeightProperty().bind(rootPane.heightProperty());
		}
	}
}