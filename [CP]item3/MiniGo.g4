grammar MiniGo;
/*ok*/
program       : decl+    ; 
decl      : var_decl//o
         | fun_decl ;//o
var_decl   : VAR IDENT type_spec//o
         | VAR IDENT ',' IDENT type_spec//o
         | VAR IDENT '[' LITERAL ']' type_spec ;//o
type_spec  : INT 
         | VOID 
         | ; 
fun_decl   : FUNC IDENT '(' params ')' type_spec compound_stmt  //o
         | FUNC IDENT '(' params ')' '(' type_spec ',' type_spec ')' compound_stmt;//o
params    :  
         | param(',' param)* ;//o
param     : IDENT type_spec //o
         | IDENT '[' ']' type_spec ; //o
stmt      : expr_stmt //o
         | compound_stmt //o
         | assign_stmt
         | if_stmt //o
         | for_stmt 
         | return_stmt; 
expr_stmt  : expr ; //o

assign_stmt : VAR IDENT ',' IDENT type_spec '=' LITERAL ',' LITERAL //o
         | VAR IDENT type_spec '=' expr //o
         | IDENT type_spec '=' expr //o
         | IDENT '[' expr ']' '=' expr ; //o
         
         
compound_stmt: '{' local_decl* stmt* '}'; //o
if_stmt       : IF expr compound_stmt //o
         | IF expr compound_stmt ELSE compound_stmt ; //o
         
         
for_stmt    : FOR expr compound_stmt;

return_stmt    : RETURN expr ',' expr//o
         | RETURN expr//o
         | RETURN ;//o
         
       
local_decl : VAR IDENT type_spec//o
             | VAR IDENT '[' LITERAL ']' type_spec;//o
             
             
expr      : (LITERAL|IDENT)
         | '(' expr ')'
         | IDENT '[' expr ']' //o
         | IDENT '(' args ')' //o
         | FMT '.' IDENT '(' args ')' //o
         | op=('-'|'+'|'--'|'++'|'!') expr  
         | left=expr op=('*'|'/'|'%') right=expr //o
         | left=expr op=('+'|'-') right=expr //o
         | left=expr op=(EQ|NE|LE|'<'|GE|'>'|AND|OR) right=expr//o
         | LITERAL ',' LITERAL //o
         | IDENT '=' expr //o
         | IDENT '[' expr ']' '=' expr;//o
args      : expr (',' expr) * //o
         | ;
         
VOID      : 'void'     ;
VAR          : 'var'   ;
FUNC      : 'func'  ;
FMT          : 'fmt'      ;
INT          : 'int'   ;
FOR          : 'for'   ;
IF       : 'if'    ;
ELSE      : 'else'  ;
RETURN    : 'return';
OR       : 'or'    ;
AND          : 'and'   ;
LE       : '<='    ;
GE       : '>='    ;
EQ       : '=='    ;
NE       : '!='    ;

IDENT     : [a-zA-Z_] 
         ( [a-zA-Z_]
         | [0-9]
         )*;
         
LITERAL       : DecimalConstant | OctalConstant | HexadecimalConstant ;

DecimalConstant    : '0' | [1-9] [0-9]* ;
OctalConstant  : '0' [0-7]* ;
HexadecimalConstant    : '0' [xX] [0-9a-fA-F]+ ;
WS       : (' '
         | '\t'
         | '\r'
         | '\n'        
         )+
   -> channel(HIDDEN)  
    ;