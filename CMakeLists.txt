cmake_minimum_required (VERSION 2.8.12)
project (tox4j)

find_package(Java REQUIRED)

set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/cmake/Modules" ${CMAKE_MODULE_PATH})
# Version
set(TOX4J_VERSION 0.0.1)
# JNI Header information
set(JNI_HEADER_LOCATION "${CMAKE_BINARY_DIR}/header")
set(JNI_HEADER_NAME "JTox.h")
# Target name for .jar
set(JAR_TARGET_NAME tox4j)
# Target name for shared library
set(LIB_TARGET_NAME tox4j-libs)

find_package(CXX11 REQUIRED)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${CXX11_FLAGS}")

find_package(Protobuf REQUIRED)
set(PROTOBUF_VERSION "2.6.1")
option(BUILD_JAVADOC "Enable building Javadoc HTML" OFF)

if (DEFINED PROTOBUF_DIRECTORY)
	get_filename_component(PROTOBUF_FULL "${PROTOBUF_DIRECTORY}/protobuf-java-${PROTOBUF_VERSION}.jar" ABSOLUTE)
else ()
	set(PROTOBUF_FULL "${CMAKE_SOURCE_DIR}/lib/protobuf-java-${PROTOBUF_VERSION}.jar")
endif()
if (NOT EXISTS "${PROTOBUF_FULL}")
	message(FATAL_ERROR "protobuf-java-${PROTOBUF_VERSION}.jar not found in path: ${PROTOBUF_FULL}")
endif()
# Subdirectories
add_subdirectory(src/main/java)
add_subdirectory(src/main/cpp)
add_subdirectory(src/test/java)
# add_subdirectory(src/test/cpp)
