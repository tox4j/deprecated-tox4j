open ApiAst

let prepend str (LName name) =
  LName (str ^ "_" ^ name)

let append (LName name) str =
  LName (name ^ "_" ^ str)
