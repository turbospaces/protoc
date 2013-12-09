package com.turbospaces.protoc.serialization;

import java.io.IOException;
import java.util.Collection;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.template.Template;
import org.msgpack.unpacker.BufferUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.gen.GeneratedMessage;

public class Streams {
    private static final Logger LOGGER = LoggerFactory.getLogger( Streams.class );
    private static MessagePack msgpack = new MessagePack();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static byte[] out(GeneratedMessage obj) throws IOException {
        BufferPacker packer = msgpack.createBufferPacker();
        Collection<FieldDescriptor> descriptors = obj.getAllDescriptors();
        for ( FieldDescriptor f : descriptors ) {
            Object v = obj.getFieldValue( f.getTag() );
            if ( v != null ) {
                LOGGER.debug( "[write] field={} value={}", f.getName(), v );
                Template template = f.getType().template();
                template.write( packer, v );
            }
            else
                packer.writeNil();
        }
        return packer.toByteArray();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static GeneratedMessage in(byte[] arr, GeneratedMessage obj) throws IOException {
        BufferUnpacker unpacker = msgpack.createBufferUnpacker( arr );
        Collection<FieldDescriptor> descriptors = obj.getAllDescriptors();
        for ( FieldDescriptor f : descriptors ) {
            Template template = f.getType().template();
            boolean nil = unpacker.trySkipNil();
            if ( !nil ) {
                Object value = template.read( unpacker, null );
                LOGGER.debug( "[read] field={}, value={}", f.getName(), value );
                obj.setFieldValue( f.getTag(), value );
            }
        }
        return obj;
    }
}
