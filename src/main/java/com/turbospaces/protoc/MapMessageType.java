package com.turbospaces.protoc;

import org.msgpack.template.Template;
import org.msgpack.template.Templates;

import com.turbospaces.protoc.types.FieldType;
import com.turbospaces.protoc.types.MessageType;

public class MapMessageType implements MessageType, InitializingBean {
    String kref, vref;
    FieldType ktype, vtype;
    Template<?> template;

    public MapMessageType(String keyRef, String valueRef) {
        this.kref = keyRef;
        this.vref = valueRef;
    }

    @Override
    public void init(ProtoGenerationContext ctx) {
        Template<?> ktemplate = null, vtemplate = null;
        for ( FieldType t : FieldType.values() ) {
            if ( t.name().equals( kref.toUpperCase() ) ) {
                this.ktype = t;
                ktemplate = t.template();
            }
            else if ( t.name().equals( vref.toUpperCase() ) ) {
                this.vtype = t;
                vtemplate = t.template();
            }
        }
        template = Templates.tMap( ktemplate, vtemplate );
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
}
