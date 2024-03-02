package edu.agh.cs.distributedsystems.chat.client

import edu.agh.cs.distributedsystems.chat.AppConfig
import edu.agh.cs.distributedsystems.chat.common.{ProtocolMessage, TCPMessage}

import scala.annotation.tailrec
import scala.io.StdIn.readLine

case class Client(login: String) extends Runnable {

  private val connection = new ClientConnection(
    client = this,
    serverHostname = AppConfig.ServeHostname,
    serverPort = AppConfig.ServerPort,
    onServerConnectionTermination = handleServerConnectionTermination
  )
  private val connectionThread = new Thread(connection)
  connectionThread.start()

  def receiveMessage(protocolMessage: ProtocolMessage): Unit = {
    val startLine = "  | "
    val messageToDisplay = protocolMessage.message
      .split('\n')
      .mkString(start = startLine, sep = s"\n$startLine", end = "")

    println(s"${protocolMessage.senderLogin}:\n$messageToDisplay")
    printCommandPrompt()
  }

  private def handleServerConnectionTermination(): Unit = {
    println("\n" + "Connection with server was terminated.")
    sys.exit(-1)
  }

  private def printCommandPrompt(): Unit = {
    print("> ")
  }

  @tailrec
  private def readUserInput(previousLines: List[String] = List.empty): String = {
    previousLines match {
      case Nil =>
        printCommandPrompt()
      case _ =>
        print("| ")
    }
    readLine() match {
      case "" => previousLines.mkString(sep = "\n")
      case nextLine => readUserInput(previousLines :+ nextLine)
    }
  }

  override def run(): Unit = try {
    while (true) {
      val newMessage = readUserInput()
      connection.sendMessage(new TCPMessage(senderLogin = login, message = newMessage))
    }
  } finally {
    connectionThread.interrupt()
  }
}
