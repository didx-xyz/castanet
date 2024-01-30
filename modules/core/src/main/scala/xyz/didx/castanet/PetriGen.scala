package xyz.didx.castanet

import cats.effect.IO
import cats.effect.IOApp
import fs2.Stream
import fs2.io.file.Files
import fs2.io.file.Path
import fs2.text
import io.circe.Json
import io.circe.yaml

case class Workflow(apiVersion: String, kind: String, metadata: Metadata, spec: Spec)
case class Spec(entrypoint: String, templates: List[Template])
enum Template:
  case Server(name: String, inputs: Arguments, container: Container)
  case Service(name: String, dag: Dag)
case class Metadata(generateName: String)
case class Dag(tasks: List[Task])
case class Task(
    name: String,
    dependencies: Option[List[String]],
    template: String,
    arguments: Arguments
)
case class Arguments(parameters: List[Parameter])
case class Parameter(name: String, value: String)
case class Inputs(parameters: List[Parameter])
case class Container(image: String, command: List[String])

object PetriGen extends IOApp.Simple:
  def fromYaml(s: String) =
    for json <- yaml.parser.parse(s)
      // x <- json.asObject
    yield json

  val converter: Stream[IO, Unit] =
    Files[IO]
      .readAll(Path("modules/protocol/src/main/workflow/workflow.yaml"))
      .through(text.utf8.decode)
      .map(fileString => fromYaml(fileString))
      .map({
        case Right(json)        => json
        case Left(e: Throwable) => Json.Null.toString
      })
      // .map(j => extractDag(j))
      // .through(stringArrayParser)
      // .through(decoder[IO,Workflow])
      .map(_.toString)
      .through(text.utf8.encode)
      .through(
        Files[IO].writeAll(Path("modules/protocol/src/main/workflow/workflow.txt"))
      )

  def run: IO[Unit] =
    val s = Stream.exec(IO.println("running...")) ++ converter ++ Stream.exec(IO(println("done!")))
    s.compile.drain
