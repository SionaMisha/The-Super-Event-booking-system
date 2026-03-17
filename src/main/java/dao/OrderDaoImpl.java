package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Event;
import model.Order;
import model.OrderDao;
import model.OrderItem;

public class OrderDaoImpl implements OrderDao {
    private Connection connection;

    public OrderDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void setup() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create orders table
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "total_price REAL NOT NULL," +
                "confirmation_code TEXT NOT NULL)");

            // Create order_items table
            stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER NOT NULL," +
                "event_id INTEGER NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "FOREIGN KEY (order_id) REFERENCES orders(id)," +
                "FOREIGN KEY (event_id) REFERENCES events(id))");
        }
    }

    @Override
    public Order createOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (username, total_price, confirmation_code) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, order.getUsername());
            pstmt.setDouble(2, order.getTotalPrice());
            pstmt.setString(3, order.getConfirmationCode());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    order.setOrderId(orderId);
                    saveOrderItems(order);
                    return getOrder(orderId);
                }
            }
        }
        throw new SQLException("Failed to create order");
    }

    @Override
    public List<Order> getUserOrders(String username) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE username = ? ORDER BY order_date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = createOrderFromResultSet(rs);
                    loadOrderItems(order);
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    @Override
    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Order order = createOrderFromResultSet(rs);
                loadOrderItems(order);
                orders.add(order);
            }
        }
        return orders;
    }

    @Override
    public Order getOrder(int orderId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = createOrderFromResultSet(rs);
                    loadOrderItems(order);
                    return order;
                }
            }
        }
        return null;
    }

    @Override
    public List<Order> getOrdersByUsername(String username) throws SQLException {
        return getUserOrders(username);
    }

    private void saveOrderItems(Order order) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, event_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (OrderItem item : order.getItems()) {
                pstmt.setInt(1, order.getOrderId());
                pstmt.setInt(2, item.getEvent().getId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.executeUpdate();
            }
        }
    }

    private void loadOrderItems(Order order) throws SQLException {
        String sql = "SELECT oi.*, e.* FROM order_items oi " +
                    "JOIN events e ON oi.event_id = e.id " +
                    "WHERE oi.order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, order.getOrderId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Create Event object from result set
                    Event event = new Event(
                        rs.getInt("event_id"),
                        rs.getString("event_name"),
                        rs.getString("venue"),
                        rs.getString("day_of_week"),
                        rs.getDouble("price"),
                        rs.getInt("total_tickets"),
                        rs.getInt("tickets_sold"),
                        rs.getBoolean("enabled")
                    );
                    // Create OrderItem and add to order
                    OrderItem item = new OrderItem(event, rs.getInt("quantity"));
                    order.getItems().add(item);
                }
            }
        }
    }

    private Order createOrderFromResultSet(ResultSet rs) throws SQLException {
        return new Order(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getTimestamp("order_date"),
            rs.getDouble("total_price"),
            rs.getString("confirmation_code")
        );
    }
} 