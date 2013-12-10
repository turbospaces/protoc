package com.turbospaces.protoc;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.turbospaces.demo.Address;
import com.turbospaces.demo.Colors;
import com.turbospaces.protoc.serialization.Streams;

public class StreamsTest {

    @Test
    public void address() throws IOException {
        Address a1 = new Address();
        a1.setAddress( "Kiev, some street, 123" );
        a1.setZip( "100423" );
        a1.setCountry( "UKRAINE" );
        a1.setPrimary( true );
        a1.setDetails1( ImmutableSet.of( "detail-l1", "details-l2" ) );
        a1.setDetails2( ImmutableList.of( "detail-s1", "details-s2" ) );
        a1.setDetails3( ImmutableMap.of( "details-m1", 123L, "details-m3", 321L ) );
        a1.setNow( new Date() );
        a1.setBint( BigInteger.valueOf( Long.MAX_VALUE / 2 ) );
        a1.setColor( Colors.GREEN );

        Address a2 = new Address();
        byte[] arr = Streams.out( a1 );
        Streams.in( arr, a2 );

        assertEquals( a1, a2 );
        System.out.println( a2 );
    }
}
