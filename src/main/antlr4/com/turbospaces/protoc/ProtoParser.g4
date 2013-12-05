grammar ProtoParser;

proto: package_def (message_def|alias_def|service_def|enum_def)+ EOF;

package_def: PACKAGE_LITERAL package_name ITEM_TERMINATOR;
package_name: QUALIFIED_IDENTIFIER;

enum_def: ENUM_LITERAL enum_name BLOCK_OPEN enum_members BLOCK_CLOSE;
enum_name: IDENTIFIER;
enum_members: enum_member_tag (COMMA enum_member_tag)*;
enum_member_tag: enum_member EQUALS enum_tag;
enum_member: IDENTIFIER;
enum_tag: TAG;

message_def: MESSAGE_LITERAL message_name BLOCK_OPEN (message_field_def)* BLOCK_CLOSE;
message_name: IDENTIFIER;

message_field_def: message_field_type message_field_name EQUALS message_field_tag ITEM_TERMINATOR;
message_field_type: collection_map_value;
message_field_name: IDENTIFIER;
message_field_tag: TAG;

service_def: SERVICE_LITERAL service_name BLOCK_OPEN (service_method_def)+ BLOCK_CLOSE;
service_name: IDENTIFIER;

service_method_def: DEF_LITERAL service_method_name PAREN_OPEN service_method_req PAREN_CLOSE COLON service_method_resp service_method_throws ITEM_TERMINATOR;
service_method_name: IDENTIFIER;
service_method_req: (collection_map_value)?;
service_method_resp : collection_map_value;
service_method_throws: (THROWS_LITEARAL service_method_excp (COMMA service_method_excp)* )?;
service_method_excp: IDENTIFIER;

alias_def: ALIAS_LITERAL alias_source EQUALS alias_destination ITEM_TERMINATOR;
alias_source: IDENTIFIER;
alias_destination: IDENTIFIER;

collection_map_value: collection_map|IDENTIFIER|TYPE_LITERAL;
collection_map: collection|map;
collection: COLLECTION_LITERAL BRACKET_OPEN collection_type BRACKET_CLOSE;
collection_type: TYPE_LITERAL|IDENTIFIER;
map: MAP_LITERAL BRACKET_OPEN map_key COMMA map_value BRACKET_CLOSE;
map_key: TYPE_LITERAL|IDENTIFIER;
map_value: TYPE_LITERAL|IDENTIFIER ;

BLOCK_OPEN : '{' ;
BLOCK_CLOSE : '}' ;
PAREN_OPEN : '(' ;
PAREN_CLOSE : ')' ;
BRACKET_OPEN : '[' ;
BRACKET_CLOSE : ']' ;
EQUALS : '=' ;
COLON : ':' ;
COMMA : ',' ;
DOT : '.' ;
ITEM_TERMINATOR : ';' ;

PACKAGE_LITERAL: 'package';
ALIAS_LITERAL: 'alias';
MESSAGE_LITERAL : 'message' ;
EXTEND_LITERAL : 'extends' ;
SERVICE_LITERAL: 'service';
DEF_LITERAL: 'def';
THROWS_LITEARAL: 'throws';
ENUM_LITERAL: 'enum';

COLLECTION_LITERAL: 'set' | 'list';
MAP_LITERAL: 'map';
TYPE_LITERAL: 'byte' | 'int16' | 'int32' | 'int64' | 'float' | 'double' | 'string' | 'bool' | 'decimal' | 'binary';
IDENTIFIER: ('a'..'z' | 'A'..'Z' ) ('a'..'z' | 'A'..'Z' | '_' | '0'..'9')*;
QUALIFIED_IDENTIFIER: IDENTIFIER (DOT IDENTIFIER)*;
TAG: ('0' | '1'..'9' '0'..'9'*); 

WS : [ \t\r\n]+ -> skip ;