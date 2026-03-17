package util;

public class ValidationUtils {
    
    public static boolean validateUsername(String username) {
        return username != null && 
               username.length() >= 3 && 
               username.matches("^[a-zA-Z0-9_]+$");
    }
    
    public static boolean validatePassword(String password) {
        return password != null && 
               password.length() >= 6;
    }
    
    public static boolean validatePreferredName(String preferredName) {
        return preferredName != null && 
               !preferredName.trim().isEmpty() && 
               preferredName.length() <= 50;
    }
    
    public static boolean validateQuantity(int quantity, int availableTickets) {
        return quantity > 0 && quantity <= availableTickets;
    }
    
    public static boolean validateConfirmationCode(String code) {
        return code != null && 
               code.matches("^\\d{6}$");
    }
    
    public static String getUsernameRequirements() {
        return "Username must be at least 3 characters long and contain only letters, numbers, and underscores.";
    }
    
    public static String getPasswordRequirements() {
        return "Password must be at least 6 characters long.";
    }
    
    public static String getPreferredNameRequirements() {
        return "Preferred name must not be empty and must be 50 characters or less.";
    }
    
    public static boolean isValidConfirmationCode(String code) {
        if (code == null || code.length() != 6) {
            return false;
        }
        
        // Check if the code contains only digits
        return code.matches("\\d{6}");
    }
    
    public static boolean hasEnoughSeats(int requested, int available) {
        return requested > 0 && requested <= available;
    }
    
    public static String formatOrderNumber(int orderId) {
        return String.format("%04d", orderId);
    }
    
    public static boolean isValidQuantity(int quantity, int availableSeats) {
        return quantity > 0 && quantity <= availableSeats;
    }
} 