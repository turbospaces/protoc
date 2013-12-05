protoc
======

protocol - better/smaller/faster than protobuf/thrift without pain!

This is alternative/minimalistic implementation of binary protocol, similar to protobuf and apache thrift without **pain**:
* **_types_**
** messages and enums
** primitives: int16, int32, int64, float, double, big decimal, byte, binary
** aliases 
** set/list/map with generics
** rpc service definitions

* **_missing features in place_**
** inheritance - completely missed in thrift, partially present in protobuf via extensions
** natural RPC IDL (request/response/exceptions)
** natural JSON formatter (none of protobuf-java-format/protostuf/thrift handles it properly)
** GWT compatible stubs
** natural support for server pushes (notifications)
 