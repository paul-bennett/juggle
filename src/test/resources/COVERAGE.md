<!-- 
    Juggle -- an API search tool for Java
   
    Copyright 2020,2023 Paul Bennett
   
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
   
       http://www.apache.org/licenses/LICENSE-2.0
   
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
# Coverage Tests

This file contains invocations designed to increase code coverage of the test suite.  Many of the samples here
don't actually do anything useful, but instead the combination of command-line parameters have been carefully
selected in order to drive up the JaCoCo coverage metrics.


## Command-line Parsing

If we pass an invalid argument, we should get an error and the help text:

```shell
$ juggle --fiddle-de-dee
Unknown option: '--fiddle-de-dee'
Usage: juggle [-hVx] [--dry-run] [--show-query] [-c=none|all|auto] [-cp=path]
              [-f=auto|plain|colour|color] [-i=packageName] [-m=moduleName]
              [-p=modulePath] [-s=access|hierarchy|name|package|score|text]
              [declaration...]
An API search tool for Java
      [declaration...]       A Java-style declaration to match against
  -c, --conversions=none|all|auto
                             Which conversions to apply
      -cp, --classpath, --class-path=path
                             JAR file or directory to include in search
      --dry-run              Dry run only
  -f, --format=auto|plain|colour|color
                             Output format
  -h, --help                 Show this help message and exit.
  -i, --import=packageName   Imported package names
  -m, --module, --add-modules=moduleName
                             Modules to search
  -p, --module-path=modulePath
                             Where to look for modules
  -s, --sort=access|hierarchy|name|package|score|text
                             Sort criteria
      --show-query           Show query
  -V, --version              Print version information and exit.
  -x, --[no-]permute         Also match permutations of parameters
$
```

Of course, we can explicitly ask for help:

```shell
$ juggle --help
Usage: juggle [-hVx] [--dry-run] [--show-query] [-c=none|all|auto] [-cp=path]
              [-f=auto|plain|colour|color] [-i=packageName] [-m=moduleName]
              [-p=modulePath] [-s=access|hierarchy|name|package|score|text]
              [declaration...]
An API search tool for Java
      [declaration...]       A Java-style declaration to match against
  -c, --conversions=none|all|auto
                             Which conversions to apply
      -cp, --classpath, --class-path=path
                             JAR file or directory to include in search
      --dry-run              Dry run only
  -f, --format=auto|plain|colour|color
                             Output format
  -h, --help                 Show this help message and exit.
  -i, --import=packageName   Imported package names
  -m, --module, --add-modules=moduleName
                             Modules to search
  -p, --module-path=modulePath
                             Where to look for modules
  -s, --sort=access|hierarchy|name|package|score|text
                             Sort criteria
      --show-query           Show query
  -V, --version              Print version information and exit.
  -x, --[no-]permute         Also match permutations of parameters
$
```

We can ask for the version of the application, but when run from an unpacked
source tree it doesn't show anything useful:
```shell
$ juggle --version
juggle (unreleased version)
Java Runtime 17.0
$
```

## No parameters in the query

```shell
$ juggle NoSuchMethodException
public NoSuchMethodException.<init>()
public NoSuchMethodException.<init>(String)
$
```

## Missing module

```shell
$ juggle -m this.module.does.not.exist
*** Error: Module this.module.does.not.exist not found
$
```

## Unknown type

```shell
$ juggle 'boolean (ThisTypeDoesNotExist)'
*** Error: Couldn't find type: ThisTypeDoesNotExist
$
```

## Methods that don't throw

```shell
$ juggle 'String (? super java.io.InputStream) throws'
public String Object.toString()
public static String String.valueOf(Object)
public static String java.util.Objects.toString(Object)
$
```

If it wasn't for the `throws` we'd expect the above query to also include 
`String java.net.URLConnection.guessContentTypeFromStream(java.io.InputStream)` in its results.


## Trying to find WildcardType

The biggest area that presently lacks test coverage is `TextOutput.decodeWildcardType()`.  This function
doesn't seem to be called at all, even when I explicitly search for a method that the JavaDoc suggests
returns a wildcard type:

```shell
$ juggle /asSubclass/
public <U> Class<T> Class<T>.asSubclass(Class<T>)
$
```

The `Class.asSubclass` method is declared to return `Class<? extends U>`. I suspect the wildcard is eliminated
at runtime due to type erasure, in which case it may be worth stripping this method from the source altogether.


## Empty -m

```shell
$ juggle -m '' /getUpperBound/
public abstract java.lang.reflect.Type[] java.lang.reflect.WildcardType.getUpperBounds()
$
```


