package com.turbospaces.protoc.serialization;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Collection;

import org.msgpack.packer.BufferPacker;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.BufferUnpacker;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.gen.GeneratedMessage;

public class ObjectTemplate extends AbstractTemplate<GeneratedMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger( ObjectTemplate.class );

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void write(Packer pk, GeneratedMessage v, boolean required) throws IOException {
        checkNotNull( v );

        Collection<FieldDescriptor> descriptors = v.getAllDescriptors();
        for ( FieldDescriptor f : descriptors ) {
            pk.write( f.getTag() );
            Object val = v.getFieldValue( f.getTag() );
            if ( val != null ) {
                Template template = f.getType().template();
                if ( template instanceof ObjectTemplate ) {
                    LOGGER.debug(
                            "writing object={} complex field[tag={}, name={}] = {} ...",
                            v.getClass().getSimpleName(),
                            f.getTag(),
                            f.getName(),
                            val );

                    BufferPacker mbp = Streams.msgpack.createBufferPacker();
                    template.write( mbp, val );
                    byte[] mbytes = mbp.toByteArray();
                    pk.write( mbytes );
                }
                else {
                    LOGGER.debug(
                            "writing object={} primitive field[tag={}, name={}] = {}...",
                            v.getClass().getSimpleName(),
                            f.getTag(),
                            f.getName(),
                            val );
                    template.write( pk, val );
                }
            }
            else
                pk.writeNil();
        }
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public GeneratedMessage read(Unpacker u, GeneratedMessage to, boolean required) throws IOException {
        checkNotNull( to );
        BufferUnpacker bu = (BufferUnpacker) u;
        int size = bu.getBufferSize();

        while ( bu.getReadByteCount() < size ) {
            int tag = bu.readInt();
            FieldDescriptor f = to.getFieldDescriptor( tag );
            Template template = f.getType().template();
            boolean nil = bu.trySkipNil();
            if ( !nil ) {
                Object value = null;
                if ( template instanceof ObjectTemplate ) {
                    byte[] arr = u.readByteArray();
                    BufferUnpacker mbu = Streams.msgpack.createBufferUnpacker( arr );
                    Class<? extends GeneratedMessage> genClass = f.getGenClass();
                    try {
                        value = genClass.newInstance();
                    }
                    catch ( InstantiationException e ) {
                        Throwables.propagate( e );
                    }
                    catch ( IllegalAccessException e ) {
                        Throwables.propagate( e );
                    }
                    mbu.read( value, template );
                }
                else {
                    LOGGER.debug( "read object={} field[tag={},name={}]", to.getClass().getSimpleName(), f.getTag(), f.getName() );
                    value = template.read( bu, null, true );
                }
                to.setFieldValue( f.getTag(), value );
            }
        }

        return to;
    }
}
