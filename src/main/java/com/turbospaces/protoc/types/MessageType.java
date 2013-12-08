package com.turbospaces.protoc.types;

import org.msgpack.template.Template;

public interface MessageType {
    Template<?> template();
    String javaTypeAsString(); // for java code gen
    boolean isMap();
    boolean isCollection();
}
