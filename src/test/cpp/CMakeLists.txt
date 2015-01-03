add_library(gtest STATIC
        /usr/src/gtest/src/gtest-all
)

link_libraries(glog gtest pthread sodium tox4j ev)
include_directories(/usr/src/gtest)

add_executable(tox_core_test
        main.cpp
        tox/core/CryptoBox_test.cpp
        tox/core/Nonce_test.cpp
        tox/core/UDP_test.cpp
)
add_test(tox_core_test tox_core_test)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fexceptions")
