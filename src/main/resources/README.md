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

<!--
  This document contains sample invocations of Juggle.
  
  The shell prompt is shown as a dollar ($) in the first column,
  followed by the word 'juggle' and the program's arguments.  Where
  necessary arguments are wrapped onto successive lines by placing
  a backslash (\) in the last column.
  
  The output follows the invocation, and is terminated by a line
  showing the shell prompt again.
  
  Adhering to this strategy strictly allows this file to be parsed
  for sample invocations and comparing their output with the actual
  output of the Juggle program by looking for boundary lines using
  regular expressions such as:
    /^\$ juggle/    -- matches first line of invocation
    /[^\]$/         -- matches invocation termination lines
    /^\$/           -- matches line following output
  
  See src/test/java/com/angellane/juggle/TestSamples for the gory details.

  To document examples that shouldn't be checked with the test harness,
  use an alternative shell prompt such as %
-->
# Juggle: an API search tool for Java

Juggle searches Java libraries for types and members that match
a given declaration.

For example, is there a method that when given a `java.time.Clock` returns a
`java.time.LocalTime`?
```shell
$ juggle "java.time.LocalTime (java.time.Clock)"
public static java.time.LocalTime java.time.LocalTime.now(java.time.Clock)
$
```
Answer: yes, the `now()` method of the `java.time.LocalTime` class.
Juggle shows the signature for the method, with the class and method
names separated by a period.


## What to look for

To ask Juggle a question, provide it with a Java declaration, omitting
parts you're unsure of.  In the above example I wrote something that
looked like a method declaration, but omitted the method name because
I didn't know it.

> **Note:**
> Java declarations tend to use shell characters such as `(` and `)` which the
> shell tries to interpret.  In many of the examples in this file, I surround
> the query with quote marks to tell the shell to back off.  This isn't always
> required; Juggle looks at the entire command-line and forms a query
> from that.

Most of the time Juggle searches for members -- constructors, fields
and methods.  To search for a method, write its return type followed
by a parentheses-wrapped comma-separated parameter list.
```shell
$ juggle 'void (double[], int, int, double)' 
public static void java.util.Arrays.fill(double[],int,int,double)
$
```

If no return type is specified, Juggle shows matching methods with
_any_ return type:
```shell
$ juggle '(double[], int, int, double)' 
public static int java.util.Arrays.binarySearch(double[],int,int,double)
public static void java.util.Arrays.fill(double[],int,int,double)
public static <E> java.util.List<E> java.util.List<E>.of(E,E,E,E)
public static <K,V> java.util.Map<K,V> java.util.Map<K,V>.of(K,V,K,V)
public static <E> java.util.Set<E> java.util.Set<E>.of(E,E,E,E)
$
```
> **Note: Type erasure**
> 
> "What are those last three results?" you might be thinking, "They don't
> match!" You're right. They don't. But at the moment Juggle works on the
> _erased type_ of methods, so these look like methods that take four
> `Object` parameters, and Juggle recognises that each of the types in
> the query -- `double[]`, `int` and `double` -- can be passed as a parameter
> of type `Object`.)

In the same way that you can omit the return type from your query, you
can also omit the parameter list, which will show methods that take _any
number_ of arguments of _any type_.  This provides a means of listing
all the ways of obtaining an object of a specific type:
```shell
$ juggle java.net.Inet6Address
public static java.net.Inet6Address java.net.Inet6Address.getByAddress(String,byte[],int) throws java.net.UnknownHostException
public static java.net.Inet6Address java.net.Inet6Address.getByAddress(String,byte[],java.net.NetworkInterface) throws java.net.UnknownHostException
$
```
So the only way to get a `Inet6Address` appears to be either of the two
static `getByAddress` methods on the `Inet6Address` class.   

