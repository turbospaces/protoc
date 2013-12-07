package com.turbospaces.protoc;

import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;

public interface GeneratedMessage {
    Object getFieldValue(int tag);
    FieldDescriptor getFieldDescriptor(int tag);
}
