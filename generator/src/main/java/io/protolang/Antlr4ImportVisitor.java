package io.protolang;

import static io.protolang.Generator.unQuote;
import static java.util.Objects.requireNonNull;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import org.slf4j.Logger;

import io.protolang.ProtoParserParser.Import_defContext;

public class Antlr4ImportVisitor extends ProtoParserBaseVisitor<Void> {
    private final Logger logger = Generator.LOGGER;
    private final Map<String, URL> urls;

    public Antlr4ImportVisitor(Map<String, URL> urls) {
        this.urls = urls;
    }
    @Override
    public Void visitImport_def(Import_defContext ctx) {
        String toImport = unQuote( ctx.import_value().STRING_LITERAL().getText() );

        URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        URL[] classLoaderUrls = classLoader.getURLs();
        logger.debug( "looking resource {} in {}", toImport, classLoaderUrls );

        URL resource = classLoader.getResource( toImport );
        urls.put( toImport, requireNonNull( resource, "unable to resolve import " + toImport ) );
        logger.debug( "found import {} in {}", toImport, resource.toExternalForm() );
        logger.debug( "\t +-> {}", toImport );

        return super.visitImport_def( ctx );
    }
}
