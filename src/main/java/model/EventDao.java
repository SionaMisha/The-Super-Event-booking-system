package model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface EventDao {
    void setup() throws SQLException;
    List<Event> getEvents() throws SQLException;
    List<Event> getAllEvents() throws SQLException;
    Event getEvent(int eventId) throws SQLException;
    void createEvent(Event event) throws SQLException;
    void updateEvent(Event event) throws SQLException;
    void deleteEvent(int eventId) throws SQLException;
    void updateEventStatus(int eventId, boolean enabled) throws SQLException;
    boolean isDuplicateEvent(Event event) throws SQLException;
    void updateTicketsSold(int eventId, int quantity) throws SQLException;
    int getTicketsSold(int eventId) throws SQLException;
    int getTotalTickets(int eventId) throws SQLException;
    void updateEventAfterPurchase(int eventId, int quantity) throws SQLException;
    void loadEvents(ArrayList<Event> events) throws SQLException;
    ArrayList<Event> readEventsFromFile(String filepath);
} 