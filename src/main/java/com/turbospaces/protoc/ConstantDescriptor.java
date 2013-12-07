package com.turbospaces.protoc;

import java.math.BigDecimal;

import com.google.common.base.Objects;
import com.turbospaces.protoc.MessageType.CollectionType;

public class ConstantDescriptor {
    String qualifier;
    MessageType type;
    Object value;

    public void setValue(String typeRef, String text) {
        type = new MessageType( typeRef, CollectionType.NONE );
        switch ( type.getType() ) {
            case BOOL:
                value = Boolean.parseBoolean( text );
                break;
            case BYTE:
                value = Byte.parseByte( text );
                break;
            case DECIMAL:
                value = new BigDecimal( text );
                break;
            case DOUBLE:
                value = Double.parseDouble( text );
                break;
            case FLOAT:
                value = Float.parseFloat( text );
                break;
            case INT16:
                value = Short.parseShort( text );
                break;
            case INT32:
                value = Integer.parseInt( text );
                break;
            case INT64:
                value = Long.parseLong( text );
                break;
            case STRING:
                value = text;
                break;
            default:
                break;
        }
    }

    @Override
    public String toString() {
        return Objects
                .toStringHelper( this )
                .add( "qualifier", qualifier )
                .add( "type", type )
                .add( "value", value )
                .toString();
    }
}
