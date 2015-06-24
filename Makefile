PREFIX	?= $(HOME)
GOAL	?= coverage

export PATH		:= $(PREFIX)/.usr/bin:$(PATH)
export PKG_CONFIG_PATH	:= $(PREFIX)/.usr/lib/pkgconfig:$(PKG_CONFIG_PATH)
export CPPFLAGS		+= -I$(PREFIX)/.usr/include -isystem $(PREFIX)/.usr/include
export LDFLAGS		+= -L$(PREFIX)/.usr/lib


COMMANDS :=		\
	scalastyle	\
	test:scalastyle	\
	checkstyle	\
	test:checkstyle	\
	publishLocal

ifeq ($(GOAL),coverage)
COMMANDS += coverage test
endif

ifeq ($(GOAL),performance)
COMMANDS += "testOnly *TravisBenchSuite"
endif

install:
	cd projects/build-basic  && sbt -batch publishLocal coverage test
	cd projects/build-extra  && sbt -batch $(COMMANDS)
	cd projects/linters      && sbt -batch $(COMMANDS)
	cd projects/tox4j        && sbt -batch $(COMMANDS)
	git diff --exit-code

setup:
	# Install external packages from git.
	tools/git-install $(PREFIX)/.usr https://github.com/yasm       yasm
	tools/git-install $(PREFIX)/.usr https://git.chromium.org/webm libvpx    --enable-pic --enable-shared
	tools/git-install $(PREFIX)/.usr git://git.opus-codec.org      opus
	tools/git-install $(PREFIX)/.usr https://github.com/jedisct1   libsodium
	tools/git-install $(PREFIX)/.usr https://github.com/irungentoo toxcore
	tools/git-install $(PREFIX)/.usr https://github.com/google     protobuf

setup-host: setup

setup-arm-linux-androideabi: setup
	tools/ndk-install $(PREFIX)
	projects/tox4j/android/build-deps.sh

clean:
	rm -rf projects/*/project/project
	rm -rf projects/*/project/target
	rm -rf projects/*/target

show:
	@echo "PREFIX   = $(PREFIX)"
	@echo "GOAL     = $(GOAL)"
	@echo "COMMANDS = $(COMMANDS)"
