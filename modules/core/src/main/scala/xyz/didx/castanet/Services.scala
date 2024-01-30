package xyz.didx.castanet

import cats.data.State

sealed trait ProtoItem
case class Service(packageName: String = "", name: String = "", rpcs: List[RPC] = List())
    extends ProtoItem

case class RPC(name: String, input: String, output: String) extends ProtoItem {
  def toFunction[I, O]: (State[Markers, I]) => State[Markers, O] = ???
}
