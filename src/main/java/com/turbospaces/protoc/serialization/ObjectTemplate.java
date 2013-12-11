package com.turbospaces.protoc.serialization;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Collection;

import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.BufferUnpacker;
import org.msgpack.unpacker.Unpacker;

import com.google.common.base.Throwables;
import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.gen.GeneratedMessage;

public class ObjectTemplate extends AbstractTemplate<GeneratedMessage> {
    private Class<? extends GeneratedMessage> owner;

    public ObjectTemplate(Class<? extends GeneratedMessage> owner) {
        this.owner = owner;
    }
    public Class<? extends GeneratedMessage> getOwner() {
        return owner;
    }
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
                    ObjectTemplate ot = (ObjectTemplate) template;
                    Streams.write( pk, ot, (GeneratedMessage) val );
                }
                else {
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
        GeneratedMessage actual = to;
        if ( to == null ) {
            try {
                actual = owner.newInstance();
            }
            catch ( Exception e ) {
                Throwables.propagate( e );
            }
        }

        BufferUnpacker bu = (BufferUnpacker) u;
        int size = bu.getBufferSize();

        while ( bu.getReadByteCount() < size ) {
            int tag = bu.readInt();
            FieldDescriptor f = actual.getFieldDescriptor( tag );
            Template template = f.getType().template();
            boolean nil = bu.trySkipNil();
            if ( !nil ) {
                Object value = null;
                if ( template instanceof ObjectTemplate ) {
                    ObjectTemplate ot = (ObjectTemplate) template;
                    value = Streams.read( bu, ot );
                }
                else {
                    value = template.read( bu, null, true );
                }
                actual.setFieldValue( f.getTag(), value );
            }
        }

        return actual;
    }
}
