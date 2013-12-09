package com.turbospaces.protoc.gen;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.serialization.Streams;

public abstract class AbstractGeneratedMessage implements GeneratedMessage {
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.write( Streams.out( this ) );
    }
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.available();
        byte[] bytes = new byte[size];
        Streams.in( bytes,  );
    }
    @Override
    public int hashCode() {
        int result = 1;
        Collection<FieldDescriptor> descriptors = getAllDescriptors();
        for ( FieldDescriptor f : descriptors ) {
            Object value = getFieldValue( f.getTag() );
            result = 31 * result + ( value == null ? 0 : value.hashCode() );
        }
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if ( obj == null )
            return false;
        if ( obj == this )
            return true;
        if ( !getClass().equals( obj.getClass() ) )
            return false;

        AbstractGeneratedMessage other = (AbstractGeneratedMessage) obj;
        boolean equals = true;
        Collection<FieldDescriptor> descriptors = getAllDescriptors();
        for ( FieldDescriptor f : descriptors ) {
            Object value = getFieldValue( f.getTag() );
            Object otherValue = other.getFieldDescriptor( f.getTag() );

            if ( value != null ) {
                equals = equals && Objects.equal( value, otherValue );
                if ( !equals )
                    break;
            }
        }
        return equals;
    }
    @Override
    public String toString() {
        ToStringHelper helper = Objects.toStringHelper( this );
        Collection<FieldDescriptor> descriptors = getAllDescriptors();
        for ( FieldDescriptor f : descriptors ) {
            Object value = getFieldValue( f.getTag() );
            if ( value != null ) {
                helper.add( f.getName(), value );
            }
        }
        return helper.toString();
    }
}
