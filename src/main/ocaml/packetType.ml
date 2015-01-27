type ('a, 'b) choice =
  | A of 'a
  | B of 'b

type (_, _, _) packet =
  (* Constants *)
  | Constant
    :  ('enctx -> Output.t -> 'enctx) *
       ('dectx -> Input.t -> 'dectx * Input.t) *
       ('enctx, 'dectx, 'a) packet
    -> ('enctx, 'dectx, 'a) packet

  (* Data *)
  | Data
    :  ('enctx -> Output.t -> 'a -> 'enctx) *
       ('dectx -> Input.t -> 'dectx * Input.t * 'a)
    -> ('enctx, 'dectx, 'a) packet

  (* List of packet formats *)
  | Cons
    :  ('enctx, 'dectx, 'a) packet *
       ('enctx, 'dectx, 'b) packet
    -> ('enctx, 'dectx, 'a * 'b) packet

  (* Other operators *)
  | Repeated
    :  ('enctx, 'dectx, int) packet *
       ('enctx, 'dectx, 'b) packet
    -> ('enctx, 'dectx, 'b list) packet

  | Choice
    :  ('enctx, 'dectx, 'a) packet *
       ('enctx, 'dectx, 'b) packet
    -> ('enctx, 'dectx, ('a, 'b) choice) packet


let ( @:: ) a b = Cons (a, b)
let ( @<< ) a b = (a, b)
