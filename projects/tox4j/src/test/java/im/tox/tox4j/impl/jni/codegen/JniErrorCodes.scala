package im.tox.tox4j.impl.jni.codegen

import java.io.{ File, PrintWriter }

import im.tox.tox4j.av.exceptions._
import im.tox.tox4j.core.exceptions._
import im.tox.tox4j.crypto.exceptions.{ ToxKeyDerivationException, ToxEncryptionException, ToxDecryptionException }
import im.tox.tox4j.exceptions.JavaOnly

object JniErrorCodes extends CodeGenerator {

  private def generateErrorCode[E <: Enum[E]](out: PrintWriter, values: Array[E]): Unit = {
    val exceptionClass = {
      val name = cxxTypeName(values(0).getClass.getEnclosingClass.getSimpleName)
      name.substring(name.indexOf('_') + 1, name.lastIndexOf('_'))
    }

    val javaEnum = values(0).getClass.getSimpleName
    val cxxEnum = cxxTypeName(javaEnum)

    out.println()
    out.println("HANDLE (\"" + javaTypeName(exceptionClass) + "\", " + exceptionClass + ")")
    out.println("{")
    out.println("  switch (error)")
    out.println("    {")
    out.println(s"    success_case ($exceptionClass);")
    values foreach { value =>
      if (value.getClass.getField(value.name).getAnnotation(classOf[JavaOnly]) == null) {
        out.println(s"    failure_case ($exceptionClass, ${value.name()});")
      }
    }
    out.println("    }")
    out.println("  return unhandled ();")
    out.println("}")
  }

  withFile(new File("src/main/cpp/ToxAv/generated/errors.cpp")) { out =>
    out.println("#include \"../ToxAv.h\"")
    out.println("#ifdef TOXAV_VERSION_MAJOR")
    generateErrorCode(out, ToxavAnswerException.Code.values)
    generateErrorCode(out, ToxavCallControlException.Code.values)
    generateErrorCode(out, ToxavCallException.Code.values)
    generateErrorCode(out, ToxavNewException.Code.values)
    generateErrorCode(out, ToxavSendFrameException.Code.values)
    generateErrorCode(out, ToxavSetBitRateException.Code.values)
    out.println("#endif")
  }

  withFile(new File("src/main/cpp/ToxCore/generated/errors.cpp")) { out =>
    out.println("#include \"../ToxCore.h\"")
    out.println("#ifdef TOX_VERSION_MAJOR")
    generateErrorCode(out, ToxBootstrapException.Code.values)
    generateErrorCode(out, ToxFileControlException.Code.values)
    generateErrorCode(out, ToxFileGetException.Code.values)
    generateErrorCode(out, ToxFileSeekException.Code.values)
    generateErrorCode(out, ToxFileSendChunkException.Code.values)
    generateErrorCode(out, ToxFileSendException.Code.values)
    generateErrorCode(out, ToxFriendAddException.Code.values)
    generateErrorCode(out, ToxFriendByPublicKeyException.Code.values)
    generateErrorCode(out, ToxFriendCustomPacketException.Code.values)
    generateErrorCode(out, ToxFriendDeleteException.Code.values)
    generateErrorCode(out, ToxFriendGetPublicKeyException.Code.values)
    generateErrorCode(out, ToxFriendSendMessageException.Code.values)
    generateErrorCode(out, ToxGetPortException.Code.values)
    generateErrorCode(out, ToxNewException.Code.values)
    generateErrorCode(out, ToxSetInfoException.Code.values)
    generateErrorCode(out, ToxSetTypingException.Code.values)
    out.println("#endif")
  }

  withFile(new File("src/main/cpp/ToxCrypto/generated/errors.cpp")) { out =>
    out.println("#include \"../ToxCrypto.h\"")
    generateErrorCode(out, ToxDecryptionException.Code.values)
    generateErrorCode(out, ToxEncryptionException.Code.values)
    generateErrorCode(out, ToxKeyDerivationException.Code.values)
  }

}
