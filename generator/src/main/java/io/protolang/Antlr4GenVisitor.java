package io.protolang;

import static io.protolang.Generator.SCHEMA_FIELD;
import static io.protolang.Generator.VERSION_FIELD;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMod;

import io.protolang.ProtoParserParser.Package_defContext;

public class Antlr4GenVisitor extends Antlr4ProtoVisitor {
    private final String schemaAsText;
    private final String version;

    public Antlr4GenVisitor(JCodeModel codeModel,
                            String containerName,
                            Map<String, JCodeModel> importedModels,
                            File input,
                            String version) throws IOException {
        super( codeModel, containerName, importedModels );

        this.version = version;
        try (InputStream in = new FileInputStream( input )) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                List<String> readLines = IOUtils.readLines( in, Charset.defaultCharset() );
                for ( String line : readLines ) {
                    out.write( line.getBytes() );
                }
                out.flush();
                schemaAsText = new String( out.toByteArray() );
            }
        }
    }
    @Override
    public Void visitPackage_def(Package_defContext ctx) {
        Void v = super.visitPackage_def( ctx );

        String pkg = ctx.package_name().getText().trim();
        JDefinedClass container = codeModel._package( pkg )._getClass( containerName );

        int mod = JMod.PUBLIC | JMod.STATIC | JMod.FINAL;
        container.field( mod, String.class, SCHEMA_FIELD, JExpr.lit( schemaAsText ) );
        container.field( mod, String.class, VERSION_FIELD, JExpr.lit( version ) );

        return v;
    }
}
