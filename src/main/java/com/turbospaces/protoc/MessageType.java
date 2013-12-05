package com.turbospaces.protoc;

import com.google.common.base.Objects;

public class MessageType {
    private CollectionType collectionType;
    private String[] typeRefs;
    private FieldType[] types;

    public CollectionType getCollectionType() {
        return collectionType;
    }
    public FieldType[] getTypes() {
        return types;
    }
    public String[] getTypeRefs() {
        return typeRefs;
    }
    // REFERENCE
    public void setTypeReference(String ref, CollectionType collectionType) {
        FieldType type = FieldType.MESSAGE;

        if ( ref != null ) {
            for ( FieldType t : FieldType.values() ) {
                if ( t.name().equals( ref.toUpperCase() ) ) {
                    type = t;
                    break;
                }
            }
            typeRefs = new String[] { ref };
            types = new FieldType[] { type };
            this.collectionType = collectionType;
        }
    }
    // MAPS
    public void setTypeReference(String keyRef, String valueRef) {
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
            typeRefs = new String[] { keyRef, valueRef };
            types = new FieldType[] { ktype, vtype };
            collectionType = CollectionType.MAP;
        }
    }
    @Override
    public String toString() {
        return Objects
                .toStringHelper( this )
                .add( "collectionType", collectionType )
                .add( "typeReferences", getTypeRefs() )
                .add( "fieldTypes", getTypes() )
                .toString();
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
    }

    public static enum CollectionType {
        LIST,
        SET,
        MAP,
        NONE;
    }
}
