add_library(gtest STATIC
        /usr/src/gtest/src/gtest-all
        /usr/src/gtest/src/gtest_main
)

link_libraries(gtest pthread sodium tox4j)
include_directories(/usr/src/gtest)

add_executable(tox_core_test
        tox/core/CryptoBox_test.cpp
        tox/core/Nonce_test.cpp
)
add_test(tox_core_test tox_core_test)
