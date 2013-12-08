package com.turbospaces.protoc.types;

import org.msgpack.template.Templates;

import com.turbospaces.protoc.ProtoGenerationContext;

public class CollectionMessageType extends ObjectMessageType {
    boolean isSet;

    public CollectionMessageType(String ref, boolean setOtherwiseList) {
        super( ref );
        this.isSet = setOtherwiseList;
    }
    @Override
    public void init(ProtoGenerationContext ctx) {
        super.init( ctx );
        template = Templates.tCollection( template );
    }
    @Override
    public String javaTypeAsString() {
        String coll = isSet ? "Set" : "List";
        return coll + "<" + super.javaTypeAsString() + ">";
    }
    @Override
    public boolean isCollection() {
        return true;
    }
}
