package com.turbospaces.protoc.gen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
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
    private Template enumTemplate, classTemplate, protoTemplate;
    private String lang = "java";
    private String version = "0.1-SNAPSHOT";

    public Generator(File outDir, String... paths) {
        this.outDir = outDir;
        this.paths = paths;

        Configuration cfg = new Configuration();
        cfg.setObjectWrapper( new BeansWrapper() );
        cfg.setDefaultEncoding( "UTF-8" );
        cfg.setTemplateLoader( new ClassTemplateLoader( getClass(), "/templates" ) );
        try {
            enumTemplate = cfg.getTemplate( lang + "/enum.ftl" );
            classTemplate = cfg.getTemplate( lang + "/class.ftl" );
            protoTemplate = cfg.getTemplate( lang + "/proto.ftl" );
        }
        catch ( IOException e ) {
            Throwables.propagate( e );
        }
    }

    public void run() throws Exception {
        outDir.mkdirs();
        logger.info( "generating code into folder = {}", outDir );

        Preconditions.checkArgument( outDir.isDirectory() );
        ProtoGenerationContext ctx = new ProtoGenerationContext();
        //
        // parse straight protocol files for further stubs generation
        //
        for ( int i = 0; i < paths.length; i++ ) {
            String path = paths[i];
            File f = loadResource( path );
            InputStream asStream = new FileInputStream( f );
            try {
                String text = CharStreams.toString( new InputStreamReader( asStream ) );
                logger.info( "parsing protoc file = {}", path );
                ProtoContainer container = parse( text );

                String n = f.getName().substring( 0, f.getName().indexOf( ".protoc" ) );
                StringBuilder b = new StringBuilder();
                String[] parts = n.split( "[-_]" );
                for ( String s : parts ) {
                    b.append( Character.toUpperCase( s.charAt( 0 ) ) + s.substring( 1 ) );
                }

                container.name = b.toString();
                ctx.containers.add( container );
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
            InputStream asStream = new FileInputStream( loadResource( path ) );
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

            {
                StringWriter out = new StringWriter();
                Map<String, Object> model = Maps.newHashMap();
                model.put( "proto", root );
                model.putAll( common );
                protoTemplate.process( model, out );

                String filename = root.getName() + '.' + lang;
                File f = new File( pkg, filename );
                f.getParentFile().mkdirs();
                Files.write( out.toString().getBytes( Charsets.UTF_8 ), f );
            }

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
    public static File loadResource(String path) throws FileNotFoundException {
        File f = new File( path );
        if ( f.exists() ) {
            return f;
        }
        else {
            URL resource = Thread.currentThread().getContextClassLoader().getResource( path );
            Preconditions.checkNotNull( resource, "no such classpath resource = %s", path );
            return new File( resource.getFile() );
        }
    }

    public static void main(String... args) throws Exception {
        File f = new File( args[0] );
        Generator g = new Generator( f, Arrays.copyOfRange( args, 1, args.length ) );
        g.run();
    }
}
