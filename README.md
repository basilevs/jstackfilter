# Overview
`jstack` is a program bundled with JDK that allows capturing stacktraces of all threads in a running JVM. It is essential in debugging poor performance and deadlocks especially outside of a full development environment. Modern Java applications abuse threads, making the full dump of Jstack bloated and noisy. Significant time should be spent to find needed threads among everpresent pools and mundane background tasks.
Jstackfilter aims to drastically reduce noise in Jstack output by filtering out trivial threads and eliminating stacktrace duplicates to allow a user to focus on unusual, meaningful threads to hopefully isolate the problem quicker.

# [Standalone application](https://github.com/basilevs/jstackfilter/tree/master/ui)
A basic Swing graphical interface. Inspect running JVMs, jstack dumps from clipboard or files.


# [Eclipse plugin](https://github.com/basilevs/jstackfilter/tree/master/eclipse)
Filters Debug view of Eclipse JDT.

# [Command line](https://github.com/basilevs/jstackfilter/tree/master/core/core)
Use in scripts, or integrate in non-Java programs.

# [Library](https://github.com/basilevs/jstackfilter/tree/master/core/core)
A JAR file to use in a Java program.
