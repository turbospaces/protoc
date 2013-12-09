package com.turbospaces.protoc.gen;

import java.io.Externalizable;
import java.util.Collection;

import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;

public interface GeneratedMessage extends Externalizable {
    Object getFieldValue(int tag);
    void setFieldValue(int tag, Object value);
    FieldDescriptor getFieldDescriptor(int tag);
    Collection<FieldDescriptor> getAllDescriptors();
    @Override String toString();
    @Override boolean equals(Object other);
    @Override int hashCode();
}
