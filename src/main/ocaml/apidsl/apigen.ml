let parse file =
  let fh = open_in file in
  let lexbuf = Lexing.from_channel fh in

  let state = ApiLexer.state () in
  let api = ApiParser.parse_api (ApiLexer.token state) lexbuf in

  close_in fh;

  api


let () =
  let api = parse "tox.h" in

  let api =
    api
    |> GetSetParams.transform
    |> LengthParams.transform
    |> ThisParams.transform
    |> (fun api -> ExtractSymbols.extract api, api)
    |> ScopeBinding.transform
    |> GetSetNames.transform
    |> SplitErrorCodes.transform
    |> ApplyStatic.transform
    |> StructTypes.transform
    |> ClassToNamespace.transform
    |> ApplyNamespaces.transform 1
    |> FlattenNamespaces.transform 1
    |> Constants.transform
    |> ScopeBinding.Inverse.transform
  in

  (*print_endline (ApiAst.show_decls api);*)

  Format.fprintf Format.std_formatter "%a\n"
    ApiCodegen.cg_decls api
