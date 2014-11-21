cmake_minimum_required (VERSION 3.0)
project (tox4j)


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

# Subdirectories
add_subdirectory(src/main/java)
add_subdirectory(src/main/c)
# add_subdirectory(src/test/java)
# add_subdirectory(src/test/c)
