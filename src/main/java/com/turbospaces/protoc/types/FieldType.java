package com.turbospaces.protoc.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;

import org.msgpack.template.Template;
import org.msgpack.template.Templates;

public enum FieldType {
    BYTE,
    INT16,
    INT32,
    INT64,
    STRING,
    BOOL,
    DOUBLE,
    FLOAT,
    BDECIMAL,
    BINTEGER,
    DATE,
    BINARY,
    ENUM,
    MESSAGE;

    public boolean isComlex() {
        return this == FieldType.MESSAGE || this == FieldType.ENUM;
    }

    public Template<?> template() {
        switch ( this ) {
            case BINARY:
                return Templates.TByteArray;
            case BOOL:
                return Templates.TBoolean;
            case BYTE:
                return Templates.TByte;
            case DATE:
                return Templates.TDate;
            case BDECIMAL:
                return Templates.TBigDecimal;
            case BINTEGER:
                return Templates.TBigInteger;
            case DOUBLE:
                return Templates.TDouble;
            case FLOAT:
                return Templates.TFloat;
            case INT16:
                return Templates.TShort;
            case INT32:
                return Templates.TInteger;
            case INT64:
                return Templates.TLong;
            case STRING:
                return Templates.TString;
            case ENUM:
                throw new Error();
            case MESSAGE:
                throw new Error();
        }
        throw new Error();
    }
    public String javaTypeAsString() {
        switch ( this ) {
            case BINARY:
                return byte[].class.getSimpleName();
            case BOOL:
                return Boolean.class.getSimpleName();
            case BYTE:
                return Byte.class.getSimpleName();
            case BDECIMAL:
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
            case DATE:
                return Date.class.getSimpleName();
            case BINTEGER:
                return BigInteger.class.getSimpleName();
            case ENUM:
                throw new Error();
            case MESSAGE:
                throw new Error();
        }
        throw new Error();
    }
}
