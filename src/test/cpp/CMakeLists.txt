link_libraries(glog pthread sodium tox4j ev)

find_path(HAVE_GTEST glog/logging.h)
if(NOT ${HAVE_GTEST} MATCHES HAVE_GTEST-NOTFOUND)
	message(STATUS "Found gtest: ${HAVE_GTEST}")
	include(${TEST_FILE})
else()
	message(STATUS "Did not find gtest; tests from ${TEST_FILE} disabled")
endif()
