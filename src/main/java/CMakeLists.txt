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
	im/tox/tox4j/Tox4j.java
	im/tox/tox4j/ToxConstants.java
	im/tox/tox4j/ToxSimpleChat.java
	im/tox/tox4j/v2/ToxConstants.java
	im/tox/tox4j/v2/ToxCore.java
	im/tox/tox4j/v2/ToxCoreImpl.java
	im/tox/tox4j/v2/ToxOptions.java
)

set(TOX4J_ENUMS
	im/tox/tox4j/v2/enums/ToxFileControl.java
	im/tox/tox4j/v2/enums/ToxFileKind.java
	im/tox/tox4j/v2/enums/ToxProxyType.java
	im/tox/tox4j/v2/enums/ToxStatus.java
)

set(TOX4J_EXCEPTIONS
	im/tox/tox4j/exceptions/ToxException.java
	im/tox/tox4j/exceptions/FriendAddException.java
	im/tox/tox4j/exceptions/FriendAddErrorCode.java
	im/tox/tox4j/exceptions/EncryptedSaveDataException.java
	im/tox/tox4j/exceptions/ToxKilledException.java
	im/tox/tox4j/exceptions/NoSuchFriendException.java
	im/tox/tox4j/exceptions/GroupAddException.java
	im/tox/tox4j/v2/exceptions/ToxAddFriendException.java
	im/tox/tox4j/v2/exceptions/ToxBootstrapException.java
	im/tox/tox4j/v2/exceptions/ToxDeleteFriendException.java
	im/tox/tox4j/v2/exceptions/ToxFileControlException.java
	im/tox/tox4j/v2/exceptions/ToxFriendQueryException.java
	im/tox/tox4j/v2/exceptions/ToxGetClientIdException.java
	im/tox/tox4j/v2/exceptions/ToxGetFriendNumberException.java
	im/tox/tox4j/v2/exceptions/ToxGetPortException.java
	im/tox/tox4j/v2/exceptions/ToxLoadException.java
	im/tox/tox4j/v2/exceptions/ToxNewException.java
	im/tox/tox4j/v2/exceptions/ToxSendCustomPacketException.java
	im/tox/tox4j/v2/exceptions/ToxSendFileException.java
	im/tox/tox4j/v2/exceptions/ToxSendMessageException.java
	im/tox/tox4j/v2/exceptions/ToxSetInfoException.java
	im/tox/tox4j/v2/exceptions/ToxSetTypingException.java
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
	im/tox/tox4j/callbacks/GroupInviteCallback.java
	im/tox/tox4j/callbacks/GroupMessageCallback.java
	im/tox/tox4j/callbacks/GroupActionCallback.java
	im/tox/tox4j/callbacks/GroupTitleChangeCallback.java
	im/tox/tox4j/callbacks/GroupNameListChangeCallback.java
	im/tox/tox4j/v2/callbacks/ConnectionStatusCallback.java
	im/tox/tox4j/v2/callbacks/FileControlCallback.java
	im/tox/tox4j/v2/callbacks/FileReceiveCallback.java
	im/tox/tox4j/v2/callbacks/FileReceiveChunkCallback.java
	im/tox/tox4j/v2/callbacks/FileSendChunkCallback.java
	im/tox/tox4j/v2/callbacks/FriendActionCallback.java
	im/tox/tox4j/v2/callbacks/FriendConnectedCallback.java
	im/tox/tox4j/v2/callbacks/FriendMessageCallback.java
	im/tox/tox4j/v2/callbacks/FriendNameCallback.java
	im/tox/tox4j/v2/callbacks/FriendRequestCallback.java
	im/tox/tox4j/v2/callbacks/FriendStatusCallback.java
	im/tox/tox4j/v2/callbacks/FriendStatusMessageCallback.java
	im/tox/tox4j/v2/callbacks/FriendTypingCallback.java
	im/tox/tox4j/v2/callbacks/LosslessPacketCallback.java
	im/tox/tox4j/v2/callbacks/LossyPacketCallback.java
	im/tox/tox4j/v2/callbacks/ReadReceiptCallback.java
)

set(TOX4J_SOURCE
	${TOX4J_CORE}
	${TOX4J_ENUMS}
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
        "${PACKAGE_NAME}.v2.ToxCoreImpl"
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

if (BUILD_JAVADOC)
	set(JAVADOC_SOURCE
		${TOX4J_CORE}
		${TOX4J_ENUMS}
		${TOX4J_EXCEPTIONS}
		${TOX4J_CALLBACKS}
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
