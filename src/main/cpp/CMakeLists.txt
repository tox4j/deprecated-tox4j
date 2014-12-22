# If we are crosscompiling, we have to add our own jni headers.
if(NOT DEFINED CMAKE_TOOLCHAIN_FILE)
	find_package(JNI REQUIRED)
	set(NEED_JNI_MD y)
else()
	# On most platforms, there is a jni_md.h that is included by jni.h. On other platforms (e.g. Android), jni_md.h does not exist.
	# If we are crosscompiling for such a platform, we have to set -DNEED_JNI_MD=n, so it is not included.
	if(NOT DEFINED NEED_JNI_MD)
		set(NEED_JNI_MD y)
	endif()
	if(NOT DEFINED JNI_H)
		message(FATAL_ERROR "Need to specify jni.h location when cross-compiling. Please specify location with -DJNI_H=/path/to/jni.h")
	endif()
	if(NOT DEFINED JNI_MD_H)
		if(NOT ${NEED_JNI_MD} MATCHES n)
			message(FATAL_ERROR "Need to specify jni_md.h location when cross-compiling. Please specify location with -DJNI_MD_H=/path/to/jni_md.h. When building for a platform without jni_md.h (e.g. Android), set -DNEED_JNI_MD=n")
		endif()
	endif()
	set(JAVA_INCLUDE_PATH ${JNI_H})
	if(NOT ${NEED_JNI_MD} MATCHES n)
		set(JAVA_INCLUDE_PATH2 ${JNI_MD_H})
	endif()
endif()
find_package(PkgConfig REQUIRED)
pkg_check_modules(LIBTOX REQUIRED libtoxcore)
pkg_check_modules(LIBTOXAV REQUIRED libtoxav)
# Depending on whether we need jni_md.h or not, define the include directories
if(${NEED_JNI_MD} MATCHES "y")
	include_directories(
		"${JNI_HEADER_LOCATION}"
		"${JAVA_INCLUDE_PATH}"
		"${JAVA_INCLUDE_PATH2}"
		"${LIBTOX_INCLUDE_DIRS}"
		"${LIBTOXAV_INCLUDE_DIRS}"
		"${CMAKE_CURRENT_SOURCE_DIR}"
		)
else()
	include_directories(
		"${JNI_HEADER_LOCATION}"
		"${JAVA_INCLUDE_PATH}"
		"${LIBTOX_INCLUDE_DIRS}"
		"${LIBTOXAV_INCLUDE_DIRS}"
		"${CMAKE_CURRENT_SOURCE_DIR}"
		)
endif()

include_directories(${CMAKE_CURRENT_BINARY_DIR})
PROTOBUF_GENERATE_CPP(PROTO_SRCS PROTO_HDRS
	${CMAKE_SOURCE_DIR}/src/main/protobuf/events.proto
)
link_directories(${LIBTOX_LIBRARY_DIRS} ${LIBTOXAV_LIBRARY_DIRS})
add_library(
	${LIB_TARGET_NAME}
	SHARED
	ToxCore/clientinfo.cpp
	ToxCore/connection.cpp
	ToxCore/custom.cpp
	ToxCore/filetransfer.cpp
	ToxCore/friendlist.cpp
	ToxCore/interaction.cpp
	ToxCore/lifecycle.cpp
	ToxCore/loadsave.cpp
	ToxCore/playground.cpp
	tox/core.cpp
	tox4j/ErrorHandling.cpp
	tox4j/Tox4j.cpp
	${PROTO_SRCS}
	${PROTO_HDRS}
)
if(CMAKE_SYSTEM_NAME STREQUAL Windows)
	set(WS2_32 ws2_32)
endif()
target_link_libraries(
	${LIB_TARGET_NAME}
	${LIBTOX_LIBRARIES}
	${LIBTOXAV_LIBRARIES}
	${PROTOBUF_LITE_LIBRARIES}
	${WS2_32}
	)
set_target_properties(${LIB_TARGET_NAME}
	PROPERTIES OUTPUT_NAME tox4j
)
add_dependencies(${LIB_TARGET_NAME} ${JAR_TARGET_NAME})
if(${CMAKE_SYSTEM_NAME} MATCHES "Windows")
	set_target_properties(${LIB_TARGET_NAME} PROPERTIES PREFIX "")
endif()
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -pedantic -fno-exceptions -fno-rtti -DGOOGLE_PROTOBUF_NO_RTTI -Wl,-z,defs")
