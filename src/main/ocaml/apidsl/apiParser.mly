%{
open ApiAst
%}

%token EOF

%token<string> MACRO

%token COMMENT_START COMMENT_START_BIG COMMENT_BREAK COMMENT_END
%token<string> COMMENT

%token VAR_START VAR_END

%token CLASS
%token CONST
%token ENUM
%token ERROR
%token EVENT
%token FOR
%token INLINE
%token NAMESPACE
%token SIZEOF
%token STATIC
%token STRUCT
%token THIS
%token TYPEDEF
%token WITH

%token STAR LBRACE RBRACE LBRACK RBRACK LSQBRACK RSQBRACK
%token PLUS EQ LE
%token COMMA SEMICOLON

%token<int> NUMBER
%token<string> LNAME UNAME

%left PLUS

%start				parse_api
%type<string ApiAst.decl list>	parse_api

%%

parse_api
	: declarations EOF
		{ $1 }


declarations
	: declaration_list
		{ List.rev $1 }

declaration_list
	: declaration
		{ [$1] }
	| declaration_list declaration
		{ $2 :: $1 }


declaration
	: class_decl		{ $1 }
	| macro_decl		{ $1 }
	| event_decl		{ $1 }
	| get_set_decl		{ $1 }
	| member_decl		{ $1 }
	| struct_decl		{ $1 }
	| error_decl		{ $1 }
	| enum_decl		{ $1 }
	| const_decl		{ $1 }
	| function_decl		{ $1 }
	| namespace_decl	{ $1 }
	| comment_section	{ $1 }
	| comment_block declaration
		{ Decl_Comment ($1, $2) }
	| INLINE declaration
		{ Decl_Inline $2 }
	| STATIC declaration
		{ Decl_Static $2 }


macro_decl
	: MACRO
		{ Decl_Macro (Macro $1) }


event_decl
	: EVENT lname LBRACE typedef_decl RBRACE
		{ Decl_Event ($2, [$4]) }


typedef_decl
	: comment_block TYPEDEF type_name parameter_list SEMICOLON
		{ Decl_Comment ($1, Decl_Typedef ($3, "cb", $4)) }


get_set_decl
	: type_name lname LBRACE declarations RBRACE
		{ Decl_GetSet ($1, $2, $4) }
	| type_name THIS LBRACE declarations RBRACE
		{ Decl_GetSet ($1, "this", $4) }


member_decl
	: type_name lname SEMICOLON
		{ Decl_Member ($1, $2) }


struct_decl
	: STRUCT THIS LBRACE declarations RBRACE
		{ Decl_Struct ("this", $4) }
	| STRUCT THIS SEMICOLON
		{ Decl_Struct ("this", []) }


error_decl
	: ERROR FOR lname LBRACE enumerators RBRACE
		{ Decl_Error ($3, $5) }


enum_decl
	: ENUM bool_opt(CLASS) uname LBRACE enumerators RBRACE
		{ Decl_Enum ($2, $3, $5) }

enumerators
	: enumerator_list
		{ List.rev $1 }

enumerator_list
	: enumerator
		{ [$1] }
	| enumerator_list enumerator
		{ $2 :: $1 }

enumerator
	: comment_block_opt uname COMMA
		{ Enum_Name ($1, $2) }
	| NAMESPACE uname LBRACE enumerators RBRACE
		{ Enum_Namespace ($2, $4) }


const_decl
	: CONST uname EQ expression SEMICOLON
		{ Decl_Const ($2, $4) }


expression
	: NUMBER
		{ E_Number $1 }
	| uname
		{ E_UName $1 }
	| SIZEOF LBRACK lname RBRACK
		{ E_Sizeof $3 }
	| expression PLUS expression
		{ E_Plus ($1, $3) }


function_decl
	: function_name parameter_list error_list
		{ Decl_Function (fst $1, snd $1, $2, $3) }

function_name
	: type_name lname
		{ ($1, $2) }
	| lname
		{ (Ty_Auto, $1) }

error_list
	: SEMICOLON
		{ Err_None }
	| LBRACE enumerators RBRACE
		{ Err_List $2 }
	| WITH ERROR FOR lname SEMICOLON
		{ Err_From $4 }


parameter_list
	: LBRACK RBRACK
		{ [] }
	| LBRACK parameters RBRACK
		{ List.rev $2 }

parameters
	: parameter
		{ [$1] }
	| parameters COMMA parameter
		{ $3 :: $1 }

parameter
	: type_name lname
		{ Param ($1, $2) }


class_decl
	: CLASS lname EQ declarations EOF
		{ Decl_Class ($2, $4) }
	| CLASS lname LBRACE declarations RBRACE
		{ Decl_Class ($2, $4) }


namespace_decl
	: NAMESPACE lname LBRACE declarations RBRACE
		{ Decl_Namespace ($2, $4) }


type_name
	: uname
		{ Ty_UName $1 }
	| lname
		{ Ty_LName $1 }
	| lname STAR
		{ Ty_Pointer (Ty_LName $1) }
	| lname LSQBRACK size_spec RSQBRACK
		{ Ty_Array ($1, $3) }
	| THIS
		{ Ty_Pointer (Ty_LName "this") }
	| CONST type_name
		{ Ty_Const $2 }

size_spec
	: lname
		{ Ss_LName $1 }
	| uname
		{ Ss_UName $1 }
	| lname LE uname
		{ Ss_Bounded ($1, $3) }


comment_block_opt
	: /* empty */
		{ Cmt_None }
	| comment_block
		{ $1 }

comment_block
	: COMMENT_START comments COMMENT_END
		{ Cmt_Doc (List.rev $2) }


comment_section
	: COMMENT_START_BIG comments COMMENT_END
		{ Decl_Section (List.rev $2) }

comments
	: comment
		{ [$1] }
	| comments comment
		{ $2 :: $1 }

comment
	: COMMENT
		{ Cmtf_Doc $1 }
	| lname
		{ Cmtf_Var [Var_LName $1] }
	| uname
		{ Cmtf_Var [Var_UName $1] }
	| VAR_START var VAR_END
		{ Cmtf_Var (List.rev $2) }
	| VAR_START EVENT var VAR_END
		{ Cmtf_Var (Var_Event :: List.rev $3) }
	| COMMENT_BREAK
		{ Cmtf_Break }


var
	: var_body
		{ [$1] }
	| var var_body
		{ $2 :: $1 }


var_body
	: lname
		{ Var_LName $1 }
	| uname
		{ Var_UName $1 }


lname: LNAME { $1 }
uname: UNAME { $1 }


bool_opt(rule)
	: rule
		{ true }
	| /* empty */
		{ false }
