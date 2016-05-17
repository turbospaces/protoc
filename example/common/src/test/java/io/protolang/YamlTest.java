package io.protolang;

import java.io.IOException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class YamlTest {
    Logger logger = LoggerFactory.getLogger( getClass() );

    @Test
    public void works() throws IOException {
        Money money = new Money();
        money.setValue( "12374.34" );
        money.setCurrency( "EUR" );
        YAMLMapper mapper = new YAMLMapper();
        String yaml = mapper.writeValueAsString( money );
        logger.info( "\n" + yaml );
        Money copy = mapper.readValue( yaml, Money.class );

        Assert.assertTrue( EqualsBuilder.reflectionEquals( copy, money ) );
    }
}
