/*
 * Copyright 2021 Ian de Beer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.didx.castanet

import scodec.bits.BitVector

import scala.collection.immutable.SortedMap

trait ColouredPetriNet:
  import cats.data.State

  val elements: SortedMap[NodeId, LinkableElement]
  val graph: PetriGraph
  val arcs: Map[ArcId, Long]

  /** Providing a state monad for traversing the Petri Net
    * @param step
    *   the current state of the Petri Net
    * @return
    *   the new state of the Petri Net
    */
  def step: State[Step, Markers] = State(step =>
    // all arcs that come from places with tokens
    val flows: Map[ArcId, Long] = arcs.filter(arc => step.inits.keySet.contains(arc._1.from))

    // all arcs that have a smaller guards than the number of markers in the place - i.e. it can step
    val steps: Map[ArcId, Long] =
      flows.filter(flow => flow._2 <= step.inits(flow._1.from).populationCount)

    // all arcs from allowable transitions (steps) and their weights
    val nextFlows: Map[ArcId, Long] = for
      step    <- steps
      element <- graph(step._1.to)
    yield (ArcId(step._1.to, element.id), arcs(ArcId(step._1.to, element.id)))

    // all arcs that have a weight that is less than the capacity allowed by the destination place
    val nextSteps = nextFlows.filter(flow =>
      flow._2 <= elements(flow._1.to)
        .asInstanceOf[Place]
        .capacity - step.markers.state(flow._1.to).populationCount
    )

    // remove markers from the origin place of allowed steps
    val m1 = steps
      .foldLeft(step.markers)((markers, node) =>
        markers.setMarker(
          Marker(node._1.from, step.markers.state(node._1.from).shiftLeft(node._2))
        )
      )

    // add markers to the destination place (as per the weight from the transition)
    val m2 = nextFlows
      .foldLeft(m1)((markers, node) =>
        markers.setMarker(
          Marker(
            node._1.to,
            step.markers
              .state(node._1.to)
              .patch(step.markers.state(node._1.to).populationCount, BitVector.fill(node._2)(true))
          )
        )
      )

    // this side effect must be moved to the IO monad
    /*if step.show then
      PetriPrinter(fileName = s"step${step.count}", petriNet = this).print(markers = Option(step.markers), steps = Option(steps ++ nextSteps))
    else ()
     */
    // update the state and return the markers resulting from the step (reduced origin and increased destination steps)
    (Step(m2, step.count + 1), m2)
  )

  /** Shows the next places and transitions that can be reached from the current state without
    * changing the state
    * @param step
    * @return
    *   a tuple of the current places and next transitions that can be reached from the current
    *   state given a set of Markers
    */

  def peek(step: Step): (Set[LinkableElement], Set[LinkableElement]) =
    val flows: Map[ArcId, Long] = arcs.filter(arc => step.inits.keySet.contains(arc._1.from))
    val steps: Map[ArcId, Long] =
      flows.filter(flow => flow._2 <= step.inits(flow._1.from).populationCount)
    val nextFlows: Map[ArcId, Long] = for
      step    <- steps
      element <- graph(step._1.to)
    yield (ArcId(step._1.to, element.id), arcs(ArcId(step._1.to, element.id)))

    // all arcs that have a weight that is less than the capacity allowed by the destination place
    val currentPlaces = nextFlows
      .filter(flow =>
        flow._2 <= elements(flow._1.to)
          .asInstanceOf[Place]
          .capacity - step.markers.state(flow._1.to).populationCount
      )
      .map(flow => elements.get(flow._1.from) /* match
        case Some(p: Place) => p.name
        case _ => "" */
      )
      .flatten
    val nextTransitions = nextFlows
      .filter(flow =>
        flow._2 <= elements(flow._1.to)
          .asInstanceOf[Place]
          .capacity - step.markers.state(flow._1.to).populationCount
      )
      .map(flow => elements.get(flow._1.to) /*  match
        case Some(t: Transition) => t.name
        case _ => "" */
      )
      .flatten
    (currentPlaces.toSet, nextTransitions.toSet)

end ColouredPetriNet
