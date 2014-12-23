include(UseJava)
set(PACKAGE_NAME im.tox.tox4j)

# Java output directory
set(CMAKE_JAVA_TARGET_OUTPUT_DIR "${CMAKE_CURRENT_BINARY_DIR}")
set(CMAKE_JAVA_INCLUDE_PATH "${PROTOBUF_FULL}")
# Define directories for classfiles
set(CLASSDIR "${CMAKE_JAVA_TARGET_OUTPUT_DIR}/CMakeFiles/${JAR_TARGET_NAME}.dir")
set(CLEANFILES
	"${CLASSDIR}/im"
)

set_directory_properties(PROPERTIES ADDITIONAL_MAKE_CLEAN_FILES "${CLEANFILES}")

# Source Files
set(TOX4J_CORE
	im/tox/tox4j/av/callbacks/CallCallback.java
	im/tox/tox4j/av/callbacks/CallControlCallback.java
	im/tox/tox4j/av/callbacks/ReceiveAudioFrameCallback.java
	im/tox/tox4j/av/callbacks/ReceiveVideoFrameCallback.java
	im/tox/tox4j/av/callbacks/RequestAudioFrameCallback.java
	im/tox/tox4j/av/callbacks/RequestVideoFrameCallback.java
	im/tox/tox4j/av/enums/ToxCallControl.java
	im/tox/tox4j/av/exceptions/ToxAnswerException.java
	im/tox/tox4j/av/exceptions/ToxAvNewException.java
	im/tox/tox4j/av/exceptions/ToxBitRateException.java
	im/tox/tox4j/av/exceptions/ToxCallControlException.java
	im/tox/tox4j/av/exceptions/ToxCallException.java
	im/tox/tox4j/av/exceptions/ToxSendFrameException.java
	im/tox/tox4j/av/ToxAv.java
	im/tox/tox4j/callbacks/ConnectionStatusCallback.java
	im/tox/tox4j/callbacks/FileControlCallback.java
	im/tox/tox4j/callbacks/FileReceiveCallback.java
	im/tox/tox4j/callbacks/FileReceiveChunkCallback.java
	im/tox/tox4j/callbacks/FileRequestChunkCallback.java
	im/tox/tox4j/callbacks/FriendActionCallback.java
	im/tox/tox4j/callbacks/FriendConnectionStatusCallback.java
	im/tox/tox4j/callbacks/FriendLosslessPacketCallback.java
	im/tox/tox4j/callbacks/FriendLossyPacketCallback.java
	im/tox/tox4j/callbacks/FriendMessageCallback.java
	im/tox/tox4j/callbacks/FriendNameCallback.java
	im/tox/tox4j/callbacks/FriendRequestCallback.java
	im/tox/tox4j/callbacks/FriendStatusCallback.java
	im/tox/tox4j/callbacks/FriendStatusMessageCallback.java
	im/tox/tox4j/callbacks/FriendTypingCallback.java
	im/tox/tox4j/callbacks/ReadReceiptCallback.java
	im/tox/tox4j/callbacks/ToxEventAdapter.java
	im/tox/tox4j/callbacks/ToxEventListener.java
	im/tox/tox4j/enums/ToxFileControl.java
	im/tox/tox4j/enums/ToxFileKind.java
	im/tox/tox4j/enums/ToxProxyType.java
	im/tox/tox4j/enums/ToxStatus.java
	im/tox/tox4j/exceptions/ToxBootstrapException.java
	im/tox/tox4j/exceptions/ToxException.java
	im/tox/tox4j/exceptions/ToxFileControlException.java
	im/tox/tox4j/exceptions/ToxFileSendChunkException.java
	im/tox/tox4j/exceptions/ToxFileSendException.java
	im/tox/tox4j/exceptions/ToxFriendAddException.java
	im/tox/tox4j/exceptions/ToxFriendByClientIdException.java
	im/tox/tox4j/exceptions/ToxFriendDeleteException.java
	im/tox/tox4j/exceptions/ToxFriendGetClientIdException.java
	im/tox/tox4j/exceptions/ToxGetPortException.java
	im/tox/tox4j/exceptions/ToxKilledException.java
	im/tox/tox4j/exceptions/ToxLoadException.java
	im/tox/tox4j/exceptions/ToxNewException.java
	im/tox/tox4j/exceptions/ToxSendCustomPacketException.java
	im/tox/tox4j/exceptions/ToxSendMessageException.java
	im/tox/tox4j/exceptions/ToxSetInfoException.java
	im/tox/tox4j/exceptions/ToxSetTypingException.java
	im/tox/tox4j/AbstractToxCore.java
	im/tox/tox4j/ToxAvImpl.java
	im/tox/tox4j/ToxConstants.java
	im/tox/tox4j/ToxCoreImpl.java
	im/tox/tox4j/ToxCore.java
	im/tox/tox4j/ToxOptions.java
)

set(TOX4J_SOURCE
	${TOX4J_CORE}
	"${CMAKE_CURRENT_BINARY_DIR}/im/tox/tox4j/proto/Av.java"
	"${CMAKE_CURRENT_BINARY_DIR}/im/tox/tox4j/proto/Core.java"
)

set(CMAKE_JAVA_TARGET_VERSION ${TOX4J_VERSION})

add_custom_target(java_dummy SOURCES ${TOX4J_SOURCE})
add_jar(${JAR_TARGET_NAME} ${TOX4J_SOURCE})

# Set arguments for javah
set(JAVAH_ARGS
	"-classpath"
	"${CLASSDIR}:${PROTOBUF_FULL}"
	"-o"
	"${JNI_HEADER_LOCATION}/${JNI_HEADER_NAME}"
	"${PACKAGE_NAME}.ToxAvImpl"
	"${PACKAGE_NAME}.ToxCoreImpl"
)

add_custom_command(
	TARGET ${JAR_TARGET_NAME}
	POST_BUILD
	COMMAND ${Java_JAVAH_EXECUTABLE} ARGS ${JAVAH_ARGS}
)

add_custom_command(
	OUTPUT im/tox/tox4j/proto/Av.java
	COMMAND ${PROTOBUF_PROTOC_EXECUTABLE}
	ARGS --java_out ${CMAKE_CURRENT_BINARY_DIR} --proto_path
	${CMAKE_SOURCE_DIR}/src/main/protobuf/
	${CMAKE_SOURCE_DIR}/src/main/protobuf/Av.proto
	COMMENT "Running Java protocol buffer compiler on Av.proto"
)

add_custom_command(
	OUTPUT im/tox/tox4j/proto/Core.java
	COMMAND ${PROTOBUF_PROTOC_EXECUTABLE}
	ARGS --java_out ${CMAKE_CURRENT_BINARY_DIR} --proto_path
	${CMAKE_SOURCE_DIR}/src/main/protobuf/
	${CMAKE_SOURCE_DIR}/src/main/protobuf/Core.proto
	COMMENT "Running Java protocol buffer compiler on Core.proto"
)

file(MAKE_DIRECTORY ${JNI_HEADER_LOCATION})

if (BUILD_JAVADOC)
	set(JAVADOC_SOURCE
		${TOX4J_CORE}
	)
	create_javadoc(${JAR_TARGET_NAME}
		FILES ${JAVADOC_SOURCE}
		CLASSPATH ${CMAKE_JAVA_INCLUDE_PATH}
		WINDOWTITLE "${JAR_TARGET_NAME}"
		DOCTITLE "<h1>${JAR_TARGET_NAME}</h1>"
		AUTHOR TRUE
		USE TRUE
		VERSION TRUE
	)
endif()
