package com.turbospaces.protoc.serialization;

import java.io.IOException;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.Unpacker;

import com.turbospaces.protoc.gen.GeneratedEnum;

@SuppressWarnings("rawtypes")
public class EnumTemplate<T extends GeneratedEnum> extends AbstractTemplate<T> {
    private T any;

    public EnumTemplate(Class<T> targetClass) {
        T[] constants = targetClass.getEnumConstants();
        if ( constants != null && constants.length > 0 ) {
            any = constants[0];
        }
    }
    @Override
    public void write(Packer pk, T v, boolean required) throws IOException {
        if ( v == null ) {
            if ( required ) {
                throw new MessageTypeException( "Attempted to write null" );
            }
            pk.writeNil();
            return;
        }
        pk.write( v.tag() );
    }
    @SuppressWarnings("unchecked")
    @Override
    public T read(Unpacker u, T to, boolean required) throws IOException {
        if ( !required && u.trySkipNil() ) {
            return null;
        }

        int tag = u.readInt();
        return (T) any.valueOf( tag );
    }
}
