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
    |> ErrorSplitFromFunction.transform
    |> (fun api -> ExtractSymbols.extract api, api)
    |> ScopeBinding.transform
    |> GetSetRename.transform
    |> GetSetFlatten.transform
    |> StaticApply.transform
    |> StructTypes.transform
    |> ClassToNamespace.transform
    |> ErrorEnumsRename.transform
    |> NamespaceApply.transform 1
    |> NamespaceFlatten.transform 1
    |> ErrorEnumsAddERR.transform
    |> ErrorEnums.transform
    |> ErrorParams.transform
    |> NamespaceApply.transform 0
    |> NamespaceFlatten.transform 0
    |> EnumNamespaceApply.transform
    |> EnumNamespaceFlatten.transform
    |> EnumApply.transform
    |> Constants.transform
    |> ScopeBinding.Inverse.transform
  in

  (*print_endline (ApiAst.show_decls api);*)

  Format.fprintf Format.std_formatter "%a\n"
    ApiCodegen.cg_decls api


let () =
  (*Printexc.record_backtrace true;*)
  main ()
