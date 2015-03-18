let parse file =
  let fh = open_in file in
  let lexbuf = Lexing.from_channel fh in

  let state = ApiLexer.state () in
  let api = ApiParser.parse_api (ApiLexer.token state) lexbuf in

  close_in fh;

  api


let (|!) x msg =
  print_endline msg;
  x


let main () =
  let api = parse "tox.h" in

  let api =
    api
    |> GetSetParams.transform
    |> LengthParams.transform
    |> ThisParams.transform
    |> SplitErrorCodes.transform
    |> (fun api -> ExtractSymbols.extract api, api)
    |> ScopeBinding.transform
    |> GetSetRename.transform
    |> GetSetFlatten.transform
    |> ApplyStatic.transform
    |> StructTypes.transform
    |> ClassToNamespace.transform
    |> ErrorEnumsRename.transform
    |> ApplyNamespaces.transform 1
    |> FlattenNamespaces.transform 1
    |> ErrorEnumsAddERR.transform
    |> ErrorEnums.transform
    |> ErrorParams.transform
    |> ApplyNamespaces.transform 0
    |> FlattenNamespaces.transform 0
    |> ApplyEnums.transform
    |> Constants.transform
    |> ScopeBinding.Inverse.transform
  in

  (*print_endline (ApiAst.show_decls api);*)

  Format.fprintf Format.std_formatter "%a\n"
    ApiCodegen.cg_decls api


let () =
  (*Printexc.record_backtrace true;*)
  main ()
