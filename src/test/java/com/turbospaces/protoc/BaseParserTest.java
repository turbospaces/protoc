package com.turbospaces.protoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import com.google.common.io.CharStreams;
import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.ProtoParserParser.ProtoContext;
import com.turbospaces.protoc.ServiceDescriptor.MethodDescriptor;
import com.turbospaces.protoc.gen.Antlr4ProtoVisitor;
import com.turbospaces.protoc.gen.ProtoGenerationContext;
import com.turbospaces.protoc.types.CollectionMessageType;
import com.turbospaces.protoc.types.FieldType;
import com.turbospaces.protoc.types.MapMessageType;
import com.turbospaces.protoc.types.ObjectMessageType;

public class BaseParserTest {
    @Test
    public void parse() throws Exception {
        String str = CharStreams.toString( new InputStreamReader( getClass().getClassLoader().getResourceAsStream( "example.protoc" ) ) );
        ANTLRInputStream input = new ANTLRInputStream( str );
        ProtoParserLexer lexer = new ProtoParserLexer( input );
        CommonTokenStream tokens = new CommonTokenStream( lexer );
        ProtoParserParser parser = new ProtoParserParser( tokens );
        parser.setTrace( true );
        ProtoContext protoContext = parser.proto();
        ProtoContainer container = new ProtoContainer();
        Antlr4ProtoVisitor visitor = new Antlr4ProtoVisitor( parser, container );
        visitor.visit( protoContext );
        ProtoGenerationContext ctx = new ProtoGenerationContext();
        ctx.containers.add( container );
        ctx.init( ctx );
        System.out.println( container );

        MessageDescriptor ack = container.messages.get( "Ack" );
        MessageDescriptor user = container.messages.get( "User" );
        assertEquals( "Ack", user.getParent() );

        assertEquals( 0, ack.getFieldDescriptors().size() );
        assertEquals( 16, user.getFieldDescriptors().size() );

        assertEquals( "Account", container.aliases.get( "User" ) );

        FieldDescriptor firstName = user.getFieldDescriptor( 1 );
        assertEquals( FieldType.STRING, ( (ObjectMessageType) firstName.getType() ).getType() );
        assertEquals( "firstName", firstName.getName() );

        FieldDescriptor age = user.getFieldDescriptor( 4 );
        assertEquals( FieldType.INT32, ( (ObjectMessageType) age.getType() ).getType() );

        FieldDescriptor enabled = user.getFieldDescriptor( 5 );
        assertEquals( FieldType.BOOL, ( (ObjectMessageType) enabled.getType() ).getType() );

        FieldDescriptor amount1 = user.getFieldDescriptor( 6 );
        assertEquals( FieldType.DOUBLE, ( (ObjectMessageType) amount1.getType() ).getType() );

        FieldDescriptor amount2 = user.getFieldDescriptor( 7 );
        assertEquals( FieldType.FLOAT, ( (ObjectMessageType) amount2.getType() ).getType() );

        FieldDescriptor values = user.getFieldDescriptor( 8 );
        assertEquals( FieldType.BDECIMAL, ( (ObjectMessageType) values.getType() ).getType() );

        FieldDescriptor tmst = user.getFieldDescriptor( 9 );
        assertEquals( FieldType.INT64, ( (ObjectMessageType) tmst.getType() ).getType() );

        FieldDescriptor small = user.getFieldDescriptor( 10 );
        assertEquals( FieldType.INT16, ( (ObjectMessageType) small.getType() ).getType() );

        FieldDescriptor verySmall = user.getFieldDescriptor( 11 );
        assertEquals( FieldType.BYTE, ( (ObjectMessageType) verySmall.getType() ).getType() );

        FieldDescriptor address = user.getFieldDescriptor( 12 );
        assertEquals( FieldType.MESSAGE, ( (ObjectMessageType) address.getType() ).getType() );

        FieldDescriptor addressSet = user.getFieldDescriptor( 13 );
        assertEquals( FieldType.MESSAGE, ( (CollectionMessageType) addressSet.getType() ).getType() );

        FieldDescriptor addressList = user.getFieldDescriptor( 14 );
        assertEquals( FieldType.MESSAGE, ( (CollectionMessageType) addressList.getType() ).getType() );

        FieldDescriptor addressMap = user.getFieldDescriptor( 15 );
        assertEquals( FieldType.STRING, ( (MapMessageType) addressMap.getType() ).getKeyType() );
        assertEquals( FieldType.MESSAGE, ( (MapMessageType) addressMap.getType() ).getValueType() );

        FieldDescriptor primitiveSet = user.getFieldDescriptor( 16 );
        assertEquals( FieldType.STRING, ( (CollectionMessageType) primitiveSet.getType() ).getType() );

        ServiceDescriptor authService = container.services.get( "AuthService" );
        Map<String, MethodDescriptor> methods = authService.getMethods();
        assertEquals( "MockService", authService.getParent() );
        MethodDescriptor ping = methods.get( "ping" );
        assertTrue( ping.request == null );
        assertTrue( ping.exceptions.size() == 0 );

        MethodDescriptor addUser = methods.get( "addUser" );
        assertEquals( FieldType.MESSAGE, ( (ObjectMessageType) addUser.request ).getType() );
        assertEquals( "com.turbospaces.demo.User", ( (ObjectMessageType) addUser.request ).getTypeReference() );
        assertTrue( addUser.exceptions.size() == 2 );

        MethodDescriptor getAllUserByFirstName = methods.get( "getAllUserByFirstName" );
        assertEquals( FieldType.STRING, ( (ObjectMessageType) getAllUserByFirstName.request ).getType() );
        assertEquals( FieldType.STRING, ( (MapMessageType) getAllUserByFirstName.response ).getKeyType() );
        assertEquals( FieldType.MESSAGE, ( (MapMessageType) getAllUserByFirstName.response ).getValueType() );
    }
}
