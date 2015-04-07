[![Build Status](https://jenkins.libtoxcore.so/job/Sync%20Tox4j/badge/icon)](https://jenkins.libtoxcore.so/job/Sync%20Tox4j/)
[![Travis Build Status](https://api.travis-ci.org/tox4j/tox4j.svg)](https://travis-ci.org/tox4j/tox4j)
[![Coverage Status](https://coveralls.io/repos/tox4j/tox4j/badge.svg)](https://coveralls.io/r/tox4j/tox4j)

# tox4j

New and improved java wrapper for Tox!

## Dependencies

### Java

* sbt
* protobuf-java (This is pulled in by sbt, to be the exact same version as protoc. protoc needs to be on $PATH for this to work correctly.)

### Native code

* CMake (>= 2.8.7)
* Toxcore
* Toxav
* protobuf (The version used in development is currently 2.6.1, other versions might work as well)
* clang-3.5 (older versions of clang segfault. G++ support is untested, the build script enforces clang-3.5 for now. If you do not have clang 3.5 installed, your build may fail.)

## Building

Build and install toxcore and toxav. Run the sbt console with ```sbt```, and then use ```compile``` to build, ```test``` to run unit tests (these are a lot of tests with high timeouts, might take 40minutes or longer), and ```package``` to create a jar and the needed native library.

### Developing on OS X

Getting the required tools for development on OS X is very easy. If you have XCode installed, you will already be ready to compile the C++ part of tox4j, otherwise you will need to install a C++ compiler with brew. To get started, follow these instructions.

Execute the following commands in a Mac terminal:
```
$ cd ~
$ mkdir homebrew && curl -L https://github.com/Homebrew/homebrew/tarball/master | tar xz --strip 1 -C homebrew
```

Now, edit your `~/.bash\_profile` file and add this line to the end:
```
export PATH=$HOME/homebrew/bin:$PATH
```

Restart your terminal or `exec $SHELL`.

Install required packages:
```
$ brew install automake libtool libsodium libvpx opus pkg-config protobuf sbt 
$ mkdir -p ~/code/git
$ cd ~/code/git
$ git clone https://github.com/irungentoo/toxcore
$ cd toxcore && ./autogen.sh
$ mkdir _build && cd _build
$ ../configure --prefix=$HOME/homebrew/Cellar/toxcore/0.0.1 --with-libsodium-headers=$HOME/homebrew/include --with-libsodium-libs=$HOME/homebrew/lib
$ make && make install
$ cd ~/homebrew/include && ln -s ../../Cellar/toxcore/0.0.1/include/* .
$ cd ~/homebrew/lib && ln -s ../../Cellar/toxcore/0.0.1/lib/* .
$ cd ~/homebrew/lib/pkgconfig && ln -s ../../Cellar/toxcore/0.0.1/lib/pkgconfig/* .
```

Now, toxcore should be installed in your `$HOME`. Finally, clone and build tox4j:
```
$ cd ~/code/git
$ git clone https://github.com/tox4j/tox4j  # or your fork
$ cd tox4j
$ sbt
```

In the SBT console, run the following commands to build and test tox4j:
```
> compile
> test
```

#### Troubleshooting

If you get an error saying something about Java target versions not being correct, run `javac -version` and `java -version` and see if the version numbers are the same. If they are not, run `ls -l $(which javac)` and the same for `java` and change one of them to the newer one using `sudo ln -sf`.

If you don't have a C++ compiler (e.g. from XCode), install it with brew:
```
$ brew install llvm --with-clang
```

## Contributing

We use Upsource for code review now. Please [sign up](https://upsource.slevermann.de)!
