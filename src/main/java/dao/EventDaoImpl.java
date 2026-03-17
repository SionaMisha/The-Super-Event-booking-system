//This class provides actual logic for working with events table in SQLite (implements DAO)
package dao;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Event;
import model.EventDao;

public class EventDaoImpl implements EventDao {
	private Connection connection;
	private final String TABLE_NAME = "events";  //table name

	public EventDaoImpl() throws SQLException {
		this.connection = Database.getConnection();
	}
	
	//Creates events table if not exists
	@Override
	public void setup() throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE IF NOT EXISTS events (" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"event_name TEXT NOT NULL," +
				"venue TEXT NOT NULL," +
				"day_of_week TEXT NOT NULL," +
				"price REAL NOT NULL," +
				"total_tickets INTEGER NOT NULL," +
				"tickets_sold INTEGER DEFAULT 0," +
				"enabled BOOLEAN DEFAULT 1," +
				"UNIQUE(event_name, venue, day_of_week))");
		}
		System.out.println("Event table created");
	}
	
	//fetches all the rows from the events table and turns them into event objects and used to display the list of events on the dashboard
	@Override
	public List<Event> getEvents() throws SQLException {
		List<Event> events = new ArrayList<>();
		String sql = "SELECT * FROM events WHERE enabled = 1";
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				events.add(createEventFromResultSet(rs));
			}
		}
		return events;
	}

	@Override
	public List<Event> getAllEvents() throws SQLException {
		List<Event> events = new ArrayList<>();
		String sql = "SELECT * FROM events";
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				events.add(createEventFromResultSet(rs));
			}
		}
		return events;
	}

	@Override
	public Event getEvent(int eventId) throws SQLException {
		String sql = "SELECT * FROM events WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return createEventFromResultSet(rs);
				}
			}
		}
		return null;
	}

	@Override
	public void createEvent(Event event) throws SQLException {
		String sql = "INSERT INTO events (event_name, venue, day_of_week, price, total_tickets) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, event.getEventName());
			pstmt.setString(2, event.getVenue());
			pstmt.setString(3, event.getDayOfWeek());
			pstmt.setDouble(4, event.getPrice());
			pstmt.setInt(5, event.getTotal_tickets());
			pstmt.executeUpdate();
		}
	}

	@Override
	public void updateEvent(Event event) throws SQLException {
		String sql = "UPDATE events SET event_name = ?, venue = ?, day_of_week = ?, price = ?, total_tickets = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, event.getEventName());
			pstmt.setString(2, event.getVenue());
			pstmt.setString(3, event.getDayOfWeek());
			pstmt.setDouble(4, event.getPrice());
			pstmt.setInt(5, event.getTotal_tickets());
			pstmt.setInt(6, event.getId());
			pstmt.executeUpdate();
		}
	}

	@Override
	public void deleteEvent(int eventId) throws SQLException {
		String sql = "DELETE FROM events WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			pstmt.executeUpdate();
		}
	}

	@Override
	public void updateEventStatus(int eventId, boolean enabled) throws SQLException {
		String sql = "UPDATE events SET enabled = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setBoolean(1, enabled);
			pstmt.setInt(2, eventId);
			pstmt.executeUpdate();
		}
	}

	@Override
	public boolean isDuplicateEvent(Event event) throws SQLException {
		String sql = "SELECT COUNT(*) FROM events WHERE event_name = ? AND venue = ? AND day_of_week = ? AND id != ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, event.getEventName());
			pstmt.setString(2, event.getVenue());
			pstmt.setString(3, event.getDayOfWeek());
			pstmt.setInt(4, event.getId());
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		}
		return false;
	}

	@Override
	public void updateTicketsSold(int eventId, int quantity) throws SQLException {
		String sql = "UPDATE events SET tickets_sold = tickets_sold + ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, quantity);
			pstmt.setInt(2, eventId);
			pstmt.executeUpdate();
		}
	}

	@Override
	public int getTicketsSold(int eventId) throws SQLException {
		String sql = "SELECT tickets_sold FROM events WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("tickets_sold");
				}
			}
		}
		return 0;
	}

	@Override
	public int getTotalTickets(int eventId) throws SQLException {
		String sql = "SELECT total_tickets FROM events WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("total_tickets");
				}
			}
		}
		return 0;
	}

	@Override
	public void updateEventAfterPurchase(int eventId, int quantity) throws SQLException {
		updateTicketsSold(eventId, quantity);
	}

	private Event createEventFromResultSet(ResultSet rs) throws SQLException {
		return new Event(
			rs.getInt("id"),
			rs.getString("event_name"),
			rs.getString("venue"),
			rs.getString("day_of_week"),
			rs.getDouble("price"),
			rs.getInt("total_tickets"),
			rs.getInt("tickets_sold"),
			rs.getBoolean("enabled")
		);
	}

	//Insert a list of eventsinto database. used to import events from .dat file and persist them in DB
    @Override
    public void loadEvents(ArrayList<Event> events) throws SQLException {
    	System.out.println("loadEvents called");
        String sql = "INSERT INTO " + TABLE_NAME + " (event_name, venue, day_of_week ,price, tickets_sold, total_tickets, enabled) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (//Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            for (Event event : events) {
                stmt.setString(1, event.getEventName());
                stmt.setString(2,event.getVenue());
                stmt.setString(3, event.getDayOfWeek());
                stmt.setDouble(4, event.getPrice());
                stmt.setInt(5, event.getTickets_sold());
                stmt.setInt(6, event.getTotal_tickets());
                stmt.setBoolean(7, true);
                stmt.addBatch();  // improves performance when inserting many rows
            }
            stmt.executeBatch();
            System.out.println("loadEvents called - stmt executed");
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
    }
    //REad events from events.dat file and creates Event objects. Used when loading sample data before calling loadEvents
    public ArrayList<Event> readEventsFromFile(String filepath) {
        ArrayList<Event> events = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 6) {
                    String eventName = parts[0];
                    String venue = parts[1];
                    String dayOfWeek = parts[2];
                    int price = Integer.parseInt(parts[3]);
                    int ticketsSold = Integer.parseInt(parts[4]);
                    int totalTickets = Integer.parseInt(parts[5]);

                    Event event = new Event(0, eventName, venue, dayOfWeek, price, totalTickets, ticketsSold, true);
                    events.add(event);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return events;
    }
}

