package model;

public class OrderItem {
    private Event event;
    private int quantity;

    public OrderItem(Event event, int quantity) {
        this.event = event;
        this.quantity = quantity;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return event.getPrice() * quantity;
    }

    public String getEventName() {
        return event.getEventName();
    }

    public double getPrice() {
        return event.getPrice();
    }

    public double getTotal() {
        return getTotalPrice();
    }
} 