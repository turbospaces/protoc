package io.protolang;

import static io.protolang.Generator.unQuote;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JAnnotationArrayMember;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JArray;
import com.helger.jcodemodel.JAtom;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JEnumConstant;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JMods;
import com.helger.jcodemodel.JPackage;

import io.protolang.ProtoParserParser.All_identifiersContext;
import io.protolang.ProtoParserParser.Any_of_defContext;
import io.protolang.ProtoParserParser.CollectionContext;
import io.protolang.ProtoParserParser.Collection_mapContext;
import io.protolang.ProtoParserParser.Collection_map_valueContext;
import io.protolang.ProtoParserParser.Collection_typeContext;
import io.protolang.ProtoParserParser.Constant_defContext;
import io.protolang.ProtoParserParser.Enum_defContext;
import io.protolang.ProtoParserParser.Enum_member_tagContext;
import io.protolang.ProtoParserParser.Import_defContext;
import io.protolang.ProtoParserParser.Literal_valueContext;
import io.protolang.ProtoParserParser.MapContext;
import io.protolang.ProtoParserParser.Map_keyContext;
import io.protolang.ProtoParserParser.Map_valueContext;
import io.protolang.ProtoParserParser.Message_defContext;
import io.protolang.ProtoParserParser.Message_field_defContext;
import io.protolang.ProtoParserParser.Message_field_default_valueContext;
import io.protolang.ProtoParserParser.Message_field_json_typeContext;
import io.protolang.ProtoParserParser.Message_field_optionsContext;
import io.protolang.ProtoParserParser.Message_field_requiredContext;
import io.protolang.ProtoParserParser.Message_field_typeContext;
import io.protolang.ProtoParserParser.One_of_defContext;
import io.protolang.ProtoParserParser.One_of_def_memberContext;
import io.protolang.ProtoParserParser.Package_defContext;
import io.protolang.ProtoParserParser.ProtoContext;
import io.protolang.ProtoParserParser.Service_defContext;
import io.protolang.ProtoParserParser.Service_method_defContext;
import io.protolang.ProtoParserParser.Service_method_excpContext;

public class Antlr4ProtoVisitor extends ProtoParserBaseVisitor<Void> {
    protected final Logger logger = Generator.LOGGER;

    protected final JCodeModel codeModel;
    protected final String containerName;
    protected final Set<String> importedContainers;
    protected final Map<String, JCodeModel> importedModels;

