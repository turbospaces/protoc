package com.turbospaces.protoc;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import com.google.common.io.CharStreams;
import com.turbospaces.protoc.MessageType.CollectionType;
import com.turbospaces.protoc.MessageType.FieldType;
import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.ProtoParserParser.ProtoContext;
import com.turbospaces.protoc.ServiceDescriptor.MethodDescriptor;

public class BaseParserTest {
    @Test
    public void parse() throws IOException {
        String str = CharStreams.toString( new InputStreamReader( getClass().getClassLoader().getResourceAsStream( "example.protoc" ) ) );
        ANTLRInputStream input = new ANTLRInputStream( str );
        ProtoParserLexer lexer = new ProtoParserLexer( input );
        CommonTokenStream tokens = new CommonTokenStream( lexer );
        ProtoParserParser parser = new ProtoParserParser( tokens );
        parser.setTrace( true );
        ProtoContext protoContext = parser.proto();
        System.out.println( protoContext.toStringTree( parser ) );
        ProtoContainer container = new ProtoContainer();
        Antlr4ProtoVisitor visitor = new Antlr4ProtoVisitor( parser, container );
        visitor.visit( protoContext );
        System.out.println( container );

        MessageDescriptor ack = container.messages.get( "Ack" );
        MessageDescriptor user = container.messages.get( "User" );
        assertEquals( "Ack", user.parentQualifier );

        assertEquals( 0, ack.fields.size() );
        assertEquals( 16, user.fields.size() );

        assertEquals( "Account", container.aliases.get( "User" ) );

        FieldDescriptor firstName = user.getFieldDesc( 1 );
        assertEquals( FieldType.STRING, firstName.type.getTypes()[0] );
        assertEquals( "firstName", firstName.qualifier );
        assertEquals( CollectionType.NONE, firstName.type.getCollectionType() );

        FieldDescriptor age = user.getFieldDesc( 4 );
        assertEquals( FieldType.INT32, age.type.getTypes()[0] );

        FieldDescriptor enabled = user.getFieldDesc( 5 );
        assertEquals( FieldType.BOOL, enabled.type.getTypes()[0] );

        FieldDescriptor amount1 = user.getFieldDesc( 6 );
        assertEquals( FieldType.DOUBLE, amount1.type.getTypes()[0] );

        FieldDescriptor amount2 = user.getFieldDesc( 7 );
        assertEquals( FieldType.FLOAT, amount2.type.getTypes()[0] );

        FieldDescriptor values = user.getFieldDesc( 8 );
        assertEquals( FieldType.DECIMAL, values.type.getTypes()[0] );

        FieldDescriptor tmst = user.getFieldDesc( 9 );
        assertEquals( FieldType.INT64, tmst.type.getTypes()[0] );

        FieldDescriptor small = user.getFieldDesc( 10 );
        assertEquals( FieldType.INT16, small.type.getTypes()[0] );

        FieldDescriptor verySmall = user.getFieldDesc( 11 );
        assertEquals( FieldType.BYTE, verySmall.type.getTypes()[0] );

        FieldDescriptor address = user.getFieldDesc( 12 );
        assertEquals( FieldType.MESSAGE, address.type.getTypes()[0] );

        FieldDescriptor addressSet = user.getFieldDesc( 13 );
        assertEquals( FieldType.MESSAGE, addressSet.type.getTypes()[0] );
        assertEquals( CollectionType.SET, addressSet.type.getCollectionType() );

        FieldDescriptor addressList = user.getFieldDesc( 14 );
        assertEquals( FieldType.MESSAGE, addressList.type.getTypes()[0] );
        assertEquals( CollectionType.LIST, addressList.type.getCollectionType() );

        FieldDescriptor addressMap = user.getFieldDesc( 15 );
        assertEquals( FieldType.STRING, addressMap.type.getTypes()[0] );
        assertEquals( FieldType.MESSAGE, addressMap.type.getTypes()[1] );
        assertEquals( CollectionType.MAP, addressMap.type.getCollectionType() );

        FieldDescriptor primitiveSet = user.getFieldDesc( 16 );
        assertEquals( FieldType.STRING, primitiveSet.type.getTypes()[0] );
        assertEquals( CollectionType.SET, primitiveSet.type.getCollectionType() );

        ServiceDescriptor authService = container.services.get( "AuthService" );
        Map<String, MethodDescriptor> methods = authService.methods;
        assertEquals( "MockService", authService.parentQualifier );
        MethodDescriptor ping = methods.get( "ping" );
        assertTrue( ping.request == null );
        assertTrue( ping.exceptions.size() == 0 );

        MethodDescriptor addUser = methods.get( "addUser" );
        assertEquals( CollectionType.NONE, addUser.request.getCollectionType() );
        assertEquals( FieldType.MESSAGE, addUser.request.getTypes()[0] );
        assertEquals( "User", addUser.request.getTypeRefs()[0] );
        assertTrue( addUser.exceptions.size() == 2 );

        MethodDescriptor getAllUserByFirstName = methods.get( "getAllUserByFirstName" );
        assertEquals( CollectionType.NONE, getAllUserByFirstName.request.getCollectionType() );
        assertEquals( FieldType.STRING, getAllUserByFirstName.request.getTypes()[0] );
        assertEquals( CollectionType.MAP, getAllUserByFirstName.response.getCollectionType() );
        assertEquals( FieldType.STRING, getAllUserByFirstName.response.getTypes()[0] );
        assertEquals( FieldType.MESSAGE, getAllUserByFirstName.response.getTypes()[1] );
    }
}
