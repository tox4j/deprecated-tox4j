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
    |> ErrorNULL.transform
    |> ErrorOK.transform
    |> GetSetParams.transform
    |> LengthParams.transform
    |> ThisParams.transform
    |> ErrorSplitFromFunction.transform
    |> (fun api -> ExtractSymbols.extract api, api)
    |> ScopeBinding.transform
    |> EventRename.transform
    |> EventApply.transform
    |> ErrorEnumsRename.transform
    |> GetSetRename.transform
    |> GetSetFlatten.transform
    |> StaticApply.transform
    |> StructTypes.transform
    |> ClassToNamespace.transform
    |> NamespaceApplyEvents.transform
    |> NamespaceApply.transform 1
    |> NamespaceFlatten.transform 1
    |> ErrorEnumsAddERR.transform
    |> ErrorEnums.transform
    |> ErrorParams.transform
    |> EventFunction.transform
    |> EventCloneFunctionName.transform
    |> EventParams.transform
    |> EventComments.transform
    |> EventFlatten.transform
    |> NamespaceApply.transform 0
    |> NamespaceFlatten.transform 0
    |> EnumNamespaceApply.transform
    |> EnumNamespaceFlatten.transform
    |> EnumApply.transform
    |> ArrayToPointer.transform
    |> StaticElide.transform
    |> Constants.transform
    |> ScopeBinding.Inverse.transform
    |> StringToCharP.transform
  in

  (*print_endline (ApiAst.show_decls api);*)

  print_endline "\
/* tox.h
 *
 * The Tox public API.
 *
 *  Copyright (C) 2013 Tox project All Rights Reserved.
 *
 *  This file is part of Tox.
 *
 *  Tox is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Tox is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Tox.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

#ifndef TOX_H
#define TOX_H

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern \"C\" {
#endif";

  Format.fprintf Format.std_formatter "%a\n"
    ApiCodegen.cg_decls api;
  print_endline "
#include \"tox_old.h\"

#ifdef __cplusplus
}
#endif

#endif"
;;


let () =
  (*Printexc.record_backtrace true;*)
  main ()
