package com.turbospaces.protoc;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ProtoContainer {
    public String pkg, name;
    public Set<String> imports = Sets.newLinkedHashSet();
    public Map<String, ServiceDescriptor> services = Maps.newHashMap();
    //
    public Map<String, MessageDescriptor> messages = Maps.newHashMap();
    public Map<String, String> aliases = Maps.newHashMap();
    public Map<String, EnumDescriptor> enums = Maps.newHashMap();
    public Map<String, ConstantDescriptor> constants = Maps.newHashMap();
    
    public String getPkg() {
        return pkg;
    }
    public String getName() {
        return name;
    }
    public Collection<MessageDescriptor> getMessages() {
        return messages.values();
    }
    public Collection<ConstantDescriptor> getConstants() {
        return constants.values();
    }
    @Override
    public String toString() {
        return Objects
                .toStringHelper( this )
                .add( "pkg", pkg )
                .add( "imports", imports )
                .add( "constants", constants.values() )
                .add( "enums", enums.values() )
                .add( "messages", messages.values() )
                .add( "services", services.values() )
                .toString();
    }
    
    public static abstract class NamedDescriptor {
        protected String name;
        public String getName() {
            return name;
        }
    }
}