> **Note: Type families**
> 
> Of course methods may return an instance of a subclass of their declared 
> return type.  `Inet6Address` is a member of a small family of classes rooted 
> in its parent `InetAddress` class, and it's possible that the runtime type
> returned by one of those methods is actually `Inet6Address`, but Juggle deals 
> in compile-time types.
>
> Another reason why Juggle won't list these methods-returning-a-superclass is
> that doing so would result in long and not particularly helpful outputs.
> (Since `Object` is a superclass of every reference type, at a minimum Juggle
> would have to include hundreds of methods that return `Object` in every 
> result.)

Omitting the return type and parameters will list all methods in the JDK.
While marginally interesting, the output is rather too long to be helpful!

### The hidden `this` parameter

Juggle treats non-static methods as if they have a silent
first parameter whose type is the class in question:
```shell
$ juggle 'java.lang.String (java.util.regex.Matcher, java.lang.String)' 
public String java.util.regex.Matcher.group(String)
public String java.util.regex.Matcher.replaceAll(String)
public String java.util.regex.Matcher.replaceFirst(String)
public static String java.util.Objects.toString(Object,String)
$
```
In the above note how the first argument to `Objects.toString` is an `Object`,
not a `Matcher`. Juggle includes this method in the result set because Java
allows instances of a subclass to be passed to a function that is expecting
a parent class (Widening Reference Conversion). This particular method is
listed last in the results, because Juggle tries to list closer matches
(i.e. where fewer conversions are necessary) first.

To list static methods which take no arguments use `()`.

(This also lists all default constructors, as well as static fields
by virtue of Juggle treating fields as having zero-arg pseudo-getters.)

### Fields: a getter and a setter

Juggle treats data fields as a pair of methods: a setter (which takes an
argument of the field's type and returns `void`), and a getter (which takes
no additional arguments and returns a value of the field's type). 

As with non-static methods, non-static fields have an additional implicit 
`this` parameter:
```shell
$ juggle 'int (java.io.InterruptedIOException)'
public int java.io.InterruptedIOException.bytesTransferred
public native int Object.hashCode()
public static native int System.identityHashCode(Object)
public static native int java.lang.reflect.Array.getLength(Object) throws IllegalArgumentException
public static int java.util.Objects.hashCode(Object)
$
```

### Exceptions

The `throws` clause allows you to filter by methods that might throw a
specific exception type:

```shell
$ juggle throws java.net.URISyntaxException
public java.net.URI.<init>(String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String,int,String,String,String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String,String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String,String,String) throws java.net.URISyntaxException
public java.net.URI java.net.URI.parseServerAuthority() throws java.net.URISyntaxException
public java.net.URI java.net.URL.toURI() throws java.net.URISyntaxException
$
```

Juggle will list all methods that include the named exception (or a subclass
of the named type) in their `throws` clause.  Specifying multiple exception
types will show only the methods that might throw _all_ the specified 
exceptions.

A query that ends `throws` (without being followed by any exception type) will
show all methods that declare no thrown types.

### Annotations

You can also ask Juggle to look for members that have particular annotations.
Juggle will list methods that have the named annotations, but it's not 
possible to include annotation data in the query.  If multiple annotations
are supplied, they must all be present on the class or method.
```shell
$ juggle @FunctionalInterface int
public abstract int java.util.Comparator<T>.compare(T,T)
public abstract int java.util.function.DoubleToIntFunction.applyAsInt(double)
public abstract int java.util.function.IntBinaryOperator.applyAsInt(int,int)
public abstract int java.util.function.IntUnaryOperator.applyAsInt(int)
public abstract int java.util.function.LongToIntFunction.applyAsInt(long)
public abstract int java.util.function.ToIntBiFunction<T,U>.applyAsInt(T,U)
public abstract int java.util.function.ToIntFunction<T>.applyAsInt(T)
public abstract int java.util.function.IntSupplier.getAsInt()
$
```

### Member names

You can follow the return type with a member name to match only members
with that name (case-sensitive, exact match):
```shell
$ juggle String substring
public String AbstractStringBuilder.substring(int)
public String AbstractStringBuilder.substring(int,int)
public String String.substring(int)
public String String.substring(int,int)
public volatile String StringBuilder.substring(int)
public volatile String StringBuilder.substring(int,int)
public synchronized String StringBuffer.substring(int)
public synchronized String StringBuffer.substring(int,int)
$
```

It's also possible to match a member name using a regular expression
by surrounding its partial name with slash characters.  An `i` after
the closing slash results in a case-insensitive match.

```shell
$ juggle String /package/i
public String Package.getImplementationTitle()
public String Package.getImplementationVendor()
public String Package.getImplementationVersion()
public String Package.getName()
public String Class<T>.getPackageName()
public String Package.getSpecificationTitle()
public String Package.getSpecificationVendor()
public String Package.getSpecificationVersion()
public String Package.toString()
public String java.lang.constant.ClassDesc.packageName()
$
```
Note how the member is considered to match if either of these two names match:
1. The member's simple name (i.e. the declaration name)
2. The member's canonical name (its declaration name prefixed with the
   declaring package and class)  

