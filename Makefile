PREFIX		?= $(HOME)
GOAL		?= coverage

ifeq ($(TOXCORE_REPO),)
TOXCORE_REPO	:= irungentoo
endif
ifeq ($(TOXCORE_BRANCH),)
TOXCORE_BRANCH	:= master
endif

export PATH		:= $(PREFIX)/.usr/bin:$(PATH)
export PKG_CONFIG_PATH	:= $(PREFIX)/.usr/lib/pkgconfig:$(PKG_CONFIG_PATH)
export CPPFLAGS		+= -I$(PREFIX)/.usr/include -isystem $(PREFIX)/.usr/include
export LDFLAGS		+= -L$(PREFIX)/.usr/lib


COMMANDS :=		\
	scalastyle	\
	test:scalastyle	\
	checkstyle	\
	test:checkstyle	\
	publishLocal	\
	makeScripts

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
	cd projects/tox4j        && for i in bin/Jni*; do $$i; done
	git diff --exit-code

setup:
	# Install external packages from git.
	tools/git-install $(PREFIX)/.usr https://github.com/yasm            yasm	master
	tools/git-install $(PREFIX)/.usr https://git.chromium.org/webm      libvpx	master    --enable-pic --enable-shared
	tools/git-install $(PREFIX)/.usr git://git.opus-codec.org           opus	master
	tools/git-install $(PREFIX)/.usr https://github.com/jedisct1        libsodium	master
	tools/git-install $(PREFIX)/.usr https://github.com/$(TOXCORE_REPO) toxcore	$(TOXCORE_BRANCH)
	tools/git-install $(PREFIX)/.usr https://github.com/google          protobuf	master

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
