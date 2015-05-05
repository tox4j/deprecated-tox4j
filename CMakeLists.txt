cmake_minimum_required(VERSION 2.8.7)
enable_testing()

if (NOT DEFINED CMAKE_MACOSX_RPATH)
	set(CMAKE_MACOSX_RPATH true)
endif()

# pkg-config requirements from SBT.
include(${DEPENDENCIES_FILE})

# Include directory for our config.h.
include_directories(${CMAKE_CURRENT_BINARY_DIR}/src/main/cpp)

subdirs(src/main/cpp)
subdirs(src/test/cpp)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -include cpp14compat.h")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3")
