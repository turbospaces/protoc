package com.turbospaces.protoc.serialization;

import java.io.IOException;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.BufferUnpacker;
import org.msgpack.unpacker.Unpacker;

import com.google.common.base.Throwables;
import com.turbospaces.protoc.gen.GeneratedMessage;

public class Streams {
    private static MessagePack msgpack = new MessagePack();

    public static byte[] out(GeneratedMessage obj) throws IOException {
        BufferPacker packer = msgpack.createBufferPacker();
        ObjectTemplate template = obj.template();
        template.write( packer, obj );
        return packer.toByteArray();
    }
    public static GeneratedMessage in(byte[] arr, GeneratedMessage obj) throws IOException {
        BufferUnpacker unpacker = msgpack.createBufferUnpacker( arr );
        ObjectTemplate template = obj.template();
        template.read( unpacker, obj );
        return obj;
    }
    public static void write(Packer pk, ObjectTemplate template, GeneratedMessage val) throws IOException {
        BufferPacker mbp = msgpack.createBufferPacker();
        template.write( mbp, val );
        byte[] mbytes = mbp.toByteArray();
        pk.write( mbytes );
    }
    public static GeneratedMessage read(Unpacker u, ObjectTemplate template) throws IOException {
        byte[] arr = u.readByteArray();
        BufferUnpacker mbu = Streams.msgpack.createBufferUnpacker( arr );
        GeneratedMessage value = null;
        try {
            value = ( (ObjectTemplate) template ).getOwner().newInstance();
        }
        catch ( Exception e ) {
            Throwables.propagate( e );
        }
        template.read( mbu, value );
        return value;
    }
}
