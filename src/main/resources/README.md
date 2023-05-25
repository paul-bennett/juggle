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
````
$ juggle "java.time.LocalTime (java.time.Clock)"
public static java.time.LocalTime java.time.LocalTime.now(java.time.Clock)
$
````
Answer: yes, the `now()` method of the `java.time.LocalTime` class.
Juggle shows the signature for the method, with the class and method
names separated by a period.

> **Note**
> This document is a little inconsistent in its use of wildcards in queries.
> Our intent is for Juggle to infer wildcards in most instances, at which
> point many of the example queries in this document can be simplified.
> 
> We've not explained that in detail in the narrative, looking forward to
> the day that GitHub issue #58 is implemented.


## What to look for

To ask Juggle a question, provide it with a Java declaration, omitting
parts you're unsure of.  In the above example we wrote something that
looked like a method declaration, but omitted the method name because
we didn't know it.

Most of the time Juggle searches for members -- constructors, fields
and methods.  To search for a method, write its return type followed
by a parentheses-wrapped comma-separated parameter list.
````
$ juggle void '(double[], int, int, double)' 
public static void java.util.Arrays.fill(double[],int,int,double)
$
````

If no return type is specified, Juggle shows matching methods with
_any_ return type:
````
$ juggle '(double[], int, int, double)' 
public static int java.util.Arrays.binarySearch(double[],int,int,double)
public static void java.util.Arrays.fill(double[],int,int,double)
$
````

Similarly, omitting the parameter list shows methods that take _any
number_ of arguments of _any type_.  This provides a means of listing
all the ways of obtaining an object of a specific type:
````
$ juggle java.net.Inet6Address
public static java.net.Inet6Address java.net.Inet6Address.getByAddress(String,byte[],int) throws java.net.UnknownHostException
public static java.net.Inet6Address java.net.Inet6Address.getByAddress(String,byte[],java.net.NetworkInterface) throws java.net.UnknownHostException
$
````
So the only way to get a `Inet6Address` appears to be either of the two
static `getByAddress` methods on the `Inet6Address` class.   

Of course methods may return an instance of a subclass of their declared return
type.  `Inet6Address` is a member of a small family of classes rooted in its
parent `InetAddress` class, and it's possible that the runtime type of an 
object returned by one of those methods is actually an `Inet6Address`, but
Juggle deals in compile-time types.

Another reason why Juggle won't list these methods-returning-a-superclass is
that doing so would result in long and not particularly helpful outputs.
(Since `Object` is a superclass of every reference type, at a minimum Juggle
would have to include hundreds of methods that return `Object` in every 
result.)

Omitting the return type and parameters will list all methods in the JDK.
While marginally interesting, the output is rather too long to be helpful!

Juggle treats non-static methods as if they have a silent
first parameter whose type is the class in question:
````
$ juggle 'java.lang.String (? super java.util.regex.Matcher, java.lang.String)' 
public String java.util.regex.Matcher.group(String)
public String java.util.regex.Matcher.replaceAll(String)
public String java.util.regex.Matcher.replaceFirst(String)
public static String java.util.Objects.toString(Object,String)
$
````
In the above note how the first argument to `Objects.toString` is an `Object`,
not a `Matcher`. Juggle includes this method in the result set because Java
allows instances of a subclass to be passed to a function that is expecting
a parent class (Widening Reference Conversion). This particular method is
listed last in the results, because Juggle tries to list closer matches
(i.e. where fewer conversions are necessary) first.

