package com.turbospaces.protoc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

public final class ServiceDescriptor {
    String qualifier, parentQualifier;
    Map<String, MethodDescriptor> methods = new HashMap<String, MethodDescriptor>();

    public static final class MethodDescriptor {
        public String qualifier;
        public MessageType request, response;
        public List<String> exceptions = new LinkedList<String>();

        @Override
        public String toString() {
            return Objects
                    .toStringHelper( this )
                    .add( "qualifier", qualifier )
                    .add( "request", request )
                    .add( "response", response )
                    .add( "exceptions", exceptions )
                    .toString();
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper( this ).add( "qualifier", qualifier ).add( "parent", parentQualifier ).add( "methods", methods ).toString();
    }
}
