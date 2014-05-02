kandidlib-emitter
=================

A small Java library to control creation and lifespan of temporary files and directories in unit tests.

The problem
---------
When running file related unit tests one might be sometimes interested in the created files but not always. How can that be switched on and off without modifying the test source?

The solution
---------
This little library allows you to do that. It consists of only one class extending `junit.framework.TestCase` by a manager for temporary files and reads a configuration file describing where to place them and how long to let them live.

How to use it
----------
As one might have already guessed it must be added to the classpath. Create a file named $HOME/.config/de.kandid/general.properties with two keys in it:

Property | Description | Default
---------|-------------|----------
de.kandid.junit.tmp.dir	| the location where to create all temporary files and and directories | build/tmp/unittest
de.kandid.junit.tmp.dir.remove | a TestCase.TmpDirDeletePolicy as a String controlling when to delete the files created for this TestCase	| passed

Building the kandidlib-junit.jar
---------------------------
This library uses the [Gradle](http://gradle.org)-1.10 or later build system. Since I refuse to add the wrapper to the source code, you need to have it installed. Then
```sh
gradle jar
```
produces the jar.

Improving kandidlib-junit
-------------
Of course any IDE may be used to work on kandidlib-emitter but support for gradle makes it more convenient.