package com.turbospaces.protoc;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.unpacker.BufferUnpacker;

import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.MessageType.CollectionType;
import com.turbospaces.protoc.MessageType.FieldType;

public class Streams {
    private static MessagePack msgpack = new MessagePack();

    @SuppressWarnings("rawtypes")
    public static byte[] out(GeneratedMessage obj) throws IOException {
        BufferPacker packer = msgpack.createBufferPacker();
        SortedMap<Integer, FieldDescriptor> descriptors = obj.getAllDescriptors();
        for ( Entry<Integer, FieldDescriptor> entry : descriptors.entrySet() ) {
            FieldDescriptor f = entry.getValue();
            Object v = obj.getFieldValue( f.getTag() );
            if ( v == null ) {
                packer.writeNil();
            }
            else {
                CollectionType collectionType = f.getType().getCollectionType();
                switch ( collectionType ) {
                    case LIST: {
                        List<?> l = ( (List) v );
                        packer.writeArrayBegin( l.size() );
                        for ( Object lv : l ) {
                            out( f.getType().getType(), lv, packer );
                        }
                        packer.writeArrayEnd( true );
                        break;
                    }
                    case MAP: {
                        break;
                    }
                    case NONE: {
                        out( f.getType().getType(), v, packer );
                        break;
                    }
                    case SET: {
                        Set<?> l = ( (Set) v );
                        packer.writeArrayBegin( l.size() );
                        for ( Object lv : l ) {
                            out( f.getType().getType(), lv, packer );
                        }
                        packer.writeArrayEnd( true );
                        break;
                    }
                    default:
                        throw new Error();
                }
            }
        }
        return packer.toByteArray();
    }

    public static GeneratedMessage in(byte[] arr, GeneratedMessage obj) throws IOException {
        BufferUnpacker unpacker = msgpack.createBufferUnpacker( arr );
        SortedMap<Integer, FieldDescriptor> descriptors = obj.getAllDescriptors();
        for ( Entry<Integer, FieldDescriptor> entry : descriptors.entrySet() ) {
            FieldDescriptor f = entry.getValue();
            CollectionType collectionType = f.getType().getCollectionType();
            boolean isNull = unpacker.trySkipNil();
            Object value = null;
            if ( !isNull ) {
                switch ( collectionType ) {
                    case LIST: {
                        break;
                    }
                    case MAP: {
                        break;
                    }
                    case NONE: {
                        value = in( f.getType().getType(), obj, unpacker );
                        break;
                    }
                    case SET: {
                        break;
                    }
                    default:
                        throw new Error();
                }
                if ( value != null ) {
                    obj.setFieldValue( f.getTag(), value );
                }
            }
        }
        return obj;
    }

    private static Object
            in(final FieldType t, final GeneratedMessage m, final BufferUnpacker unpacker) throws IOException {
        Object value = null;
        switch ( t ) {
            case BINARY: {
                value = unpacker.readByteArray();
                break;
            }
            case BOOL: {
                value = unpacker.readBoolean();
                break;
            }
            case BYTE: {
                value = unpacker.readByte();
                break;
            }
            case DECIMAL: {
                // TODO
                break;
            }
            case DOUBLE: {
                value = unpacker.readDouble();
                break;
            }
            case FLOAT: {
                value = unpacker.readFloat();
                break;
            }
            case INT16: {
                value = unpacker.readShort();
                break;
            }
            case INT32: {
                value = unpacker.readInt();
                break;
            }
            case INT64: {
                value = unpacker.readLong();
                break;
            }
            case MESSAGE: {
                // TODO
                break;
            }
            case STRING: {
                value = unpacker.readString();
                break;
            }
            default:
                throw new Error();
        }
        return value;
    }
    private static void out(final FieldType t, final Object v, final BufferPacker packer) throws IOException {
        switch ( t ) {
            case BINARY: {
                packer.write( (byte[]) v );
                break;
            }
            case BOOL: {
                packer.write( (Boolean) v );
                break;
            }
            case BYTE: {
                packer.write( (Byte) v );
                break;
            }
            case DECIMAL: {
                // TODO
                break;
            }
            case DOUBLE: {
                packer.write( (Double) v );
                break;
            }
            case FLOAT: {
                packer.write( (Float) v );
                break;
            }
            case INT16: {
                packer.write( (Short) v );
                break;
            }
            case INT32: {
                packer.write( (Integer) v );
                break;
            }
            case INT64: {
                packer.write( (Long) v );
                break;
            }
            case MESSAGE: {
                // TODO
                break;
            }
            case STRING: {
                packer.write( (String) v );
                break;
            }
            default:
                throw new Error();
        }
    }
}
