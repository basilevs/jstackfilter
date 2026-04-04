# Noise-free Analysis of Java Thread Dumps
**jstackfilter** streamlines the analysis of noisy thread dumps produced by [`jstack`](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jstack.html). It highlights unusual or potentially problematic threads by eliminating duplicates and filtering out trivial or system threads. This helps you focus on what actually matters - finding the root cause of performance issues, deadlocks, or CPU spikes.

jstackfilter is available as a standalone GUI application, a command-line tool, an Eclipse IDE plugin, and a reusable Java library.

# Features
* **Stacktrace Filtering**: Removes duplicate and known-idle threads for concise output.
* **Custom Idle Threads**: Add your own filters to ignore threads common to your workloads.
* **Supports Large Dumps**: Handles even huge multi-megabyte thread dump files quickly.
* **Human-Centric Output**: Surfaces unusual or unique stack traces for faster debugging.
* **Multiple Integration Modes**: Use standalone GUI, CLI, as an Eclipse plugin, or as a library in your tools.
* **Input Flexibility**: Accepts thread dumps from live processes, files, or clipboard.
* **Compatibility**: Supports modern Java versions (≥11) and runs cross-platform.

# [Standalone application](https://github.com/basilevs/jstackfilter/tree/master/ui)
A basic Swing graphical interface. Inspect running JVMs, jstack dumps from clipboard or files.


# [Eclipse plugin](https://github.com/basilevs/jstackfilter/tree/master/eclipse)
Filters Debug view of Eclipse JDT.

# [Command line](https://github.com/basilevs/jstackfilter/tree/master/core/core)
Use in scripts, or integrate in non-Java programs.

# [Library](https://github.com/basilevs/jstackfilter/tree/master/core/core)
A JAR file to use in a Java program.
