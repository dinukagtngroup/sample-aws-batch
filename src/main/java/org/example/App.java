package org.example;
public class App {
    public static void main( String[] args ) {
        System.out.println( "Hello World!" );

        int count = 0;
        for (int i = 0; i < 100; i++) {
            count += i;
        }

        System.out.println("Count is " + count);

        int div = 14/0;
    }
}