### Constructors

Juggle treats constructors as if they were static methods called `<init>`
returning an instance of the declaring class.  In the following example
you'll see a couple of constructors and a static method, all with broadly
similar signatures:
```shell
$ juggle 'java.io.InputStream (String)'
public static java.io.InputStream ClassLoader.getSystemResourceAsStream(String)
public java.io.FileInputStream.<init>(String) throws java.io.FileNotFoundException
public java.io.StringBufferInputStream.<init>(String)
$
```

> **Note: Reference widening conversion**
> 
> Here we see an example of a Reference Widening converion: `FileInputStream`
> and `StringBufferInputStream` are both descendant classes of `InputStream`,
> so objects of those first two types can be assigned to a variable of the
> latter type.

> **Note: Constructor name**
> 
> Juggle does not provide a means of searching for a constructor by name.

## Where to look

You can tell Juggle which JARs to include in the search by using the `-j`
option:
```shell
$ juggle -j build/libs/testLib.jar package com.angellane.juggle.testinput.lib.Lib
public static com.angellane.juggle.testinput.lib.Lib com.angellane.juggle.testinput.lib.Lib.libFactory()
com.angellane.juggle.testinput.lib.Lib.<init>()
$
```

> **Warning: the `-j` option may change
> 
> In the future I expect to replace the `-j` option with a more comprehensive
> `-cp` option that allows a search classpath to be specified so that Juggle
> can search JARs as well as directories of class files. See GitHub issue #5.

The `-m` flag can be used to specify JMODs to search.  Juggle will also search
any modules that this module requires transitively (sometimes referred to as
"implied reads").

```shell
$ juggle -m java.sql java.sql.CallableStatement
public abstract java.sql.CallableStatement java.sql.Connection.prepareCall(String) throws java.sql.SQLException
public abstract java.sql.CallableStatement java.sql.Connection.prepareCall(String,int,int) throws java.sql.SQLException
public abstract java.sql.CallableStatement java.sql.Connection.prepareCall(String,int,int,int) throws java.sql.SQLException
$
```

By default Juggle searches for modules in the current working directory.
To change this, use the `-p` / `--module-path` option.

At present there's no support for scanning an unpacked JAR, or a directory of
class files.

## Sorting the results

Juggle can sort its output in a number of ways. Specify sort criteria using
`-s`. The first criteria sorts the results, with ties resolved by subsequent
criteria.

| Option         | Description                                                             |
|----------------|-------------------------------------------------------------------------|
| `-s access`    | Shows members by access, with `public` first and `private` last         |
| `-s hierarchy` | Classes higher in the `extends`/`implements` hierarchy are listed first | 
| `-s name`      | Sorts results by name alphabetically                                    |
| `-s package`   | Orders members from imported (`-i`) packages before others              |
| `-s score`     | Shows best matches for a query first                                    |
| `-s text`      | Sort by output text (approximately)                                     |

