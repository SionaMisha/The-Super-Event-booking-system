package model;

import java.sql.SQLException;
import java.util.List;

public interface OrderDao {
    void setup() throws SQLException;
    Order createOrder(Order order) throws SQLException;
    List<Order> getUserOrders(String username) throws SQLException;
    List<Order> getAllOrders() throws SQLException;
    Order getOrder(int orderId) throws SQLException;
    List<Order> getOrdersByUsername(String username) throws SQLException;
} 