package loving.ai.services;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Service
public class PasswordAndOTPGenerator {

    //Use cryptographically secure random number generator
    private final Random random = new SecureRandom();

    public String generatePassword(int length) {

        //minimum length of 8
        if (length < 8) {
            length = 8;
        }

        final char[] lowercase = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        final char[] uppercase = "ABCDEFGJKLMNPRSTUVWXYZ".toCharArray();
        final char[] numbers = "0123456789".toCharArray();
        final char[] symbols = "^$?!@#%&".toCharArray();
        final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789^$?!@#%&".toCharArray();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length - 4; i++) {
            stringBuilder.append(allAllowed[random.nextInt(allAllowed.length)]);
        }

        //Ensure password policy is met by inserting required random chars in random positions
        stringBuilder.insert(random.nextInt(stringBuilder.length()), lowercase[random.nextInt(lowercase.length)]);
        stringBuilder.insert(random.nextInt(stringBuilder.length()), uppercase[random.nextInt(uppercase.length)]);
        stringBuilder.insert(random.nextInt(stringBuilder.length()), numbers[random.nextInt(numbers.length)]);
        stringBuilder.insert(random.nextInt(stringBuilder.length()), symbols[random.nextInt(symbols.length)]);

        return stringBuilder.toString();
    }

    public String generateOTP(int length) {
        StringBuilder stringBuilder = new StringBuilder();

        if (length < 6 ) {
            length = 6;
        }

        for (int i = 0; i < length; i++) {
            stringBuilder.append(random.nextInt(length));
        }
        return stringBuilder.toString();
    }
}
