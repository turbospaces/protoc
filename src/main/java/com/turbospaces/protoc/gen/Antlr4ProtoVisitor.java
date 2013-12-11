package com.turbospaces.protoc.gen;

import static com.turbospaces.protoc.gen.GenException.check;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.turbospaces.protoc.ConstantDescriptor;
import com.turbospaces.protoc.EnumDescriptor;
import com.turbospaces.protoc.MessageDescriptor;
import com.turbospaces.protoc.ProtoContainer;
import com.turbospaces.protoc.ProtoParserBaseVisitor;
import com.turbospaces.protoc.ProtoParserParser;
import com.turbospaces.protoc.ServiceDescriptor;
import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.ProtoParserParser.Alias_defContext;
import com.turbospaces.protoc.ProtoParserParser.CollectionContext;
import com.turbospaces.protoc.ProtoParserParser.Collection_mapContext;
import com.turbospaces.protoc.ProtoParserParser.Collection_map_valueContext;
import com.turbospaces.protoc.ProtoParserParser.Collection_typeContext;
import com.turbospaces.protoc.ProtoParserParser.Constant_defContext;
import com.turbospaces.protoc.ProtoParserParser.Enum_defContext;
import com.turbospaces.protoc.ProtoParserParser.Enum_member_tagContext;
import com.turbospaces.protoc.ProtoParserParser.Import_defContext;
import com.turbospaces.protoc.ProtoParserParser.MapContext;
import com.turbospaces.protoc.ProtoParserParser.Map_keyContext;
import com.turbospaces.protoc.ProtoParserParser.Map_valueContext;
import com.turbospaces.protoc.ProtoParserParser.Message_defContext;
import com.turbospaces.protoc.ProtoParserParser.Message_field_defContext;
import com.turbospaces.protoc.ProtoParserParser.Message_field_typeContext;
import com.turbospaces.protoc.ProtoParserParser.Package_defContext;
import com.turbospaces.protoc.ProtoParserParser.ProtoContext;
import com.turbospaces.protoc.ProtoParserParser.Service_defContext;
import com.turbospaces.protoc.ProtoParserParser.Service_method_defContext;
import com.turbospaces.protoc.ProtoParserParser.Service_method_excpContext;
import com.turbospaces.protoc.ServiceDescriptor.MethodDescriptor;
import com.turbospaces.protoc.types.CollectionMessageType;
import com.turbospaces.protoc.types.MapMessageType;
import com.turbospaces.protoc.types.MessageType;
import com.turbospaces.protoc.types.ObjectMessageType;

public class Antlr4ProtoVisitor extends ProtoParserBaseVisitor<Void> {
    public static final Set<String> KEYWORDS = Sets.newHashSet();
    private final Logger logger = LoggerFactory.getLogger( getClass() );
    private final ProtoContainer container;
    private final ProtoParserParser parser;

    static {
        KEYWORDS.add( "message" );
        KEYWORDS.add( "enum" );
        KEYWORDS.add( "service" );
        KEYWORDS.add( "alias" );
        KEYWORDS.add( "const" );
    }

