package com.turbospaces.protolang;

import java.net.URL;
import java.util.Collection;
import java.util.Objects;

import com.turbospaces.protolang.ProtoParserParser.Import_defContext;

public class Antlr4ImportVisitor extends ProtoParserBaseVisitor<Void> {
    private final Collection<URL> urls;

    public Antlr4ImportVisitor(Collection<URL> urls) {
        this.urls = urls;
    }
    @Override
    public Void visitImport_def(Import_defContext ctx) {
        String toImport = ctx.import_value().STRING_LITERAL().getText().trim();
        toImport = toImport.substring( 1, toImport.length() - 1 );

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource( toImport );
        Objects.requireNonNull( resource, "unable to load import " + toImport );
        urls.add( resource );
        Generator.LOGGER.debug( "Import({}) -> {}", toImport, resource.toExternalForm() );

        return super.visitImport_def( ctx );
    }
}
