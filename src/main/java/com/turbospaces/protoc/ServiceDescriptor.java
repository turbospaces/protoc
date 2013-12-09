package com.turbospaces.protoc;

import static com.turbospaces.protoc.gen.GenException.check;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;
import com.turbospaces.protoc.ProtoContainer.NamedDescriptor;
import com.turbospaces.protoc.types.MessageType;

public final class ServiceDescriptor extends NamedDescriptor {
    private String parent;
    private Map<String, MethodDescriptor> methods = new HashMap<String, MethodDescriptor>();

    public ServiceDescriptor(String name) {
        this.name = name;
    }
    public void setParent(String parent) {
        this.parent = parent;
    }
    public void addMethod(String name, MethodDescriptor m) {
        check( !methods.containsKey( name ), "service method with name=%s already defined", name );
        methods.put( name, m );
    }
    public Map<String, MethodDescriptor> getMethods() {
        return Collections.unmodifiableMap( methods );
    }
    public String getParent() {
        return parent;
    }

    public static final class MethodDescriptor extends NamedDescriptor {
        public MessageType request, response;
        public List<String> exceptions = new LinkedList<String>();

        public MethodDescriptor(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return Objects
                    .toStringHelper( this )
                    .add( "name", name )
                    .add( "request", request )
                    .add( "response", response )
                    .add( "exceptions", exceptions )
                    .toString();
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper( this ).add( "name", name ).add( "parent", parent ).add( "methods", methods ).toString();
    }
}
