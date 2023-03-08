package org.example;

import org.example.awslogger.AWSLogger;

public class App {
    public static void main( String[] args ) {
        AWSLogger logger = AWSLogger.getInstance();

        System.out.println( "Hello World!" );

        int count = 0;
        for (int i = 0; i < 100; i++) {
            count += i;
        }

        System.out.println("Count is " + count);
        try {
            int div = 10/0;
        } catch (Exception e) {
            logger.tagJob("FAIL", "Cannot divide by Zero.");
            throw new RuntimeException("DivisionByZeroError");
        }

        logger.tagJob("SUCCESS");
    }
}
