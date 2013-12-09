package com.turbospaces.protoc.types;

import org.msgpack.template.Template;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.turbospaces.protoc.gen.GeneratedEnum;
import com.turbospaces.protoc.gen.ProtoGenerationContext;
import com.turbospaces.protoc.serialization.EnumTemplate;

public class ObjectMessageType implements MessageType {
    Template<?> template;
    FieldType type;
    String ref;

    public ObjectMessageType(String ref) {
        this.ref = ref;
    }
    public ObjectMessageType(FieldType type, String ref) {
        this.type = type;
        this.ref = ref;
        try {
            setTemplate();
        }
        catch ( ClassNotFoundException e ) {
            Throwables.propagate( e );
        }
    }
    @Override
    public void init(ProtoGenerationContext ctx) throws Exception {
        for ( FieldType t : FieldType.values() ) {
            if ( t.name().equals( ref.toUpperCase() ) ) {
                this.type = t;
                break;
            }
        }
        if ( this.type == null ) {
            String q = ctx.qualifiedMessageReference( getTypeReference() );
            if ( q != null ) {
                this.type = FieldType.MESSAGE;
                ref = q;
            }
            q = ctx.qualifiedEnumReference( getTypeReference() );
            if ( q != null ) {
                this.type = FieldType.ENUM;
                ref = q;
            }
        }
        assert ( this.type != null );
        setTemplate();
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
    public String getTypeReference() {
        return ref;
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void setTemplate() throws ClassNotFoundException {
        if ( getType().isComlex() ) {
            if ( getType() == FieldType.MESSAGE ) {}
            else if ( getType() == FieldType.ENUM ) {
                Class<? extends GeneratedEnum> enumClass = (Class<? extends GeneratedEnum>) Class.forName( getTypeReference() );
                template = new EnumTemplate( enumClass );
            }
        }
        else {
            template = getType().template();
        }
    }
}
