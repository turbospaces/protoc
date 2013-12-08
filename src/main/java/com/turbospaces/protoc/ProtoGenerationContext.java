package com.turbospaces.protoc;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ProtoGenerationContext implements InitializingBean {
    Set<ProtoContainer> containers = Sets.newLinkedHashSet();
    Set<ProtoContainer> imports = Sets.newLinkedHashSet();
    //
    Map<String, String> qualifiedNames = Maps.newHashMap();

    @Override
    public void init(ProtoGenerationContext ctx) {
        Set<ProtoContainer> all = Sets.newHashSet();
        all.addAll( containers );
        all.addAll( imports );
        
        // 1. check for duplicates
        {
            for ( ProtoContainer c : all ) {
                Map<String, MessageDescriptor> messages = c.messages;
                Map<String, EnumDescriptor> enums = c.enums;
                Map<String, String> aliases = c.aliases;
            }
        }
    }
}
