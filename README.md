### Is there a bug in mill or do I miss something?

I took an example application of [Apache Commons BCEL](https://commons.apache.org/proper/commons-bcel/) and modified it a bit. I compiled and run the Java code with Temurin 21 (also tried other). Strangely, my mill build and my maven build return different runtime results.
I did the same tests on my Mac and got the same strange result.

"
The Byte Code Engineering Library (Apache Commons BCEL™) is intended to give users a convenient way to analyze, create, and manipulate (binary) Java class files (those ending with .class). Classes are represented by objects which contain all the symbolic information of the given class: methods, fields and byte code instructions, in particular.
"

## maven

```
cd maven
mvn clean test
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running TransitiveHullTest
FIND G>C
FIND G>java.lang.String
FIND G>MyEnum
FIND G>F
FIND G>B
FIND G>H
FIND G>H
FIND G>E
FIND G>A$Inner
FIND G>A
FIND G>D
FIND G>A$Inner
FIND G>A$Inner
FIND G>G
FIND G>G
FIND G>G
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.161 s -- in TransitiveHullTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.741 s
[INFO] Finished at: 2025-11-01T19:11:24+01:00
[INFO] ------------------------------------------------------------------------
```

There is no error!

## mill

I used mill Version 1.0.6-jvm because my processor is too old for the compiled version:

```
./mill foo.compile
The current machine does not support all of the following CPU features that are required by the image: [CX8, CMOV, FXSR, MMX, SSE, SSE2, SSE3, SSSE3, SSE4_1, SSE4_2, POPCNT, LZCNT, AVX, AVX2, BMI1, BMI2, FMA, F16C].
Please rebuild the executable with an appropriate setting of the -march option.
```

```
cd mill
./mill foo.compile
[35/35] ============================== foo.compile ==============================

./mill foo.test
[97/97] foo.test.testForked
[97] Running Test Class TransitiveHullTest
[97] Test run started (JUnit Jupiter)
[97] Test [33mTransitiveHullTest#getHull() started
[97] Test TransitiveHullTest.getHull failed: org.opentest4j.AssertionFailedError: expected: <[A, MyEnum, B, C, D, E, F, H, A$Inner]> but was: <[A, MyEnum, B, C, D, E, F, G, H, A$Inner]>, took 0.178s
[97]     at TransitiveHullTest.getHull(TransitiveHullTest.java:8)
[97]     at java.lang.reflect.Method.invoke(Method.java:566)
[97]     at java.util.ArrayList.forEach(ArrayList.java:1541)
[97]     at java.util.ArrayList.forEach(ArrayList.java:1541)
[97] Test [33mTransitiveHullTest finished, took 0.204s
[97] Test junit-jupiter finished, took 0.233s
[97] Test run finished: 1 failed, 0 ignored, 1 total, 0.268s
[97/97, 1 failed] ============================== foo.test ============================== 1s
1 tasks failed
[97] foo.test.testForked 1 tests failed: 
  TransitiveHullTest getHull()
```

The error is that class "G" is not found by the modified programm.


##  sbt

# UPDATE: It must be a difference how zinc is generating the java byte code.

A test with sbt fails as well:

```
cd sbt
./sbt clean test
[info] Fetched artifacts of sbt-build
[info] compiling 1 Scala source to /home/florian/git/compare_mill_maven/sbt/project/target/scala-2.12/sbt-1.0/classes ...
[info] loading settings for project root from build.sbt...
[info] set current project to bcel-test (in build file:/home/florian/git/compare_mill_maven/sbt/)
[success] Total time: 0 s, completed Oct 14, 2025, 1:01:56 PM
[info] compiling 1 Java source to /home/florian/git/compare_mill_maven/sbt/target/scala-2.13/classes ...
[info] compiling 10 Java sources to /home/florian/git/compare_mill_maven/sbt/target/scala-2.13/test-classes ...
[info] Test run started (JUnit Jupiter)
[info] Test [33mTransitiveHullTest#getHull() started
[error] Test TransitiveHullTest.getHull failed: org.opentest4j.AssertionFailedError: expected: <[A, MyEnum, B, C, D, E, F, H, A$Inner]> but was: <[A, MyEnum, B, C, D, E, F, G, H, A$Inner]>, took 0.135s
[error]     at TransitiveHullTest.getHull(TransitiveHullTest.java:8)
[info] Test run finished: 1 failed, 0 ignored, 1 total, 0.219s
[error] Failed: Total 1, Failed 1, Errors 0, Passed 0
[error] Failed tests:
[error] 	TransitiveHullTest
[error] (Test / test) sbt.TestsFailedException: Tests unsuccessful
[error] Total time: 3 s, completed Oct 14, 2025, 1:01:59 PM
```

# UPDATE: I found the problem, there are simply other default compiler flags set in maven, sbt and mill:

There are no bugs :-)... mill and sbt simply does not use the compiler flag "-g" by default but maven does. 
Adding the compiler flag with:  

```
javacOptions +="-g"
```

in the build.sbt solves the issue, and my test runs with no error:

```
sbt clean test
[info] welcome to sbt 1.11.7 (Eclipse Adoptium Java 21.0.8)
[info] loading settings for project sbt-build from plugins.sbt...
[info] loading project definition from /home/florian/git/compare_mill_maven/sbt/project
[info] loading settings for project root from build.sbt...
[info] set current project to bcel-test (in build file:/home/florian/git/compare_mill_maven/sbt/)
[success] Total time: 0 s, completed Nov 1, 2025, 7:00:17 PM
[info] compiling 1 Java source to /home/florian/git/compare_mill_maven/sbt/target/scala-2.13/classes ...
[info] compiling 10 Java sources to /home/florian/git/compare_mill_maven/sbt/target/scala-2.13/test-classes ...
[info] Test run started (JUnit Jupiter)
[info] Test [33mTransitiveHullTest#getHull() started
[info] FIND G>C
[info] FIND G>java.lang.String
[info] FIND G>MyEnum
[info] FIND G>F
[info] FIND G>B
[info] FIND G>H
[info] FIND G>H
[info] FIND G>E
[info] FIND G>A$Inner
[info] FIND G>A
[info] FIND G>D
[info] FIND G>A$Inner
[info] FIND G>A$Inner
[info] FIND G>G
[info] FIND G>G
[info] FIND G>G
[info] Test run finished: 0 failed, 0 ignored, 1 total, 0.217s
[info] Passed: Total 1, Failed 0, Errors 0, Passed 1
[success] Total time: 4 s, completed Nov 1, 2025, 7:00:21 PM
```

### Without the compiler flags the Java compiler does not generate the "LocalVariableTable" [(see here)](https://docs.oracle.com/en/java/javacard/3.1/guide/setting-java-compiler-options.html]).
