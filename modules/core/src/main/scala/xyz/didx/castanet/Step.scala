package xyz.didx.castanet

import scodec.bits.BitVector

import scala.collection.immutable.SortedMap

case class Step(markers: Markers, count: Int = 0):
  val inits: SortedMap[NodeId, BitVector] =
    markers.state.filter(m => m._2 > BitVector.empty.padRight(m._2.size))
