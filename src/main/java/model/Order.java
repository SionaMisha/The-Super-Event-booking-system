package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Order {
    private int orderId;
    private String username;
    //private LocalDateTime orderDate;
    private Timestamp orderDate;
    private double totalPrice;
    private String confirmationCode;
    private List<OrderItem> items;
    private StringProperty confirmationCodeProperty;
    private DoubleProperty totalPriceProperty;

    public Order(int orderId, String username, Timestamp orderDate, double totalPrice, String confirmationCode) {
        this.orderId = orderId;
        this.username = username;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.confirmationCode = confirmationCode;
        this.items = new ArrayList<>();
        this.confirmationCodeProperty = new SimpleStringProperty(confirmationCode);
        this.totalPriceProperty = new SimpleDoubleProperty(totalPrice);
    }

    public Order(int orderId, String username, Timestamp orderDate, double totalPrice, String confirmationCode, ArrayList<OrderItem> items) {
        this.orderId = orderId;
        this.username = username;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.confirmationCode = confirmationCode;
        this.items = items;
        this.confirmationCodeProperty = new SimpleStringProperty(confirmationCode);
        this.totalPriceProperty = new SimpleDoubleProperty(totalPrice);
    }
    
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public StringProperty usernameProperty() {
        return new SimpleStringProperty(username);
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

//    public String getFormattedOrderDate() {
//        return orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
        this.totalPriceProperty.set(totalPrice);
    }

    public DoubleProperty totalPriceProperty() {
        return totalPriceProperty;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
        this.confirmationCodeProperty.set(confirmationCode);
    }

    public StringProperty confirmationCodeProperty() {
        return confirmationCodeProperty;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getFormattedOrderId() {
        return String.format("%04d", orderId);
    }

    public double getTotal() {
        return totalPrice;
    }

    public int getId() {
        return orderId;
    }
} 