package com.turbospaces.protoc;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import com.google.common.io.CharStreams;
import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.ProtoParserParser.ProtoContext;
import com.turbospaces.protoc.ServiceDescriptor.MethodDescriptor;
import com.turbospaces.protoc.types.ObjectMessageType.CollectionType;
import com.turbospaces.protoc.types.ObjectMessageType.FieldType;

public class BaseParserTest {
    @Test
    public void parse() throws IOException {
        String str = CharStreams.toString( new InputStreamReader( getClass().getClassLoader().getResourceAsStream(
                "example.protoc" ) ) );
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
        assertEquals( "Ack", user.getParent() );

        assertEquals( 0, ack.getFieldDescriptors().size() );
        assertEquals( 16, user.getFieldDescriptors().size() );

        assertEquals( "Account", container.aliases.get( "User" ) );

        FieldDescriptor firstName = user.getFieldDescriptor( 1 );
        assertEquals( FieldType.STRING, firstName.getType().getType() );
        assertEquals( "firstName", firstName.getName() );
        assertEquals( CollectionType.NONE, firstName.getType().getCollectionType() );

        FieldDescriptor age = user.getFieldDescriptor( 4 );
        assertEquals( FieldType.INT32, age.getType().getType() );

        FieldDescriptor enabled = user.getFieldDescriptor( 5 );
        assertEquals( FieldType.BOOL, enabled.getType().getType() );

        FieldDescriptor amount1 = user.getFieldDescriptor( 6 );
        assertEquals( FieldType.DOUBLE, amount1.getType().getType() );

        FieldDescriptor amount2 = user.getFieldDescriptor( 7 );
        assertEquals( FieldType.FLOAT, amount2.getType().getType() );

        FieldDescriptor values = user.getFieldDescriptor( 8 );
        assertEquals( FieldType.DECIMAL, values.getType().getType() );

        FieldDescriptor tmst = user.getFieldDescriptor( 9 );
        assertEquals( FieldType.INT64, tmst.getType().getType() );

        FieldDescriptor small = user.getFieldDescriptor( 10 );
        assertEquals( FieldType.INT16, small.getType().getType() );

        FieldDescriptor verySmall = user.getFieldDescriptor( 11 );
        assertEquals( FieldType.BYTE, verySmall.getType().getType() );

        FieldDescriptor address = user.getFieldDescriptor( 12 );
        assertEquals( FieldType.MESSAGE, address.getType().getType() );

        FieldDescriptor addressSet = user.getFieldDescriptor( 13 );
        assertEquals( FieldType.MESSAGE, addressSet.getType().getType() );
        assertEquals( CollectionType.SET, addressSet.getType().getCollectionType() );

        FieldDescriptor addressList = user.getFieldDescriptor( 14 );
        assertEquals( FieldType.MESSAGE, addressList.getType().getType() );
        assertEquals( CollectionType.LIST, addressList.getType().getCollectionType() );

        FieldDescriptor addressMap = user.getFieldDescriptor( 15 );
        assertEquals( FieldType.STRING, addressMap.getType().getType() );
        assertEquals( FieldType.MESSAGE, addressMap.getType().getValueType() );
        assertEquals( CollectionType.MAP, addressMap.getType().getCollectionType() );

        FieldDescriptor primitiveSet = user.getFieldDescriptor( 16 );
        assertEquals( FieldType.STRING, primitiveSet.getType().getType() );
        assertEquals( CollectionType.SET, primitiveSet.getType().getCollectionType() );

        ServiceDescriptor authService = container.services.get( "AuthService" );
        Map<String, MethodDescriptor> methods = authService.methods;
        assertEquals( "MockService", authService.parent );
        MethodDescriptor ping = methods.get( "ping" );
        assertTrue( ping.request == null );
        assertTrue( ping.exceptions.size() == 0 );

        MethodDescriptor addUser = methods.get( "addUser" );
        assertEquals( CollectionType.NONE, addUser.request.getCollectionType() );
        assertEquals( FieldType.MESSAGE, addUser.request.getType() );
        assertEquals( "User", addUser.request.getTypeRef() );
        assertTrue( addUser.exceptions.size() == 2 );

        MethodDescriptor getAllUserByFirstName = methods.get( "getAllUserByFirstName" );
        assertEquals( CollectionType.NONE, getAllUserByFirstName.request.getCollectionType() );
        assertEquals( FieldType.STRING, getAllUserByFirstName.request.getType() );
        assertEquals( CollectionType.MAP, getAllUserByFirstName.response.getCollectionType() );
        assertEquals( FieldType.STRING, getAllUserByFirstName.response.getType() );
        assertEquals( FieldType.MESSAGE, getAllUserByFirstName.response.getValueType() );
    }
}
