package edu.agh.cs.distributedsystems.chat.client

object ClientLauncher {
  def main(args: Array[String]): Unit = {
    val client = new Client

    client.run()
  }
}
