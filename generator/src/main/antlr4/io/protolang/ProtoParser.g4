grammar ProtoParser;

all_identifiers : IDENTIFIER | QUALIFIED_IDENTIFIER ;

literal_value : literal_substitude | literal_without_substitude;
literal_substitude : SUBSTITUDE_START all_identifiers SUBSTITUDE_END ;
literal_without_substitude
  :  INTEGER_LITERAL
  |  STRING_LITERAL
  |  BOOL_LITERAL
  |  FLOAT_LITERAL
  ;

proto: package_def (import_def)* (constant_def | enum_def |message_def | service_def )+ EOF ;

import_def: IMPORT_LITERAL import_value ITEM_TERMINATOR ;
import_value : STRING_LITERAL ;

package_def : PACKAGE_LITERAL package_name ITEM_TERMINATOR;
package_name : QUALIFIED_IDENTIFIER;

constant_def : CONST_LITERAL constant_type constant_name EQUALS literal_value ITEM_TERMINATOR ;
constant_type : TYPE_LITERAL ;
constant_name : IDENTIFIER ;

one_of_def : ONE_OF_LITERAL BRACKET_OPEN one_of_def_member (COMMA one_of_def_member)* BRACKET_CLOSE ;
one_of_def_member : all_identifiers;
any_of_def : ANY_OF_LITERAL BRACKET_OPEN any_of_def_base_type BRACKET_CLOSE ;
any_of_def_base_type : all_identifiers ;

enum_def : ENUM_LITERAL enum_name BLOCK_OPEN (enum_member_tag ITEM_TERMINATOR)* BLOCK_CLOSE ;
enum_name : IDENTIFIER ;
enum_member_tag : enum_member EQUALS enum_tag ;
enum_member : IDENTIFIER ;
enum_tag : INTEGER_LITERAL ;

message_def : messsage_type message_name message_parent BLOCK_OPEN (message_field_def)* BLOCK_CLOSE ;
messsage_type : MESSAGE_LITERAL | ERROR_LITERAL ;
message_name : IDENTIFIER ;
message_parent : (EXTEND_LITERAL message_parent_message)? ;
message_parent_message : all_identifiers ;

message_field_def : message_field_type message_field_name EQUALS message_field_tag (BRACKET_OPEN message_field_options (COMMA message_field_options)* BRACKET_CLOSE)? ITEM_TERMINATOR ;
message_field_options : (message_field_required) ? | (message_field_default_value)? | (message_field_json_type)? ;
message_field_required : REQUIRED_LITERAL EQUALS literal_value ;
message_field_default_value : DEFAULT_LITERAL EQUALS literal_value ;
message_field_json_type : JSON_TYPE EQUALS literal_value ;
message_field_type : collection_map_value | one_of_def | any_of_def ;
message_field_name : IDENTIFIER ;
message_field_tag : INTEGER_LITERAL ;

service_def : SERVICE_LITERAL service_name service_parent BLOCK_OPEN (service_method_def)+ BLOCK_CLOSE ;
service_name : IDENTIFIER ;
service_parent : (EXTEND_LITERAL service_parent_message)? ;
service_parent_message : all_identifiers ;

service_method_def : DEF_LITERAL service_method_name PAREN_OPEN service_method_req PAREN_CLOSE COLON service_method_resp service_method_throws ITEM_TERMINATOR ;
service_method_name : IDENTIFIER ;
service_method_req : (collection_map_value)? ;
service_method_resp : collection_map_value ;
service_method_throws: (THROWS_LITEARAL service_method_excp (COMMA service_method_excp)* )? ;
service_method_excp : all_identifiers ;

collection_map_value : collection_map | all_identifiers | TYPE_LITERAL ;
collection_map : collection | map ;
collection : COLLECTION_LITERAL BRACKET_OPEN collection_type BRACKET_CLOSE ;
collection_type : TYPE_LITERAL | all_identifiers ;
map : MAP_LITERAL BRACKET_OPEN map_key COMMA map_value BRACKET_CLOSE ;
map_key : TYPE_LITERAL | all_identifiers ;
map_value : TYPE_LITERAL | all_identifiers ;

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

SUBSTITUDE_START : '{{' ;
SUBSTITUDE_END : '}}' ;
ONE_OF_LITERAL : 'oneOf' ;
ANY_OF_LITERAL : 'anyOf' ;
REQUIRED_LITERAL : 'required' ;
JSON_TYPE : 'json_type' ;
DEFAULT_LITERAL : 'default' ;
CONST_LITERAL : 'const' ;
PACKAGE_LITERAL : 'package' ;
MESSAGE_LITERAL : 'message' ;
ERROR_LITERAL : 'error' ;
EXTEND_LITERAL : 'extends' ;
SERVICE_LITERAL : 'service' ;
DEF_LITERAL : 'def' ;
THROWS_LITEARAL : 'throws' ;
ENUM_LITERAL : 'enum' ;
IMPORT_LITERAL : 'import' ;

COLLECTION_LITERAL : 'set' | 'list' ;
MAP_LITERAL : 'map' ;
TYPE_LITERAL : 'byte' | 'int16' | 'int32' | 'int64' | 'float' | 'double' | 'string' | 'boolean' | 'binary' ;

INTEGER_LITERAL
  :  HEX_LITERAL
  |  OCTAL_LITERAL
  |  DECIMAL_LITERAL
  ;
fragment HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;
fragment HEX_LITERAL : '-'? '0' ('x'|'X') HEX_DIGIT+ ;
fragment OCTAL_LITERAL : '-'? '0' ('0'..'7')+ ;
fragment DECIMAL_LITERAL : ('0' | '-'? '1'..'9' '0'..'9'*) ;

STRING_LITERAL
  :  '"' STRING_GUTS '"'
  ;
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

BOOL_LITERAL : 'true' | 'false';

FLOAT_LITERAL
  :  '-'? ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
  |  '-'? '.' ('0'..'9')+ EXPONENT?
  |  '-'? ('0'..'9')+ EXPONENT
  ;

fragment EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

IDENTIFIER : '_'* ('a'..'z' | 'A'..'Z' ) ('a'..'z' | 'A'..'Z' | '_' | '0'..'9')* ;
QUALIFIED_IDENTIFIER : IDENTIFIER ('.' IDENTIFIER)+ ;

WS : [ \t\r\n]+ -> skip ;