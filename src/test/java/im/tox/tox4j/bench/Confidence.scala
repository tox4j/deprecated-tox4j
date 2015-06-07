package im.tox.tox4j.bench

import org.scalameter.KeyValue
import org.scalameter.api._

/**
 * Contains some predefined confidence levels for benchmarks.
 *
 * VM invocation overhead numbers are experimental estimates based on running it on an i7-3632QM CPU @ 2.20GHz. It
 * includes VM warm-up time.
 */
object Confidence {
  /**
   * This is the minimal number (2) of benchmark runs to gather a number. Use this as a one-time run to see how long a
   * single run takes. The statistics from this confidence level are almost certainly useless.
   *
   * VM invocation overhead: 1 second.
   */
  val lowest = Seq[KeyValue](
    exec.benchRuns -> 2,
    exec.independentSamples -> 1
  )

  /**
   * Choose if you don't yet know how long your benchmark will run and you want to get a feeling for what performance
   * characteristics it exhibits.
   *
   * VM invocation overhead: 1 second.
   */
  val low = Seq[KeyValue](
    exec.benchRuns -> 10,
    exec.independentSamples -> 1
  )

  /**
   * The usual configuration used in benchmarks based on [[PerformanceReportBase]]. Usually this is the right choice for
   * production benchmarks.
   *
   * VM invocation overhead: 4 seconds.
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
   *
   * VM invocation overhead: 17 seconds.
   */
  val high = Seq[KeyValue](
    exec.benchRuns -> 100,
    exec.independentSamples -> 10
  )

  /**
   * Run 50 independent VMs with 20 runs each. Use this if you need very high confidence in the test results. This will
   * likely take a long time, regardless of how fast the actual test is, simply because it needs to spawn 50 JVM
   * instances. Don't use this for production benchmarks, only for local experiments.
   *
   * VM invocation overhead: 1 minute, 20 seconds.
   */
  val extreme = Seq[KeyValue](
    exec.benchRuns -> 1000,
    exec.independentSamples -> 50
  )
}
