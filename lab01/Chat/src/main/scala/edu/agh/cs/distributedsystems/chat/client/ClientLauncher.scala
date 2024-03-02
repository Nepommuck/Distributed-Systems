package edu.agh.cs.distributedsystems.chat.client

object ClientLauncher {
  private def validateArguments(args: List[String]): Either[String, String] =
    if (args.length == 1)
      Right(args.head)
    else Left(
      s"Expected 1 argument - User Login. Received ${args.length}: ${args.mkString(start = "[", sep = ",", end = "]")}"
    )

  def main(args: Array[String]): Unit = {
    validateArguments(args.toList) match {
      case Left(error) =>
        println(s"ERROR: $error")
      case Right(login) =>
        Client(login).run()
    }
  }
}
