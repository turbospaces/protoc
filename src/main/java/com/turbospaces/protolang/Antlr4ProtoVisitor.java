package com.turbospaces.protolang;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JEnumConstant;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.turbospaces.protolang.ProtoParserParser.CollectionContext;
import com.turbospaces.protolang.ProtoParserParser.Collection_mapContext;
import com.turbospaces.protolang.ProtoParserParser.Collection_map_valueContext;
import com.turbospaces.protolang.ProtoParserParser.Collection_typeContext;
import com.turbospaces.protolang.ProtoParserParser.Constant_defContext;
import com.turbospaces.protolang.ProtoParserParser.Enum_defContext;
import com.turbospaces.protolang.ProtoParserParser.Enum_member_tagContext;
import com.turbospaces.protolang.ProtoParserParser.MapContext;
import com.turbospaces.protolang.ProtoParserParser.Map_keyContext;
import com.turbospaces.protolang.ProtoParserParser.Map_valueContext;
import com.turbospaces.protolang.ProtoParserParser.Message_defContext;
import com.turbospaces.protolang.ProtoParserParser.Message_field_defContext;
import com.turbospaces.protolang.ProtoParserParser.Package_defContext;
import com.turbospaces.protolang.ProtoParserParser.ProtoContext;
import com.turbospaces.protolang.ProtoParserParser.Service_defContext;
import com.turbospaces.protolang.ProtoParserParser.Service_method_defContext;
import com.turbospaces.protolang.ProtoParserParser.Service_method_excpContext;

public class Antlr4ProtoVisitor extends ProtoParserBaseVisitor<Void> {
    public static final String INT16 = "int16";
    public static final String INT32 = "int32";
    public static final String INT64 = "int64";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";
    public static final String BYTE = "byte";
    public static final String STRING = "string";
    public static final String BOOLEAN = "boolean";
    public static final String BIG_DECIMAL = "bigdecimal";
    public static final String BIG_INTEGER = "biginteger";
    public static final String BINARY = "binary";

    private final Logger logger = Generator.LOGGER;

    private final JCodeModel codeModel;
    private final String containerName;

