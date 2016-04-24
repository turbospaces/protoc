package com.turbospaces.protolang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JAnnotationStringValue;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JPackage;
import com.turbospaces.protolang.ProtoParserParser.ProtoContext;

public class Generator {
    public static Logger LOGGER = LoggerFactory.getLogger( "protolang" );

    private final File outDir;
    private final String[] paths;
    private final boolean debug;

    public Generator(File outDir, String[] paths, boolean debug) {
        this.outDir = outDir;
        this.paths = paths;
        this.debug = debug;
    }

    public void run() throws Exception {
        outDir.mkdirs();

        //
        // parse all imports first
        //
        JCodeModel codeModel = new JCodeModel();
        JCodeModel[] importedModels = new JCodeModel[paths.length];
        for ( int i = 0; i < paths.length; i++ ) {
            File f = loadResource( paths[i] );
            try (InputStream stream = new FileInputStream( f )) {
                List<URL> urls = new LinkedList<>();
                parse( stream, new Antlr4ImportVisitor( urls ) );

                importedModels[i] = new JCodeModel();
                for ( URL url : urls ) {
                    try (InputStream s = url.openStream()) {
                        parse( s, new Antlr4ProtoVisitor( importedModels[i], toContainerName( new File( url.toURI() ) ) ) );
                    }
                }
                Iterator<JPackage> packages = importedModels[i].packages();
                while ( packages.hasNext() ) {
                    JPackage pkg = packages.next();
                    Collection<JDefinedClass> classes = pkg.classes();
                    LOGGER.debug( "imported pkg {}", pkg.name() );
                    for ( JDefinedClass next : classes ) {
                        if ( next.isClass() && !next.isInterface() && next.getClassType() != EClassType.ENUM ) {
                            LOGGER.debug( "\t +-> {}", next.name() );
                            codeModel._package( next.getPackage().name() );
                        }
                    }
                }
            }
        }

        for ( String path : paths ) {
            File f = loadResource( path );
            try (InputStream asStream = new FileInputStream( f )) {
                Antlr4ProtoVisitor visitor = new Antlr4ProtoVisitor( codeModel, toContainerName( f ) );
                parse( asStream, visitor );
            }
        }

        List<JPackage> packages = new LinkedList<>();
        Iterator<JPackage> it = codeModel.packages();
        while ( it.hasNext() ) {
            packages.add( it.next() );
        }

        for ( JPackage jPackage : packages ) {
            Collection<JDefinedClass> classes = jPackage.classes();
            for ( JDefinedClass next : classes ) {
                if ( next.isClass() && !next.isInterface() && next.getClassType() != EClassType.ENUM ) {
                    Collection<JFieldVar> fields = next.fields().values();
                    TreeMap<Integer, String> fieldsOrder = new TreeMap<>();
                    for ( JFieldVar f : fields ) {
                        int fieldTag = tag( f, next );
                        if ( fieldsOrder.containsKey( fieldTag ) )
                            throw new IllegalStateException( String.format( "field with tag = %s already defined in this message", fieldTag ) );
                        fieldsOrder.put( fieldTag, f.name() );
                    }

                    AbstractJClass travel = next._extends();
                    while ( travel != null ) {
                        for ( JCodeModel importedCodeModel : importedModels ) {
                            JDefinedClass parent = importedCodeModel._getClass( travel.fullName() );
                            if ( parent == null ) {
                                parent = codeModel._getClass( travel.fullName() );
                            }
                            if ( parent != null ) {
                                for ( JFieldVar f : fields ) {
                                    if ( parent.containsField( f.name() ) )
                                        throw new IllegalStateException( String.format( "parent %s contains field(%s) already",
                                                                                        parent.fullName(),
                                                                                        f.name() ) );
                                    for ( JFieldVar pf : parent.fields().values() ) {
                                        int fieldTag = tag( f, next );
                                        int parentFieldTag = tag( pf, parent );
                                        if ( fieldTag == parentFieldTag )
                                            throw new IllegalStateException( String.format( "field with tag = %s already defined in %s",
                                                                                            fieldTag,
                                                                                            parent.fullName() ) );
                                    }
                                }
                                for ( JFieldVar pf : parent.fields().values() ) {
                                    fieldsOrder.put( tag( pf, parent ), pf.name() );
                                }
                            }
                        }
                        travel = travel._extends();
                    }

                    Collection<String> order = fieldsOrder.values();
                    if ( !order.isEmpty() ) {
                        next.annotate( JsonPropertyOrder.class ).paramArray( "value", order.toArray( new String[order.size()] ) );
                    }
                }
            }
        }

        codeModel.build( outDir );
    }
    public static int tag(JFieldVar f, JDefinedClass m) {
        JMethod getter = m.getMethod( "get" + WordUtils.capitalize( f.name() ), new AbstractJType[] {} );
        Collection<JAnnotationUse> annotations = getter.annotations();
        for ( JAnnotationUse annotation : annotations ) {
            if ( JsonProperty.class.getName().equals( annotation.getAnnotationClass().fullName() ) ) {
                JAnnotationStringValue val = (JAnnotationStringValue) annotation.getParam( "index" );
                return (int) val.nativeValue();
            }
        }
        throw new IllegalStateException( "no field tag defined in " + f.name() );
    }
    public void parse(InputStream stream, ProtoParserBaseVisitor<Void> visitor) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream( stream );
        ProtoParserLexer lexer = new ProtoParserLexer( input );
        CommonTokenStream tokens = new CommonTokenStream( lexer );
        ProtoParserParser parser = new ProtoParserParser( tokens );
        parser.setTrace( debug );
        parser.removeErrorListeners();
        parser.addErrorListener( new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer,
                                    Object offendingSymbol,
                                    int line,
                                    int charPositionInLine,
                                    String msg,
                                    RecognitionException e) {
                List<String> stack = ( (ProtoParserParser) recognizer ).getRuleInvocationStack();
                Collections.reverse( stack );
                LOGGER.error( "rule stack: {}", stack );
                LOGGER.error( "line {}:{} at {}: error={}", line, charPositionInLine, offendingSymbol, msg );
            }
        } );
        ProtoContext protoContext = parser.proto();
        visitor.visit( protoContext );
    }
    public static File loadResource(String path) {
        File f = new File( path );
        if ( f.exists() )
            return f;
        URL resource = Thread.currentThread().getContextClassLoader().getResource( path );
        return new File( resource.getFile() );
    }
    public static String toContainerName(File f) {
        String name = f.getName().substring( 0, f.getName().indexOf( ".lang" ) );

        StringBuilder b = new StringBuilder();
        String[] parts = name.split( "[-_]" );
        for ( String s : parts ) {
            b.append( Character.toUpperCase( s.charAt( 0 ) ) + s.substring( 1 ) );
        }
        return b.toString();
    }
    public static void main(String... args) throws Exception {
        Options options = new Options();
        options.addOption( "o", "output", true, "output folder" );
        options.addOption( "i", "input", true, "input file" );
        options.addOption( "d", "debug", false, "enable extensive debug" );

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args );
        String[] inputs = cmd.getOptionValues( "input" );
        String output = cmd.getOptionValue( "output" );
        boolean debug = false;
        if ( cmd.hasOption( "debug" ) ) {
            debug = Boolean.parseBoolean( cmd.getOptionValue( "debug" ) );
        }

        LOGGER.info( "input={},output={}", inputs, output );

        File f = new File( output );
        Generator g = new Generator( f, inputs, debug );
        g.run();
    }
}
