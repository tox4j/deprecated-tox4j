let parse file =
  let fh = open_in file in
  let lexbuf = Lexing.from_channel fh in

  let state = ApiLexer.state () in
  let api = ApiParser.parse_api (ApiLexer.token state) lexbuf in

  close_in fh;

  api


let () =
  let tox_api = parse "tox.h" in

  let api =
    tox_api
    |> ScopeBinding.transform
    |> ScopeBinding.inverse
  in

  let capi =
    tox_api
    |> GetSet.transform
    |> ApplyStatic.transform
    |> FlattenNamespace.transform
    |> FlattenClass.transform
    |> Constants.transform
  in

  ignore capi;

  (*print_endline (ApiAst.show_decls api);*)

  Format.fprintf Format.std_formatter "%a\n"
    ApiCodegen.cg_decls api
