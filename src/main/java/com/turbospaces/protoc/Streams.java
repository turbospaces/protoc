package com.turbospaces.protoc;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;

public class Streams {
    private static MessagePack msgpack = new MessagePack();

    public static byte[] out(MessageDescriptor ms, Object obj) {
        BufferPacker bufferPacker = msgpack.createBufferPacker();
        
        return bufferPacker.toByteArray();
    }
}
