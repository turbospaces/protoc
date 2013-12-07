package com.turbospaces.protoc;

import java.util.Set;

import com.google.common.collect.Sets;
import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.MessageType.FieldType;

public class ProtoGenerationContext {
    Set<ProtoContainer> containers = Sets.newLinkedHashSet();
    Set<ProtoContainer> imports = Sets.newLinkedHashSet();

    public void validate() {}

    public String qualified(FieldDescriptor f) {
        String qualified = f.getName();
        MessageType messageType = f.getType();
        if ( messageType.getType() == FieldType.MESSAGE ) {
            for ( ProtoContainer c : containers ) {
                if ( c.messages.containsKey( f.getMessageDescriptor().getName() ) ) {
                    
                }
            }
        }
        return qualified;
    }
}
