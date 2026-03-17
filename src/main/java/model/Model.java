package model;

import dao.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import util.DateValidator;
import util.ErrorHandler;
import util.PasswordUtil;
import util.ValidationUtils;

public class Model {

    // Singleton instance
    private static Model instance;

    private UserDao userDao;
    private User currentUser;
    private List<CartItem> cart;
    private EventDao eventDao;
    private OrderDao orderDao;
    private boolean isAdmin = false;

    // Private constructor
    private Model() throws SQLException {
        userDao = new UserDaoImpl();
        eventDao = new EventDaoImpl();
        orderDao = new OrderDaoImpl(Database.getConnection());
        cart = new ArrayList<>();
        initializeAdminUser();
    }

    private void initializeAdminUser() throws SQLException {
        try {
        	System.out.println("Admin user create called");
            // Check if admin user exists
            User adminUser = userDao.getUser("admin", "Admin321");
            
            if (adminUser == null) {
                // Create admin user if it doesn't exist
                userDao.createUser("admin", PasswordUtil.encrypt("Admin321"), "Admin");
            }
        } catch (SQLException e) {
            ErrorHandler.handleError("Failed to initialize admin user", e);
        }
    }

    public User login(String username, String password) throws SQLException {
        User user = userDao.getUser(username, password);
        if (user != null) {
            setCurrentUser(user);
            isAdmin = username.equalsIgnoreCase("admin");
        }
        return user;
    }
    public boolean validateCurrentPassword(String username, String password) throws SQLException {
        User user = userDao.getUser(username, password);
        
        if (user != null) {
        	return true;
        }
        return false;
    }
    
    public void changePassword(String newPassword) throws SQLException {
        if (currentUser != null) {
        	userDao.changePassword(currentUser.getUsername(), newPassword);
        }
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }

    // Static method to get the single instance
    public static Model getInstance() {
        if (instance == null) {
            try {
                instance = new Model();
            } catch (SQLException e) {
                ErrorHandler.handleError("Failed to initialize model", e);
                throw new RuntimeException("Model initialization failed: " + e.getMessage());
            }
        }
        return instance;
    }

    // ------------------- Existing Methods -------------------

    public void setup() throws SQLException {
        userDao.setup();
        System.out.println("Loadded user table");
        eventDao.setup();
        System.out.println("Loadded event table");
        orderDao.setup();
        System.out.println("Loadded order table");
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.isAdmin = user != null && "admin".equalsIgnoreCase(user.getUsername());
        if (!isAdmin) {
            this.cart = new ArrayList<>(); // Reset cart on login for non-admin users
        }
    }

    public List<CartItem> getCart() {
        if (currentUser == null) {
            throw new IllegalStateException("No user is currently logged in");
        }
        return cart;
    }

    public boolean isSeatAvailable(Event event, int requestedQuantity) throws SQLException {
        if (currentUser == null) {
            throw new IllegalStateException("No user is currently logged in");
        }
        
        int availableSeats = event.getTotal_tickets() - event.getTickets_sold();
        
        // Check if event is already in cart
        for (CartItem item : cart) {
            if (item.getEvent().getId() == event.getId()) {
                availableSeats -= item.getQuantity();
            }
        }
        
        return requestedQuantity <= availableSeats;
    }

    public void addToCart(Event event, int quantity) throws SQLException {
        if (currentUser == null) {
            throw new IllegalStateException("No user is currently logged in");
        }
        
        // Validate date
        if (!DateValidator.isValidBookingDay(event.getDayOfWeek())) {
            throw new IllegalArgumentException("Cannot book events for past days");
        }

        // Check seat availability
        if (!isSeatAvailable(event, quantity)) {
            throw new IllegalArgumentException("Not enough seats available");
        }

        // Check if event is already in cart
        for (CartItem item : cart) {
            if (item.getEvent().getId() == event.getId()) {
                int newQuantity = item.getQuantity() + quantity;
                if (!isSeatAvailable(event, newQuantity)) {
                    throw new IllegalArgumentException("Not enough seats available");
                }
                item.setQuantity(newQuantity);
                return;
            }
        }

        // Add new item to cart
        cart.add(new CartItem(event, quantity));
    }

    public void updateCartItem(Event event, int newQuantity) throws SQLException {
        if (!DateValidator.isValidBookingDay(event.getDayOfWeek())) {
            throw new IllegalArgumentException("Cannot book events for past days");
        }

        int availableSeats = event.getTotal_tickets() - event.getTickets_sold();
        if (!ValidationUtils.isValidQuantity(newQuantity, availableSeats)) {
            throw new IllegalArgumentException("Invalid quantity or not enough seats available");
        }

        for (CartItem item : cart) {
            if (item.getEvent().getId() == event.getId()) {
                item.setQuantity(newQuantity);
                return;
            }
        }
    }

