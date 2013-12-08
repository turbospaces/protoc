package com.turbospaces.protoc;

import java.io.IOException;

import static org.junit.Assert.*;

import org.junit.Test;

import com.turbospaces.demo.Address;

public class StreamsTest {

    @Test
    public void address() throws IOException {
        Address a1 = new Address();
        a1.setAddress( "Kiev, some street, 123" );
        a1.setCountry( "UKRAINE" );
        a1.setPrimary( true );
        a1.setZip( "100423" );
        
        Address a2 = new Address();
        byte[] arr = Streams.out( a1 );
        Streams.in( arr, a2 );
        
        assertEquals( a1, a2 );
        System.out.println(a2);
    }
}
