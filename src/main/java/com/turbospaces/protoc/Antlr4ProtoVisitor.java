package com.turbospaces.protoc;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.turbospaces.protoc.types.MessageType;
import com.turbospaces.protoc.types.ObjectMessageType;

public class Antlr4ProtoVisitor extends ProtoParserBaseVisitor<Void> {
    private final Logger logger = LoggerFactory.getLogger( getClass() );
    private final ProtoContainer container;
    private final ProtoParserParser parser;

    public Antlr4ProtoVisitor(ProtoParserParser parser, ProtoContainer fileContainer) {
        this.parser = parser;
        this.container = fileContainer;
    }
    @Override
    public Void visitImport_def(Import_defContext ctx) {
        String imp = ctx.import_value().IMPORT().getText().trim();
        logger.debug( "parsing import = {}...", imp );
        container.imports.add( imp );
        return super.visitImport_def( ctx );
    }
    @Override
    public Void visitConstant_def(Constant_defContext ctx) {
        String name = ctx.constant_name().getText().trim();
        logger.debug( "parsing constant = {}...", name );
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
        String parent = null;
        ProtoContext pkgCtx = (ProtoContext) ctx.parent;
        String pkg = pkgCtx.package_def().package_name().getText().trim();
        if ( ctx.message_parent() != null && ctx.message_parent().message_parent_message() != null ) {
            parent = ctx.message_parent().message_parent_message().getText();
        }
        MessageDescriptor m = new MessageDescriptor( name, parent, pkg );

        checkArgument( !container.messages.containsKey( name ), "message with name=% already defined", name );
        checkArgument( !container.enums.containsKey( name ), "enum with name=% already defined", name );
        checkArgument( !container.aliases.containsKey( name ), "alias with name=%s already defined", name );

        container.messages.put( m.name, m );
        return super.visitMessage_def( ctx );
    }
    @Override
    public Void visitMessage_field_def(Message_field_defContext ctx) {
        int tag = Integer.parseInt( ctx.message_field_tag().getText() );
        String name = ctx.message_field_name().getText();

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
        EnumDescriptor e = new EnumDescriptor( name );

        checkArgument( !container.enums.containsKey( name ), "enum with name=% already defined", name );
        checkArgument( !container.messages.containsKey( name ), "message with name=% already defined", name );
        checkArgument( !container.aliases.containsKey( name ), "alias with name=%s already defined", name );

        container.enums.put( e.name, e );
        return super.visitEnum_def( ctx );
    }
    @Override
    public Void visitEnum_member_tag(Enum_member_tagContext ctx) {
        Enum_defContext eCtx = (Enum_defContext) ctx.parent.parent;
        EnumDescriptor protoEnum = container.enums.get( eCtx.enum_name().getText() );
        protoEnum.members.put( Integer.parseInt( ctx.enum_tag().TAG().getText() ), ctx
                .enum_member()
                .IDENTIFIER()
                .getText() );
        return super.visitEnum_member_tag( ctx );
    }
    @Override
    public Void visitService_def(Service_defContext ctx) {
        ServiceDescriptor s = new ServiceDescriptor();
        s.name = ctx.service_name().getText();
        container.services.put( s.name, s );
        if ( ctx.service_parent() != null && ctx.service_parent().service_parent_message() != null ) {
            s.parent = ctx.service_parent().service_parent_message().getText();
        }
        logger.debug( "parsing service = {}...", s.name );
        return super.visitService_def( ctx );
    }
    @Override
    public Void visitService_method_def(Service_method_defContext ctx) {
        MethodDescriptor m = new MethodDescriptor();
        m.qualifier = ctx.service_method_name().getText();
        if ( ctx.service_method_req().collection_map_value() != null )
            m.request = parseGenericType( ctx.service_method_req().collection_map_value() );
        m.response = parseGenericType( ctx.service_method_resp().collection_map_value() );

        List<Service_method_excpContext> excpCtxs = ctx.service_method_throws().service_method_excp();
        for ( Service_method_excpContext excpCtx : excpCtxs ) {
            m.exceptions.add( excpCtx.IDENTIFIER().getText() );
        }

        Service_defContext sCtx = (Service_defContext) ctx.parent.getRuleContext();
        container.services.get( sCtx.service_name().getText() ).methods.put( m.qualifier, m );

        logger.debug( ctx.toStringTree( parser ) );
        return super.visitService_method_def( ctx );
    }
    @Override
    public Void visitAlias_def(Alias_defContext ctx) {
        container.aliases.put( ctx.alias_source().getText(), ctx.alias_destination().getText() );
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
