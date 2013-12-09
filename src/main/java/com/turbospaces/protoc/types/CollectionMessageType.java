package com.turbospaces.protoc.types;

import org.msgpack.template.ListTemplate;
import org.msgpack.template.SetTemplate;
import com.turbospaces.protoc.gen.ProtoGenerationContext;

public class CollectionMessageType extends ObjectMessageType {
    private boolean isSet;

    public CollectionMessageType(String ref, boolean setOtherwiseList) {
        super( ref );
        this.isSet = setOtherwiseList;
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CollectionMessageType(FieldType type, String ref, boolean setOtherwiseList) {
        super( type, ref );
        this.isSet = setOtherwiseList;
        template = isSet ? new SetTemplate( template ) : new ListTemplate( template );
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void init(ProtoGenerationContext ctx) throws Exception {
        super.init( ctx );
        template = isSet ? new SetTemplate( template ) : new ListTemplate( template );
    }
    @Override
    public String javaTypeAsString() {
        String coll = isSet ? "Set" : "List";
        return coll + "<" + super.javaTypeAsString() + ">";
    }
    public boolean isSet() {
        return isSet;
    }
    @Override
    public boolean isCollection() {
        return true;
    }
}
