grammar ProtoParser;

proto: (import_def)* (package_def)? (constant_def | enum_def |message_def | alias_def | service_def )+ EOF;

literal_value: BOOLEAN_LITERAL | INTEGER_LITERAL | STRING_LITERAL | FLOAT_LITERAL;

import_def: IMPORT_LITERAL import_value ITEM_TERMINATOR;
import_value: IMPORT;

package_def: PACKAGE_LITERAL package_name ITEM_TERMINATOR;
package_name: QUALIFIED_IDENTIFIER;

constant_def: CONST_LITERAL constant_type constant_name EQUALS literal_value ITEM_TERMINATOR;
constant_type: TYPE_LITERAL;
constant_name: IDENTIFIER;

enum_def: ENUM_LITERAL enum_name BLOCK_OPEN enum_members BLOCK_CLOSE;
enum_name: IDENTIFIER;
enum_members: enum_member_tag (COMMA enum_member_tag)*;
enum_member_tag: enum_member EQUALS enum_tag;
enum_member: IDENTIFIER;
enum_tag: TAG;

message_def: MESSAGE_LITERAL message_name message_parent BLOCK_OPEN (message_field_def)* BLOCK_CLOSE;
message_name: IDENTIFIER;
message_parent: (EXTEND_LITERAL message_parent_message)?;
message_parent_message: IDENTIFIER;

message_field_def: message_field_type message_field_name EQUALS message_field_tag ITEM_TERMINATOR;
message_field_type: collection_map_value;
message_field_name: IDENTIFIER;
message_field_tag: TAG;

service_def: SERVICE_LITERAL service_name service_parent BLOCK_OPEN (service_method_def)+ BLOCK_CLOSE;
service_name: IDENTIFIER;
service_parent: (EXTEND_LITERAL service_parent_message)?;
service_parent_message: IDENTIFIER;

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

CONST_LITERAL: 'const';
PACKAGE_LITERAL: 'package';
ALIAS_LITERAL: 'alias';
MESSAGE_LITERAL : 'message' ;
EXTEND_LITERAL : 'extends' ;
SERVICE_LITERAL: 'service';
DEF_LITERAL: 'def';
THROWS_LITEARAL: 'throws';
ENUM_LITERAL: 'enum';
IMPORT_LITERAL: 'import';

BOOLEAN_LITERAL: 'true' | 'false';
COLLECTION_LITERAL: 'set' | 'list';
MAP_LITERAL: 'map';
TYPE_LITERAL: 'byte' | 'int16' | 'int32' | 'int64' | 'float' | 'double' | 'string' | 'bool' | 'bdecimal' | 'binteger' | 'binary' | 'date';
IDENTIFIER: ('a'..'z' | 'A'..'Z' ) ('a'..'z' | 'A'..'Z' | '_' | '0'..'9')*;
QUALIFIED_IDENTIFIER: IDENTIFIER (DOT IDENTIFIER)*;
TAG: ('0' | '1'..'9' '0'..'9'*);
IMPORT: '"' QUALIFIED_IDENTIFIER '"';

INTEGER_LITERAL: HEX_LITERAL | OCTAL_LITERAL | DECIMAL_LITERAL;
fragment HEX_DIGIT: ('0'..'9'|'a'..'f'|'A'..'F') ;
fragment HEX_LITERAL: '-'? '0' ('x'|'X') HEX_DIGIT+ ;
fragment OCTAL_LITERAL: '-'? '0' ('0'..'7')+ ;
fragment DECIMAL_LITERAL: ('0' | '-'? '1'..'9' '0'..'9'*) ;

FLOAT_LITERAL
  :  '-'? ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
  |  '-'? '.' ('0'..'9')+ EXPONENT?
  |  '-'? ('0'..'9')+ EXPONENT
  ;
fragment EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

STRING_LITERAL: '"' STRING_GUTS '"';
fragment STRING_GUTS : ( ESCAPE_SEQUENCE | ~('\\'|'"'|'\n'|'\r') )* ;
fragment ESCAPE_SEQUENCE
  :  '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
  |  OCTAL_ESCAPE
  |  UNICODE_ESCAPE
  ;
fragment OCTAL_ESCAPE
  :  '\\' ('0'..'3') ('0'..'7') ('0'..'7')
  |  '\\' ('0'..'7') ('0'..'7')
  |  '\\' ('0'..'7')
  ;
fragment UNICODE_ESCAPE
  :  '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
  ;

WS : [ \t\r\n]+ -> skip ;