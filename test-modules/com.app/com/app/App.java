package com.app;

import com.modA.A;
import com.modB.B;

public class App {
    public static void main( String[] args ) {
        System.out.println( "Module A can give Strings: " + new A().getStrings() );
        System.out.println( "Module B can give Strings: " + new B().getMoreStrings() );

        System.out.println( "What is empty? " + new B().empty() );
        new A().handle( new Exception() );
    }
}
