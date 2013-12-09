package com.turbospaces.protoc.gen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.turbospaces.protoc.EnumDescriptor;
import com.turbospaces.protoc.MessageDescriptor;
import com.turbospaces.protoc.ProtoContainer;
import com.turbospaces.protoc.ProtoParserLexer;
import com.turbospaces.protoc.ProtoParserParser;
import com.turbospaces.protoc.ProtoParserParser.ProtoContext;

import freemarker.cache.ClassTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class Generator {
    private static Logger logger = LoggerFactory.getLogger( Generator.class );
    private File outDir;
    private String[] paths;
    private Template enumTemplate, classTemplate;
    private String lang = "java";
    private String version = "0.1-SNAPSHOT";

    public Generator(File outDir, String... paths) {
        this.outDir = outDir;
        this.paths = paths;

        Preconditions.checkArgument( outDir.isDirectory() );

        Configuration cfg = new Configuration();
        cfg.setObjectWrapper( new BeansWrapper() );
        cfg.setDefaultEncoding( "UTF-8" );
        cfg.setTemplateLoader( new ClassTemplateLoader( getClass(), "/templates" ) );
        try {
            enumTemplate = cfg.getTemplate( lang + "/enum.ftl" );
            classTemplate = cfg.getTemplate( lang + "/class.ftl" );
        }
        catch ( IOException e ) {
            Throwables.propagate( e );
        }
    }

    public void generate() throws Exception {
        logger.info( "generating code into folder = {}", outDir );
        ProtoGenerationContext ctx = new ProtoGenerationContext();
        //
        // parse straight protocol files for further stubs generation
        //
        for ( int i = 0; i < paths.length; i++ ) {
            String path = paths[i];
            InputStream asStream = getClass().getClassLoader().getResourceAsStream( path );
            Preconditions.checkNotNull( asStream, "no such classpath resource = %s", path );
            try {
                String text = CharStreams.toString( new InputStreamReader( asStream ) );
                logger.info( "parsing protoc file = {}", path );
                ctx.containers.add( parse( text ) );
            }
            finally {
                asStream.close();
            }
        }
        //
        // parse imported, but skip generation
        //
        Set<String> allImports = Sets.newLinkedHashSet();
        for ( ProtoContainer c : ctx.containers ) {
            allImports.addAll( c.imports );
        }

        for ( String path : allImports ) {
            InputStream asStream = getClass().getClassLoader().getResourceAsStream( path );
            Preconditions.checkNotNull( asStream, "no such classpath resource = %s", path );
            try {
                String text = CharStreams.toString( new InputStreamReader( asStream ) );
                logger.info( "parsing imported protoc file = {}", path );
                ctx.imports.add( parse( text ) );
            }
            finally {
                asStream.close();
            }
        }

        ctx.init( ctx );

        for ( ProtoContainer root : ctx.containers ) {
            Collection<EnumDescriptor> enums = root.enums.values();
            Collection<MessageDescriptor> messages = root.messages.values();
            File pkg = new File( outDir, root.pkg.replace( '.', File.separatorChar ) );
            pkg.mkdirs();
            Map<String, Object> common = Maps.newHashMap();
            common.put( "pkg", root.pkg );
            common.put( "version", version );

            for ( EnumDescriptor d : enums ) {
                StringWriter out = new StringWriter();
                Map<String, Object> model = Maps.newHashMap();
                model.put( "enum", d );
                model.putAll( common );
                enumTemplate.process( model, out );

                String filename = d.getName() + '.' + lang;
                File f = new File( pkg, filename );
                f.getParentFile().mkdirs();
                Files.write( out.toString().getBytes( Charsets.UTF_8 ), f );
            }

            for ( MessageDescriptor d : messages ) {
                StringWriter out = new StringWriter();
                Map<String, Object> model = Maps.newHashMap();
                model.put( "clazz", d );
                model.putAll( common );
                classTemplate.process( model, out );

                String filename = d.getName() + '.' + lang;
                File f = new File( pkg, filename );
                f.getParentFile().mkdirs();
                Files.write( out.toString().getBytes( Charsets.UTF_8 ), f );
            }
        }
    }
    public static ProtoContainer parse(String text) {
        ANTLRInputStream input = new ANTLRInputStream( text );
        ProtoParserLexer lexer = new ProtoParserLexer( input );
        CommonTokenStream tokens = new CommonTokenStream( lexer );
        ProtoParserParser parser = new ProtoParserParser( tokens );
        parser.setTrace( logger.isDebugEnabled() );
        parser.removeErrorListeners();
        parser.addErrorListener( new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
                List<String> stack = ( (ProtoParserParser) recognizer ).getRuleInvocationStack();
                Collections.reverse( stack );
                logger.error( "rule stack: {}", stack );
                logger.error( "line {}:{} at {}: error={}", line, charPositionInLine, offendingSymbol, msg );
            }
        } );
        ProtoContext protoContext = parser.proto();
        ProtoContainer container = new ProtoContainer();
        Antlr4ProtoVisitor visitor = new Antlr4ProtoVisitor( parser, container );
        visitor.visit( protoContext );
        return container;
    }
}
