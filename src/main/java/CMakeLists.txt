find_package(Java REQUIRED)

include(UseJava)
set(PACKAGE_NAME im.tox.tox4j)

# Java output directory
set(CMAKE_JAVA_TARGET_OUTPUT_DIR "${CMAKE_CURRENT_BINARY_DIR}")

# Define directories for classfiles
set(CLASSDIR "${CMAKE_JAVA_TARGET_OUTPUT_DIR}/CMakeFiles/${JAR_TARGET_NAME}.dir")
set(CLEANFILES
	"${CLASSDIR}/im/tox/tox4j/Tox4j.class"
)

set_directory_properties(PROPERTIES ADDITIONAL_MAKE_CLEAN_FILES "${CLEANFILES}")

# Source Files
set(TOX4J_CORE
	im/tox/tox4j/Tox4j.java
)

set(TOX4J_SOURCE ${TOX4J_CORE})

set(CMAKE_JAVA_TARGET_VERSION ${TOX4J_VERSION})

add_jar(${JAR_TARGET_NAME} ${TOX4J_SOURCE})

# Set arguments for javah
set(JAVAH_ARGS
	"-classpath"
	"${CLASSDIR}"
	"-o"
	"${JNI_HEADER_LOCATION}/${JNI_HEADER_NAME}"
	"${PACKAGE_NAME}.Tox4j"
)

add_custom_command(
	TARGET ${JAR_TARGET_NAME}
	POST_BUILD
	COMMAND ${Java_JAVAH_EXECUTABLE} ARGS ${JAVAH_ARGS}
)

file(MAKE_DIRECTORY ${JNI_HEADER_LOCATION})
