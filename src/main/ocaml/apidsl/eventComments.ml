open ApiAst
open ApiMap


let make_event comment event_name typedef func =
  let comment =
    match comment with
    | Cmt_Doc frags ->
        let frags = [
          Cmtf_Break;
          Cmtf_Doc " Set the callback for the `";
          Cmtf_LName event_name;
          Cmtf_Doc "` event. Pass NULL to unset.";
          Cmtf_Break;
        ] @ frags in
        Cmt_Doc frags

    | Cmt_None -> Cmt_None
  in

  Decl_Event (event_name, [
      typedef;
      Decl_Comment (comment, func);
    ])


let map_decl v symtab = function

  | Decl_Event (event_name, [
      typedef;
      func;
    ]) ->
      let comment = Cmt_Doc [] in
      make_event comment event_name typedef func

  | Decl_Comment (
      comment,
      Decl_Event (event_name, [
          typedef;
          func;
        ])) ->
      make_event comment event_name typedef func

  | decl ->
      visit_decl v symtab decl


let v = { default with map_decl }


let transform (symtab, decls) =
  symtab, visit_decls v symtab decls
