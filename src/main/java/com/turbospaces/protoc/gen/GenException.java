package com.turbospaces.protoc.gen;

@SuppressWarnings("serial")
public class GenException extends RuntimeException {
    public GenException(String message) {
        super( message );
    }
    public GenException(Throwable cause) {
        super( cause );
    }

    public static void check(boolean exp, String message, Object... args) {
        if ( !exp ) {
            throw new GenException( String.format( message, args ) );
        }
    }
}
