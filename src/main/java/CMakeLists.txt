find_package(Java REQUIRED)

include(UseJava)
set(PACKAGE_NAME im.tox.tox4j)

# Java output directory
set(CMAKE_JAVA_TARGET_OUTPUT_DIR "${CMAKE_CURRENT_BINARY_DIR}")
set(CMAKE_JAVA_INCLUDE_PATH "${CMAKE_SOURCE_DIR}/lib/protobuf-java-2.6.1.jar")
# Define directories for classfiles
set(CLASSDIR "${CMAKE_JAVA_TARGET_OUTPUT_DIR}/CMakeFiles/${JAR_TARGET_NAME}.dir")
set(CLEANFILES
	"${CLASSDIR}/im"
)

set_directory_properties(PROPERTIES ADDITIONAL_MAKE_CLEAN_FILES "${CLEANFILES}")

# Source Files
set(TOX4J_CORE
	im/tox/tox4j/Tox4j.java
	im/tox/tox4j/ToxConstants.java
	im/tox/tox4j/ToxSimpleChat.java
)

set(TOX4J_EXCEPTIONS
	im/tox/tox4j/exceptions/ToxException.java
	im/tox/tox4j/exceptions/FriendAddException.java
	im/tox/tox4j/exceptions/FriendAddErrorCode.java
	im/tox/tox4j/exceptions/EncryptedSaveDataException.java
	im/tox/tox4j/exceptions/ToxKilledException.java
)

set(TOX4J_CALLBACKS
	im/tox/tox4j/callbacks/ActionCallback.java
	im/tox/tox4j/callbacks/ConnectionStatusCallback.java
	im/tox/tox4j/callbacks/FriendRequestCallback.java
	im/tox/tox4j/callbacks/MessageCallback.java
	im/tox/tox4j/callbacks/NameChangeCallback.java
	im/tox/tox4j/callbacks/StatusMessageCallback.java
	im/tox/tox4j/callbacks/TypingChangeCallback.java
	im/tox/tox4j/callbacks/UserStatusCallback.java
)

set(TOX4J_SOURCE
	${TOX4J_CORE}
	${TOX4J_EXCEPTIONS}
	${TOX4J_CALLBACKS}
	"${CMAKE_CURRENT_BINARY_DIR}/im/tox/tox4j/proto/Events.java"
)

set(CMAKE_JAVA_TARGET_VERSION ${TOX4J_VERSION})

add_custom_target(java_dummy SOURCES ${TOX4J_SOURCE})
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

add_custom_command(
	OUTPUT im/tox/tox4j/proto/Events.java
	COMMAND ${PROTOBUF_PROTOC_EXECUTABLE}
	ARGS --java_out ${CMAKE_CURRENT_BINARY_DIR} --proto_path
	${CMAKE_SOURCE_DIR}/src/main/protobuf/
	${CMAKE_SOURCE_DIR}/src/main/protobuf/events.proto
	COMMENT "Running Java protocol buffer compiler on events.proto"
)

file(MAKE_DIRECTORY ${JNI_HEADER_LOCATION})
