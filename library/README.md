# Overview
Jstack is a program bundled with JDK that allows capturing stacktraces of all threads in a running JVM. It is essential in debugging poor performance and deadlocks especially outside of a full development environment. Modern Java applications abuse threads, making the full dump of Jstack bloated and noisy. Significant time should be spent to find needed threads among everpresent pools and mundane background tasks.
Jstackfilter aims to drastically reduce noise in Jstack output by filtering out trivial threads and eliminating stacktrace duplicates to allow a user to focus on unusual, meaningful threads to hopefully isolate the problem quicker.
# Usage
Download jstackfilter.

List running Java processes using `jps` command (distributed with JDK).

```
$ jps -v
33986 jstackfilterui.jar
33299 Eclipse -Dosgi.requiredJavaVersion=17 -Dosgi.instance.area.default=@user.home/eclipse-workspace -Dosgi.dataAreaRequiresExplicitInit=true -Dorg.eclipse.swt.graphics.Resource.reportNonDisposed=true -Xms256m -Xmx2048m -XX:+UseG1GC -XX:+UseStringDeduplication --add-modules=ALL-SYSTEM -XstartOnFirstThread -Dorg.eclipse.swt.internal.carbon.smallFonts -Xdock:icon=../Resources/Eclipse.icns -Declipse.p2.max.threads=10 -Doomph.update.url=https://download.eclipse.org/oomph/updates/milestone/latest -Doomph.redirection.index.redirection=index:/->http://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/
34051 Jps  -Xms8m -Djdk.module.main=jdk.jcmd
```

Locate the program of interest, matching its command line.

Run `jstack`, supplying the process PID.

```
$ jstack 33299
...
"qtp1119904845-3716-acceptor-0@58651ec1-ServerConnector@7dd1bb29{HTTP/1.1, (http/1.1)}{127.0.0.1:56341}" #3716 prio=3 os_prio=31 cpu=0.48ms elapsed=68312.83s tid=0x0000000123a7f000 nid=0x23a23 runnable [0x00000002e5e82000]
 java.lang.Thread.State: RUNNABLE
	at sun.nio.ch.Net.accept(java.base@17.0.4.1/Native Method)
	at sun.nio.ch.ServerSocketChannelImpl.implAccept(java.base@17.0.4.1/ServerSocketChannelImpl.java:425)
	at sun.nio.ch.ServerSocketChannelImpl.accept(java.base@17.0.4.1/ServerSocketChannelImpl.java:391)
	at org.eclipse.jetty.server.ServerConnector.accept(ServerConnector.java:409)
	at org.eclipse.jetty.server.AbstractConnector$Acceptor.run(AbstractConnector.java:734)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:894)
	at org.eclipse.jetty.util.thread.QueuedThreadPool$Runner.run(QueuedThreadPool.java:1038)
	at java.lang.Thread.run(java.base@17.0.4.1/Thread.java:833)
...
```

Notice, how the output is wast and requires prolonged analysis to be useful.
Now filter output of `jstack` through `jstackfilter`

```
$ jstack 33299 | java -jar jstackfilter.jar 
...
"Worker-53: Process Console Input Job" #373 prio=5 os_prio=31 cpu=39.35ms elapsed=110.61s tid=0x00000002ad3e7e00 nid=0x1d947 in Object.wait() [0x00000002aa8a2000]
 java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(java.base@17.0.4.1/Native Method)
	- waiting on <no object reference available>
	at java.lang.Object.wait(java.base@17.0.4.1/Object.java:338)
	at org.eclipse.ui.console.IOConsoleInputStream.waitForData(IOConsoleInputStream.java:141)
	at org.eclipse.ui.console.IOConsoleInputStream.read(IOConsoleInputStream.java:92)
	- locked <0x00000007ab644a78> (a org.eclipse.ui.console.IOConsoleInputStream)
	at org.eclipse.ui.console.IOConsoleInputStream.read(IOConsoleInputStream.java:114)
	at org.eclipse.debug.internal.ui.views.console.ProcessConsole$InputReadJob.run(ProcessConsole.java:908)
	at org.eclipse.core.internal.jobs.Worker.run(Worker.java:63)
...
```

Observe how only more interesting threads are left.
The same analysis can be applied to jstack output saved to a file, using shell tools:

```
$ jstack 33299 > jstack.txt
$ java -jar jstackfilter.jar < jstack.txt
```

There are some additional tools provided for convenience:

```
java --class-path jstackfilter.jar org.basilevs.jstackfilter.ProcessFile /tmp/1.txt # Process a file without shell redirects
java --class-path jstackfilter.jar org.basilevs.jstackfilter.RunExternalJstack 33299 # Run jstack given a PID
```

# Jstackfilter UI
Jstackfilter is designed to be minimal in function and size and is used in a command line shell. It is usually sufficient for occasional use but can be a chore working with large datasets, multiple files, multiple hosts, etc. Consider using [an interactive UI wrapper of jstackfilter](https://github.com/basilevs/jstackfilter/tree/master/ui) if this is the case.
  