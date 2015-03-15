let parse file =
  let fh = open_in file in
  let lexbuf = Lexing.from_channel fh in

  let state = ApiLexer.state () in
  let api = ApiParser.parse_api (ApiLexer.token state) lexbuf in

  close_in fh;

  api


let () =
  let api = parse "tox.h" in

  (*print_endline (ApiAst.show_decls api);*)

  Format.fprintf Format.std_formatter "%a\n"
    ApiPp.pp_decls api
