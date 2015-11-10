#!/bin/sh

set -ex

TOX4J_TARGET=${1-arm-linux-androideabi}

COMPILER=clang3.6

unset CPPFLAGS
unset LDFLAGS

cd `dirname $0`

if [ -z "$ANDROID_NDK_HOME" ]; then
  export ANDROID_NDK_HOME=$HOME/android-ndk
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
    git checkout $3
    git reset --hard origin/$3
  else
    git clone "$1/$2".git -b $3 $4
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
  CLONE https://chromium.googlesource.com/webm libvpx master
  patch -p1 < ../libvpx.patch

  export AR="$TOOLCHAIN/bin/$TOX4J_TARGET-ar"
  export CC="$TOOLCHAIN/bin/$TOX4J_TARGET-gcc"
  export CXX="$TOOLCHAIN/bin/$TOX4J_TARGET-g++"
  mkdir build-android && cd build-android
  ../configure                          \
    --target=$PLATFORM_VPX              \
    --prefix=$TOOLCHAIN/sysroot/usr     \
    --disable-examples                  \
    --disable-unit-tests                \
    --disable-vp9                       \
    --sdk-path=$ANDROID_NDK_HOME || (cat config.log && false)
  make -j8
  make install
)
# libsodium
(
  CLONE https://github.com/jedisct1 libsodium stable --depth=1
  INSTALL sodium --disable-pie
)
# opus
(
  CLONE git://git.xiph.org opus master --depth=1
  INSTALL opus
)
# toxcore
(
  CLONE https://github.com/irungentoo toxcore master --depth=1
  patch -p1 < ../toxcore.patch
  INSTALL toxcore --disable-rt --disable-testing --disable-tests
)
# protobuf
(
  CLONE https://github.com/google protobuf master
  #git checkout tags/v2.6.1
  #git checkout tags/v2.5.0
  #git checkout tags/v2.4.1
  patch -p1 < ../protobuf.patch
  INSTALL protobuf --with-protoc=protoc
)
