package com.turbospaces.protoc;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.unpacker.BufferUnpacker;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.types.ObjectMessageType.CollectionType;
import com.turbospaces.protoc.types.ObjectMessageType.FieldType;

public class Streams {
    private static MessagePack msgpack = new MessagePack();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static byte[] out(GeneratedMessage obj) throws IOException {
        BufferPacker packer = msgpack.createBufferPacker();
        SortedMap<Integer, FieldDescriptor> descriptors = obj.getAllDescriptors();
        for ( Entry<Integer, FieldDescriptor> entry : descriptors.entrySet() ) {
            FieldDescriptor f = entry.getValue();
            Object v = obj.getFieldValue( f.getTag() );
            CollectionType collectionType = f.getType().getCollectionType();
            switch ( collectionType ) {
                case LIST: {
                    List l = ( (List) v );
                    packer.writeArrayBegin( l.size() );
                    for ( Object lv : l ) {
                        out( f.getType().getType(), lv, packer );
                    }
                    packer.writeArrayEnd( true );
                    break;
                }
                case MAP: {
                    Map m = (Map<?, ?>) v;
                    packer.writeMapBegin( m.size() );
                    Set<Map.Entry> entrySet = m.entrySet();
                    for ( Map.Entry e : entrySet ) {
                        out( f.getType().getType(), e.getKey(), packer );
                        out( f.getType().getValueType(), e.getValue(), packer );
                    }
                    packer.writeMapEnd( true );
                    break;
                }
                case NONE: {
                    out( f.getType().getType(), v, packer );
                    break;
                }
                case SET: {
                    Set l = ( (Set) v );
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
        return packer.toByteArray();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
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
                        List l = Lists.newLinkedList();
                        int elements = unpacker.readArrayBegin();
                        for ( int i = 0; i < elements; i++ ) {
                            l.add( in( f.getType().getType(), unpacker ) );
                        }
                        unpacker.readArrayEnd( true );
                        value = l;
                        break;
                    }
                    case MAP: {
                        Map m = Maps.newHashMap();
                        int elements = unpacker.readMapBegin();
                        for ( int i = 0; i < elements; i++ ) {
                            Object k = in( f.getType().getType(), unpacker );
                            Object v = in( f.getType().getValueType(), unpacker );
                            m.put( k, v );
                        }
                        unpacker.readMapEnd( true );
                        value = m;
                        break;
                    }
                    case NONE: {
                        value = in( f.getType().getType(), unpacker );
                        break;
                    }
                    case SET: {
                        Set s = Sets.newHashSet();
                        int elements = unpacker.readArrayBegin();
                        for ( int i = 0; i < elements; i++ ) {
                            s.add( in( f.getType().getType(), unpacker ) );
                        }
                        unpacker.readArrayEnd( true );
                        value = s;
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
    private static Object in(final FieldType t, final BufferUnpacker unp) throws IOException {
        Object value = null;
        switch ( t ) {
            case BINARY: {
                value = unp.readByteArray();
                break;
            }
            case BOOL: {
                value = unp.readBoolean();
                break;
            }
            case BYTE: {
                value = unp.readByte();
                break;
            }
            case DECIMAL: {
                // TODO
                break;
            }
            case DOUBLE: {
                value = unp.readDouble();
                break;
            }
            case FLOAT: {
                value = unp.readFloat();
                break;
            }
            case INT16: {
                value = unp.readShort();
                break;
            }
            case INT32: {
                value = unp.readInt();
                break;
            }
            case INT64: {
                value = unp.readLong();
                break;
            }
            case MESSAGE: {
                // TODO
                break;
            }
            case STRING: {
                value = unp.readString();
                break;
            }
            default:
                throw new Error();
        }
        return value;
    }
    private static void out(final FieldType t, final Object v, final BufferPacker p) throws IOException {
        switch ( t ) {
            case BINARY: {
                p.write( (byte[]) v );
                break;
            }
            case BOOL: {
                p.write( (Boolean) v );
                break;
            }
            case BYTE: {
                p.write( (Byte) v );
                break;
            }
            case DECIMAL: {
                // TODO
                break;
            }
            case DOUBLE: {
                p.write( (Double) v );
                break;
            }
            case FLOAT: {
                p.write( (Float) v );
                break;
            }
            case INT16: {
                p.write( (Short) v );
                break;
            }
            case INT32: {
                p.write( (Integer) v );
                break;
            }
            case INT64: {
                p.write( (Long) v );
                break;
            }
            case ENUM : {
                
            }
            case MESSAGE: {
                // TODO
                break;
            }
            case STRING: {
                p.write( (String) v );
                break;
            }
            default:
                throw new Error();
        }
    }
}
