package com.turbospaces.protoc.types;

import org.msgpack.template.Template;

import com.turbospaces.protoc.InitializingBean;

public interface MessageType extends InitializingBean {
    Template<?> template();
    String javaTypeAsString(); // for java code gen
    boolean isMap();
    boolean isCollection();
}
