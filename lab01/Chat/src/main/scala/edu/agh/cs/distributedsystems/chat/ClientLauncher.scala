package edu.agh.cs.distributedsystems.chat

object ClientLauncher {
  def main(args: Array[String]): Unit = {
    val client = new Client

    new Thread(client).start()
  }
}
