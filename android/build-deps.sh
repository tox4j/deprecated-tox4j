#!/bin/sh

set -ex

COMPILER=clang3.6

if [ -z "$ANDROID_NDK_HOME" ]; then
  export ANDROID_NDK_HOME=$HOME/usr/android-ndk
fi

if [ -z "$TOX4J_TARGET" ]; then
   TOX4J_TARGET=arm-linux-androideabi
fi

PLATFORM=$TOX4J_TARGET
case $TOX4J_TARGET in
   arm-linux-androideabi)
      PLATFORM_NDK=$PLATFORM
      PLATFORM_VPX=armv7-android-gcc
      ;;
   i686-linux-android)
      PLATFORM_NDK=x86
      PLATFORM_VPX=x86-android-gcc
      ;;
esac

export TOOLCHAIN=$PWD/$TOX4J_TARGET
export PKG_CONFIG_PATH=$TOOLCHAIN/sysroot/usr/lib/pkgconfig
if [ -d /usr/lib/jvm/default-java ]; then
  export JAVA_HOME=/usr/lib/jvm/default-java
fi
export PATH="$TOOLCHAIN/bin:$PATH"

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

  mkdir build-android && cd build-android
  ../configure                            \
    --host=$TOX4J_TARGET                  \
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
    --toolchain=$PLATFORM_NDK-$COMPILER                         \
    --install-dir=$TOOLCHAIN                                    \
    --platform=android-9
}
# libvpx
(
  CLONE https://git.chromium.org/webm libvpx
  patch -p1 < ../libvpx.patch

  mkdir build-android && cd build-android
  ../configure                          \
    --target=$PLATFORM_VPX              \
    --prefix=$TOOLCHAIN/sysroot/usr     \
    --disable-examples                  \
    --disable-unit-tests                \
    --disable-vp9                       \
    --sdk-path=$ANDROID_NDK_HOME
  make -j8
  make install
)
# libsodium
(
  CLONE https://github.com/jedisct1 libsodium --depth=1
  INSTALL sodium --disable-pie
)
# opus
(
  CLONE git://git.opus-codec.org opus --depth=1
  INSTALL opus
)
# toxcore
(
  CLONE https://github.com/irungentoo toxcore --depth=1
  patch -p1 < ../toxcore.patch
  INSTALL toxcore --disable-rt --disable-testing --disable-tests
)
# protobuf
(
  CLONE https://github.com/google protobuf
  git checkout tags/v2.6.1
  #git checkout tags/v2.5.0
  #git checkout tags/v2.4.1
  patch -p1 < ../protobuf.patch
  INSTALL protobuf --with-protoc=protoc
)
