package im.tox.tox4j.bench

import org.scalameter.KeyValue
import org.scalameter.api._

object Confidence {
  val low = Seq[KeyValue](
    exec.benchRuns -> 10,
    exec.independentSamples -> 1
  )

  val normal = Seq[KeyValue](
    exec.benchRuns -> 36,
    exec.independentSamples -> 3
  )

  val high = Seq[KeyValue](
    exec.benchRuns -> 100,
    exec.independentSamples -> 10
  )

  val extreme = Seq[KeyValue](
    exec.benchRuns -> 1000,
    exec.independentSamples -> 50
  )
}
