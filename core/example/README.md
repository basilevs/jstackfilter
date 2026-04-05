# jstackfilter Core Example

This example shows how to use `org.basilevs.jstackfilter:org.basilevs.jstackfilter` from a standalone Maven project.

## What It Does

- Builds a small synthetic jstack text input
- Parses it with `JstackParser.parseThreads(...)`
- Prints parsed thread id, name, and state

## Run

From repository root:

```bash
mvn --file core/example/pom.xml clean compile exec:java
```

If you already have the library installed locally, only the second command is needed.

## GitHub Packages Authentication

If Maven needs to download `org.basilevs.jstackfilter` from GitHub Packages, add this to `~/.m2/settings.xml`:

```xml
<servers>
	<server>
		<id>github</id>
		<username>YOUR_GITHUB_USERNAME</username>
		<password>YOUR_GITHUB_TOKEN</password>
	</server>
</servers>
```

The server `id` must match the repository id (`github`) defined in `core/example/pom.xml`.
