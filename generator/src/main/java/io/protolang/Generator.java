package io.protolang;

import static java.lang.String.format;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JAnnotationArrayMember;
import com.helger.jcodemodel.JAnnotationStringValue;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JAtom;
import com.helger.jcodemodel.JAtomDouble;
import com.helger.jcodemodel.JAtomFloat;
import com.helger.jcodemodel.JAtomInt;
import com.helger.jcodemodel.JAtomLong;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCase;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldRef;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JMods;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.JStringLiteral;
import com.helger.jcodemodel.JSwitch;
import com.helger.jcodemodel.JVar;

import io.protolang.ProtoParserParser.ProtoContext;

public class Generator {
    public static Logger LOGGER = LoggerFactory.getLogger( "protolang" );

    public static final String TAG = "tag";
    public static final String FIELD = "field";
    public static final String MESSAGES_FIELD = "$$$_MESSAGES_$$$";
    public static final String VERSION_FIELD = "$$$_VERSION_$$$";
    public static final String SCHEMA_FIELD = "$$$_SCHEMA_$$$";
    public static final String UNDERSCORE = "_";

    private final File out;
    private final File[] inputs;
    private final boolean debug;
    private final String version;
    private final boolean failOnParseErrors;

    public Generator(File out, File[] inputs, boolean debug, String version, boolean failOnParseErrors) {
        List<File> in = new ArrayList<File>();
        for ( File file : inputs ) {
            if ( !file.exists() )
                throw new IllegalStateException( String.format( "file %s does not exist", file.getAbsolutePath() ) );
            if ( file.isDirectory() ) {
                in.addAll( Arrays.asList( file.listFiles() ) );
            }
            else {
                in.add( file );
            }
        }
        this.out = out;
        this.debug = debug;
        this.inputs = in.toArray( new File[in.size()] );
        this.version = version;
        this.failOnParseErrors = failOnParseErrors;
    }

