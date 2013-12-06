package com.turbospaces.protoc;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Objects;

public final class MessageDescriptor {
    String qualifier, parentQualifier;
    Map<Integer, FieldDescriptor> fields = new HashMap<Integer, FieldDescriptor>();

    public FieldDescriptor getFieldDesc(int tag) {
        return fields.get( tag );
    }

    public static final class FieldDescriptor {
        int tag;
        String qualifier;
        MessageType type;

        @Override
        public String toString() {
            return Objects.toStringHelper( this ).add( "tag", tag ).add( "qualifier", qualifier ).add( "type", type ).toString();
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper( this ).add( "qualifier", qualifier ).add( "parent", parentQualifier ).add( "fields", fields ).toString();
    }
}