    public Antlr4ProtoVisitor(ProtoParserParser parser, ProtoContainer fileContainer) {
        this.parser = parser;
        this.container = fileContainer;
    }
    @Override
    public Void visitImport_def(Import_defContext ctx) {
        String imp = ctx.import_value().STRING_LITERAL().getText().trim().replace( "\"", "" );
        logger.debug( "parsing import = {}...", imp );
        container.imports.add( imp );
        return super.visitImport_def( ctx );
    }
    @Override
    public Void visitConstant_def(Constant_defContext ctx) {
        String name = ctx.constant_name().getText().trim();
        logger.debug( "parsing constant = {}...", name );
        check( !KEYWORDS.contains( name.toLowerCase() ), "Const uses reserved keyword" );
        String type = ctx.constant_type().TYPE_LITERAL().getText();
        String value = ctx.literal_value().getText();
        ConstantDescriptor c = new ConstantDescriptor( name, type, value );
        container.constants.put( c.getName(), c );
        return super.visitConstant_def( ctx );
    }
    @Override
    public Void visitPackage_def(Package_defContext ctx) {
        container.pkg = ctx.package_name().getText().trim();
        return super.visitPackage_def( ctx );
    }
    @Override
    public Void visitMessage_def(Message_defContext ctx) {
        String name = ctx.message_name().getText();
        logger.debug( "parsing message = {}...", name );
        check( !KEYWORDS.contains( name.toLowerCase() ), "Message uses reserved keyword" );
        String parent = null;
        ProtoContext pkgCtx = (ProtoContext) ctx.parent;
        String pkg = pkgCtx.package_def().package_name().getText().trim();
        if ( ctx.message_parent() != null && ctx.message_parent().message_parent_message() != null ) {
            parent = ctx.message_parent().message_parent_message().getText();
        }
        MessageDescriptor m = new MessageDescriptor( name, parent, pkg );

        check( !container.messages.containsKey( name ), "message with name=% already defined", name );
        check( !container.enums.containsKey( name ), "enum with name=% already defined", name );
        check( !container.aliases.containsKey( name ), "alias with name=%s already defined", name );

        container.messages.put( m.getName(), m );
        return super.visitMessage_def( ctx );
    }
    @Override
    public Void visitMessage_field_def(Message_field_defContext ctx) {
        int tag = Integer.parseInt( ctx.message_field_tag().getText() );
        String name = ctx.message_field_name().getText();
        check( !KEYWORDS.contains( name.toLowerCase() ), "Field uses reserved keyword" );

        Message_field_typeContext mft = ctx.message_field_type();
        Collection_map_valueContext cmp = mft.collection_map_value();
        MessageType type = parseGenericType( cmp );

        FieldDescriptor desc = new FieldDescriptor( tag, name, type );

        Message_defContext mCtx = ( (Message_defContext) ctx.parent.getRuleContext() );
        container.messages.get( mCtx.message_name().getText() ).addField( desc.getTag(), desc );

        logger.debug( ctx.toStringTree( parser ) );
        return super.visitMessage_field_def( ctx );
    }
    @Override
    public Void visitEnum_def(Enum_defContext ctx) {
        String name = ctx.enum_name().getText();
        logger.debug( "parsing enum = {}...", name );
        check( !KEYWORDS.contains( name.toLowerCase() ), "Enum uses reserved keyword" );
        EnumDescriptor e = new EnumDescriptor( name );

        check( !container.enums.containsKey( name ), "enum with name=% already defined", name );
        check( !container.messages.containsKey( name ), "message with name=% already defined", name );
        check( !container.aliases.containsKey( name ), "alias with name=%s already defined", name );

        container.enums.put( e.getName(), e );
        return super.visitEnum_def( ctx );
    }
    @Override
    public Void visitEnum_member_tag(Enum_member_tagContext ctx) {
        Enum_defContext eCtx = (Enum_defContext) ctx.parent.parent;
        EnumDescriptor protoEnum = container.enums.get( eCtx.enum_name().getText() );
        protoEnum.addMember( Integer.parseInt( ctx.enum_tag().TAG().getText() ), ctx
                .enum_member()
                .IDENTIFIER()
                .getText() );
        return super.visitEnum_member_tag( ctx );
    }
    @Override
    public Void visitService_def(Service_defContext ctx) {
        String name = ctx.service_name().getText();
        logger.debug( "parsing service = {}...", name );
        check( !KEYWORDS.contains( name.toLowerCase() ), "Service uses reserved keyword" );

        ServiceDescriptor s = new ServiceDescriptor( name );
        container.services.put( s.getName(), s );
        if ( ctx.service_parent() != null && ctx.service_parent().service_parent_message() != null ) {
            s.setParent( ctx.service_parent().service_parent_message().getText() );
        }

        return super.visitService_def( ctx );
    }
    @Override
    public Void visitService_method_def(Service_method_defContext ctx) {
        String name = ctx.service_method_name().getText();
        logger.debug( "parsing service method = {}...", name );
        check( !KEYWORDS.contains( name.toLowerCase() ), "Service method uses reserved keyword" );

        MethodDescriptor m = new MethodDescriptor( name );
        if ( ctx.service_method_req().collection_map_value() != null ) {
            m.setRequestType( parseGenericType( ctx.service_method_req().collection_map_value() ) );
        }
        m.setResponseType( parseGenericType( ctx.service_method_resp().collection_map_value() ) );

        List<Service_method_excpContext> excpCtxs = ctx.service_method_throws().service_method_excp();
        for ( Service_method_excpContext excpCtx : excpCtxs ) {
            m.addException( excpCtx.IDENTIFIER().getText() );
        }

        Service_defContext sCtx = (Service_defContext) ctx.parent.getRuleContext();
        container.services.get( sCtx.service_name().getText() ).addMethod( m.getName(), m );
        return super.visitService_method_def( ctx );
    }
    @Override
    public Void visitAlias_def(Alias_defContext ctx) {
        String type = ctx.alias_source().getText();
        String alias = ctx.alias_destination().getText();
        check( !KEYWORDS.contains( alias.toLowerCase() ), "Alias uses reserved keyword" );

        container.aliases.put( type, alias );
        return super.visitAlias_def( ctx );
    }
    /**
     * PARSE IDENTIFIER(i.e. message reference) / TYPE (i.e. primitive type) OR COLLECTIONS (SET[K], LIST[K]) or even
     * MAP[K,V] where key and map could be again primitives or type reference.
     * 
     * Nested type nesting inside maps/sets/lists is not supported atm.
     */
    private MessageType parseGenericType(Collection_map_valueContext cmp) {
        if ( cmp.TYPE_LITERAL() != null ) {
            return new ObjectMessageType( cmp.TYPE_LITERAL().getText() );
        }
        else if ( cmp.IDENTIFIER() != null ) {
            return new ObjectMessageType( cmp.IDENTIFIER().getText() );
        }
        else {
            Collection_mapContext ctx = cmp.collection_map();
            MapContext mapCtx = ctx.map();
            CollectionContext collectionCtx = ctx.collection();
            if ( collectionCtx != null ) {
                Collection_typeContext typeContext = collectionCtx.collection_type();
                boolean isSet = "set".equalsIgnoreCase( collectionCtx.COLLECTION_LITERAL().getText() );

                if ( typeContext.TYPE_LITERAL() != null ) {
                    return new CollectionMessageType( typeContext.TYPE_LITERAL().getText(), isSet );
                }
                else {
                    return new CollectionMessageType( typeContext.IDENTIFIER().getText(), isSet );
                }
            }
            else {
                Map_keyContext map_key = mapCtx.map_key();
                Map_valueContext map_value = mapCtx.map_value();

                String key, value;

                // key
                if ( map_key.IDENTIFIER() != null ) {
                    key = map_key.IDENTIFIER().getText();
                }
                else {
                    key = map_key.TYPE_LITERAL().getText();
                }

                // value
                if ( map_value.TYPE_LITERAL() != null ) {
                    value = map_value.TYPE_LITERAL().getText();
                }
                else {
                    value = map_value.IDENTIFIER().getText();
                }
                return new MapMessageType( key, value );
            }
        }
    }
}
