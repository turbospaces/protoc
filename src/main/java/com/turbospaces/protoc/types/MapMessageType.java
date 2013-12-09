package com.turbospaces.protoc.types;

import org.msgpack.template.Template;
import org.msgpack.template.Templates;

import com.google.common.base.Throwables;
import com.turbospaces.protoc.InitializingBean;
import com.turbospaces.protoc.gen.GeneratedEnum;
import com.turbospaces.protoc.gen.ProtoGenerationContext;
import com.turbospaces.protoc.serialization.EnumTemplate;

public class MapMessageType implements MessageType, InitializingBean {
    String kref, vref;
    FieldType ktype, vtype;
    Template<?> template;

    public MapMessageType(String keyRef, String valueRef) {
        this.kref = keyRef;
        this.vref = valueRef;
    }
    public MapMessageType(FieldType ktype, String kref, FieldType vtype, String vref) {
        this.kref = kref;
        this.vref = vref;
        this.ktype = ktype;
        this.vtype = vtype;
        try {
            setTemplate();
        }
        catch ( ClassNotFoundException e ) {
            Throwables.propagate( e );
        }
    }
    public String getKeyTypeReference() {
        return kref;
    }
    public String getValueTypeReference() {
        return vref;
    }
    public FieldType getKeyType() {
        return ktype;
    }
    public FieldType getValueType() {
        return vtype;
    }
    @Override
    public void init(ProtoGenerationContext ctx) throws Exception {
        for ( FieldType t : FieldType.values() ) {
            if ( t.name().equals( kref.toUpperCase() ) ) {
                this.ktype = t;
            }
            else if ( t.name().equals( vref.toUpperCase() ) ) {
                this.vtype = t;
            }
        }

        {
            if ( this.ktype == null ) {
                String kq = ctx.qualifiedMessageReference( getKeyTypeReference() );
                if ( kq != null ) {
                    this.ktype = FieldType.MESSAGE;
                    this.kref = kq;
                }
                kq = ctx.qualifiedEnumReference( getKeyTypeReference() );
                if ( kq != null ) {
                    this.ktype = FieldType.ENUM;
                    this.kref = kq;
                }
            }
            assert ( this.ktype != null );
        }
        {
            if ( this.vtype == null ) {
                String vq = ctx.qualifiedMessageReference( getValueTypeReference() );
                if ( vq != null ) {
                    this.vtype = FieldType.MESSAGE;
                    this.vref = vq;
                }
                vq = ctx.qualifiedEnumReference( getValueTypeReference() );
                if ( vq != null ) {
                    this.vtype = FieldType.ENUM;
                    this.vref = vq;
                }
            }
            assert ( this.vtype != null );
        }
        setTemplate();
    }
    @Override
    public String javaTypeAsString() {
        String k = ktype.isComlex() ? kref : ktype.javaTypeAsString();
        String v = vtype.isComlex() ? vref : vtype.javaTypeAsString();
        return "Map<" + k + "," + v + ">";
    }
    @Override
    public Template<?> template() {
        return template;
    }
    @Override
    public boolean isMap() {
        return true;
    }
    @Override
    public boolean isCollection() {
        return false;
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void setTemplate() throws ClassNotFoundException {
        Template ktemplate = null, vtemplate = null;
        {
            if ( getKeyType().isComlex() ) {
                if ( getKeyType() == FieldType.MESSAGE ) {}
                else if ( getKeyType() == FieldType.ENUM ) {
                    Class<? extends GeneratedEnum> enumClass = (Class<? extends GeneratedEnum>) Class.forName( getKeyTypeReference() );
                    ktemplate = new EnumTemplate( enumClass );
                }
            }
            else {
                ktemplate = getKeyType().template();
            }
        }
        {
            if ( getValueType().isComlex() ) {
                if ( getValueType() == FieldType.MESSAGE ) {}
                else if ( getValueType() == FieldType.ENUM ) {
                    Class<? extends GeneratedEnum> enumClass = (Class<? extends GeneratedEnum>) Class.forName( getValueTypeReference() );
                    vtemplate = new EnumTemplate( enumClass );
                }
            }
            else {
                vtemplate = getValueType().template();
            }
        }
        template = Templates.tMap( ktemplate, vtemplate );
    }
}