    public void removeFromCart(Event event) {
        cart.removeIf(item -> item.getEvent().getId() == event.getId());
    }

    public double getCartTotal() {
        return cart.stream()
                .mapToDouble(item -> item.getEvent().getPrice() * item.getQuantity())
                .sum();
    }

    public Order checkout(String confirmationCode) throws SQLException {
        if (!ValidationUtils.isValidConfirmationCode(confirmationCode)) {
            throw new IllegalArgumentException("Invalid confirmation code");
        }
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        ArrayList<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart) {
            orderItems.add(new OrderItem(cartItem.getEvent(), cartItem.getQuantity()));
        }

        Order order = new Order(
            0, // ID will be set by the database
            currentUser.getUsername(),
            new Timestamp(System.currentTimeMillis()),
            getCartTotal(),
            confirmationCode
        );
        order.setItems(orderItems);

        Order createdOrder = orderDao.createOrder(order);

        for (OrderItem item : orderItems) {
            eventDao.updateEventAfterPurchase(item.getEvent().getId(), item.getQuantity());
        }

        cart.clear();
        return createdOrder;
    }

    public List<Order> getUserOrders() {
        if (currentUser == null) return new ArrayList<>();
        try {
            return new ArrayList<>(orderDao.getOrdersByUsername(currentUser.getUsername()));
        } catch (SQLException e) {
            ErrorHandler.handleError("Failed to get user orders", e);
            return new ArrayList<>();
        }
    }

    public void exportOrders(String filePath) throws IOException {
        try {
            ArrayList<Order> orders = new ArrayList<>(orderDao.getOrdersByUsername(currentUser.getUsername()));
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
                writer.println("Order History for " + currentUser.getPreferredName());
                writer.println("==========================================");
                
                for (Order order : orders) {
                    writer.println("\nOrder #" + ValidationUtils.formatOrderNumber(order.getOrderId()));
                    writer.println("Date: " + order.getOrderDate());
                    writer.println("Total: $" + String.format("%.2f", order.getTotalPrice()));
                    writer.println("Confirmation Code: " + order.getConfirmationCode());
                    writer.println("\nItems:");
                    
                    for (OrderItem item : order.getItems()) {
                        writer.println("- " + item.getEvent().getEventName() + 
                                     " (" + item.getQuantity() + " tickets)");
                    }
                    writer.println("------------------------------------------");
                }
            }
        } catch (SQLException e) {
            throw new IOException("Failed to retrieve orders: " + e.getMessage());
        }
    }

    public void addOrUpdateItem(Event event, int quantity) {
        for (CartItem item : cart) {
            if (item.getEvent().getId() == event.getId()) {
                item.setQuantity(quantity);
                return;
            }
        }
        cart.add(new CartItem(event, quantity));
    }

    // Admin-specific methods
    public List<Event> getAdminEvents() throws SQLException {
        if (!isAdmin) {
            throw new IllegalStateException("Only admin can view all events");
        }
        return eventDao.getAllEvents();
    }

    public void disableEvent(int eventId) throws SQLException {
        if (!isAdmin) {
            throw new IllegalStateException("Only admin can disable events");
        }
        eventDao.updateEventStatus(eventId, false);
    }

    public void enableEvent(int eventId) throws SQLException {
        if (!isAdmin) {
            throw new IllegalStateException("Only admin can enable events");
        }
        eventDao.updateEventStatus(eventId, true);
    }

    public void addEvent(Event event) throws SQLException {
        if (!isAdmin) {
            throw new IllegalStateException("Only admin can add events");
        }
        if (eventDao.isDuplicateEvent(event)) {
            throw new IllegalArgumentException("Event with same name, venue, and day already exists");
        }
        eventDao.createEvent(event);
    }

    public void deleteEvent(int eventId) throws SQLException {
        if (!isAdmin) {
            throw new IllegalStateException("Only admin can delete events");
        }
        eventDao.deleteEvent(eventId);
    }

    public void updateEvent(Event event) throws SQLException {
        if (!isAdmin) {
            throw new IllegalStateException("Only admin can update events");
        }
        if (eventDao.isDuplicateEvent(event)) {
            throw new IllegalArgumentException("Event with same name, venue, and day already exists");
        }
        eventDao.updateEvent(event);
    }

    public List<Order> getAllOrders() throws SQLException {
        if (!isAdmin) {
            throw new IllegalStateException("Only admin can view all orders");
        }
        return orderDao.getAllOrders();
    }
}

