package com.turbospaces.protoc.types;

import org.msgpack.template.ListTemplate;
import org.msgpack.template.SetTemplate;
import org.msgpack.template.Template;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.turbospaces.protoc.gen.ProtoGenerationContext;

public class CollectionMessageType extends ObjectMessageType {
    private boolean isSet;

    public CollectionMessageType(String ref, boolean setOtherwiseList) {
        super( ref );
        this.isSet = setOtherwiseList;
    }
    public CollectionMessageType(FieldType type, String ref, boolean setOtherwiseList) {
        super( type, ref );
        this.isSet = setOtherwiseList;
        initiCollectionTemplate();
    }
    @Override
    public void init(ProtoGenerationContext ctx) throws Exception {
        super.init( ctx );
        initiCollectionTemplate();
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
    private void initiCollectionTemplate() {
        final Supplier<Template<?>> ptemplate = super.template;

        template = Suppliers.memoize( new Supplier<Template<?>>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Template<?> get() {
                return isSet ? new SetTemplate( ptemplate.get() ) : new ListTemplate( ptemplate.get() );
            }
        } );
    }
}
