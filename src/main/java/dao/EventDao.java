package dao;

import java.sql.SQLException;
import java.util.ArrayList;

import model.Event;

public interface EventDao {
	void setup() throws SQLException;  // Create the events table if doesnt exist
	
	public ArrayList<Event> getEvents() throws SQLException;  //Retrieve all events from database
	
	public void loadEvents(ArrayList<Event> events) throws SQLException; //Save a list of events to dB
	
	void updateEventAfterPurchase(int eventId, int quantity) throws SQLException;

}