## Multiple -t options

```shell
$ juggle throws java.io.NotActiveException, java.io.InvalidObjectException
public void java.io.ObjectInputStream.registerValidation(java.io.ObjectInputValidation,int) throws java.io.NotActiveException,java.io.InvalidObjectException
$
```

## Explicit value of -x

By default, boolean arguments in picocli carry no value.  If you specify them on the command-line, the value `true`
is passed to the corresponding function.  

It feels wrong within the setter function to not use the value of the boolean parameter, even though we know it
will only ever take the value `true`.  That means JaCoCo will always present one path in an `if` statement as not
followed.

```shell
$ juggle '(String,ClassLoader,boolean)'
public static <E> java.util.List<E> java.util.List<E>.of(E,E,E)
public static <E> java.util.Set<E> java.util.Set<E>.of(E,E,E)
$
```

```shell
$ juggle -x '(String,ClassLoader,boolean)'
public static Class<T> Class<T>.forName(String,boolean,ClassLoader) throws ClassNotFoundException
public void ClassLoader.setClassAssertionStatus(String,boolean)
public void ClassLoader.setPackageAssertionStatus(String,boolean)
public static <E> java.util.List<E> java.util.List<E>.of(E,E,E)
public static <E> java.util.Set<E> java.util.Set<E>.of(E,E,E)
$
```

But there's a workaround... add `negatable=true` to the `@Option` annotation, and 
suddenly the long option name can be prefixed with `no-` on the command-line.

```shell
$ juggle --permute '(String,ClassLoader,boolean)'
public static Class<T> Class<T>.forName(String,boolean,ClassLoader) throws ClassNotFoundException
public void ClassLoader.setClassAssertionStatus(String,boolean)
public void ClassLoader.setPackageAssertionStatus(String,boolean)
public static <E> java.util.List<E> java.util.List<E>.of(E,E,E)
public static <E> java.util.Set<E> java.util.Set<E>.of(E,E,E)
$
```

```shell
$ juggle --no-permute '(String,ClassLoader,boolean)'
public static <E> java.util.List<E> java.util.List<E>.of(E,E,E)
public static <E> java.util.Set<E> java.util.Set<E>.of(E,E,E)
$
```

## Missing dependency

The (contrived) App class from testApp uses the Lib class from testLib in its interface, but doesn't include these
dependent classes in the JAR (it's not an uberjar).  This means trying to load the App class fails.  

```shell
$ juggle -cp build/libs/testApp.jar 'com.angellane.juggle.testinput.app.App()'            
*** Warning: related class com.angellane.juggle.testinput.app.App: java.lang.NoClassDefFoundError: com/angellane/juggle/testinput/lib/Lib
*** Error: Couldn't find type: com.angellane.juggle.testinput.app.App
$
```


## Methods with no modifiers

Curiously this test fails here, but works in README.md.  See GitHub issue #39.
```shell
% juggle -cp build/libs/testLib.jar package com.angellane.juggle.testinput.lib.Lib
public static com.angellane.juggle.testinput.lib.Lib com.angellane.juggle.testinput.lib.Lib.libFactory()
com.angellane.juggle.testinput.lib.Lib.<init>()
%
```

## Dry-Run and Show-Query options

```shell
$ juggle --dry-run --show-query record
QUERY: TypeQuery{flavour=RECORD, annotationTypes=null, accessibility=PUBLIC, modifierMask=0, modifiers=0, declarationPattern=null, supertype=null, superInterfaces=null, subtype=null, isSealed=null, permittedSubtypes=null, recordComponents=null}
$
```
```shell
$ juggle --dry-run --show-query '()'
QUERY: MemberQuery{annotationTypes=null, accessibility=PUBLIC, modifierMask=0, modifiers=0, returnType=null, declarationPattern=null, params=[], exceptions=null}
$
```

## Classpath source

```shell
$ juggle -cp build/classes/java/main com.angellane.juggle.Juggler
public com.angellane.juggle.Juggler.<init>()
public com.angellane.juggle.Juggler com.angellane.juggle.Main.juggler
public com.angellane.juggle.Juggler com.angellane.juggle.source.Source.getJuggler()
$
```

```shell
$ juggle -cp this-path-does-not-exist             
*** Error: Couldn't locate this-path-does-not-exist
$
```

This test will only work on UNIX-like operating systems:
```shell
$ juggle -cp /dev/null
*** Error: Not a file or directory: `/dev/null'
$
```

And this one relies on `/etc/sudoers` being unreadable:
```shell
$ juggle -cp /etc/sudoers
*** Error: /etc/sudoers (Permission denied)
$
```
