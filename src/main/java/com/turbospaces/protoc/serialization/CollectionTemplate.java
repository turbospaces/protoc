package com.turbospaces.protoc.serialization;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

import com.turbospaces.protoc.gen.GeneratedMessage;

public class CollectionTemplate<E> extends AbstractTemplate<Collection<E>> {
    private Template<E> elementTemplate;
    private boolean isSet;

    public CollectionTemplate(Template<E> elementTemplate, boolean isSet) {
        this.elementTemplate = elementTemplate;
        this.isSet = isSet;
    }

    @Override
    public void write(Packer pk, Collection<E> target, boolean required) throws IOException {
        checkNotNull( target );

        pk.writeArrayBegin( target.size() );
        for ( E e : target ) {
            if ( elementTemplate instanceof ObjectTemplate ) {
                ObjectTemplate ot = (ObjectTemplate) elementTemplate;
                Streams.write( pk, ot, (GeneratedMessage) e );
            }
            else {
                elementTemplate.write( pk, e );
            }
        }
        pk.writeArrayEnd();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<E> read(Unpacker u, Collection<E> to, boolean required) throws IOException {
        int n = u.readArrayBegin();
        if ( to == null ) {
            to = isSet ? new HashSet<E>( n ) : new ArrayList<E>( n );
        }
        else {
            to.clear();
        }
        for ( int i = 0; i < n; i++ ) {
            E e = null;
            if ( elementTemplate instanceof ObjectTemplate ) {
                ObjectTemplate ot = (ObjectTemplate) elementTemplate;
                e = (E) Streams.read( u, ot );
            }
            else {
                e = elementTemplate.read( u, null );
            }
            to.add( e );
        }
        u.readArrayEnd();
        return to;
    }
}
