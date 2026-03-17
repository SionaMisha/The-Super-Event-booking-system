package controller;

import model.Event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class Validator {
    public static boolean is6DigitCode(String code) {
        return Pattern.matches("\\d{6}", code);
    }

    public static boolean isDateValid(Event event) {
        String day = event.getDayOfWeek().toUpperCase();
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        DayOfWeek eventDay = DayOfWeek.valueOf(day.substring(0, 3).toUpperCase());
        return eventDay.compareTo(today) >= 0;
    }

    public static int getQuantity(Event event, int cartQuantity) {
        return event.getTotal_tickets() - event.getTickets_sold() - cartQuantity;
    }
}
