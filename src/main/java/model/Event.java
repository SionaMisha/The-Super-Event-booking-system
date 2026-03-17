//This class represents a single event in the system
package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Event {
	private int id; 
	private StringProperty eventName;
	private StringProperty venue;
	private StringProperty dayOfWeek;
	private DoubleProperty price;
	private int tickets_sold;
	private int total_tickets;
	private boolean enabled;
	
	//Initializes an Event object with all its fields
	public Event(int id, String eventName, String venue, String dayOfWeek, double price, int total_tickets, int tickets_sold, boolean enabled) {
        this.id = id;
        this.eventName = new SimpleStringProperty(eventName);
        this.venue = new SimpleStringProperty(venue);
        this.dayOfWeek = new SimpleStringProperty(dayOfWeek);
        this.price = new SimpleDoubleProperty(price);
        this.tickets_sold = tickets_sold;
        this.total_tickets = total_tickets;
        this.enabled = enabled;
    }
	
	//Standard getters and setters for each field. These allow controlled access and modification of the attributes.
	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getEventName() {
		return eventName.get();
	}


	public void setEventName(String eventName) {
		this.eventName.set(eventName);
	}


	public StringProperty eventNameProperty() {
		return eventName;
	}


	public String getVenue() {
		return venue.get();
	}


	public void setVenue(String venue) {
		this.venue.set(venue);
	}


	public StringProperty venueProperty() {
		return venue;
	}


	public String getDayOfWeek() {
		return dayOfWeek.get();
	}


	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek.set(dayOfWeek);
	}


	public StringProperty dayOfWeekProperty() {
		return dayOfWeek;
	}


	public double getPrice() {
		return price.get();
	}


	public void setPrice(double price) {
		this.price.set(price);
	}


	public DoubleProperty priceProperty() {
		return price;
	}

	
	public int getTickets_sold() {
		return tickets_sold;
	}


	public void setTickets_sold(int tickets_sold) {
		this.tickets_sold = tickets_sold;
	}


	public int getTotal_tickets() {
		return total_tickets;
	}


	public void setTotal_tickets(int total_tickets) {
		this.total_tickets = total_tickets;
	}

    // Add this method to get available tickets
    public int getAvailableTickets() {
        return total_tickets - tickets_sold;
    }

    // Also add a setter for available tickets if needed (or update ticketsSold)
    public void setAvailableTickets(int availableTickets) {
        // To update ticketsSold accordingly:
        this.tickets_sold = this.total_tickets - availableTickets;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


	
	
	

}