The default sort is equivalent to `-s score -s hierarchy -s access -s package -s name -s text`.
The intent is that this default causes Juggle to list the "best" matches first.
If that's not what's happening in practice, I'd like to hear about it! 

## Output format

The `-f` option allows you to select the format in which Juggle output
results.  The two key values are `colour` and `plain`, where the former
uses ANSI escape sequences to highlight certain elements of the output
while the latter outputs unformatted text only.

The default formatter is `auto`, which is the same as `colour` if Juggle
is connected to a console or `plain` otherwise.

| Formatter   | Description                                   |
|-------------|-----------------------------------------------|
| `-f plain`  | Unformatted, plain text                       |
| `-f colour` | Text highlighted with ANSI escape sequences   |
| `-f color`  | Alternative spelling of `-f colour`           |
| `-f auto`   | Automatically choose                          |


## Extra goodies

### Modifiers

By default, Juggle will only show `public` members. Including an access
modifier will set an alternative minimum level of accessibility
(`public`, `package`,`protected`, or `private`).
```shell
$ juggle 'protected java.io.OutputStream ()'
public java.io.OutputStream.<init>()
public static java.io.OutputStream java.io.OutputStream.nullOutputStream()
public static final java.io.PrintStream System.err
public static final java.io.PrintStream System.out
public java.io.ByteArrayOutputStream.<init>()
public java.io.PipedOutputStream.<init>()
protected java.io.ObjectOutputStream.<init>() throws java.io.IOException,SecurityException
$
```

Of course `private` members can't be used, so `protected` is likely the most
nosey you should be.

> **Note: Default access**
> 
> Use the word `package` as an access modifier if you want to list methods
> that have at least the default level of access.

This output also shows that Juggle is inspecting the runtime and not the
specification. That can result in some pseudo-private members or classes
leaking into output. Just because you _can_ call a method doesn't mean you
_should_.

All other member and type modifiers are supported too (`static`, `final`,
`synchronized`, `volatile`, `transient`, `native`, `abstract`, `strictfp`,
`sealed` and `non-sealed`).  A candidate member or type must match _all_
specified modifiers to be included in the results.


### Parameter Metadata

Juggle supports matching parameter metadata as well, but this isn't as useful
as you might expect.  There are three types of parameter metadata:
1. Annotations
2. The `final` modifier
3. Parameter name

Of these, annotations can always be checked (but only those with a `RUNTIME`
retention policy).  To match either of the other two, the metadata must be
present in the class file.  This is achieved by passing the `-parameters` flag
to `javac` when compiling the original source.  This restriction also applies
to classes in the JDK itself, and unfortunately I'm not aware of any JDKs that
have been built in this way.

> **Note: Building a JDK with parameter metadata**
>
> If you want to build your own JDK to do this, it might be as simple as
> specifying exporting a `JAVAC_FLAGS=-parameters` environment variable 
> before following the usual build instructions.

When checking a candidate against a query, if the candidate wasn't compiled with
parameter metadata, then those elements of the query are ignored (i.e. the
parameter is matched by annotations and type only).


### Permutation of Parameters

Sometimes you might not know the order of parameters.  The `-x` option causes
Juggle to also match methods whose parameter types match the supplied ones, but
not necessarily in the order you specified.

For example, there are no methods that take a `double[]`, an `int`, a
`double` and then an `int`:

```shell
$ juggle "void (double[],int,double,int)"
$
```

However, allowing Juggle to permute parameters, locates a match:

```shell
$ juggle -x "void (double[],int,double,int)"
public static void java.util.Arrays.fill(double[],int,int,double)
$
```

> **Warning:**
> Parameter permutation can significantly increase runtime,
> so it's not enabled by default.

### Imports

