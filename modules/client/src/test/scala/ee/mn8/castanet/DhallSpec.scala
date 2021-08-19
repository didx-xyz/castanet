package ee.mn8.castanet

import java.nio.file.Path

import scala.collection.immutable.ListSet
import scala.io.Source

import cats.syntax.functor._
import io.circe.Decoder
import io.circe.Decoder.Result
import io.circe.Encoder
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import munit.*
import org.dhallj.circe.Converter
import org.dhallj.core.Expr
import org.dhallj.core.converters.JsonConverter
import org.dhallj.imports.mini.Resolver
import org.dhallj.parser.DhallParser
import org.dhallj.parser.DhallParser.parse
import org.dhallj.syntax.*
import org.dhallj.yaml.YamlConverter

class DhallSpec extends FunSuite {
  import LinkableElement._
  val confFile = "./modules/client/src/main/resources/petri.dhall"
  given decodeEvent: Decoder[LinkableElement] =
    List[Decoder[LinkableElement]](
      Decoder[Place].widen,
      Decoder[Transition].widen
    ).reduceLeft(_ or _)
  test("read from file") {
    val conf = Source.fromFile(confFile).getLines.mkString("\n")
    val expr = Resolver.resolve(parse(conf))
    val norm = expr.normalize
    println(s"${expr.typeCheck}")
    println(s"\n\n${norm}")

    println(s"\n\n${JsonConverter.toCompactString(norm)}")
    val json     = JsonConverter.toCompactString(norm)
    val elements = decode[List[LinkableElement]](json)
    val list     = ListSet[LinkableElement]() ++ elements.getOrElse(List[LinkableElement]())
    println(s"\n\n${list}")

    val petriNet = PetriNetBuilder()
      .addAll(list)
      .build()
    println(petriNet.elements)
  }
}