    public Antlr4ProtoVisitor(JCodeModel codeModel, String containerName, Map<String, JCodeModel> importedModels) {
        this.codeModel = codeModel;
        this.containerName = containerName;
        this.importedModels = importedModels;
        this.importedContainers = new HashSet<String>();
    }
    @Override
    public Void visitImport_def(Import_defContext ctx) {
        String toImport = unQuote( ctx.import_value().STRING_LITERAL().getText() );
        String c = Generator.toContainerName( toImport );
        importedContainers.add( c );
        return super.visitImport_def( ctx );
    }
    @Override
    public Void visitPackage_def(Package_defContext ctx) {
        try {
            String pkg = ctx.package_name().getText();
            JDefinedClass container = codeModel._package( pkg )._interface( containerName );
            logger.info( "Proto({}.{})", container._package().name(), container.name() );
            return super.visitPackage_def( ctx );
        }
        catch ( JClassAlreadyExistsException err ) {
            throw new RuntimeException( err );
        }
    }
    @Override
    public Void visitConstant_def(Constant_defContext ctx) {
        String constName = ctx.constant_name().IDENTIFIER().getText();
        String constType = ctx.constant_type().TYPE_LITERAL().getText();
        Literal_valueContext lit = ctx.literal_value();
        ProtoTypes type = ProtoTypes.valueOf( constType.toUpperCase() );

        ProtoContext pkgCtx = (ProtoContext) ctx.getParent();
        String pkg = pkgCtx.package_def().package_name().QUALIFIED_IDENTIFIER().getText();
        JDefinedClass container = codeModel._package( pkg )._getClass( containerName );

        int modifier = JMod.PUBLIC | JMod.STATIC | JMod.FINAL;
        AbstractJType jtype = primitiveType( type ).unboxify();

        JFieldVar f = container.field( modifier, jtype, constName.toUpperCase(), parse( type, container, lit ) );
        logger.info( "\t +-> {} {} = {}", f.type().name(), f.name(), lit.getText() );

        return super.visitConstant_def( ctx );
    }
    @Override
    public Void visitMessage_def(Message_defContext ctx) {
        try {
            boolean isError = ctx.messsage_type().ERROR_LITERAL() != null;
            String name = ctx.message_name().IDENTIFIER().getText();
            AbstractJClass parent = isError ? codeModel.ref( Exception.class ) : codeModel.ref( Object.class );
            ProtoContext pkgCtx = (ProtoContext) ctx.getParent();
            String pkg = pkgCtx.package_def().package_name().QUALIFIED_IDENTIFIER().getText();
            JPackage jPackage = codeModel._package( pkg );
            JDefinedClass m = jPackage._class( name );
            JDefinedClass container = jPackage._getClass( containerName );

            if ( ctx.message_parent() != null && ctx.message_parent().message_parent_message() != null ) {
                All_identifiersContext parentCtx = ctx.message_parent().message_parent_message().all_identifiers();
                parent = resolveType( container, parentCtx );
            }

            if ( parent != null ) {
                m._extends( parent );
            }
            container.field( JMod.PUBLIC | JMod.FINAL | JMod.STATIC, m, name, JExpr._new( m ) );
            addDenifition( container, m );

            JAnnotationUse generated = m.annotate( Generated.class );
            generated.param( "date", new Date().toString() );
            generated.paramArray( JAnnotationUse.SPECIAL_KEY_VALUE ).param( Generator.class.getName() );
            logger.info( "Message({}.{})", m._package().name(), m.name() );
            return super.visitMessage_def( ctx );
        }
        catch ( JClassAlreadyExistsException err ) {
            throw new RuntimeException( err );
        }
    }
    @Override
    public Void visitMessage_field_def(Message_field_defContext ctx) {
        try {
            int fieldTag = Integer.parseInt( ctx.message_field_tag().INTEGER_LITERAL().getText() );
            String fieldName = ctx.message_field_name().IDENTIFIER().getText();
            List<Message_field_optionsContext> optionsCtxs = ctx.message_field_options();

            Message_defContext mCtx = ( (Message_defContext) ctx.getParent().getRuleContext() );
            ProtoContext pkgCtx = (ProtoContext) mCtx.getParent();
            String pkg = pkgCtx.package_def().package_name().QUALIFIED_IDENTIFIER().getText();
            JPackage jPackage = codeModel._package( pkg );
            String msgName = mCtx.message_name().IDENTIFIER().getText();
            JDefinedClass m = jPackage._getClass( msgName );
            JDefinedClass container = jPackage._getClass( containerName );

            Message_field_requiredContext requiredCtx = null;
            Message_field_default_valueContext defaultValueCtx = null;
            Message_field_json_typeContext jsonTypeCtx = null;

            if ( optionsCtxs != null ) {
                for ( Message_field_optionsContext option : optionsCtxs ) {
                    if ( option.message_field_default_value() != null ) {
                        defaultValueCtx = option.message_field_default_value();
                    }
                    if ( option.message_field_json_type() != null ) {
                        jsonTypeCtx = option.message_field_json_type();
                    }
                    if ( option.message_field_required() != null ) {
                        requiredCtx = option.message_field_required();
                    }
                }
            }

            String jsonType = null;

            if ( jsonTypeCtx != null ) {
                jsonType = parseString( container, jsonTypeCtx.literal_value() );
            }

            AbstractJClass fieldType = null;
            JFieldVar field = null;
            IJExpression fieldInit = null;

            List<AbstractJClass> oneOfSubTypes = new LinkedList<AbstractJClass>();
            AbstractJClass anyOfType = null;

            Message_field_typeContext mftCtx = ctx.message_field_type();
            One_of_defContext oneOfCtx = mftCtx.one_of_def();
            Any_of_defContext anyOfCtx = mftCtx.any_of_def();
            if ( oneOfCtx != null ) {
                fieldType = codeModel.ref( Object.class );
                for ( One_of_def_memberContext next : oneOfCtx.one_of_def_member() ) {
                    oneOfSubTypes.add( resolveType( container, next.all_identifiers() ) );
                }
            }
            else if ( anyOfCtx != null ) {
                fieldType = resolveType( container, anyOfCtx.any_of_def_base_type().all_identifiers() );
                anyOfType = fieldType;
            }
            else {
                Collection_map_valueContext collectionMapValueCtx = mftCtx.collection_map_value();
                if ( defaultValueCtx != null ) {
                    ProtoTypes type = ProtoTypes.valueOf( collectionMapValueCtx.TYPE_LITERAL().getText().toUpperCase() );
                    fieldInit = parse( type, container, defaultValueCtx.literal_value() );
                }
                fieldType = resolveType( container, collectionMapValueCtx );
            }

            field = m.field( JMod.PRIVATE, fieldType, fieldName, fieldInit );

            //
            // field meta-data
            //
            int staticMod = JMod.PUBLIC | JMod.FINAL | JMod.STATIC;
            String constTagName = Generator.toConstTagName( field );
            String constFieldName = Generator.toConstFieldName( field );
            m.field( staticMod, int.class, constTagName, JExpr.lit( fieldTag ) );
            m.field( staticMod, String.class, constFieldName, JExpr.lit( field.name() ) );

            //
            // getter
            //

            String getterName = Generator.getterName( field, m, codeModel );
            JMethod getter = m.method( JMod.PUBLIC, field.type(), getterName );
            getter.body()._return( field );
            JAnnotationUse jsonProperty = getter.annotate( JsonProperty.class );
            jsonProperty.param( "index", JExpr.ref( constTagName ) );
            jsonProperty.param( JAnnotationUse.SPECIAL_KEY_VALUE, JExpr.ref( constFieldName ) );

            if ( !oneOfSubTypes.isEmpty() ) {
                JAnnotationUse jsonTypeInfo = getter.annotate( JsonTypeInfo.class );
                jsonTypeInfo.param( "use", JsonTypeInfo.Id.NAME );
                jsonTypeInfo.param( "include", JsonTypeInfo.As.PROPERTY );
                if ( jsonType != null ) {
                    jsonTypeInfo.param( "property", jsonType );
                }

                JAnnotationUse jsonSubTypes = getter.annotate( JsonSubTypes.class );
                JAnnotationArrayMember jsonSubTypesArray = jsonSubTypes.paramArray( JAnnotationUse.SPECIAL_KEY_VALUE );

                for ( AbstractJClass oneOf : oneOfSubTypes ) {
                    JAnnotationUse jsonSubType = jsonSubTypesArray.annotate( JsonSubTypes.Type.class );
                    jsonSubType.param( JAnnotationUse.SPECIAL_KEY_VALUE, oneOf );
                }
            }

            if ( anyOfType != null ) {
                JAnnotationUse jsonTypeInfo = getter.annotate( JsonTypeInfo.class );
                jsonTypeInfo.param( "use", JsonTypeInfo.Id.CLASS );
                jsonTypeInfo.param( "include", JsonTypeInfo.As.PROPERTY );
                if ( jsonType != null ) {
                    jsonTypeInfo.param( "property", jsonType );
                }
            }

            if ( optionsCtxs != null ) {
                if ( fieldInit != null ) {
                    jsonProperty.param( "defaultValue", JExpr.lit( Generator.expressionValue( fieldInit ).toString() ) );
                }
                if ( requiredCtx != null ) {
                    boolean required = parseBool( container, requiredCtx.literal_value() );
                    jsonProperty.param( "required", required );
                    getter.annotate( NotNull.class );
                }
            }

            //
            // setter
            //
            String var = "var";
            JMethod setter = m.method( JMod.PUBLIC, void.class, "set" + WordUtils.capitalize( field.name() ) );
            setter.param( fieldType, var );
            setter.body().assign( JExpr._this().ref( field.name() ), JExpr.ref( var ) );

            logger.info( "\t +-> {}.{} {}({})", m._package().name(), m.name(), field.name(), field.type().name() );

            return super.visitMessage_field_def( ctx );
        }
        catch ( Throwable err ) {
            throw new RuntimeException( err );
        }
    }
    @Override
    public Void visitEnum_def(Enum_defContext ctx) {
        try {
            String name = ctx.enum_name().IDENTIFIER().getText();
            ProtoContext pkgCtx = (ProtoContext) ctx.getParent();
            String pkg = pkgCtx.package_def().package_name().QUALIFIED_IDENTIFIER().getText();
            JPackage jPackage = codeModel._package( pkg );
            JDefinedClass m = codeModel._package( pkg )._enum( name );
            JDefinedClass container = jPackage._getClass( containerName );

            String tag = "tag";
            m.field( JMod.PUBLIC | JMod.FINAL, codeModel.INT, tag );

            JMethod constructor = m.constructor( JMod.PRIVATE );
            constructor.param( codeModel.INT, tag );
            constructor.body().assign( JExpr._this().ref( tag ), JExpr.ref( tag ) );

            addDenifition( container, m );

            logger.info( "Enum({}.{})", m._package().name(), m.name() );
            return super.visitEnum_def( ctx );
        }
        catch ( JClassAlreadyExistsException err ) {
            throw new RuntimeException( err );
        }
    }
    @Override
    public Void visitEnum_member_tag(Enum_member_tagContext ctx) {
        int enumTag = Integer.parseInt( ctx.enum_tag().INTEGER_LITERAL().getText() );
        String enumMember = ctx.enum_member().IDENTIFIER().getText();

        Enum_defContext enumCtx = (Enum_defContext) ctx.getParent();
        String enumName = enumCtx.enum_name().IDENTIFIER().getText();
        ProtoContext pkgCtx = (ProtoContext) enumCtx.getParent();
        String pkg = pkgCtx.package_def().package_name().QUALIFIED_IDENTIFIER().getText();

        JDefinedClass m = codeModel._package( pkg )._getClass( enumName );
        JEnumConstant enumConstant = m.enumConstant( enumMember );
        enumConstant.arg( JExpr.lit( enumTag ) ).annotate( JsonProperty.class ).param( "index", enumTag );

        logger.info( "\t +-> {}.{} {}", m._package().name(), m.name(), enumMember );
        return super.visitEnum_member_tag( ctx );
    }
    @Override
    public Void visitService_def(Service_defContext ctx) {
        try {
            String name = ctx.service_name().IDENTIFIER().getText();

            ProtoContext pkgCtx = (ProtoContext) ctx.getParent();
            String pkg = pkgCtx.package_def().package_name().QUALIFIED_IDENTIFIER().getText();
            JPackage jPackage = codeModel._package( pkg );
            JDefinedClass i = jPackage._interface( name );
            JDefinedClass container = jPackage._getClass( containerName );

            if ( ctx.service_parent() != null && ctx.service_parent().service_parent_message() != null ) {
                All_identifiersContext parent = ctx.service_parent().service_parent_message().all_identifiers();
                i._extends( resolveType( container, parent ) );
            }

            addDenifition( container, i );

            logger.info( "Interface({}.{})", i._package().name(), i.name() );
            return super.visitService_def( ctx );
        }
        catch ( JClassAlreadyExistsException err ) {
            throw new RuntimeException( err );
        }
    }
    @Override
    public Void visitService_method_def(Service_method_defContext ctx) {
        String methodName = ctx.service_method_name().IDENTIFIER().getText();
        ProtoContext pkgCtx = (ProtoContext) ctx.getParent().getParent();
        String pkg = pkgCtx.package_def().package_name().QUALIFIED_IDENTIFIER().getText();
        JPackage jPackage = codeModel._package( pkg );
        JDefinedClass container = jPackage._getClass( containerName );

        Service_defContext sCtx = (Service_defContext) ctx.getParent();
        String serviceName = sCtx.service_name().IDENTIFIER().getText();
        JDefinedClass service = jPackage._getClass( serviceName );

        Collection_map_valueContext reqCtx = ctx.service_method_req().collection_map_value();
        AbstractJClass respType = resolveType( container, ctx.service_method_resp().collection_map_value() );
        AbstractJClass reqType = null;

        JMethod m = service.method( JMod.PUBLIC, respType, methodName );
        if ( reqCtx != null ) {
            reqType = resolveType( container, reqCtx );
            m.param( reqType, "var" );
        }

        List<Service_method_excpContext> excpCtxs = ctx.service_method_throws().service_method_excp();
        for ( Service_method_excpContext excpCtx : excpCtxs ) {
            AbstractJClass exception = resolveType( container, excpCtx.all_identifiers() );
            m._throws( exception );
        }

        logger.info( "\t +-> {}({}) -> {}", m.name(), ( reqType != null ? reqType.name() : "" ), respType.name() );

        return super.visitService_method_def( ctx );
    }
    private AbstractJClass resolveType(JDefinedClass container, All_identifiersContext identifier) {
        AbstractJClass definedClass = null;
        if ( identifier.IDENTIFIER() != null ) {
            String name = identifier.IDENTIFIER().getText();
            definedClass = container.getPackage()._getClass( name );
            if ( definedClass == null ) {
                List<String> pkgs = new LinkedList<String>();
                for ( String ic : importedContainers ) {
                    JCodeModel cm = importedModels.get( ic );
                    Iterator<JPackage> it = cm.packages();
                    while ( it.hasNext() ) {
                        JPackage jPackage = it.next();
                        pkgs.add( jPackage.name() );
                        AbstractJClass toUpdate = jPackage._getClass( name );
                        if ( toUpdate != null ) {
                            if ( definedClass != null && !definedClass.equals( toUpdate ) )
                                throw new IllegalStateException( "no unique type reference to " + identifier.getText() );
                            definedClass = toUpdate;
                        }
                    }
                }
                if ( definedClass == null ) {
                    String err = String.format( "unable to resolve type %s in any of %s packages", name, pkgs );
                    throw new IllegalStateException( err );
                }
            }
        }
        else {
            String qname = identifier.QUALIFIED_IDENTIFIER().getText();
            int idx = qname.lastIndexOf( '.' );
            String pkg = qname.substring( 0, idx );
            String msgName = qname.substring( idx + 1 );

            for ( String ic : importedContainers ) {
                JCodeModel cm = importedModels.get( ic );
                Iterator<JPackage> it = cm.packages();
                while ( it.hasNext() ) {
                    JPackage jPackage = it.next();
                    if ( pkg.equals( jPackage.name() ) ) {
                        definedClass = jPackage._getClass( msgName );
                    }
                }
            }
            if ( definedClass == null ) {
                String err = String.format( "unable to resolve fully qualified type %s, check imports", qname );
                throw new IllegalStateException( err );
            }
        }

        return definedClass;
    }

