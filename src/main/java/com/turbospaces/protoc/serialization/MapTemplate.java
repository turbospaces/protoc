package com.turbospaces.protoc.serialization;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

import com.turbospaces.protoc.gen.GeneratedMessage;

public class MapTemplate<K, V> extends AbstractTemplate<Map<K, V>> {
    private Template<K> keyTemplate;
    private Template<V> valueTemplate;

    public MapTemplate(Template<K> keyTemplate, Template<V> valueTemplate) {
        this.keyTemplate = keyTemplate;
        this.valueTemplate = valueTemplate;
    }

    public void write(Packer pk, Map<K, V> target, boolean required) throws IOException {
        checkNotNull( target );
        Map<K, V> map = (Map<K, V>) target;
        pk.writeMapBegin( map.size() );
        for ( Map.Entry<K, V> pair : map.entrySet() ) {
            if ( keyTemplate instanceof ObjectTemplate ) {
                ObjectTemplate ot = (ObjectTemplate) keyTemplate;
                Streams.write( pk, ot, (GeneratedMessage) pair.getKey() );
            }
            else {
                keyTemplate.write( pk, pair.getKey() );
            }

            if ( valueTemplate instanceof ObjectTemplate ) {
                ObjectTemplate ot = (ObjectTemplate) valueTemplate;
                Streams.write( pk, ot, (GeneratedMessage) pair.getValue() );
            }
            else {
                valueTemplate.write( pk, pair.getValue() );
            }
        }
        pk.writeMapEnd();
    }

    @SuppressWarnings("unchecked")
    public Map<K, V> read(Unpacker u, Map<K, V> to, boolean required) throws IOException {
        int n = u.readMapBegin();
        Map<K, V> map;
        if ( to != null ) {
            map = (Map<K, V>) to;
            map.clear();
        }
        else {
            map = new HashMap<K, V>( n );
        }
        for ( int i = 0; i < n; i++ ) {
            K key;
            V value;
            if ( keyTemplate instanceof ObjectTemplate ) {
                ObjectTemplate ot = (ObjectTemplate) keyTemplate;
                key = ( (K) Streams.read( u, ot ) );
            }
            else {
                key = keyTemplate.read( u, null );
            }

            if ( valueTemplate instanceof ObjectTemplate ) {
                ObjectTemplate ot = (ObjectTemplate) valueTemplate;
                value = ( (V) Streams.read( u, ot ) );
            }
            else {
                value = valueTemplate.read( u, null );
            }
            map.put( key, value );
        }
        u.readMapEnd();
        return map;
    }
}
