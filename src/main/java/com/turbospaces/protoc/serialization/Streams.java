package com.turbospaces.protoc.serialization;

import java.io.IOException;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.unpacker.BufferUnpacker;

import com.turbospaces.protoc.gen.GeneratedMessage;

public class Streams {
    static MessagePack msgpack = new MessagePack();

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
}
