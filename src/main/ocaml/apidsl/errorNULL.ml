open ApiAst
open ApiMap


let map_error_list v state = function
  | Err_List (Enum_Name (Cmt_None, "NULL") :: enumerators) ->
      let comment =
        Cmt_Doc [
          Cmtf_Break;
          Cmtf_Doc " One of the arguments to the function was NULL when it \
                    was not expected.";
        ]
      in
      let enumerators = Enum_Name (comment, "NULL") :: enumerators in
      Err_List enumerators

  | error_list ->
      visit_error_list v state error_list


let v = {
  default with
  map_error_list;
}


let transform decls =
  visit_decls v () decls
