{
  open ApiParser
  open Lexing

  exception Lexing_error of position * string

  type scan_state =
    | LexNormal
    | LexComment
    | LexVariable

  type state = {
    mutable state : scan_state;
    mutable nesting : int;
  }
}

let lower	= ['a'-'z']
let upper	= ['A'-'Z']

let digit	= ['0'-'9']

let lname	= lower (digit | lower | '_')*
let uname	= upper (digit | upper | '_')*


let ws	= [' ' '\t' '\r']


rule eoi = parse
| _					{ EOF }


and normal state =
  parse
| '\n'					{ normal state lexbuf }
| ' '					{ normal state lexbuf }
| "//" [^'\n']*				{ normal state lexbuf }

| "#define " uname ([^'\n']|"\\\n")+	{ MACRO (Lexing.lexeme lexbuf) }

| "/**"					{ state.state <- LexComment; COMMENT_START }
| "/**" '*'+				{ state.state <- LexComment; COMMENT_START_BIG }

| "class"				{ CLASS }
| "const"				{ CONST }
| "enum"				{ ENUM }
| "error"				{ ERROR }
| "event"				{ EVENT }
| "for"					{ FOR }
| "inline"				{ INLINE }
| "namespace"				{ NAMESPACE }
| "sizeof"				{ SIZEOF }
| "static"				{ STATIC }
| "struct"				{ STRUCT }
| "this"				{ THIS }
| "typedef"				{ TYPEDEF }
| "with"				{ WITH }

| "*"					{ STAR }
| "{"					{ LBRACE }
| "}"					{ RBRACE }
| "("					{ LBRACK }
| ")"					{ RBRACK }
| "["					{ LSQBRACK }
| "]"					{ RSQBRACK }
| "+"					{ PLUS }
| "<="					{ LE }
| "="					{ EQ }
| ","					{ COMMA }
| ";"					{ SEMICOLON }

| digit+ as s				{ NUMBER (int_of_string s) }
| lname as s				{ LNAME s }
| uname as s				{ UNAME s }

| eof					{ EOF }
| _ as c				{ raise (Lexing_error (lexeme_start_p lexbuf, Char.escaped c)) }


and comment state =
  parse
| "$" (lname as s)			{ LNAME s }
| "$" (uname as s)			{ UNAME s }
| "${"					{ state.state <- LexVariable; VAR_START }

| [^'$''\n']+ as c			{ COMMENT c }
| '\n' ' '+ '*'				{ COMMENT_BREAK }
| '\n' ' '+ '*'+ '/'			{ state.state <- LexNormal; COMMENT_END }
| _ as c				{ raise (Lexing_error (lexeme_start_p lexbuf, Char.escaped c)) }


and variable state =
  parse
| '.'					{ variable state lexbuf }
| "}"					{ state.state <- LexComment; VAR_END }
| "event "				{ EVENT }
| lname as s				{ LNAME s }
| uname as s				{ UNAME s }
| _ as c				{ raise (Lexing_error (lexeme_start_p lexbuf, Char.escaped c)) }


{
  let state () = {
    state = LexNormal;
    nesting = 0;
  }

  let string_of_token = let open ApiParser in
    function
    | EOF -> "EOF"

    | MACRO s -> "MACRO " ^ (String.escaped s)

    | COMMENT_START -> "COMMENT_START"
    | COMMENT_START_BIG -> "COMMENT_START_BIG"
    | COMMENT_BREAK -> "COMMENT_BREAK"
    | COMMENT_END -> "COMMENT_END"
    | COMMENT c -> "COMMENT " ^ c

    | CLASS -> "CLASS"
    | CONST -> "CONST"
    | ENUM -> "ENUM"
    | ERROR -> "ERROR"
    | EVENT -> "EVENT"
    | FOR -> "FOR"
    | INLINE -> "INLINE"
    | NAMESPACE -> "NAMESPACE"
    | SIZEOF -> "SIZEOF"
    | STATIC -> "STATIC"
    | STRUCT -> "STRUCT"
    | THIS -> "THIS"
    | TYPEDEF -> "TYPEDEF"
    | WITH -> "WITH"

    | STAR -> "STAR"
    | LBRACE -> "LBRACE"
    | RBRACE -> "RBRACE"
    | LBRACK -> "LBRACK"
    | RBRACK -> "RBRACK"
    | LSQBRACK -> "LSQBRACK"
    | RSQBRACK -> "RSQBRACK"
    | PLUS -> "PLUS"
    | EQ -> "EQ"
    | LE -> "LE"
    | COMMA -> "COMMA"
    | SEMICOLON -> "SEMICOLON"

    | VAR_START -> "VAR_START"
    | VAR_END -> "VAR_END"

    | NUMBER s -> "NUMBER " ^ string_of_int s
    | LNAME s -> "LNAME " ^ s
    | UNAME s -> "UNAME " ^ s


  let token state lexbuf =
    let token =
      match state.state with
      | LexNormal -> normal state lexbuf
      | LexComment -> comment state lexbuf
      | LexVariable -> variable state lexbuf
    in
    (*print_endline (string_of_token token);*)
    token
}