    private AbstractJClass resolveType(JDefinedClass container, Collection_map_valueContext cmp) {
        //
        // resolve primitive type or reference
        //
        if ( cmp.TYPE_LITERAL() != null )
            return primitiveType( ProtoTypes.valueOf( cmp.TYPE_LITERAL().getText().toUpperCase() ) );
        else if ( cmp.all_identifiers() != null )
            return resolveType( container, cmp.all_identifiers() );

        //
        // resolve (set/list)
        //
        Collection_mapContext cmCtx = cmp.collection_map();
        CollectionContext collectionCtx = cmCtx.collection();
        if ( collectionCtx != null ) {
            Collection_typeContext typeContext = collectionCtx.collection_type();
            boolean isSet = "set".equalsIgnoreCase( collectionCtx.COLLECTION_LITERAL().getText() );
            AbstractJClass collectionClass = isSet ? codeModel.ref( Set.class ) : codeModel.ref( List.class );

            if ( typeContext.TYPE_LITERAL() != null )
                return collectionClass.narrow( primitiveType( ProtoTypes.valueOf( typeContext.TYPE_LITERAL().getText().toUpperCase() ) ) );
            return collectionClass.narrow( resolveType( container, typeContext.all_identifiers() ) );
        }

        //
        // resolve map
        //
        MapContext mapCtx = cmCtx.map();
        Map_keyContext map_key = mapCtx.map_key();
        Map_valueContext map_value = mapCtx.map_value();

        AbstractJClass keyClass;
        AbstractJClass valueClass;

        if ( map_key.TYPE_LITERAL() != null ) {
            keyClass = primitiveType( ProtoTypes.valueOf( map_key.TYPE_LITERAL().getText().toUpperCase() ) );
        }
        else {
            keyClass = resolveType( container, map_key.all_identifiers() );
        }

        if ( map_value.TYPE_LITERAL() != null ) {
            valueClass = primitiveType( ProtoTypes.valueOf( map_value.TYPE_LITERAL().getText().toUpperCase() ) );
        }
        else {
            valueClass = resolveType( container, map_value.all_identifiers() );
        }

        AbstractJClass mapClass = codeModel.ref( Map.class );
        return mapClass.narrow( keyClass, valueClass );
    }
    private AbstractJClass primitiveType(ProtoTypes type) {
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
            case BINARY: {
                return codeModel.ref( byte[].class );
            }
            default:
                throw new IllegalStateException( type.name() );
        }
    }
    private IJExpression parseSubstitution(JDefinedClass container, All_identifiersContext identifier) {
        JFieldVar f = null;
        if ( identifier.IDENTIFIER() != null ) {
            String contantName = identifier.IDENTIFIER().getText();
            if ( container.containsField( contantName ) ) {
                f = container.fields().get( contantName );
            }
            else {
                Set<String> pkgs = new HashSet<String>();
                for ( String ic : importedContainers ) {
                    JCodeModel cm = importedModels.get( ic );
                    Iterator<JPackage> it = cm.packages();
                    while ( it.hasNext() ) {
                        JPackage jPackage = it.next();
                        pkgs.add( jPackage.name() );
                        JDefinedClass c = jPackage._getClass( ic );
                        Collection<JFieldVar> fields = c.fields().values();
                        for ( JFieldVar candidate : fields ) {
                            JMods mods = candidate.mods();
                            if ( mods.isStatic() ) {
                                if ( candidate.name().equals( contantName ) ) {
                                    if ( f != null )
                                        throw new IllegalStateException( "no unique substitution of " + identifier.getText() );
                                    f = candidate;
                                }
                            }
                        }
                    }
                }
                if ( f == null ) {
                    String err = String.format( "unable to resolve subsitution %s in any of %s packages", contantName, pkgs );
                    throw new IllegalStateException( err );
                }
            }
        }
        else {
            String qname = identifier.QUALIFIED_IDENTIFIER().getText();
            int idx = qname.lastIndexOf( '.' );
            String pkg = qname.substring( 0, idx );
            String contantName = qname.substring( idx + 1 );

            for ( String ic : importedContainers ) {
                JCodeModel cm = importedModels.get( ic );
                Iterator<JPackage> it = cm.packages();
                while ( it.hasNext() ) {
                    JPackage jPackage = it.next();
                    if ( pkg.equals( jPackage.name() ) ) {
                        JDefinedClass c = jPackage._getClass( ic );
                        Collection<JFieldVar> fields = c.fields().values();
                        for ( JFieldVar candidate : fields ) {
                            JMods mods = candidate.mods();
                            if ( mods.isStatic() ) {
                                if ( candidate.name().equals( contantName ) ) {
                                    f = candidate;
                                }
                            }
                        }
                    }
                }
            }

            if ( f == null ) {
                String err = String.format( "unable to resolve fully qualified subsitution %s, check imports", qname );
                throw new IllegalStateException( err );
            }
        }

        return Generator.fieldInit( f );
    }
    private IJExpression parse(ProtoTypes type, JDefinedClass container, Literal_valueContext literal) {
        switch ( type ) {
            case INT16: {
                return JExpr.lit( parseShort( container, literal ) );
            }
            case INT32: {
                return JExpr.lit( parseInt( container, literal ) );
            }
            case INT64: {
                return JExpr.lit( parseLong( container, literal ) );
            }
            case DOUBLE: {
                return JExpr.lit( parseDouble( container, literal ) );
            }
            case FLOAT: {
                return JExpr.lit( parseFloat( container, literal ) );
            }
            case BYTE: {
                return JExpr.lit( parseByte( container, literal ) );
            }
            case STRING: {
                return JExpr.lit( parseString( container, literal ) );
            }
            case BOOLEAN: {
                return JExpr.lit( parseBool( container, literal ) );
            }
            default:
                throw new IllegalStateException( literal.getText() );
        }
    }
    private String parseString(JDefinedClass container, Literal_valueContext l) {
        if ( l.literal_substitude() != null ) {
            All_identifiersContext subsitude = l.literal_substitude().all_identifiers();
            IJExpression expression = parseSubstitution( container, subsitude );
            return Generator.expressionValue( expression ).toString();
        }
        String text = l.literal_without_substitude().STRING_LITERAL().getText();
        return text.substring( 1, text.length() - 1 );
    }
    private float parseFloat(JDefinedClass container, Literal_valueContext l) {
        if ( l.literal_substitude() != null ) {
            All_identifiersContext subsitude = l.literal_substitude().all_identifiers();
            return Float.parseFloat( Generator.expressionValue( parseSubstitution( container, subsitude ) ).toString() );
        }
        return Float.parseFloat( l.literal_without_substitude().FLOAT_LITERAL().getText() );
    }
    private double parseDouble(JDefinedClass container, Literal_valueContext l) {
        if ( l.literal_substitude() != null ) {
            All_identifiersContext subsitude = l.literal_substitude().all_identifiers();
            return Double.parseDouble( Generator.expressionValue( parseSubstitution( container, subsitude ) ).toString() );
        }
        return Double.parseDouble( l.literal_without_substitude().FLOAT_LITERAL().getText() );
    }
    private long parseLong(JDefinedClass container, Literal_valueContext l) {
        if ( l.literal_substitude() != null ) {
            All_identifiersContext subsitude = l.literal_substitude().all_identifiers();
            return Long.parseLong( Generator.expressionValue( parseSubstitution( container, subsitude ) ).toString() );
        }
        return Long.parseLong( l.literal_without_substitude().INTEGER_LITERAL().getText() );
    }
    private int parseInt(JDefinedClass container, Literal_valueContext l) {
        if ( l.literal_substitude() != null ) {
            All_identifiersContext subsitude = l.literal_substitude().all_identifiers();
            return Integer.parseInt( Generator.expressionValue( parseSubstitution( container, subsitude ) ).toString() );
        }
        return Integer.parseInt( l.literal_without_substitude().INTEGER_LITERAL().getText() );
    }
    private short parseShort(JDefinedClass container, Literal_valueContext l) {
        if ( l.literal_substitude() != null ) {
            All_identifiersContext subsitude = l.literal_substitude().all_identifiers();
            return Short.parseShort( Generator.expressionValue( parseSubstitution( container, subsitude ) ).toString() );
        }
        return Short.parseShort( l.literal_without_substitude().INTEGER_LITERAL().getText() );
    }
    private byte parseByte(JDefinedClass container, Literal_valueContext l) {
        if ( l.literal_substitude() != null ) {
            All_identifiersContext subsitude = l.literal_substitude().all_identifiers();
            return Byte.parseByte( Generator.expressionValue( parseSubstitution( container, subsitude ) ).toString() );
        }
        return Byte.parseByte( l.literal_without_substitude().INTEGER_LITERAL().getText() );
    }
    private boolean parseBool(JDefinedClass container, Literal_valueContext l) {
        String asText;
        if ( l.literal_substitude() != null ) {
            All_identifiersContext subsitude = l.literal_substitude().all_identifiers();
            JAtom init = (JAtom) parseSubstitution( container, subsitude );
            asText = init.what();
        }
        else {
            asText = l.literal_without_substitude().BOOL_LITERAL().getText();
        }

        if ( Boolean.TRUE.toString().equals( asText ) )
            return true;
        else if ( Boolean.FALSE.toString().equals( asText ) )
            return false;
        throw new IllegalArgumentException( "unable to parse boolean value " + asText );
    }
    private void addDenifition(JDefinedClass container, JDefinedClass member) {
        if ( !container.containsField( Generator.MESSAGES_FIELD ) ) {
            container.field( JMod.PUBLIC | JMod.FINAL | JMod.STATIC,
                             Class[].class,
                             Generator.MESSAGES_FIELD,
                             JExpr.newArray( codeModel.ref( Class.class ) ) );
        }
        for ( JFieldVar next : container.fields().values() ) {
            if ( next.name().equals( Generator.MESSAGES_FIELD ) ) {
                JArray fieldInit = (JArray) Generator.fieldInit( next );
                fieldInit.add( member.dotclass() );
                break;
            }
        }
    }
}
