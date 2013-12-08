package com.turbospaces.protoc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.turbospaces.protoc.ProtoContainer.NamedDescriptor;
import com.turbospaces.protoc.types.ObjectMessageType;

public class ConstantDescriptor extends NamedDescriptor {
    ObjectMessageType type;
    Object value;

    public ConstantDescriptor(String name, String typeRef, String text) {
        this.name = name;
        type = new ObjectMessageType( typeRef );
        switch ( type.getType() ) {
            case BOOL:
                value = Boolean.parseBoolean( text );
                break;
            case BYTE:
                value = Byte.parseByte( text );
                break;
            case BDECIMAL:
                value = new BigDecimal( text );
                break;
            case BINTEGER:
                value = new BigInteger( text );
                break;
            case DATE:
                try {
                    value = DateFormat.getDateInstance().parse( text );
                }
                catch ( ParseException e ) {
                    Throwables.propagate( e );
                }
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
            case BINARY:
                throw new Error();
            case ENUM:
                throw new Error();
            case MESSAGE:
                throw new Error();
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper( this ).add( "name", name ).add( "type", type ).add( "value", value ).toString();
    }
}
