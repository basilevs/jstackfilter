# jstackfilter Core Example

This example shows how to use `org.basilevs.jstackfilter:org.basilevs.jstackfilter` from a standalone Maven project.

## What It Does

- Builds a small synthetic jstack text input
- Parses it with `JstackParser.parseThreads(...)`
- Prints parsed thread id, name, and state

## Run

From repository root:

```bash
mvn clean install -pl core/core -am
mvn -f core/example/pom.xml clean compile exec:java
```

If you already have the library installed locally, only the second command is needed.
