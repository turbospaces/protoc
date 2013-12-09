package com.turbospaces.protoc.gen;

public interface GeneratedEnum<T extends Enum<T>> {
    T valueOf(int tag);
    int tag();
}
