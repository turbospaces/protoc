package com.turbospaces.protoc.gen;

import java.io.Externalizable;
import java.util.Collection;

import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.serialization.ObjectTemplate;

public interface GeneratedMessage extends Externalizable, Cloneable {
    Object getFieldValue(int tag);
    void setFieldValue(int tag, Object value);
    FieldDescriptor getFieldDescriptor(int tag);
    Collection<FieldDescriptor> getAllDescriptors();
    ObjectTemplate template();
    @Override String toString();
    @Override boolean equals(Object other);
    @Override int hashCode();
}
