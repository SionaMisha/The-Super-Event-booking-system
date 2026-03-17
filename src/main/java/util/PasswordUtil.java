package util;

public class PasswordUtil {
    private static final int SHIFT = 3; // Simple Caesar cipher shift

    public static String encrypt(String password) {
        StringBuilder encrypted = new StringBuilder();
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                encrypted.append((char) ((c - base + SHIFT) % 26 + base));
            } else if (Character.isDigit(c)) {
                encrypted.append((char) ((c - '0' + SHIFT) % 10 + '0'));
            } else {
                encrypted.append(c);
            }
        }
        return encrypted.toString();
    }

    public static String decrypt(String encrypted) {
        StringBuilder decrypted = new StringBuilder();
        for (char c : encrypted.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                decrypted.append((char) ((c - base - SHIFT + 26) % 26 + base));
            } else if (Character.isDigit(c)) {
                decrypted.append((char) ((c - '0' - SHIFT + 10) % 10 + '0'));
            } else {
                decrypted.append(c);
            }
        }
        return decrypted.toString();
    }

    public static boolean validateConfirmationCode(String code) {
        return code != null && code.matches("\\d{6}");
    }
} 