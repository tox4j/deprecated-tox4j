package im.tox.tox4j.bench

import org.scalameter.KeyValue
import org.scalameter.api._

/**
 * Contains some predefined confidence levels for benchmarks.
 */
object Confidence {
  /**
   * Choose if you don't yet know how long your benchmark will run and you want to get a feeling for what performance
   * characteristics it exhibits.
   */
  val low = Seq[KeyValue](
    exec.benchRuns -> 10,
    exec.independentSamples -> 1
  )

  /**
   * The usual configuration used in benchmarks based on [[PerformanceReportBase]]. Usually this is the right choice for
   * production benchmarks.
   */
  val normal = Seq[KeyValue](
    exec.benchRuns -> 36,
    exec.independentSamples -> 3
  )

  /**
   * Choose this if the function you're benchmarking has very flaky performance. This runs fewer tests per VM
   * invocation, so if you run into GC issues, you may want to choose this configuration. For tests that are flaky for
   * another reason, you may find that adding `config (exec.benchRuns -> 100)` and using the [[normal]] configuration
   * has a better effect.
   */
  val high = Seq[KeyValue](
    exec.benchRuns -> 100,
    exec.independentSamples -> 10
  )

  /**
   * Run 50 independent VMs with 20 runs each. Use this if you need very high confidence in the test results. This will
   * likely take a long time, regardless of how fast the actual test is, simply because it needs to spawn 50 JVM
   * instances. Don't use this for production benchmarks, only for local experiments.
   */
  val extreme = Seq[KeyValue](
    exec.benchRuns -> 1000,
    exec.independentSamples -> 50
  )
}