Juggle treats data fields as a pair of methods: a setter (which takes an
argument of the field's type and returns `void`), and a getter (which takes
no additional arguments and returns a value of the field's type). 

As with non-static methods, non-static fields have an additional implicit 
`this` parameter:
````
$ juggle 'int (? super java.io.InterruptedIOException)'
public int java.io.InterruptedIOException.bytesTransferred
public native int Object.hashCode()
public static native int System.identityHashCode(Object)
public static native int java.lang.reflect.Array.getLength(Object) throws IllegalArgumentException
public static int java.util.Objects.hashCode(Object)
public static int sun.invoke.util.ValueConversions.widenSubword(Object)
$
````

To list static methods which take no arguments use `()`.

(This also lists all default constructors, as well as static fields
by virtue of Juggle treating fields as having zero-arg pseudo-getters.)

The `throws` clause allows you to filter by methods that might throw a
specific exception type:

````
$ juggle throws java.net.URISyntaxException
public java.net.URI.<init>(String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String,int,String,String,String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String,String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String,String,String) throws java.net.URISyntaxException
public java.net.URI java.net.URI.parseServerAuthority() throws java.net.URISyntaxException
public java.net.URI java.net.URL.toURI() throws java.net.URISyntaxException
$
````

Juggle will list all methods that include the named exception (or a subclass
of the named type) in their `throws` clause.  Specifying multiple exception
types will show only the methods that might throw _all_ the specified 
exceptions.

A query that ends `throws` (without being followed by any exception type) will
show all methods that declare no thrown types.

You can also ask Juggle to look for annotations.  Juggle will list methods
that have the named annotations, but it's not possible to include annotation
data in the query.  If multiple annotations are supplied, they must all be
present on the class or method.
````
$ juggle @FunctionalInterface int
public abstract int java.util.function.DoubleToIntFunction.applyAsInt(double)
public abstract int java.util.function.IntBinaryOperator.applyAsInt(int,int)
public abstract int java.util.function.IntUnaryOperator.applyAsInt(int)
public abstract int java.util.function.LongToIntFunction.applyAsInt(long)
public abstract int java.util.function.ToIntBiFunction<T,U>.applyAsInt(T,U)
public abstract int java.util.function.ToIntFunction<T>.applyAsInt(T)
public abstract int java.util.Comparator<T>.compare(T,T)
public abstract int java.util.function.IntSupplier.getAsInt()
$
````

You can follow the return type with a member name to match only members
with that name (case-sensitive, exact match):
````
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
````

It's also possible to match a member name using a regular expression
by surrounding its partial name with slash characters.  An `i` after
the closing slash results in a case-insensitive match.

````
$ juggle String /package/i
public String Class<T>.getPackageName()
public static final String jdk.internal.module.ClassFileConstants.MODULE_PACKAGES
public static final String sun.reflect.misc.ReflectUtil.PROXY_PACKAGE
public String sun.util.locale.provider.LocaleProviderAdapter.Type.getTextResourcesPackage()
public String sun.util.locale.provider.LocaleProviderAdapter.Type.getUtilResourcesPackage()
public String jdk.internal.org.objectweb.asm.commons.Remapper.mapPackageName(String)
public String java.lang.constant.ClassDesc.packageName()
public String jdk.internal.org.objectweb.asm.ClassReader.readPackage(int,char[])
public static String jdk.internal.module.Checks.requirePackageName(String)
public static String jdk.internal.module.Resources.toPackageName(String)
$
````

## Where to look

You can tell Juggle which JARs to include in the search by using the `-j`
option:
````
$ juggle -j build/libs/testLib.jar package com.angellane.juggle.testinput.lib.Lib
public static com.angellane.juggle.testinput.lib.Lib com.angellane.juggle.testinput.lib.Lib.libFactory()
com.angellane.juggle.testinput.lib.Lib.<init>()
$
````

The `-m` flag can be used to specify JMODs to search.  Juggle will also search
any modules that this module requires transitively (sometimes referred to as
"implied reads").

````
$ juggle -m java.sql java.sql.CallableStatement
public abstract java.sql.CallableStatement java.sql.Connection.prepareCall(String) throws java.sql.SQLException
public abstract java.sql.CallableStatement java.sql.Connection.prepareCall(String,int,int) throws java.sql.SQLException
public abstract java.sql.CallableStatement java.sql.Connection.prepareCall(String,int,int,int) throws java.sql.SQLException
$
````

At present there's no support for scanning an unpacked JAR, or a directory of
class files.

## Sorting the results

Juggle can sort its output in a number of ways. Specify sort criteria using
`-s`. The first criteria sorts the results, with ties resolved by any
subsequent criteria.

| Option       | Description                                                     |
|--------------|-----------------------------------------------------------------|
| `-s access`  | Shows members by access, with `public` first and `private` last |
| `-s name`    | Sorts results by name alphabetically                            |
| `-s package` | Orders members from imported (`-i`) packages before others      |
| `-s score`   | Shows best matches for a query first                            |
| `-s text`    | Sort by output text (approximately)                             |

The default sort is equivalent to `-s score -s access -s package -s name -s text`.
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
````
$ juggle 'protected ? extends java.io.OutputStream ()'
public static final java.io.PrintStream System.err
public static final java.io.PrintStream System.out
public java.io.ByteArrayOutputStream.<init>()
public java.io.OutputStream.<init>()
public java.io.PipedOutputStream.<init>()
public static java.io.OutputStream java.io.OutputStream.nullOutputStream()
public sun.net.www.http.PosterOutputStream.<init>()
public sun.security.util.DerOutputStream.<init>()
protected java.io.ObjectOutputStream.<init>() throws java.io.IOException,SecurityException
$
````

Of course `private` members can't be used, so `protected` is likely the most
nosey you should be.

This output also shows that Juggle is inspecting the runtime and not the
specification. That can result in some pseudo-private members or classes
(such as `sun.security.util.DerOutputStream` above) leaking into output.
Just because you _can_ call a method doesn't mean you _should_.

### Permutation of Parameters

Sometimes you might not know the order of parameters.  The `-x` option causes
Juggle to also match methods whose parameter types match the supplied ones, but
not necessarily in the order you specified.

For example, there are no methods that take a `double[]`, an `int`, a
`double` and then an `int`:

````
$ juggle "void (double[],int,double,int)"
$
````

However, if we allow Juggle to permute parameters, it locates a match:

````
$ juggle -x "void (double[],int,double,int)"
public static void java.util.Arrays.fill(double[],int,int,double)
$
````

Note that parameter permutation can significantly increase runtime,
so it's not enabled by default.

### Imports

To make life easier, packages can be imported with `-i` so that fully qualified
class names don't have to be written out each time. As you would expect,
`java.lang` is always implicitly imported.  Juggle omits imported package 
names in its output.
````
% juggle                                                                \
    -j build/libs/juggle-1.0-SNAPSHOT.jar                               \
    -i com.angellane.juggle                                             \
    -i java.util                                                        \
    -r CartesianProduct
public CartesianProduct<T>.<init>(List<E>[])
public static <T> CartesianProduct<T> CartesianProduct<T>.of(List<E>[])
%
````
(Note that in this example, Juggle isn't yet unifying the type arguments;
ideally it should output `CartesianProduct<T>.of(List<T>[])`.)

### Wildcards

Juggle adapts Java's wildcard syntax to allow querying for unknown or
partially-known types.

A simple `?` wildcard can stand for any type.  So here are all the members
that take a `String`, an `int` and two other parameters of unknown type:
````
juggle '(String,int,?,?)'
public java.net.Socket.<init>(String,int,java.net.InetAddress,int) throws java.io.IOException
public java.text.StringCharacterIterator.<init>(String,int,int,int)
public javax.security.auth.callback.ConfirmationCallback.<init>(String,int,int,int)
public javax.security.auth.callback.ConfirmationCallback.<init>(String,int,String[],int)
public static java.util.Map<K,V> sun.util.locale.provider.CalendarDataUtility.retrieveFieldValueNames(String,int,int,java.util.Locale)
public static java.util.Map<K,V> sun.util.locale.provider.CalendarDataUtility.retrieveJavaTimeFieldValueNames(String,int,int,java.util.Locale)
$
````

Specifying a `?` as a return type is equivalent to leaving it out.  This
helps us get around an ambiguity in Juggle's parser.  Juggle assumes that
the first identifier in the query is the return type, so `juggle substring`
causes it to look for a type called `substring` not a method name.  To find
all members called `substring`, we can say:
````
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
````
As you might expect, with many shells we need to escape this special charater.

Juggle also supported bounded wildcards.  Lower bounds are specified as
`? super Inet6Address`; this matches `Inet6Address` as well as its super
classes and interfaces (i.e. `InetAddress`, `Object` or `Serializable`).

Upper bounds are specified `? extends InetAddress`, which would match
`InetAddress` and any subclasses (i.e. `Inet4Address` and `Inet6Address`).
Multiple upper bounds (a class and an interface, or multiple interfaces)
are separated with an ampersand: `? extends InetAddress & Serializable`.

### Type Matches

Juggle's most useful for finding members.  But you can also ask it about data
types by providing the header of a type declaration.

What types directly extend `AccessibleObject`?
````
$ juggle class extends java.lang.reflect.AccessibleObject
public abstract class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration
public final class java.lang.reflect.Field extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member
$
````
Note that this doesn't show indirect derived classes.  To do that we need
to ask Juggle for the classes that extends an unknown class which itself
extends the class in question.  This looks a little clumsy!

````
$ juggle class extends \? extends java.lang.reflect.AccessibleObject
public final class java.lang.reflect.Constructor<T> extends java.lang.reflect.Executable
public abstract class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration
public final class java.lang.reflect.Field extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member
public final class java.lang.reflect.Method extends java.lang.reflect.Executable
$
````

All of the other kinds of type are also supported: `enum`, `record`, 
`interface` and `@interface`. As with member queries, we can restrict the
search by specifying annotations and other modifiers.

### Constructors

Juggle treats constructors as if they were static methods called `<init>`
returning an instance of the declaring class.  In the following example
you'll see a couple of constructors and a static method, all with broadly
similar signatures:
````
$ juggle '? extends java.io.InputStream (? super String)'
public static java.io.InputStream ClassLoader.getSystemResourceAsStream(String)
public java.io.FileInputStream.<init>(String) throws java.io.FileNotFoundException
public java.io.StringBufferInputStream.<init>(String)
$
````
(We see another example of Reference Widening here too: `FileInputStream` 
and `StringBufferInputStream` are both descendant classes of `InputStream`, 
so objects of those first two types can be assigned to a variable of the 
latter type.)

## Command-line summary

Each command-line option has a long name equivalent. This table summarises all options.

| Option | Long Equivalent | Argument                                     | Default                                         | Description                                         |
|--------|-----------------|----------------------------------------------|-------------------------------------------------|-----------------------------------------------------|
| `-i`   | `--import`      | package name                                 |                                                 | Packages to import (`java.lang` is always searched) |
| `-j`   | `--jar`         | file path                                    |                                                 | JAR files to search                                 |
| `-m`   | `--module`      | module name(s)                               | `-m java.base`                                  | JMODs to search                                     |
| `-s`   | `--sort`        | `access`, `name`, `package`, `score`, `text` | `-s score -s access -s package -s name -s text` | Sort criteria                                       |
| `-x`   | `--permute`     | (none)                                       | (don't permute)                                 | Match permutations of supplied parameters           |
| `-f`   | `--format`      | `auto`, `colour`, `color`, `plain`           | `auto`                                          | Output format                                       |

A declaration-style query can follow all arguments.