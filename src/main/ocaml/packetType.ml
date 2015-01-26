type ('a, 'b) choice =
  | A of 'a
  | B of 'b

type (_, _) packet =
  (* Constants *)
  | Constant
    :  ('c -> Output.t -> unit) * ('c -> Input.t -> 'c * Input.t) * ('c, 'b) packet
    -> ('c, 'b) packet

  (* Data *)
  | Data
    : ('c -> Output.t -> 'a -> unit) * ('c -> Input.t -> 'c * Input.t * 'a)
    -> ('c, 'a) packet

  (* List of packet formats *)
  | Cons
    : ('c, 'a) packet * ('c, 'b) packet
    -> ('c, 'a * 'b) packet

  (* Other operators *)
  | Repeated
    : ('c, int) packet * ('c, 'b) packet
    -> ('c, 'b list) packet

  | Choice
    : ('c, 'a) packet * ('c, 'b) packet
    -> ('c, ('a, 'b) choice) packet


let ( @:: ) a b = Cons (a, b)
let ( @<< ) a b = (a, b)