To make life easier, packages can be imported with `-i` so that fully qualified
class names don't have to be written out each time. As you would expect,
`java.lang` is always implicitly imported.  Juggle omits imported package 
names in its output.
```shell
% juggle                                                                \
    -j build/libs/juggle-1.0-SNAPSHOT.jar                               \
    -i com.angellane.juggle                                             \
    -i java.util                                                        \
    -r CartesianProduct
public CartesianProduct<T>.<init>(List<E>[])
public static <T> CartesianProduct<T> CartesianProduct<T>.of(List<E>[])
%
```
> **Note: Type arguments**
> 
> Juggle doesn't yet unify type arguments.  The `E` and `T` in the The last
> match in the above example refer to the same thing, so Juggle should
> ideally have output 
> `public static <T> CartesianProduct<T> CartesianProduct<T>.of(List<T>[])`.

### Wildcards

Juggle adapts Java's wildcard syntax to allow querying for unknown or
partially-known types.

A simple `?` wildcard can stand for any type.  So here are all the members
that take a `String`, an `int` and two other parameters of unknown type:
```shell
$ juggle '(String,int,?,?)'
public java.net.Socket.<init>(String,int,java.net.InetAddress,int) throws java.io.IOException
public java.text.StringCharacterIterator.<init>(String,int,int,int)
public javax.security.auth.callback.ConfirmationCallback.<init>(String,int,int,int)
public javax.security.auth.callback.ConfirmationCallback.<init>(String,int,String[],int)
public static int Integer.parseInt(CharSequence,int,int,int) throws NumberFormatException
public static long Long.parseLong(CharSequence,int,int,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(CharSequence,int,int,int) throws NumberFormatException
public static long Long.parseUnsignedLong(CharSequence,int,int,int) throws NumberFormatException
public static <E> java.util.List<E> java.util.List<E>.of(E,E,E,E)
public static <K,V> java.util.Map<K,V> java.util.Map<K,V>.of(K,V,K,V)
public static <E> java.util.Set<E> java.util.Set<E>.of(E,E,E,E)
$
```

Specifying a `?` as a return type is equivalent to leaving it out.

This helps us get around an ambiguity in Juggle's parser.  Juggle assumes
that the first identifier in the query is the return type, so `juggle substring`
causes it to look for a type called `substring` not a method name.  To find
all members called `substring`, we can say:
```shell
$ juggle \? substring
public String AbstractStringBuilder.substring(int)
public String AbstractStringBuilder.substring(int,int)
public String String.substring(int)
public String String.substring(int,int)
public volatile String StringBuilder.substring(int)
public volatile String StringBuilder.substring(int,int)
public synchronized String StringBuffer.substring(int)
public synchronized String StringBuffer.substring(int,int)
$
```

The same ambiguity exists with parameter names. If you want to search for a
method with parameters of a particular name but unknown type, use an unbounded
wildcard.

### Bounded Wildcards

Juggle also supported bounded wildcards.  Lower bounds are specified as
`? super Inet6Address`; this matches `Inet6Address` as well as its super
classes and interfaces (i.e. `InetAddress`, `Object` or `Serializable`).

Upper bounds are specified `? extends InetAddress`, which would match
`InetAddress` and any subclasses (i.e. `Inet4Address` and `Inet6Address`).
Multiple upper bounds (a class and an interface, or multiple interfaces)
are separated with an ampersand: `? extends InetAddress & Serializable`.

Lower-bounds are typically used on parameter specifications and upper
bounds on return types.

So for example, what could replace `UnknownMethod` in this code?
```java
class Foo {
    CharSequence f(String s, int i, int j) {
        return UnknownMethod(myString, i, j);
    }
}
```

Let's ask Juggle. What's a method that takes a `String` (or any superclass
of `String`) and two `int`s, and returns a `CharSequence` (or any subclass)?
```shell
$ juggle '? extends CharSequence (? super String,int,int)'
public CharSequence String.subSequence(int,int)
public abstract CharSequence CharSequence.subSequence(int,int)
public String String.substring(int,int)
public static java.nio.CharBuffer java.nio.CharBuffer.wrap(CharSequence,int,int)
$
```

