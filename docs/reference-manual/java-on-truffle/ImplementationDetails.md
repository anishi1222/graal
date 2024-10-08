---
layout: docs
toc_group: espresso
link_title: Implementation Details
permalink: /reference-manual/espresso/implementation/
redirect_from: /reference-manual/java-on-truffle/implementation/
---

# Implementation Details

Espresso operates, like other languages implemented with Truffle, both as a native executable or on HotSpot (currently possible on Linux only).
In the first case, when the Espresso runtime is compiled to a native executable, it does not require HotSpot to run Java.
However, it requires a standard core Java library (the _rt.jar_ library for Java 8 or the _lib/modules file_ for Java 11+, as well as the associated native libraries: `libjava`, `libnio`, and so on).

Espresso is a minified Java VM that implements all core components of a VM including:
* Bytecode interpreter
* Bytecode verifier
* Single Java Class File parser
* Simple object model
* Java Native Interface (JNI) implementation in Java
* Virtual Machine Implementation in Java
* Java Debug Wire Protocol (JDWP)

Espresso reuses all JAR files and native libraries from GraalVM.
All native libraries and methods are loaded/accessed/called via [Truffle Native Function Interface (JNI)](../../../truffle/docs/NFI.md).
JNI handles are implemented in Espresso, for example, all Truffle NFI methods only receive and return primitives.
Some methods are substituted for performance, for example, `Math.sqrt`, `System.arraycopy`, avoiding the expensive transition to native.

Some native libraries might contain static data that would conflict if were used from multiple Espresso contexts or even from both Espresso and Java running on HotSpot.
On Linux, Espresso uses the capability of Truffle NFI to try to load libraries in isolated namespaces (`dlmopen`). This is only available on Linux with `glibc` and has many limitations.
This mode is not used when running in a native executable since there will be no conflict with HotSpot.

## Current Limitations

* Espresso does not implement the [JVM Tool Interface (JVMTI)](https://docs.oracle.com/en/java/javase/22/docs/specs/jvmti.html). As a result, it does not support the `-agentlib`, or `-agentpath` VM options.
* Espresso does not implement the `java.lang.instrument` interface. As a result it does not support the `-javaagent` VM option.
* Espresso currently uses the standard native libraries from the Java core library. This requires allowing a polyglot `Context` native access. Because of the way these libraries are loaded (via [Truffle NFI](../../../truffle/docs/NFI.md)), running on HotSpot only works on Linux (with `glibc`). Running as part of a native executable works on Linux, Windows, and macOS but it currently limited to one context.
* Support for [Java Management Extensions (JMX)](https://docs.oracle.com/javase/tutorial/jmx/index.html) is partial and some methods might return partial data.
* The [Debugger Protocol Implementation (JDWP)](https://docs.oracle.com/javase/8/docs/technotes/guides/jpda/jdwp-spec.html) lacks some capabilities compared to HotSpot. It will correctly report the supported [capabilities](https://docs.oracle.com/javase/8/docs/platform/jpda/jdwp/jdwp-protocol.html#JDWP_VirtualMachine_Capabilities). In particular actions that require to enumerate all Java objects are not supported. However, it does support a few hot reloading cases that HotSpot does not.
* When the `java.MultiThreaded` option is set to "false", [reference processing](https://docs.oracle.com/en/java/javase/22/docs/api/java.base/java/lang/ref/package-summary.html) will not happen. Depending on the application, this could create resource leaks. Note that this option is set to "false" automatically if Espresso runs in a context where a single-threaded language is enabled (for example, JavaScript).
* Espresso does not support the [Polyglot API](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/package-summary.html) yet. However, it provides a guest Java Polyglot API, described in `polyglot.jar`. For more information, see [Interoperability with Truffle Languages](Interoperability.md).