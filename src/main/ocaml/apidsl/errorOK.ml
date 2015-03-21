open ApiAst
open ApiMap


let map_err_list v state enumerators =
  let comment =
    Cmt_Doc [
      Cmtf_Break;
      Cmtf_Doc " The function returned successfully.";
    ]
  in
  Enum_Name (comment, "OK") :: enumerators


let map_error_list v state = function
  | Err_List (enumerators) ->
      Err_List (map_err_list v state enumerators)

  | error_list ->
      visit_error_list v state error_list


let map_decl v state = function
  | Decl_Error (lname, enumerators) ->
      Decl_Error (lname, map_err_list v state enumerators)

  | decl ->
      visit_decl v state decl


let v = {
  default with
  map_error_list;
  map_decl;
}


let transform decls =
  visit_decls v () decls