Since Java normally applies these conversions automatically, Juggle does too.

If the query doesn't use a _bounded_ wildcard, Juggle considers each parameter
type to be a lower bound for a wildcard, and return and exception types to be
upper bounds.

So the above query can be written more naturally, and Juggle will give the
same results:
```shell
$ juggle 'CharSequence (String,int,int)'
public CharSequence String.subSequence(int,int)
public abstract CharSequence CharSequence.subSequence(int,int)
public String String.substring(int,int)
public static java.nio.CharBuffer java.nio.CharBuffer.wrap(CharSequence,int,int)
$
```

### Controlling Conversion

Sometimes these automatic conversions get in the way.  You can control whether
Juggle performs conversions (replacing types with bounded wildcards) using
the `-c` or `--conversions` option. 

| Conversion | Description                                                                    |
|------------|--------------------------------------------------------------------------------|
| `-c all`   | Treat plain parameters as lower bounded wildcards, and return as upper bounded |
| `-c none`  | Apply no conversions; all types are matched exactly                            |
| `-c auto`  | Behave as `all` if no bounded wildcards appear in query, or `none` otherwise   |

The default is `-c auto`.


## Type Matches

Juggle's most useful for finding members.  But you can also ask it about data
types by providing the header of a type declaration.

What types extend `AccessibleObject`?
```shell
$ juggle class extends java.lang.reflect.AccessibleObject
public abstract sealed class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration permits java.lang.reflect.Constructor<T>, java.lang.reflect.Method
public final class java.lang.reflect.Field extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member
public final class java.lang.reflect.Constructor<T> extends java.lang.reflect.Executable
public final class java.lang.reflect.Method extends java.lang.reflect.Executable
$
```

Note that this shows direct and indirect derived classes.   To see just those
classes that directly extend a base class, turn off conversions:
```shell
$ juggle -c none class extends java.lang.reflect.AccessibleObject
public abstract sealed class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration permits java.lang.reflect.Constructor<T>, java.lang.reflect.Method
public final class java.lang.reflect.Field extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member
$
```

All the other kinds of type are also supported: `enum`, `record`, 
`interface` and `@interface`. Amd just like when searching for members,
it's possible to restrict the search by specifying annotations and other
modifiers.

> **Note: `class` and `interface`**
> 
> If you're looking for a class, you must use the `class` keyword.
> If you're looking for an interface, you must use `interface`.
> There's no way to ask Juggle to list every super-type (`class` or
> `interface`) of a class in a single query.


## Command-line summary

Each command-line option has a long name equivalent. This table summarises all options.

| Option | Long Equivalent | Argument                                                  | Default                                                      | Description                                         |
|--------|-----------------|-----------------------------------------------------------|--------------------------------------------------------------|-----------------------------------------------------|
| `-c`   | `--conversions` | `auto`, `none`, `all`                                     | `-c auto`                                                    | Whether to apply type conversions                   |
| `-i`   | `--import`      | package name                                              |                                                              | Packages to import (`java.lang` is always searched) |
| `-j`   | `--jar`         | file path                                                 |                                                              | JAR files to search                                 |
| `-m`   | `--add-module`  | module name(s)                                            | `-m java.base`                                               | JMODs to search                                     |
| `-p`   | `--module-path` | path`:`path                                               | `-p .`                                                       | Directories to search for modules                   |
| `-s`   | `--sort`        | `access`, `hierarchy`, `name`, `package`, `score`, `text` | `-s score -s hierarchy -s access -s package -s name -s text` | Sort criteria                                       |
| `-x`   | `--permute`     | (none)                                                    | (don't permute)                                              | Match permutations of supplied parameters           |
| `-f`   | `--format`      | `auto`, `colour`, `color`, `plain`                        | `auto`                                                       | Output format                                       |

A declaration-style query can follow all arguments.