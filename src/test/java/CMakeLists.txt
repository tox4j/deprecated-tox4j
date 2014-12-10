include(UseJava)
add_custom_target(junit)
if (WIN32 AND NOT CYGWIN)
    set(SEPARATOR ";")
else ()
    set(SEPARATOR ":")
endif ()
set(TOX4J_JAR_PATH "${CMAKE_BINARY_DIR}/src/main/java/tox4j.jar")
set(JUNIT_JAR_PATH "${CMAKE_SOURCE_DIR}/lib/junit-4.12.jar")
set(HAMCREST_JAR_PATH "${CMAKE_SOURCE_DIR}/lib/hamcrest-core-1.3.jar")
function(add_junit_test TARGET_NAME CLASSPATH TEST_CLASSES)
    set(CLASSPATH "${CLASSPATH}${SEPARATOR}${TOX4J_JAR_PATH}${SEPARATOR}${CMAKE_SOURCE_DIR}/lib/protobuf-java-2.6.1.jar${SEPARATOR}${JUNIT_JAR_PATH}${SEPARATOR}${HAMCREST_JAR_PATH}")
    add_custom_target(${TARGET_NAME}
        COMMAND java -Djava.library.path=$<TARGET_FILE_DIR:${LIB_TARGET_NAME}> -classpath ${CLASSPATH} org.junit.runner.JUnitCore ${TEST_CLASSES}
    )

endfunction(add_junit_test)

set(TEST_FILES
    im/tox/tox4j/Tox4jTest.java
    im/tox/tox4j/ToxSimpleChatTest.java
)

set(TEST_CLASSES
    im.tox.tox4j.Tox4jTest
)

set(CMAKE_JAVA_INCLUDE_PATH "${TOX4J_JAR_PATH}" "${JUNIT_JAR_PATH}")
add_jar(unit-tests ${TEST_FILES})
add_dependencies(unit-tests ${LIB_TARGET_NAME})
add_junit_test(test-tox4j "${CMAKE_CURRENT_BINARY_DIR}/unit-tests.jar" ${TEST_CLASSES})
add_dependencies(test-tox4j unit-tests)
add_dependencies(junit test-tox4j)