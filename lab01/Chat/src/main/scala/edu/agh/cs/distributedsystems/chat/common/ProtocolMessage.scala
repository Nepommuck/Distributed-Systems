package edu.agh.cs.distributedsystems.chat.common

sealed abstract case class ProtocolFlag(encodedValue: String)

object ProtocolFlag {
  def decodeFlag(encodedValue: String): Option[ProtocolFlag] =
    encodedValue match {
      case TCPTransmission.encodedValue => Some(TCPTransmission)
      case UDPTransmission.encodedValue => Some(TCPTransmission)
      case _ => None
    }
}

object TCPTransmission extends ProtocolFlag("T") {
  override def toString: String = "TCP"
}

object UDPTransmission extends ProtocolFlag("U") {
  override def toString: String = "UDP"
}

case class ProtocolMessage(senderLogin: String, flag: ProtocolFlag, message: String) {
  def encode: Option[String] =
    message.trim match {
      case "" => None
      case _ =>
        val singleLineMessage = message.replace("\n", "\\n")
        Some(
          s"$senderLogin${ProtocolMessage.separator}${flag.encodedValue}${ProtocolMessage.separator}$singleLineMessage"
        )
    }
}

object ProtocolMessage {
  private val separator = '|'

  def decodeRawMessage(rawMessage: String): Option[ProtocolMessage] =
    rawMessage
      .split(separator)
      .toList match {
      case senderLogin :: encodedProtocolFlag :: messageParts =>
        ProtocolFlag.decodeFlag(encodedProtocolFlag) match {
          case None => None

          case Some(protocolFlag) =>
            val message = messageParts
              .mkString(sep = separator.toString)
              .replace("\\n", "\n")
            Some(ProtocolMessage(senderLogin, protocolFlag, message))
        }
      case _ => None
    }
}

class TCPMessage(senderLogin: String, message: String)
  extends ProtocolMessage(senderLogin, flag = TCPTransmission, message)

class UDPMessage(senderLogin: String, message: String)
  extends ProtocolMessage(senderLogin, flag = UDPTransmission, message)
