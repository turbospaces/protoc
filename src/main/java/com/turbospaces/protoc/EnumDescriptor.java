package com.turbospaces.protoc;

import java.util.Collections;
import java.util.SortedMap;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.turbospaces.protoc.ProtoContainer.NamedDescriptor;

public final class EnumDescriptor extends NamedDescriptor {
    SortedMap<Integer, String> members = Maps.newTreeMap();

    @Override
    public String toString() {
        return Objects.toStringHelper( this ).add( "name", name ).add( "values", members ).toString();
    }
    public String getName() {
        return name;
    }
    public SortedMap<Integer, String> getMembers() {
        return Collections.unmodifiableSortedMap( members );
    }
}
