package com.turbospaces.protoc.gen;

import java.util.Collection;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.base.Objects.ToStringHelper;
import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.serialization.ObjectTemplate;

public interface GeneratedMessage extends Cloneable {
    Object getFieldValue(int tag);
    void setFieldValue(int tag, Object value);
    FieldDescriptor getFieldDescriptor(int tag);
    Collection<FieldDescriptor> getAllDescriptors();
    ObjectTemplate template();
    @Override String toString();
    @Override boolean equals(Object other);
    @Override int hashCode();

    public static abstract class Util {
        public static GeneratedMessage clone(GeneratedMessage m) {
            GeneratedMessage clone = null;
            try {
                clone = m.getClass().newInstance();
            }
            catch ( Exception e ) {
                Throwables.propagate( e );
            }
            Collection<FieldDescriptor> descriptors = m.getAllDescriptors();
            for ( FieldDescriptor f : descriptors ) {
                Object obj = m.getFieldValue( f.getTag() );
                if ( obj != null ) {
                    clone.setFieldValue( f.getTag(), obj );
                }
            }
            return clone;
        }
        public static int hashCode(GeneratedMessage m) {
            int result = 1;
            Collection<FieldDescriptor> descriptors = m.getAllDescriptors();
            for ( FieldDescriptor f : descriptors ) {
                Object value = m.getFieldValue( f.getTag() );
                result = 31 * result + ( value == null ? 0 : value.hashCode() );
            }
            return result;
        }
        public static boolean equals(GeneratedMessage thiz, Object obj) {
            if ( obj == null )
                return false;
            if ( obj == thiz )
                return true;
            if ( !thiz.getClass().equals( obj.getClass() ) )
                return false;

            GeneratedMessage other = (GeneratedMessage) obj;
            boolean equals = true;
            Collection<FieldDescriptor> descriptors = thiz.getAllDescriptors();
            for ( FieldDescriptor f : descriptors ) {
                Object value = thiz.getFieldValue( f.getTag() );
                Object otherValue = other.getFieldValue( f.getTag() );

                equals = equals && Objects.equal( value, otherValue );
                if ( !equals ) {
                    break;
                }
            }
            return equals;
        }
        public static String toString(GeneratedMessage m) {
            ToStringHelper helper = Objects.toStringHelper( m );
            Collection<FieldDescriptor> descriptors = m.getAllDescriptors();
            for ( FieldDescriptor f : descriptors ) {
                Object value = m.getFieldValue( f.getTag() );
                if ( value != null ) {
                    helper.add( f.getName(), value );
                }
            }
            return helper.toString();
        }
        private Util() {}
    }
}
