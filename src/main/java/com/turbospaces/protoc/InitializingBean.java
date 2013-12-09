package com.turbospaces.protoc;

import com.turbospaces.protoc.gen.ProtoGenerationContext;

public interface InitializingBean {
    void init(ProtoGenerationContext ctx) throws Exception;
}
