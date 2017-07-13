package io.protolang;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

@Mojo(name = "gen-json-schema", defaultPhase = LifecyclePhase.PACKAGE)
public class ProtolangToJsonSchemaGenMojo extends ProtolangBaseGenMojo {
    @Parameter(property = "gen.jsonSchema", defaultValue = "false")
    private Boolean jsonSchema;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void generate(Class<?> clazz) throws Exception {
        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        mapper.acceptJsonFormatVisitor( mapper.constructType( clazz ), visitor );
        JsonSchema schemaFor = visitor.finalSchema();
        getLog().debug( mapper.writerWithDefaultPrettyPrinter().writeValueAsString( schemaFor ) );
    }
}