    public void run() throws Exception {
        //
        // parse all imports first
        //
        JCodeModel codeModel = new JCodeModel();
        Map<String, JCodeModel> importedModels = new HashMap<String, JCodeModel>();
        for ( File f : inputs ) {
            try (InputStream stream = new FileInputStream( f )) {
                Map<String, URL> urls = new HashMap<String, URL>();
                parse( stream, new Antlr4ImportVisitor( urls ), false );

                for ( Entry<String, URL> entry : urls.entrySet() ) {
                    try (InputStream importStream = entry.getValue().openStream()) {
                        String containerName = toContainerName( entry.getKey() );
                        if ( !importedModels.containsKey( containerName ) ) {
                            JCodeModel cm = new JCodeModel();
                            importedModels.put( containerName, cm );
                            parse( importStream, new Antlr4ProtoVisitor( cm, containerName, importedModels ), false );
                        }
                    }
                }
            }
        }

        for ( File f : inputs ) {
            try (InputStream asStream = new FileInputStream( f )) {
                String containerName = toContainerName( f.getName() );
                Antlr4GenVisitor visitor = new Antlr4GenVisitor( codeModel, containerName, importedModels, f, version );
                parse( asStream, visitor, debug );
            }
        }

        List<JPackage> packages = new LinkedList<JPackage>();
        Iterator<JPackage> it = codeModel.packages();
        while ( it.hasNext() ) {
            packages.add( it.next() );
        }

        for ( JPackage jPackage : packages ) {
            Collection<JDefinedClass> classes = jPackage.classes();
            for ( JDefinedClass next : classes ) {
                if ( next.isClass() && !next.isInterface() && next.getClassType() != EClassType.ENUM ) {
                    Collection<JFieldVar> fields = next.fields().values();
                    TreeMap<Integer, JFieldVar> fieldsOrder = new TreeMap<Integer, JFieldVar>();

                    //
                    // try to detect duplicate field(s) by tag in class
                    //
                    for ( JFieldVar f : fields ) {
                        JMods mods = f.mods();
                        if ( !mods.isStatic() ) {
                            int fieldTag = tag( f, next, codeModel );
                            if ( fieldsOrder.containsKey( fieldTag ) )
                                throw new IllegalStateException( String.format( "field with tag = %s already defined in this message", fieldTag ) );
                            fieldsOrder.put( fieldTag, f );
                        }
                    }

                    //
                    // try to detect duplicate field(s) in hierarchy
                    //
                    AbstractJClass travel = next._extends();
                    while ( travel != null ) {
                        for ( JCodeModel importedCodeModel : importedModels.values() ) {
                            JDefinedClass parent = importedCodeModel._getClass( travel.fullName() );
                            if ( parent == null ) {
                                parent = codeModel._getClass( travel.fullName() );
                            }
                            if ( parent != null ) {
                                for ( JFieldVar f : fields ) {
                                    JMods mods = f.mods();
                                    if ( !mods.isStatic() ) {
                                        if ( parent.containsField( f.name() ) )
                                            throw new IllegalStateException( format( "parent %s contains field(%s) already",
                                                                                     parent.fullName(),
                                                                                     f.name() ) );
                                        for ( JFieldVar pf : parent.fields().values() ) {
                                            mods = pf.mods();
                                            if ( !mods.isStatic() ) {
                                                int fieldTag = tag( f, next, codeModel );
                                                int pFieldTag = tag( pf, parent, codeModel );
                                                if ( fieldTag == pFieldTag )
                                                    throw new IllegalStateException( format( "field with tag = %s already defined in %s",
                                                                                             fieldTag,
                                                                                             parent.fullName() ) );
                                                fieldsOrder.put( pFieldTag, pf );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        travel = travel._extends();
                    }

                    JAnnotationArrayMember jsonOrderArray = next.annotate( JsonPropertyOrder.class ).paramArray( JAnnotationUse.SPECIAL_KEY_VALUE );

                    String var = "var";
                    String value = "value";
                    JMethod m1 = next.method( JMod.PUBLIC, Object.class, "value" );
                    JMethod m2 = next.method( JMod.PUBLIC, int.class, "tag" );
                    JMethod m3 = next.method( JMod.PUBLIC, String.class, "field" );
                    JMethod m4 = next.method( JMod.PUBLIC, void.class, "value" );
                    AbstractJClass override = codeModel.ref( Override.class );
                    AbstractJClass ioException = codeModel.ref( IOException.class );
                    AbstractJClass classNotFound = codeModel.ref( ClassNotFoundException.class );

                    String writeExternalLiteral = "writeExternal";
                    String readExternalLiteral = "readExternal";
                    String writeIntLiteral = "writeInt";
                    String writeObjectLiteral = "writeObject";
                    String readIntLiteral = "readInt";
                    String readObject = "readObject";

                    JMethod writeMethod = next.method( JMod.PUBLIC, void.class, writeExternalLiteral )._throws( ioException );
                    writeMethod.param( codeModel.ref( ObjectOutput.class ), var );
                    writeMethod.annotate( override );

                    JMethod readMethod = next.method( JMod.PUBLIC, void.class, readExternalLiteral )._throws( ioException )._throws( classNotFound );
                    readMethod.param( codeModel.ref( ObjectInput.class ), var );
                    readMethod.annotate( override );

                    JBlock readBody = readMethod.body()._while( JExpr.TRUE ).body();
                    JVar readTagVar = readBody.decl( codeModel.INT, TAG );
                    JVar readTagValueVar = readBody.decl( codeModel.ref( Object.class ), value, JExpr._null() );

                    readBody.assign( readTagVar, JExpr.ref( var ).invoke( readIntLiteral ) );
                    readBody._if( JExpr.ref( readTagVar ).ne( JExpr.lit( 0 ) ) )._then().assign( readTagValueVar,
                                                                                                 JExpr.ref( var ).invoke( readObject ) );
                    JSwitch readTagSwitch = readBody._switch( readTagVar );
                    readTagSwitch._case( JExpr.lit( 0 ) ).body()._return();

                    m1.param( int.class, var );
                    m2.param( String.class, var );
                    m3.param( int.class, var );
                    m4.param( int.class, var );
                    m4.param( Object.class, value );
                    JSwitch s1 = m1.body()._switch( JExpr.ref( var ) );
                    JSwitch s2 = m2.body()._switch( JExpr.ref( var ) );
                    JSwitch s3 = m3.body()._switch( JExpr.ref( var ) );
                    JSwitch s4 = m4.body()._switch( JExpr.ref( var ) );

                    next._implements( codeModel.ref( Externalizable.class ) );
                    for ( Entry<Integer, JFieldVar> e : fieldsOrder.entrySet() ) {
                        JFieldVar f = e.getValue();
                        JFieldRef constTag = JExpr.ref( toConstTagName( f ) );
                        String constFieldName = toConstFieldName( f );
                        JInvocation getter = JExpr.invoke( getterName( f, next, codeModel ) );

                        jsonOrderArray.param( JExpr.direct( next.name() + "." + constFieldName ) );

                        writeMethod.body().invoke( JExpr.ref( var ), writeIntLiteral ).arg( constTag );
                        writeMethod.body().invoke( JExpr.ref( var ), writeObjectLiteral ).arg( getter );

                        JCase case1 = s1._case( constTag );
                        JCase case2 = s2._case( JExpr.ref( constFieldName ) );
                        JCase case3 = s3._case( constTag );
                        JCase case4 = s4._case( constTag );
                        JCase case5 = readTagSwitch._case( constTag );

                        case1.body()._return( getter );
                        case2.body()._return( constTag );
                        case3.body()._return( JExpr.ref( constFieldName ) );
                        JInvocation setInvocation = JExpr.invoke( setterName( f ) ).arg( JExpr.cast( f.type(), JExpr.ref( value ) ) );
                        case4.body().add( setInvocation )._break();
                        case5.body().add( setInvocation )._break();
                    }

                    writeMethod.body().invoke( JExpr.ref( var ), writeIntLiteral ).arg( JExpr.lit( 0 ) );

                    JInvocation err = JExpr._new( codeModel.ref( IllegalArgumentException.class ) );
                    s1._default().body()._throw( err );
                    s2._default().body()._throw( err );
                    s3._default().body()._throw( err );
                    s4._default().body()._break();
                    readTagSwitch._default().body()._break();
                }
            }
        }

        LOGGER.info( "writing generated code model to {} ...", out );
        codeModel.build( out );
    }
    public static IJExpression fieldInit(JFieldVar jf) {
        try {
            Field init = ( (Class<?>) jf.getClass().getGenericSuperclass() ).getDeclaredField( "m_aInitExpr" );
            init.setAccessible( true );
            return (IJExpression) init.get( jf );
        }
        catch ( Exception e ) {
            throw new Error( e );
        }
    }
    public static Object expressionValue(IJExpression expression) {
        if ( expression instanceof JStringLiteral ) {
            JStringLiteral init = (JStringLiteral) expression;
            return init.what();
        }
        else if ( expression instanceof JAtomInt ) {
            JAtomInt init = (JAtomInt) expression;
            return Integer.valueOf( init.what() ).toString();
        }
        else if ( expression instanceof JAtomLong ) {
            JAtomLong init = (JAtomLong) expression;
            return Long.valueOf( init.what() ).toString();
        }
        else if ( expression instanceof JAtomDouble ) {
            JAtomDouble init = (JAtomDouble) expression;
            return Double.valueOf( init.what() ).toString();
        }
        else if ( expression instanceof JAtomFloat ) {
            JAtomFloat init = (JAtomFloat) expression;
            return Float.valueOf( init.what() ).toString();
        }
        JAtom init = (JAtom) expression;
        return init.what();
    }
    public static String toConstTagName(JFieldVar field) {
        StringBuilder builder = new StringBuilder( TAG ).append( UNDERSCORE );
        return toJavaConstName( builder, field.name() );
    }
    public static String toConstFieldName(JFieldVar field) {
        StringBuilder builder = new StringBuilder( FIELD ).append( UNDERSCORE );
        return toJavaConstName( builder, field.name() );
    }
    public static String toJavaConstName(StringBuilder b, String s) {
        Iterator<String> i = Arrays.asList( s.split( "(?=\\p{Upper})" ) ).iterator();
        while ( i.hasNext() ) {
            b.append( i.next() );
            if ( i.hasNext() ) {
                b.append( UNDERSCORE );
            }
        }
        return b.toString().toUpperCase();
    }
    public static int tag(JFieldVar f, JDefinedClass m, JCodeModel codeModel) {
        String getterName = getterName( f, m, codeModel );
        JMethod getter = m.getMethod( getterName, new AbstractJType[] {} );
        Collection<JAnnotationUse> annotations = getter.annotations();
        for ( JAnnotationUse annotation : annotations ) {
            if ( JsonProperty.class.getName().equals( annotation.getAnnotationClass().fullName() ) ) {
                JAnnotationStringValue val = (JAnnotationStringValue) annotation.getParam( "index" );
                Object nativeValue = val.nativeValue();
                if ( nativeValue instanceof Integer )
                    return (Integer) val.nativeValue();
                JFieldRef ref = (JFieldRef) nativeValue;
                return Integer.valueOf( expressionValue( fieldInit( m.fields().get( ref.name() ) ) ).toString() );
            }
        }
        throw new IllegalStateException( "no field tag defined in " + f.name() );
    }
    public void parse(InputStream stream, ProtoParserBaseVisitor<Void> visitor, boolean trace) throws IOException {
        final ANTLRInputStream input = new ANTLRInputStream( stream );
        final ProtoParserLexer lexer = new ProtoParserLexer( input );
        final CommonTokenStream tokens = new CommonTokenStream( lexer );
        final ProtoParserParser parser = new ProtoParserParser( tokens );
        parser.setTrace( trace );
        parser.removeErrorListeners();
        final List<String> parseErrros = new LinkedList<String>();
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

                String parseError = String.format( "line %s:%s at %s: error=%s", line, charPositionInLine, offendingSymbol, msg );

                LOGGER.warn( "rule stack: " + stack );
                LOGGER.warn( parseError );
                parseErrros.add( parseError );
            }
        } );
        if ( failOnParseErrors && !parseErrros.isEmpty() ) {
            for ( String err : parseErrros ) {
                LOGGER.error( err );
            }
            throw new IllegalStateException( "schema has errors" );
        }
        ProtoContext protoContext = parser.proto();
        visitor.visit( protoContext );
    }
    public static String unQuote(String str) {
        return str.substring( 1, str.length() - 1 );
    }
    public static String toContainerName(String orig) {
        String name = orig.substring( 0, orig.indexOf( ".lang" ) );

        StringBuilder b = new StringBuilder();
        String[] parts = name.split( "[-_]" );
        for ( String s : parts ) {
            b.append( Character.toUpperCase( s.charAt( 0 ) ) + s.substring( 1 ) );
        }
        return b.toString();
    }
    public static String getterName(JFieldVar f, JDefinedClass m, JCodeModel codeModel) {
        String getterName = "get" + WordUtils.capitalize( f.name() );
        if ( f.type().equals( codeModel.ref( Boolean.class ) ) ) {
            getterName = "is" + WordUtils.capitalize( f.name() );
        }
        return getterName;
    }
    public static String setterName(JFieldVar f) {
        String setterName = "set" + WordUtils.capitalize( f.name() );
        return setterName;
    }
    public static void main(String... args) throws Exception {
        Options options = new Options();
        options.addOption( "o", "output", true, "output folder" );
        options.addOption( "i", "input", true, "input file" );
        options.addOption( "v", "version", true, "version of schema" );
        options.addOption( "d", "debug", false, "enable extensive debug" );
        options.addOption( "f", "failOnParseError", false, "fail on parse errors" );

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args );
        String[] inputs = cmd.getOptionValues( "input" );
        String output = cmd.getOptionValue( "output" );
        String version = cmd.getOptionValue( "version" );
        boolean debug = Boolean.parseBoolean( cmd.getOptionValue( "debug", Boolean.FALSE.toString() ) );
        boolean failOnParseErrors = Boolean.parseBoolean( cmd.getOptionValue( "failOnParseError", Boolean.TRUE.toString() ) );

        LOGGER.info( "input={},output={}", inputs, output );

        File[] in = new File[inputs.length];
        for ( int i = 0; i < inputs.length; i++ ) {
            String next = inputs[i];
            in[i] = new File( next );
        }

        File out = new File( output );
        Generator g = new Generator( out, in, debug, version, failOnParseErrors );
        g.run();
    }
}
