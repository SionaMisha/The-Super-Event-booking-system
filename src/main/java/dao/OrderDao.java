package dao;

import java.sql.SQLException;
import java.util.ArrayList;
import model.Order;
import model.OrderItem;

public interface OrderDao {
    void setup() throws SQLException;
    Order createOrder(String username, double totalPrice, String confirmationCode, ArrayList<OrderItem> items) throws SQLException;
    ArrayList<Order> getOrdersByUsername(String username) throws SQLException;
} 