    public Antlr4ProtoVisitor(JCodeModel codeModel, String containerName) {
        this.codeModel = codeModel;
        this.containerName = containerName;
    }
    @Override
    public Void visitPackage_def(Package_defContext ctx) {
        try {
            String pkg = ctx.package_name().getText().trim();
            JDefinedClass m = codeModel._package( pkg )._interface( containerName );
            logger.debug( "Proto({}.{})", m._package().name(), m.name() );
            return super.visitPackage_def( ctx );
        }
        catch ( JClassAlreadyExistsException err ) {
            throw new RuntimeException( err );
        }
    }
    @Override
    public Void visitConstant_def(Constant_defContext ctx) {
        String constName = ctx.constant_name().getText().trim();
        String constType = ctx.constant_type().TYPE_LITERAL().getText();
        String constValue = ctx.literal_value().getText();

        ProtoContext pkgCtx = (ProtoContext) ctx.parent;
        String pkg = pkgCtx.package_def().package_name().getText().trim();
        JDefinedClass m = codeModel._package( pkg )._getClass( containerName );

        int modifier = JMod.PUBLIC | JMod.STATIC | JMod.FINAL;
        AbstractJType type = resolvePrimiteType( constType ).unboxify();

        JFieldVar f = m.field( modifier, type, constName.toUpperCase(), parseConstant( constType, constValue ) );
        logger.debug( "\t +-> {} {} = {}", f.type().name(), f.name(), constValue );

        return super.visitConstant_def( ctx );
    }
    @Override
    public Void visitMessage_def(Message_defContext ctx) {
        try {
            boolean isError = ctx.messsage_type().ERROR_LITERAL() != null;
            String name = ctx.message_name().getText();
            String parent = null;
            ProtoContext pkgCtx = (ProtoContext) ctx.parent;
            String pkg = pkgCtx.package_def().package_name().getText().trim();
            if ( ctx.message_parent() != null && ctx.message_parent().message_parent_message() != null ) {
                parent = ctx.message_parent().message_parent_message().getText();
            }
            JPackage jPackage = codeModel._package( pkg );
            JDefinedClass m = jPackage._class( name );
            if ( parent != null ) {
                m._extends( resolveType( jPackage, parent ) );
            }
            else {
                if ( isError ) {
                    m._extends( Exception.class );
                }
            }
            logger.debug( "Message({}.{})", m._package().name(), m.name() );
            return super.visitMessage_def( ctx );
        }
        catch ( JClassAlreadyExistsException err ) {
            throw new RuntimeException( err );
        }
    }
    @Override
    public Void visitMessage_field_def(Message_field_defContext ctx) {
        try {
            int fieldTag = Integer.parseInt( ctx.message_field_tag().getText() );
            String fieldName = ctx.message_field_name().getText();

            Message_defContext mCtx = ( (Message_defContext) ctx.parent.getRuleContext() );
            ProtoContext pkgCtx = (ProtoContext) mCtx.parent;
            String pkg = pkgCtx.package_def().package_name().getText().trim();
            JPackage jPackage = codeModel._package( pkg );
            String msgName = mCtx.message_name().getText();
            JDefinedClass m = jPackage._getClass( msgName );

            JFieldVar f = m.field( JMod.PRIVATE, resolveType( jPackage, ctx.message_field_type().collection_map_value() ), fieldName );

            JMethod getter = m.method( JMod.PUBLIC, f.type(), "get" + WordUtils.capitalize( f.name() ) );
            getter.body()._return( f );
            getter.annotate( JsonProperty.class ).param( "index", fieldTag );

            String var = "var";
            JMethod setter = m.method( JMod.PUBLIC, void.class, "set" + WordUtils.capitalize( f.name() ) );
            setter.param( f.type(), var );
            setter.body().assign( JExpr._this().ref( f.name() ), JExpr.ref( var ) );

            JMethod with = m.method( JMod.PUBLIC, m, "with" + WordUtils.capitalize( f.name() ) );
            with.param( f.type(), var );
            with.body().assign( JExpr._this().ref( f.name() ), JExpr.ref( var ) )._return( JExpr._this() );

            logger.debug( "\t +-> {}.{} {}({})", m._package().name(), m.name(), f.name(), f.type().name() );

            return super.visitMessage_field_def( ctx );
        }
        catch ( Throwable err ) {
            throw new RuntimeException( err );
        }
    }
    @Override
    public Void visitEnum_def(Enum_defContext ctx) {
        try {
            String name = ctx.enum_name().getText();
            ProtoContext pkgCtx = (ProtoContext) ctx.parent;
            String pkg = pkgCtx.package_def().package_name().getText().trim();
            JDefinedClass m = codeModel._package( pkg )._enum( name );

            String tag = "tag";
            m.field( JMod.PUBLIC | JMod.FINAL, codeModel.INT, tag );

            JMethod constructor = m.constructor( JMod.PRIVATE );
            constructor.param( codeModel.INT, tag );
            constructor.body().assign( JExpr._this().ref( tag ), JExpr.ref( tag ) );

            logger.debug( "Enum({}.{})", m._package().name(), m.name() );
            return super.visitEnum_def( ctx );
        }
        catch ( JClassAlreadyExistsException err ) {
            throw new RuntimeException( err );
        }
    }
    @Override
    public Void visitEnum_member_tag(Enum_member_tagContext ctx) {
        int enumTag = Integer.parseInt( ctx.enum_tag().TAG().getText() );
        String enumMember = ctx.enum_member().IDENTIFIER().getText();

        Enum_defContext enumCtx = (Enum_defContext) ctx.parent.parent;
        String enumName = enumCtx.enum_name().getText();
        ProtoContext pkgCtx = (ProtoContext) enumCtx.parent;
        String pkg = pkgCtx.package_def().package_name().getText().trim();

        JDefinedClass m = codeModel._package( pkg )._getClass( enumName );
        JEnumConstant enumConstant = m.enumConstant( enumMember );
        enumConstant.arg( JExpr.lit( enumTag ) ).annotate( JsonProperty.class ).param( "index", enumTag );

        logger.debug( "\t +-> {}.{} {}", m._package().name(), m.name(), enumMember );
        return super.visitEnum_member_tag( ctx );
    }
    @Override
    public Void visitService_def(Service_defContext ctx) {
        try {
            String name = ctx.service_name().getText();

            ProtoContext pkgCtx = (ProtoContext) ctx.parent;
            String pkg = pkgCtx.package_def().package_name().getText().trim();
            JPackage jPackage = codeModel._package( pkg );
            JDefinedClass i = jPackage._interface( name );

            if ( ctx.service_parent() != null && ctx.service_parent().service_parent_message() != null ) {
                String parent = ctx.service_parent().service_parent_message().getText();
                i._extends( resolveType( jPackage, parent ) );
            }

            logger.debug( "Interface({}.{})", i._package().name(), i.name() );
            return super.visitService_def( ctx );
        }
        catch ( JClassAlreadyExistsException err ) {
            throw new RuntimeException( err );
        }
    }
    @Override
    public Void visitService_method_def(Service_method_defContext ctx) {
        String methodName = ctx.service_method_name().getText();
        ProtoContext pkgCtx = (ProtoContext) ctx.parent.parent;
        String pkg = pkgCtx.package_def().package_name().getText().trim();
        JPackage jPackage = codeModel._package( pkg );

        Service_defContext sCtx = (Service_defContext) ctx.parent;
        String serviceName = sCtx.service_name().getText();
        JDefinedClass service = jPackage._getClass( serviceName );

        Collection_map_valueContext reqCtx = ctx.service_method_req().collection_map_value();
        AbstractJClass respType = resolveType( jPackage, ctx.service_method_resp().collection_map_value() );
        AbstractJClass reqType = null;

        JMethod m = service.method( JMod.PUBLIC, respType, methodName );
        if ( reqCtx != null ) {
            reqType = resolveType( jPackage, reqCtx );
            m.param( reqType, "var" );
        }

        List<Service_method_excpContext> excpCtxs = ctx.service_method_throws().service_method_excp();
        for ( Service_method_excpContext excpCtx : excpCtxs ) {
            AbstractJClass exception = resolveType( jPackage, excpCtx.IDENTIFIER().getText() );
            m._throws( exception );
        }

        logger.debug( "\t +-> {}({}) -> {}", m.name(), ( reqType != null ? reqType.name() : "" ), respType.name() );

        return super.visitService_method_def( ctx );
    }
    private AbstractJClass resolveType(JPackage pkg, String name) {
        AbstractJClass definedClass = pkg._getClass( name );
        if ( definedClass == null ) {
            Collection<Class<?>> candidates = new LinkedList<>();
            Iterator<JPackage> packages = codeModel.packages();
            while ( packages.hasNext() ) {
                JPackage jPackage = packages.next();
                try {
                    Class<?> loadClass = Thread.currentThread().getContextClassLoader().loadClass( jPackage.name() + "." + name );
                    candidates.add( loadClass );
                }
                catch ( ClassNotFoundException e ) {}
            }
            if ( !candidates.isEmpty() ) {
                Iterator<Class<?>> it = candidates.iterator();
                definedClass = codeModel.ref( it.next() );
                if ( it.hasNext() )
                    throw new IllegalStateException( "collision in import for name " + name );
            }
        }
        return Objects.requireNonNull( definedClass, "unable to find reference to " + name );
    }
    private AbstractJClass resolveType(JPackage pkg, Collection_map_valueContext cmp) {
        //
        // resolve primitive type or reference
        //
        if ( cmp.TYPE_LITERAL() != null || cmp.IDENTIFIER() != null )
            return cmp.TYPE_LITERAL() != null ? resolvePrimiteType( cmp.TYPE_LITERAL().getText() ) : resolveType( pkg, cmp.IDENTIFIER().getText() );

        //
        // resolve (set/list)
        //
        Collection_mapContext cmCtx = cmp.collection_map();
        CollectionContext collectionCtx = cmCtx.collection();
        if ( collectionCtx != null ) {
            Collection_typeContext typeContext = collectionCtx.collection_type();
            boolean isSet = "set".equalsIgnoreCase( collectionCtx.COLLECTION_LITERAL().getText() );

            AbstractJClass detailClass = typeContext.TYPE_LITERAL() != null ? resolvePrimiteType( typeContext.TYPE_LITERAL().getText() )
                    : resolveType( pkg, typeContext.IDENTIFIER().getText() );
            AbstractJClass collectionClass = isSet ? codeModel.ref( Set.class ) : codeModel.ref( List.class );
            return collectionClass.narrow( detailClass );
        }

        //
        // resolve map
        //
        MapContext mapCtx = cmCtx.map();
        Map_keyContext map_key = mapCtx.map_key();
        Map_valueContext map_value = mapCtx.map_value();

        AbstractJClass keyClass = map_key.TYPE_LITERAL() != null ? resolvePrimiteType( map_key.TYPE_LITERAL().getText() )
                : resolveType( pkg, map_key.IDENTIFIER().getText() );
        AbstractJClass valueClass = map_value.TYPE_LITERAL() != null ? resolvePrimiteType( map_value.TYPE_LITERAL().getText() )
                : resolveType( pkg, map_value.IDENTIFIER().getText() );
        AbstractJClass mapClass = codeModel.ref( Map.class );
        return mapClass.narrow( keyClass, valueClass );
    }
    private AbstractJClass resolvePrimiteType(String type) {
        switch ( type ) {
            case INT16:
                return codeModel.ref( Short.class );
            case INT32:
                return codeModel.ref( Integer.class );
            case INT64:
                return codeModel.ref( Long.class );
            case DOUBLE:
                return codeModel.ref( Double.class );
            case FLOAT:
                return codeModel.ref( Float.class );
            case BYTE:
                return codeModel.ref( Byte.class );
            case STRING:
                return codeModel.ref( String.class );
            case BOOLEAN:
                return codeModel.ref( Boolean.class );
            case BIG_INTEGER:
                return codeModel.ref( BigInteger.class );
            case BIG_DECIMAL:
                return codeModel.ref( BigDecimal.class );
            case BINARY: {
                return codeModel.ref( byte[].class );
            }
            default:
                throw new IllegalStateException( type );
        }
    }
    private static IJExpression parseConstant(String type, String text) {
        switch ( type ) {
            case INT16: {
                return JExpr.lit( Short.parseShort( text ) );
            }
            case INT32: {
                return JExpr.lit( Integer.parseInt( text ) );
            }
            case INT64: {
                return JExpr.lit( Long.parseLong( text ) );
            }
            case DOUBLE: {
                return JExpr.lit( Double.parseDouble( text ) );
            }
            case FLOAT: {
                return JExpr.lit( Float.parseFloat( text ) );
            }
            case BYTE: {
                return JExpr.lit( Byte.parseByte( text ) );
            }
            case STRING: {
                return JExpr.lit( text.substring( 1, text.length() - 1 ) );
            }
            case BOOLEAN: {
                return JExpr.lit( Boolean.parseBoolean( text ) );
            }
            default:
                throw new IllegalStateException( text );
        }
    }
}
