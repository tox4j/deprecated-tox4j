include(UseJava)
add_custom_target(junit)
if (WIN32 AND NOT CYGWIN)
    set(SEPARATOR ";")
else ()
    set(SEPARATOR ":")
endif ()

set (JUNIT_VERSION "4.12")
set (HAMCREST_VERSION "1.3")
IF (DEFINED JUNIT_DIRECTORY)
	get_filename_component(JUNIT_FULL "${JUNIT_DIRECTORY}/junit-${JUNIT_VERSION}.jar" ABSOLUTE)
else ()
	set(JUNIT_FULL "${CMAKE_SOURCE_DIR}/lib/junit-${JUNIT_VERSION}.jar")
endif()

if (NOT EXISTS "${JUNIT_FULL}")
	message(FATAL_ERROR "junit-${JUNIT_VERSION}.jar not found in path: ${JUNIT_FULL}")
endif ()

IF (DEFINED HAMCREST_DIRECTORY)
	get_filename_component(HAMCREST_FULL "${HAMCREST_DIRECTORY}/hamcrest-core-${HAMCREST_VERSION}.jar" ABSOLUTE)
else ()
	set(HAMCREST_FULL "${CMAKE_SOURCE_DIR}/lib/hamcrest-core-${HAMCREST_VERSION}.jar")
endif()

if (NOT EXISTS "${HAMCREST_FULL}")
	message(FATAL_ERROR "hamcrest-core-${HAMCREST_VERSION}.jar not found in path: ${HAMCREST_FULL}")
endif ()

set(TOX4J_JAR_PATH "${CMAKE_BINARY_DIR}/src/main/java/tox4j.jar")
function(add_junit_test TARGET_NAME CLASSPATH TEST_CLASSES)
	set(CLASSPATH "${CLASSPATH}${SEPARATOR}${TOX4J_JAR_PATH}${SEPARATOR}${PROTOBUF_FULL}${SEPARATOR}${JUNIT_FULL}${SEPARATOR}${HAMCREST_FULL}")
    add_custom_target(${TARGET_NAME}
        COMMAND java -Djava.library.path=$<TARGET_FILE_DIR:${LIB_TARGET_NAME}> -classpath ${CLASSPATH} org.junit.runner.JUnitCore ${TEST_CLASSES}
    )

endfunction(add_junit_test)

set(TEST_FILES
    im/tox/tox4j/v2/callbacks/ConnectionStatusCallbackTest.java
    im/tox/tox4j/v2/callbacks/FileReceiveCallbackTest.java
    im/tox/tox4j/v2/callbacks/FileTransferTest.java
    im/tox/tox4j/v2/callbacks/FriendActionCallbackTest.java
    im/tox/tox4j/v2/callbacks/FriendConnectedCallbackTest.java
    im/tox/tox4j/v2/callbacks/FriendMessageCallbackTest.java
    im/tox/tox4j/v2/callbacks/FriendNameCallbackTest.java
    im/tox/tox4j/v2/callbacks/FriendRequestCallbackTest.java
    im/tox/tox4j/v2/callbacks/FriendStatusCallbackTest.java
    im/tox/tox4j/v2/callbacks/FriendStatusMessageCallbackTest.java
    im/tox/tox4j/v2/callbacks/FriendTypingCallbackTest.java
    im/tox/tox4j/v2/callbacks/ReadReceiptCallbackTest.java
    im/tox/tox4j/v2/AliceBobTestBase.java
    im/tox/tox4j/v2/ToxCoreImplTest.java
    im/tox/tox4j/v2/ToxCoreTest.java
    im/tox/tox4j/v2/ToxCoreTestBase.java
)

set(TEST_CLASSES
    im.tox.tox4j.v2.ToxCoreImplTest
    im.tox.tox4j.v2.callbacks.ConnectionStatusCallbackTest
    im.tox.tox4j.v2.callbacks.FileReceiveCallbackTest
    im.tox.tox4j.v2.callbacks.FileTransferTest
    im.tox.tox4j.v2.callbacks.FriendActionCallbackTest
    im.tox.tox4j.v2.callbacks.FriendConnectedCallbackTest
    im.tox.tox4j.v2.callbacks.FriendMessageCallbackTest
    im.tox.tox4j.v2.callbacks.FriendNameCallbackTest
    im.tox.tox4j.v2.callbacks.FriendRequestCallbackTest
    im.tox.tox4j.v2.callbacks.FriendStatusCallbackTest
    im.tox.tox4j.v2.callbacks.FriendStatusMessageCallbackTest
    im.tox.tox4j.v2.callbacks.FriendTypingCallbackTest
    im.tox.tox4j.v2.callbacks.ReadReceiptCallbackTest
)

set(CMAKE_JAVA_INCLUDE_PATH "${TOX4J_JAR_PATH}" "${JUNIT_FULL}" "${HAMCREST_FULL}" "${PROTOBUF_FULL}")
add_jar(unit-tests ${TEST_FILES})
add_dependencies(unit-tests ${LIB_TARGET_NAME})
add_junit_test(test-tox4j "${CMAKE_CURRENT_BINARY_DIR}/unit-tests.jar" "${TEST_CLASSES}")
add_dependencies(test-tox4j unit-tests)
add_dependencies(junit test-tox4j)
