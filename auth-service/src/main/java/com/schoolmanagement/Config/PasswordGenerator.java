package com.schoolmanagement.Config;

import java.security.SecureRandom;

public class PasswordGenerator {

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!";

    public static String generatePassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(
                    CHARACTERS.charAt(random.nextInt(CHARACTERS.length()))
            );
        }

        return password.toString();
    }
    
    
}
