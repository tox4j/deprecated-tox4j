main.native: $(wildcard *.ml *.mli) Makefile
	ocamlbuild -use-ocamlfind -lflag -thread -cflag -thread -pkgs sodium,async,async_unix main.native

clean:
	ocamlbuild -clean
