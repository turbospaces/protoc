package com.turbospaces.protoc;

import java.util.SortedMap;

import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;

public interface GeneratedMessage {
    Object getFieldValue(int tag);
    void setFieldValue(int tag, Object value);
    FieldDescriptor getFieldDescriptor(int tag);
    SortedMap<Integer, FieldDescriptor> getAllDescriptors();
    @Override String toString();
    @Override boolean equals(Object other);
    @Override int hashCode();
}
