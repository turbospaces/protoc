package io.protolang;

import org.apache.avro.Schema;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.fasterxml.jackson.dataformat.avro.schema.AvroSchemaGenerator;

@Mojo(name = "gen-avro", defaultPhase = LifecyclePhase.PACKAGE)
public class ProtolangToAvroGenMojo extends ProtolangBaseGenMojo {
    @Parameter(property = "gen.avroSchema", defaultValue = "false")
    private Boolean avro;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void generate(Class<?> clazz) throws Exception {
        AvroSchemaGenerator visitor = new AvroSchemaGenerator();
        mapper.acceptJsonFormatVisitor( clazz, visitor );
        AvroSchema schemaFor = visitor.getGeneratedSchema();
        Schema avroSchema = schemaFor.getAvroSchema();
        getLog().debug( avroSchema.toString( true ) );
    }
}
