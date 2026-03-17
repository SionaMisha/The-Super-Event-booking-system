package util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class DateValidator {
    
	public static boolean isValidBookingDay(String dayOfWeek) {
		// Convert the input day to uppercase for comparison
		String normalizedDay = dayOfWeek.toUpperCase();
		
		// Get current day of week (1 = Monday, 7 = Sunday)
		DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
		int currentDayValue = currentDay.getValue();
		
		// Find the target day value
		int targetDayValue = 0;
		for (DayOfWeek day : DayOfWeek.values()) {
			if (day.getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase().equals(normalizedDay)) {
				targetDayValue = day.getValue();
				break;
			}
		}
		
		// If target day is before current day, it's not valid
		if (targetDayValue < currentDayValue) {
			return false;
		}
		
		// If target day is more than 6 days after current day, it's not valid
		if (targetDayValue - currentDayValue > 6) {
			return false;
		}
		
		return true;
	}
	
	public static String getCurrentDayOfWeek() {
		return LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US);
	}
} 