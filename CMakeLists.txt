cmake_minimum_required(VERSION 2.8.7)
enable_testing()

if (NOT DEFINED CMAKE_MACOSX_RPATH)
	set(CMAKE_MACOSX_RPATH true)
endif()

# Non-pkg-config libraries.
link_libraries(glog)

# pkg-config requirements from SBT.
include(${DEPENDENCIES_FILE})

subdirs(src/main/cpp)
subdirs(src/test/cpp)

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3")
