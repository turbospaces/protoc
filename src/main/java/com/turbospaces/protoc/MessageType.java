package com.turbospaces.protoc;

import java.math.BigDecimal;
import java.util.Arrays;

import com.google.common.base.Objects;

public class MessageType {
    private CollectionType collectionType;
    private String typeRef, valueRef;
    private FieldType type, valueType;

    public MessageType(String ref, CollectionType collectionType) {
        if ( ref != null ) {
            this.type = FieldType.MESSAGE;
            for ( FieldType t : FieldType.values() ) {
                if ( t.name().equals( ref.toUpperCase() ) ) {
                    this.type = t;
                    break;
                }
            }
            typeRef = ref;
            this.collectionType = collectionType;
        }
    }
    public MessageType(String keyRef, String valueRef) {
        FieldType ktype = FieldType.MESSAGE;
        FieldType vtype = FieldType.MESSAGE;

        if ( keyRef != null && valueRef != null ) {
            for ( FieldType t : FieldType.values() ) {
                if ( t.name().equals( keyRef.toUpperCase() ) ) {
                    ktype = t;
                    break;
                }
                else if ( t.name().equals( keyRef.toUpperCase() ) ) {
                    vtype = t;
                    break;
                }
            }
            this.typeRef = keyRef;
            this.valueRef = valueRef;
            type = ktype;
            valueType = vtype;
            collectionType = CollectionType.MAP;
        }
    }

    public boolean isMap() {
        return collectionType == CollectionType.MAP;
    }
    public boolean isCollection() {
        return collectionType == CollectionType.LIST || collectionType == CollectionType.SET;
    }
    public CollectionType getCollectionType() {
        return collectionType;
    }
    public String getValueRef() {
        return valueRef;
    }
    public String getTypeRef() {
        return typeRef;
    }
    public FieldType getType() {
        return type;
    }
    public FieldType getValueType() {
        return valueType;
    }
    @Override
    public String toString() {
        return Objects
                .toStringHelper( this )
                .add( "collectionType", collectionType )
                .add( "typeReferences", Arrays.toString( new String[] { typeRef, valueRef } ) )
                .add( "fieldTypes", Arrays.toString( new FieldType[] { type, valueType } ) )
                .toString();
    }
    public String genericJavaTypeAsString() {
        String ktype = getType().isComlex() ? typeRef : primitives();
        switch ( getCollectionType() ) {
            case LIST:
                return "List<" + ktype + ">";
            case MAP:
                String vtype = getValueType().isComlex() ? getValueRef() : primitives();
                return "Map<" + ktype + "," + vtype + ">";
            case NONE:
                return ktype;
            case SET:
                return "Set<" + ktype + ">";
            default:
                throw new Error();
        }
    }
    private String primitives() {
        switch ( getType() ) {
            case BINARY:
                return byte[].class.getSimpleName();
            case BOOL:
                return Boolean.class.getSimpleName();
            case BYTE:
                return Byte.class.getSimpleName();
            case DECIMAL:
                return BigDecimal.class.getSimpleName();
            case DOUBLE:
                return Double.class.getSimpleName();
            case FLOAT:
                return Float.class.getSimpleName();
            case INT16:
                return Short.class.getSimpleName();
            case INT32:
                return Integer.class.getSimpleName();
            case INT64:
                return Long.class.getSimpleName();
            case STRING:
                return String.class.getSimpleName();
            default:
                throw new Error();
        }
    }

    public static enum FieldType {
        BYTE,
        INT16,
        INT32,
        INT64,
        STRING,
        BOOL,
        DOUBLE,
        FLOAT,
        DECIMAL,
        BINARY,
        MESSAGE;

        public boolean isComlex() {
            return this == FieldType.MESSAGE;
        }
    }

    public static enum CollectionType {
        LIST,
        SET,
        MAP,
        NONE;
    }
}
