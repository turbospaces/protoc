package io.protolang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;

public class AvroTest {
    Logger logger = LoggerFactory.getLogger( getClass() );

    @Test
    public void works() throws IOException {
        Money money = new Money();
        money.setValue( "12374.34" );
        money.setCurrency( "EUR" );
        AvroMapper mapper = new AvroMapper();
        AvroSchema schema = mapper.schemaFor( money.getClass() );
        logger.info( schema.getAvroSchema().toString( true ) );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writer( schema ).writeValue( baos, money );
        baos.flush();
        byte[] byteArray = baos.toByteArray();

        Money copy = mapper.readerFor( Money.class ).with( schema ).readValue( byteArray );
        Assert.assertTrue( EqualsBuilder.reflectionEquals( copy, money ) );
    }
}
