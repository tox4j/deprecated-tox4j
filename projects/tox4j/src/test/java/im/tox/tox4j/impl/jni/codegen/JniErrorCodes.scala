package im.tox.tox4j.impl.jni.codegen

import im.tox.tox4j.av.exceptions._
import im.tox.tox4j.core.exceptions._
import im.tox.tox4j.crypto.exceptions.{ ToxDecryptionException, ToxEncryptionException, ToxKeyDerivationException }
import im.tox.tox4j.exceptions.JavaOnly

object JniErrorCodes extends CodeGenerator {

  def generateErrorCode[E <: Enum[E]](values: Array[E]): String = {
    val exceptionClass = {
      val name = cxxTypeName(values(0).getClass.getEnclosingClass.getSimpleName)
      name.substring(name.indexOf('_') + 1, name.lastIndexOf('_'))
    }

    val javaEnum = values(0).getClass.getSimpleName
    val cxxEnum = cxxTypeName(javaEnum)

    val failureCases = values.flatMap { value =>
      if (value.getClass.getField(value.name).getAnnotation(classOf[JavaOnly]) == null) {
        Seq(s"    failure_case ($exceptionClass, ${value.name()});")
      } else {
        Nil
      }
    }.mkString("\n")

    s"""
    |HANDLE ("${javaTypeName(exceptionClass)}", $exceptionClass)
    |{
    |  switch (error)
    |    {
    |    success_case ($exceptionClass);
    |$failureCases
    |    }
    |  return unhandled ();
    |}
    |""".stripMargin
  }

  writeFile("ToxAv/generated/errors.cpp") {
    s"""
    |#include "../ToxAv.h"
    |
    |#ifdef TOXAV_VERSION_MAJOR
    |
    |${generateErrorCode(ToxavAnswerException.Code.values)}
    |${generateErrorCode(ToxavCallControlException.Code.values)}
    |${generateErrorCode(ToxavCallException.Code.values)}
    |${generateErrorCode(ToxavNewException.Code.values)}
    |${generateErrorCode(ToxavSendFrameException.Code.values)}
    |${generateErrorCode(ToxavSetBitRateException.Code.values)}
    |
    |#endif
    |""".stripMargin
  }

  writeFile("ToxCore/generated/errors.cpp") {
    s"""
    |#include "../ToxCore.h"
    |
    |#ifdef TOX_VERSION_MAJOR
    |
    |${generateErrorCode(ToxBootstrapException.Code.values)}
    |${generateErrorCode(ToxFileControlException.Code.values)}
    |${generateErrorCode(ToxFileGetException.Code.values)}
    |${generateErrorCode(ToxFileSeekException.Code.values)}
    |${generateErrorCode(ToxFileSendChunkException.Code.values)}
    |${generateErrorCode(ToxFileSendException.Code.values)}
    |${generateErrorCode(ToxFriendAddException.Code.values)}
    |${generateErrorCode(ToxFriendByPublicKeyException.Code.values)}
    |${generateErrorCode(ToxFriendCustomPacketException.Code.values)}
    |${generateErrorCode(ToxFriendDeleteException.Code.values)}
    |${generateErrorCode(ToxFriendGetPublicKeyException.Code.values)}
    |${generateErrorCode(ToxFriendSendMessageException.Code.values)}
    |${generateErrorCode(ToxGetPortException.Code.values)}
    |${generateErrorCode(ToxNewException.Code.values)}
    |${generateErrorCode(ToxSetInfoException.Code.values)}
    |${generateErrorCode(ToxSetTypingException.Code.values)}
    |
    |#endif
    |""".stripMargin
  }

  writeFile("ToxCrypto/generated/errors.cpp") {
    s"""
    |#include "../ToxCrypto.h"
    |
    |${generateErrorCode(ToxDecryptionException.Code.values)}
    |${generateErrorCode(ToxEncryptionException.Code.values)}
    |${generateErrorCode(ToxKeyDerivationException.Code.values)}
    |""".stripMargin
  }

}
