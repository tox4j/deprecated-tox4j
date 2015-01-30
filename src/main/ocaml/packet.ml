open Core.Std

exception Format_error


let unpack_repeated packet ~size ~f =
  let open Or_error in
  let rec loop acc = function
    | 0 ->
        return (List.rev acc)
    | n ->
        f packet >>= fun v ->
        loop (v :: acc) (n - 1)
  in

  let len = size packet in
  loop [] len
