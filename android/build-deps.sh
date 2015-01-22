#!/bin/sh

set -ex

if [ -z "$ANDROID_NDK_HOME" ]; then
  export ANDROID_NDK_HOME=$HOME/usr/android-ndk
fi

export TOOLCHAIN=$PWD/arm-linux-androideabi
export PKG_CONFIG_PATH=$TOOLCHAIN/sysroot/usr/lib/pkgconfig
if [ -d /usr/lib/jvm/default-java ]; then
  export JAVA_HOME=/usr/lib/jvm/default-java
fi
export PATH="$PATH:$TOOLCHAIN/bin"

CLONE() {
  if [ -d $2 ]; then
    cd $2
    git fetch
    git clean -fdx
    git reset --hard
    git checkout master
    git reset --hard origin/master
  else
    git clone "$1/$2".git $3
    cd $2
  fi
}

INSTALL() {
  PACKAGE=$1
  shift

  autoreconf -fi

  export PKG_CONFIG_PATH=$TOOLCHAIN/sysroot/usr/lib/pkgconfig
  mkdir build-android && cd build-android
  ../configure                            \
    --host=arm-linux-androideabi          \
    --prefix=$TOOLCHAIN/sysroot/usr       \
    --with-sysroot=$TOOLCHAIN/sysroot     \
    --disable-shared                      \
    --disable-soname-versions             \
    "$@"
  make -j8
  make install
}

# toolchain
{
  rm -rf $TOOLCHAIN
  "$ANDROID_NDK_HOME/build/tools/make-standalone-toolchain.sh"  \
    --ndk-dir="$ANDROID_NDK_HOME"                               \
    --toolchain=arm-linux-androideabi-clang3.5                  \
    --install-dir=$TOOLCHAIN                                    \
    --platform=android-9
}
# libev
(
  rm -rf libev
  cvs -z3 -d :pserver:anonymous@cvs.schmorp.de/schmorpforge co libev
  cd libev
  INSTALL libev CFLAGS=-DEV_USE_SELECT=0
)
# libsodium
(
  CLONE https://github.com/jedisct1 libsodium --depth=1
  INSTALL sodium --disable-pie
)
# kalium-jni
(
  CLONE https://github.com/joshjdevl kalium-jni --depth=1
  patch -p1 < ../kalium-jni.patch
  swig -java -package org.abstractj.kalium -outdir src/main/java/org/abstractj/kalium jni/sodium.i

  $ANDROID_NDK_HOME/ndk-build

  mvn -Dmaven.test.skip=true clean install
)
# opus
(
  CLONE git://git.opus-codec.org opus --depth=1
  INSTALL opus
)
# libvpx
(
  CLONE http://git.chromium.org/webm libvpx
  patch -p1 < ../libvpx.patch

  mkdir build-android && cd build-android
  ../configure                          \
    --target=armv7-android-gcc          \
    --prefix=$TOOLCHAIN/sysroot/usr     \
    --disable-examples                  \
    --disable-unit-tests                \
    --disable-vp9                       \
    --sdk-path=$ANDROID_NDK_HOME
  make -j8
  make install
)
# toxcore
(
  CLONE https://github.com/irungentoo toxcore --depth=1
  INSTALL toxcore --disable-rt --disable-testing --disable-tests
)
# protobuf
(
  CLONE https://github.com/google protobuf
  #git checkout tags/v2.6.1
  git checkout 0c995c930067241b40b10b0b01504c784cade03e
  patch -p1 < ../protobuf.patch
  INSTALL protobuf --with-protoc=protoc
)
