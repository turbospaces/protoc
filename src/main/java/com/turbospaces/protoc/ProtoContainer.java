package com.turbospaces.protoc;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class ProtoContainer {
    String pkg;
    Map<String, MessageDescriptor> messages = Maps.newHashMap();
    Map<String, ServiceDescriptor> services = Maps.newHashMap();
    Map<String, String> aliases = Maps.newHashMap();
    Map<String, EnumDescriptor> enums = Maps.newHashMap();

    @Override
    public String toString() {
        return Objects
                .toStringHelper( this )
                .add( "pkg", pkg )
                .add( "enums", enums )
                .add( "messages", messages )
                .add( "services", services )
                .toString();
    }
}
