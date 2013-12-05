protoc
======

protocol - better/smaller/faster than protobuf/thrift without pain!

This is alternative/minimalistic implementation of binary protocol, similar to protobuf and apache thrift without **pain**:
#### types
* messages and enums
* primitives: int16, int32, int64, float, double, big decimal, byte, binary
* aliases 
* set/list/map with generics
* rpc service definitions

#### missing features in place
+ inheritance - completely missed in thrift, partially present in protobuf via extensions
+ natural RPC IDL (request/response/exceptions)
+ natural JSON formatter (none of protobuf-java-format/protostuf/thrift handles it properly)
+ GWT compatible stubs
+ natural support for server pushes (notifications)
 
##### samples
[example of schema](http://github.com/turbospaces/protoc/tree/master/src/test/resources/example.proto)