package io.protolang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JavaSerializationTest {
    Logger logger = LoggerFactory.getLogger( getClass() );

    @Test
    public void works() throws IOException, ClassNotFoundException {
        Headers headers = new Headers();
        headers.setMessageId( UUID.randomUUID().toString() );
        headers.setPriority( (short) 1 );
        headers.setRetryAttempt( (short) 2 );
        headers.setTimeout( 100 );
        headers.setTimestamp( System.currentTimeMillis() );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream( baos )) {
            out.writeObject( headers );
        }
        byte[] bytes = baos.toByteArray();
        try (ObjectInputStream input = new ObjectInputStream( new ByteArrayInputStream( bytes ) )) {
            headers = (Headers) input.readObject();
            ObjectMapper mapper = new ObjectMapper();
            logger.debug( mapper.writerWithDefaultPrettyPrinter().writeValueAsString( headers ) );
        }
    }
}
