package edu.agh.cs.distributedsystems.chat.common

sealed abstract case class ProtocolFlag(encodedValue: String)

object ProtocolFlag {
  def decodeFlag(encodedValue: String): Option[ProtocolFlag] =
    encodedValue match {
      case TcpTransmission.encodedValue => Some(TcpTransmission)
      case UdpRegistration.encodedValue => Some(UdpRegistration)
      case UdpTransmission.encodedValue => Some(UdpTransmission)
      case _ => None
    }
}

object TcpTransmission extends ProtocolFlag("T") {
  override def toString: String = "TCP"
}

object UdpTransmission extends ProtocolFlag("U") {
  override def toString: String = "UDP"
}

object UdpRegistration extends ProtocolFlag("UR") {
  override def toString: String = "UDP Registration"
}

abstract class ProtocolMessage(val protocolFlag: ProtocolFlag) {
  def senderLogin: String

  def message: String

  def encode: Option[String] =
    message.trim match {
      case "" => None
      case _ =>
        val singleLineMessage = message.replace("\n", "\\n")
        Some(
          s"$senderLogin${ProtocolMessage.separator}${protocolFlag.encodedValue}"
            + s"${ProtocolMessage.separator}$singleLineMessage"
        )
    }
}

object ProtocolMessage {
  private val separator = '|'

  def apply(senderLogin: String, flag: ProtocolFlag, message: String): ProtocolMessage =
    flag match {
      case TcpTransmission => TcpMessage(senderLogin, message)
      case UdpRegistration => UdpRegistrationMessage(senderLogin)
      case UdpTransmission => UdpTransmissionMessage(senderLogin, message)
    }

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

case class TcpMessage(senderLogin: String, message: String)
  extends ProtocolMessage(protocolFlag = TcpTransmission)

abstract class UdpMessage(protocolFlag: ProtocolFlag) extends ProtocolMessage(protocolFlag)

case class UdpTransmissionMessage(senderLogin: String, message: String)
  extends UdpMessage(protocolFlag = UdpTransmission)

case class UdpRegistrationMessage(senderLogin: String)
  extends UdpMessage(protocolFlag = UdpRegistration) {
  override def message: String = UdpRegistration.toString
}
