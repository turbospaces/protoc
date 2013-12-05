package com.turbospaces.protoc;

import java.util.SortedMap;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public final class EnumDescriptor {
    String qualifier;
    SortedMap<Integer, String> values = Maps.newTreeMap();

    @Override
    public String toString() {
        return Objects.toStringHelper( this ).add( "qualifier", qualifier ).add( "values", values ).toString();
    }
}
