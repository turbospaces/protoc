package com.mycompany.protolang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompamy.example.service.account.AccountServiceRequest;
import com.mycompamy.example.service.account.AccountServiceResponse;
import com.mycompamy.example.service.account.GetUserDetailsRequestData;
import com.mycompamy.example.service.account.GetUserDetailsResponseData;
import com.mycompamy.example.types.Address;
import com.mycompamy.example.types.ContactTypeByEmail;
import com.mycompamy.example.types.Country;
import com.mycompamy.example.types.Money;
import com.mycompamy.example.types.User;

import io.protolang.Headers;
import io.protolang.Status;

public class JsonSerializationTest {
    Logger logger = LoggerFactory.getLogger( getClass() );

    public static User mockUser() {
        Set<String> details1 = new LinkedHashSet<>();
        List<String> details2 = new LinkedList<>();
        Map<String, Long> details3 = new HashMap<>();

        details1.add( "s1" );
        details1.add( "s2" );
        details1.add( "s3" );

        details2.add( "l1" );
        details2.add( "l2" );
        details2.add( "l3" );

        details3.put( "m1", 1L );
        details3.put( "m2", 2L );
        details3.put( "m3", 3L );

        Address primaryAddress = new Address();
        primaryAddress.setAddress( "somewhere" );
        primaryAddress.setCountry( Country.UKRAINE );
        primaryAddress.setPrimary( true );
        primaryAddress.setZip( "04112" );
        primaryAddress.setDetails1( details1 );
        primaryAddress.setDetails2( details2 );
        primaryAddress.setDetails3( details3 );

        Set<Address> unsortedAddresses = new HashSet<>();
        Map<String, Address> zip2address = new HashMap<>();
        for ( int i = 0; i < Country.values().length; i++ ) {
            Address address = new Address();
            address.setAddress( "another-address-" + i );
            address.setCountry( Country.values()[i] );
            address.setDetails1( details1 );
            address.setDetails2( details2 );
            address.setDetails3( details3 );
            address.setPrimary( false );
            address.setZip( "zip-" + i );
            unsortedAddresses.add( address );
            zip2address.put( address.getZip(), address );
        }
        List<Address> sortedAddresses = new LinkedList<>( unsortedAddresses );
        Collections.shuffle( sortedAddresses );

        ContactTypeByEmail contactTypeByEmail = new ContactTypeByEmail();
        Set<String> friends = new HashSet<>();
        friends.add( "mother" );
        friends.add( "brother" );

        Money redeem = new Money();
        redeem.setValue( "1255" );
        redeem.setPrecision( 2 );
        redeem.setScale( 2 );

        User user = new User();
        user.setFirstName( "firt-name-x" );
        user.setSecondName( "second-name-y" );
        user.setMiddleName( "middle-name-z" );
        user.setEmail( "my@dot.com" );
        contactTypeByEmail.setEmail( user.getEmail() );
        user.setPrimaryAddress( primaryAddress );
        user.setInactiveAddresses( unsortedAddresses );
        user.setActiveAddresses( sortedAddresses );
        user.setAllAddresses( zip2address );
        user.setAge( 16 );
        user.setEnabled( true );
        user.setCreatedTimestamp( System.currentTimeMillis() );
        user.setUpdatedTimestamp( user.getCreatedTimestamp() );
        user.setProfileUpdatesCount( (short) 1 );
        user.setTags( Collections.singleton( "tag1" ) );
        user.setAvatarPhoto( new byte[] { 1, 2, 5 } );
        user.setContactType( contactTypeByEmail );
        user.setCreditBalance( 199.99 );
        user.setFriends( friends );
        user.setSubsribeToNews( true );
        user.setRedeemMax( redeem );
        return user;
    }
    @Test
    public void json() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        User user = mockUser();
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString( user );
        logger.debug( json );
        mapper.readValue( json, User.class );
    }

    @Test
    public void messagePack() throws IOException {
        ObjectMapper mapper = new ObjectMapper( new MessagePackFactory() );
        User user = mockUser();
        byte[] bytes = mapper.writeValueAsBytes( user );
        logger.debug( new String( bytes ) );
        mapper.readValue( bytes, User.class );
    }

    @Test
    public void externalizable() throws IOException, ClassNotFoundException {
        User user = mockUser();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream( baos )) {
            user.writeExternal( out );
        }
        byte[] bytes = baos.toByteArray();
        logger.debug( "data size = {}", bytes.length );

        try (ObjectInputStream input = new ObjectInputStream( new ByteArrayInputStream( bytes ) )) {
            user = new User();
            user.readExternal( input );
        }
    }

    @Test
    public void requestReply() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        User user = mockUser();

        AccountServiceRequest req1 = new AccountServiceRequest();
        AccountServiceResponse resp1 = new AccountServiceResponse();

        GetUserDetailsRequestData reqData = new GetUserDetailsRequestData();
        reqData.setUsername( user.getSecondName() + "/" + user.getFirstName() );

        Headers headers = new Headers();
        headers.setMessageId( UUID.randomUUID().toString() );
        headers.setPriority( (short) 1 );
        headers.setReplyTo( "channel-users" );
        headers.setRetryAttempt( (short) 2 );
        headers.setTimestamp( System.currentTimeMillis() );
        headers.setTimeout( 30 );

        req1.setPayload( reqData );
        req1.setHeaders( headers );

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString( req1 );
        logger.debug( json );

        mapper.readValue( json, AccountServiceRequest.class );

        GetUserDetailsResponseData respData = new GetUserDetailsResponseData();
        respData.setUser( user );

        resp1.setHeaders( headers );
        resp1.setPayload( respData );
        resp1.setStatus( new Status() );
        resp1.getStatus().setCode( "OK" );
        resp1.getStatus().setText( "success" );

        json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString( resp1 );
        logger.debug( json );
        mapper.readValue( json, AccountServiceResponse.class );
    }
}
