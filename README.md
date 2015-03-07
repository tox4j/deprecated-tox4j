[![Build Status](https://jenkins.libtoxcore.so/job/Sync%20Tox4j/badge/icon)](https://jenkins.libtoxcore.so/job/Sync%20Tox4j/)
#tox4j


New and improved java wrapper for Tox.

##Dependencies

###Java
* sbt
* protobuf-java (This is pulled in by sbt, to be the exact same version as protoc. protoc needs to be on $PATH for this to work correctly.)

###Native code
* CMake (>= 2.8.7)
* Toxcore
* Toxav
* protobuf (The version used in development is currently 2.6.1, other versions might work as well)
* clang-3.5 (older versions of clang segfault. G++ support is untested, the build script enforces clang-3.5 for now. If you do not have clang 3.5 installed, your build may fail.)

##Building
Build and install toxcore and toxav. Run the sbt console with ```sbt```, and then use ```compile``` to build, ```test``` to run unit tests (these are a lot of tests with high timeouts, might take 40minutes or longer), and ```package``` to create a jar and the needed native library.


## Contributing

We use Upsource for code review now. Please sign up! (URL will be added later)
