type 'a id = int [@@deriving show]

type uname = UName of uname id * string [@@deriving show]
type lname = LName of lname id * string [@@deriving show]
type macro = Macro of string [@@deriving show]


type comment_fragment =
  | Cmtf_Doc of string
  | Cmtf_UName of uname
  | Cmtf_LName of lname
  | Cmtf_Break
  [@@deriving show]


type comment =
  | Cmt_None
  | Cmt_Doc of comment_fragment list
  | Cmt_Section of comment_fragment list
  [@@deriving show]


type size_spec =
  | Ss_UName of uname
  | Ss_LName of lname
  | Ss_Size
  | Ss_Bounded of size_spec * uname
  [@@deriving show]


type type_name =
  | Ty_UName of uname
  | Ty_LName of lname
  | Ty_Array of lname * size_spec
  | Ty_This
  | Ty_Const of type_name
  [@@deriving show]


type enumerator =
  | Enum_Name of comment * uname
  | Enum_Namespace of uname * enumerator list
  [@@deriving show]


type error_list =
  | Err_None
  | Err_From of lname
  | Err_List of enumerator list
  [@@deriving show]


type parameter =
  | Param of type_name * lname
  [@@deriving show]


type function_name =
  | Fn_Custom of type_name * lname
  | Fn_Size
  | Fn_Get
  | Fn_Set
  [@@deriving show]


type expr =
  | E_Number of int
  | E_UName of uname
  | E_Sizeof of lname
  | E_Plus of expr * expr
  [@@deriving show]


type decl =
  | Decl_Comment of comment * decl
  | Decl_Static of decl
  | Decl_Macro of macro
  | Decl_Namespace of lname * decl list
  | Decl_Class of lname * decl list
  | Decl_Function of function_name * parameter list * error_list
  | Decl_Const of uname * expr
  | Decl_Enum of bool * uname * enumerator list
  | Decl_Error of lname * enumerator list
  | Decl_Struct of decl list
  | Decl_Member of type_name * lname
  | Decl_GetSet of type_name * lname * decl list
  | Decl_Typedef of lname * parameter list
  | Decl_Event of lname * decl
  [@@deriving show]


type decls = decl list
  [@@deriving show]
