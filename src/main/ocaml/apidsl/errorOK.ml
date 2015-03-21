open ApiAst
open ApiMap


let map_error_list v state = function
  | Err_List enumerators ->
      let comment =
        Cmt_Doc [
          Cmtf_Break;
          Cmtf_Doc " The function returned successfully.";
        ]
      in
      let enumerators = Enum_Name (comment, "OK") :: enumerators in
      Err_List enumerators

  | error_list ->
      visit_error_list v state error_list


let v = {
  default with
  map_error_list;
}


let transform decls =
  visit_decls v () decls
