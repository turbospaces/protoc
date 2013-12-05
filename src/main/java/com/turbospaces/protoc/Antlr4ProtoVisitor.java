package com.turbospaces.protoc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.turbospaces.protoc.MessageType.CollectionType;
import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.ProtoParserParser.Alias_defContext;
import com.turbospaces.protoc.ProtoParserParser.CollectionContext;
import com.turbospaces.protoc.ProtoParserParser.Collection_mapContext;
import com.turbospaces.protoc.ProtoParserParser.Collection_map_valueContext;
import com.turbospaces.protoc.ProtoParserParser.Collection_typeContext;
import com.turbospaces.protoc.ProtoParserParser.Enum_defContext;
import com.turbospaces.protoc.ProtoParserParser.Enum_member_tagContext;
import com.turbospaces.protoc.ProtoParserParser.MapContext;
import com.turbospaces.protoc.ProtoParserParser.Map_keyContext;
import com.turbospaces.protoc.ProtoParserParser.Map_valueContext;
import com.turbospaces.protoc.ProtoParserParser.Message_defContext;
import com.turbospaces.protoc.ProtoParserParser.Message_field_defContext;
import com.turbospaces.protoc.ProtoParserParser.Message_field_typeContext;
import com.turbospaces.protoc.ProtoParserParser.Package_defContext;
import com.turbospaces.protoc.ProtoParserParser.Service_defContext;
import com.turbospaces.protoc.ProtoParserParser.Service_method_defContext;
import com.turbospaces.protoc.ProtoParserParser.Service_method_excpContext;
import com.turbospaces.protoc.ServiceDescriptor.MethodDescriptor;

public class Antlr4ProtoVisitor extends ProtoParserBaseVisitor<Void> {
    private final Logger logger = LoggerFactory.getLogger( getClass() );
    private final ProtoContainer container;
    private final ProtoParserParser parser;

    public Antlr4ProtoVisitor(ProtoParserParser parser, ProtoContainer fileContainer) {
        this.parser = parser;
        this.container = fileContainer;
    }
    @Override
    public Void visitPackage_def(Package_defContext ctx) {
        container.pkg = ctx.package_name().getText();
        return super.visitPackage_def( ctx );
    }
    @Override
    public Void visitMessage_def(Message_defContext ctx) {
        MessageDescriptor m = new MessageDescriptor();
        m.qualifier = ctx.message_name().getText();
        container.messages.put( m.qualifier, m );
        logger.debug( "parsing message = {}...", m.qualifier );
        return super.visitMessage_def( ctx );
    }
    @Override
    public Void visitMessage_field_def(Message_field_defContext ctx) {
        FieldDescriptor desc = new FieldDescriptor();
        desc.tag = Integer.parseInt( ctx.message_field_tag().getText() );
        desc.qualifier = ctx.message_field_name().getText();

        Message_field_typeContext mft = ctx.message_field_type();
        Collection_map_valueContext cmp = mft.collection_map_value();
        desc.type = parseGenericType( cmp );

        Message_defContext mCtx = ( (Message_defContext) ctx.parent.getRuleContext() );
        container.messages.get( mCtx.message_name().getText() ).fields.put( desc.tag, desc );

        logger.debug( ctx.toStringTree( parser ) );
        return super.visitMessage_field_def( ctx );
    }
    @Override
    public Void visitEnum_def(Enum_defContext ctx) {
        EnumDescriptor e = new EnumDescriptor();
        e.qualifier = ctx.enum_name().getText();
        logger.debug( "parsing enum = {}...", e.qualifier );
        container.enums.put( e.qualifier, e );
        return super.visitEnum_def( ctx );
    }
    @Override
    public Void visitEnum_member_tag(Enum_member_tagContext ctx) {
        Enum_defContext eCtx = (Enum_defContext) ctx.parent.parent;
        EnumDescriptor protoEnum = container.enums.get( eCtx.enum_name().getText() );
        protoEnum.values.put( Integer.parseInt( ctx.enum_tag().TAG().getText() ), ctx.enum_member().IDENTIFIER().getText() );
        return super.visitEnum_member_tag( ctx );
    }
    @Override
    public Void visitService_def(Service_defContext ctx) {
        ServiceDescriptor s = new ServiceDescriptor();
        s.qualifier = ctx.service_name().getText();
        container.services.put( s.qualifier, s );
        logger.debug( "parsing service = {}...", s.qualifier );
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
     * 
     * TODO: apply recursive later if needed
     */
    private MessageType parseGenericType(Collection_map_valueContext cmp) {
        MessageType type = new MessageType();
        if ( cmp.TYPE_LITERAL() != null ) {
            type.setTypeReference( cmp.TYPE_LITERAL().getText(), CollectionType.NONE );
        }
        else if ( cmp.IDENTIFIER() != null ) {
            type.setTypeReference( cmp.IDENTIFIER().getText(), CollectionType.NONE );
        }
        else {
            Collection_mapContext ctx = cmp.collection_map();
            MapContext mapCtx = ctx.map();
            CollectionContext collectionCtx = ctx.collection();
            if ( collectionCtx != null ) {
                Collection_typeContext typeContext = collectionCtx.collection_type();
                CollectionType collectionType = CollectionType.valueOf( collectionCtx.COLLECTION_LITERAL().getText().toUpperCase() );

                if ( typeContext.TYPE_LITERAL() != null ) {
                    type.setTypeReference( typeContext.TYPE_LITERAL().getText(), collectionType );
                }
                else {
                    type.setTypeReference( typeContext.IDENTIFIER().getText(), collectionType );
                }
            }
            if ( mapCtx != null ) {
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
                type.setTypeReference( key, value );
            }
        }
        return type;
    }
}
