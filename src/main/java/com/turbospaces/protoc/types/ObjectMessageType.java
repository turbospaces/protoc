package com.turbospaces.protoc.types;

import static com.google.common.base.Suppliers.memoize;

import org.msgpack.template.Template;

import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.turbospaces.protoc.gen.GeneratedEnum;
import com.turbospaces.protoc.gen.ProtoGenerationContext;
import com.turbospaces.protoc.serialization.EnumTemplate;
import com.turbospaces.protoc.serialization.ObjectTemplate;

public class ObjectMessageType implements MessageType {
    Supplier<Template<?>> template;
    FieldType type;
    String ref;

    public ObjectMessageType(String ref) {
        this.ref = ref;
    }
    public ObjectMessageType(FieldType type, String ref) {
        this.type = type;
        this.ref = ref;
        initTemplate();
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
        initTemplate();
    }
    @Override
    public String javaTypeAsString() {
        return getType().isComlex() ? ref : getType().javaTypeAsString();
    }
    @Override
    public Template<?> template() {
        return template.get();
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
    protected void initTemplate() {
        template = memoize( new Supplier<Template<?>>() {
            @Override
            public Template<?> get() {
                for ( ;; )
                    try {
                        Template<?> t = null;
                        if ( getType().isComlex() ) {
                            if ( getType() == FieldType.MESSAGE ) {
                                t = new ObjectTemplate();
                            }
                            else if ( getType() == FieldType.ENUM ) {
                                t = new EnumTemplate( (Class<? extends GeneratedEnum>) Class.forName( getTypeReference() ) );
                            }
                        }
                        else {
                            t = getType().template();
                        }
                        return t;
                    }
                    catch ( ClassNotFoundException e ) {
                        Throwables.propagate( e );
                    }
            }
        } );
    }
}
