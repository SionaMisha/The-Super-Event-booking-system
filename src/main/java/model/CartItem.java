package model;


import javafx.beans.property.*;

public class CartItem {
    private Event event;
    private IntegerProperty quantity;

    public CartItem(Event event, int quantity) {
        this.event = event;
        this.quantity = new SimpleIntegerProperty(quantity);
    }

    public Event getEvent() {
        return event;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int q) {
        this.quantity.set(q);
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }
}
