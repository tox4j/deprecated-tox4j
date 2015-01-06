add_library(gtest STATIC /usr/src/gtest/src/gtest-all.cc)

link_libraries(glog gtest pthread sodium tox4j ev)
include_directories(/usr/src/gtest)

include(${TEST_FILE})
