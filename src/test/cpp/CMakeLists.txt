link_libraries(pthread sodium tox4j)

if(NOT DEFINED CMAKE_TOOLCHAIN_FILE)
  include(${TEST_FILE})
else()
  message(STATUS "Not building tests from ${TEST_FILE} due to cross-compilation")
endif()
