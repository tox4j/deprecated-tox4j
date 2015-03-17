let parse file =
  let fh = open_in file in
  let lexbuf = Lexing.from_channel fh in

  let state = ApiLexer.state () in
  let api = ApiParser.parse_api (ApiLexer.token state) lexbuf in

  close_in fh;

  api


let () =
  let api = parse "tox.h" in

  let symtab = ExtractSymbols.extract api in

  (*print_endline @@ SymbolTable.show symtab;*)

  let api =
    api
    |> ScopeBinding.transform symtab
    |> ScopeBinding.inverse symtab
  in

  let capi =
    api
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
