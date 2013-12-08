package com.turbospaces.protoc.types;

import org.msgpack.template.Template;

import com.google.common.base.Objects;
import com.turbospaces.protoc.InitializingBean;
import com.turbospaces.protoc.ProtoGenerationContext;

public class ObjectMessageType implements MessageType, InitializingBean {
    Template<?> template;
    FieldType type;
    String ref;

    public ObjectMessageType(String ref) {
        this.ref = ref;
    }
    @Override
    public void init(ProtoGenerationContext ctx) {
        for ( FieldType t : FieldType.values() ) {
            if ( t.name().equals( ref.toUpperCase() ) ) {
                this.type = t;
                this.template = t.template();
                break;
            }
        }
        if ( this.type == null ) {
            // MESSAGE
            // ENUM
        }
    }
    @Override
    public String javaTypeAsString() {
        return getType().isComlex() ? ref : getType().javaTypeAsString();
    }
    @Override
    public Template<?> template() {
        return template;
    }
    public FieldType getType() {
        return type;
    }
    @Override
    public String toString() {
        return Objects.toStringHelper( this ).add( "type", type ).add( "ref", ref ).toString();
    }
    @Override
    public boolean isMap() {
        return false;
    }
    @Override
    public boolean isCollection() {
        return false;
    }
}
