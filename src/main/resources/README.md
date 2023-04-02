# Juggle: an API search tool for Java

Juggle searches Java libraries for methods that match a given type signature.

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
  
  See the src/test/java/com/angellane/juggle/TestSamples for the gory details.

  To document examples that shouldn't be checked with the test harness,
  use an alternative shell prompt such as %
-->

For example, is there a method that when given a `java.time.Clock` returns a
`java.time.LocalTime`?
````
$ juggle -p java.time.Clock -r java.time.LocalTime
public static java.time.LocalTime java.time.LocalTime.now(java.time.Clock)
$
````
Answer: yes, the `now()` method of the `java.time.LocalTime` class.
Juggle shows the signature for the method, with the class and method
names separated by a period.

## What to look for

Use the `-r` option to specify the function's desired return type.

The `-p` option specifies a comma-separated list of parameter types.
(Using multiple `-p` options is an alternative way of specifying
multiple parameter types.) 
````
$ juggle -p double[] -p int -p int -p double -r void 
public static void java.util.Arrays.fill(double[],int,int,double)
$
````

If no `-r` is specified, Juggle shows matching methods with _any_ return type:
````
$ juggle -p double[] -p int -p int -p double 
public static int java.util.Arrays.binarySearch(double[],int,int,double)
public static void java.util.Arrays.fill(double[],int,int,double)
$
````

Similarly, omitting `-p` lists methods that take _any number_ of arguments
of _any type_.  This provides a means of listing all the ways of obtaining
an object of a specific type:
````
$ juggle -r java.net.Inet6Address
public static java.net.Inet6Address java.net.Inet6Address.getByAddress(String,byte[],int) throws java.net.UnknownHostException
public static java.net.Inet6Address java.net.Inet6Address.getByAddress(String,byte[],java.net.NetworkInterface) throws java.net.UnknownHostException
$
````
So the only way to get a `Inet6Address` appears to be either of the two
static `getByAddress` methods on the `Inet6Address` class.   

Of course methods may return an instance of a subclass of their declared return
type.  `Inet6Address` is a member of a small family of classes rooted in its
parent `InetAddress` class, and indeed searching `juggle -r InetAddress` will
reveal many other way of obtaining an instance of a member this family, such
as `Inet6Address`.

Juggle won't list these methods-returning-a-superclass though, since doing that
would result in long and not particularly helpful outputs. (Since `Object` is a
superclass of every reference type, at a minimum Juggle would have to include
hundreds of methods that return `Object` in every result.)

Omitting `-p` and omitting `-r` will list all methods in the JDK.
While marginally interesting, the output is rather too long to be helpful!
(This is also very slow unless you tweak the sort options; see below.)

If Juggle can't find classes that you mention in `-p` or `-r` arguments, it
emits a warning and treats them as if you specified `Object` instead.  This
can result in exceedingly lengthy output.

Juggle treats non-static methods as if they have a silent
first parameter whose type is the class in question:
````
$ juggle -p java.util.regex.Matcher -p java.lang.String -r java.lang.String
public String java.util.regex.Matcher.group(String)
public String java.util.regex.Matcher.replaceAll(String)
public String java.util.regex.Matcher.replaceFirst(String)
public static String java.util.Objects.toString(Object,String)
$
````````
In the above note how the first argument to `Objects.toString` is an `Object`,
not a `String` (as specified with `-p`). Juggle includes this method in the
result set because Java allows instances of a subclass to be passed to
a function that is expecting a parent class (Widening Reference Conversion).

Juggle treats data fields as a pair of methods: a setter (which takes an
argument of the field's type and returns `void`), and a getter (which takes
no additional arguments and returns a value of the field's type). 

As with non-static methods, non-static fields have an additional implicit 
`this` parameter:
````
$ juggle -r int -p java.io.InterruptedIOException
public int java.io.InterruptedIOException.bytesTransferred
public int Object.hashCode()
public static int System.identityHashCode(Object)
public static int java.util.Objects.hashCode(Object)
public static int sun.invoke.util.ValueConversions.widenSubword(Object)
public static int java.lang.reflect.Array.getLength(Object) throws IllegalArgumentException
$
````

To list static methods which take no arguments use `-p ""`.

(This also lists all default constructors, as well as static fields
by virtue of Juggle treating fields as having zero-arg pseudo-getters.)

The `-t` option allows you to filter by methods that might throw a specific exception type:

````
$ juggle -t java.net.URISyntaxException
public java.net.URI java.net.URI.parseServerAuthority() throws java.net.URISyntaxException
public java.net.URI java.net.URL.toURI() throws java.net.URISyntaxException
public java.net.URI.<init>(String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String,int,String,String,String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String,String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String,String,String) throws java.net.URISyntaxException
$
````

Juggle will list all methods that include the named exception (or a subclass of the named type) 
in their `throws` clause.  Specifying multiple exception types (either comma-separated, or with
multiple `-t` options) will show only the methods that might throw _all_ the specified exceptions.

A query for `-t ''` will show all methods that declare no thrown types.

You can also ask Juggle to look for annotations using the `-@` option.  Juggle will list methods that 
have the named annotations, or whose declaring class is so annotated.  If multiple annotations are
supplied, they must all be present on the class or method.
````
$ juggle -@ FunctionalInterface -r int
public int java.util.Comparator<T>.compare(T,T)
public int java.util.function.DoubleToIntFunction.applyAsInt(double)
public int java.util.function.IntBinaryOperator.applyAsInt(int,int)
public int java.util.function.IntSupplier.getAsInt()
public int java.util.function.IntUnaryOperator.applyAsInt(int)
public int java.util.function.LongToIntFunction.applyAsInt(long)
public int java.util.function.ToIntBiFunction<T,U>.applyAsInt(T,U)
public int java.util.function.ToIntFunction<T>.applyAsInt(T)
$
````


## Where to look

You can tell Juggle which JARs to include in the search by using the `-j`
option:
````
% juggle                                                                \
    -j mylib.jar                                                        \
...
````

The `-m` flag can be used to specify JMODs to search.
(Caveats: modules must be in the current working directory;
this feature hasn't been thoroughly tested yet.)

At present there's no support for scanning an unpacked JAR, or a directory of
class files.

## Sorting the results

Juggle can sort its output in a number of ways. Specify sort criteria using
`-s`. The first criteria sorts the results, with ties resolved by any
subsequent criteria.

| Option       | Description                                                     |
|--------------|-----------------------------------------------------------------|
| `-s access`  | Shows members by access, with `public` first and `private` last |
| `-s closest` | Member that most closely matches query type first.              |
| `-s name`    | Sorts results by name alphabetically                            |
| `-s package` | Orders members from imported (`-i`) packages before others      |
| `-s type`    | Presents more specific types before less specific ones          |
|              |                                                                 |

The default sort is equivalent to `-s type -s access -s package -s name`.
The intent is that this default causes Juggle to list the "best" matches first.
If that's not what's happening in practice, I'd like to hear about it! 
 
Warning: `-s closest` slows things down tremendously, especially for large
result sets.  If Juggle appears to be taking too long, re-run your query
with a simpler search order such as `-s name`.

## Extra goodies

To make life easier, packages can be imported with `-i` so that fully qualified
class names don't have to be written out each time. As you would expect,
`java.lang` is always implicitly imported.  Juggle omits imported package 
names in its output.
````
$ juggle                                                                \
    -j build/libs/juggle-1.0-SNAPSHOT.jar                               \
    -i com.angellane.juggle                                             \
    -i java.util                                                        \
    -r CartesianProduct
public CartesianProduct<T>.<init>(List<E>[])
public static <T> CartesianProduct<T> CartesianProduct<T>.of(List<E>[])
$
````
(Note that in this example, Juggle isn't yet unifying the type arguments;
ideally it should output `CartesianProduct<T>.of(List<T>[])`.)

Juggle treats constructors as if they were static methods called `<init>`
returning an instance of the declaring class.  In the following example
you'll see a couple of constructors and a static method, all with broadly
similar signatures:
````
$ juggle -p String -r java.io.InputStream
public static java.io.InputStream ClassLoader.getSystemResourceAsStream(String)
public java.io.FileInputStream.<init>(String) throws java.io.FileNotFoundException
public java.io.StringBufferInputStream.<init>(String)
$
````
(We see another example of Reference Widening here too: `FileInputStream` 
and `StringBufferInputStream` are both descendant classes of `InputStream`, 
so objects of those first two types can be assigned to a variable of the 
latter type.)

By default, Juggle will only show `public` members. Use the `-a` option to
set an alternative minimum level of accessibility (`public`, `package`, 
`protected`, or `private`).

````
$ juggle -r java.io.OutputStream -p '' -a private
public java.io.OutputStream.<init>()
public static java.io.OutputStream java.io.OutputStream.nullOutputStream()
public static java.io.PrintStream System.err
public static java.io.PrintStream System.out
public java.io.ByteArrayOutputStream.<init>()
public java.io.PipedOutputStream.<init>()
public sun.net.www.http.PosterOutputStream.<init>()
public sun.security.util.DerOutputStream.<init>()
static ProcessBuilder.NullOutputStream ProcessBuilder.NullOutputStream.INSTANCE
com.sun.java.util.jar.pack.CodingChooser.Sizer.<init>()
static java.io.PrintStream jdk.internal.logger.SimpleConsoleLogger.outputStream()
protected java.io.ObjectOutputStream.<init>() throws java.io.IOException,SecurityException
private ProcessBuilder.NullOutputStream.<init>()
private static java.io.PrintStream sun.launcher.LauncherHelper.ostream
$
````

Of course `private` members can't be used, so `-a protected` is likely the most
nosey you should be.

This output also shows that Juggle is inspecting the runtime and not the
specification. That can result in some pseudo-private members or classes
(such as `sun.security.util.DerOutputStream` above) leaking into output.
Just because you _can_ call a method doesn't mean you _should_.

Sometimes you might not know the order of parameters.  The `-x` option causes
Juggle to also match methods whose parameter types match the supplied ones, but
not necessarily in the order you specified.

For example, there are no methods that take a `double[]`, an `int`, a
`double` and then an `int`:

````
$ juggle -p double[],int,double,int -r void
$ 
````

However, if we allos Juggle to permute parmeters, it locates a match:

````
$ juggle -p double[],int,double,int -r void
public static void java.util.Arrays.fill(double[],int,int,double)
$ 
````

Note that parameter permutation can significantly increase runtime,
so it's not enabled by default.


## Command-line summary

Each command-line option has a long name equivalent. This table summarises all options.

| Option | Long Equivalent | Argument                                                 | Default                                   | Description                                         |
|--------|-----------------|----------------------------------------------------------|-------------------------------------------|-----------------------------------------------------|
| `-a`   | `--access`      | `private`, `protected`, `package`, `public`              | `-a public`                               | Minimum accessibility                               |
| `-i`   | `--import`      | package name                                             |                                           | Packages to import (`java.lang` is always searched) |
| `-j`   | `--jar`         | file path                                                |                                           | JAR files to search                                 |
| `-m`   | `--module`      | module name(s)                                           | `-m java.base`                            | JMODs to search                                     |
| `-p`   | `--param`       | type name(s)                                             | (don't match parameters)                  | Type of parameters to search for                    |
| `-r`   | `--return`      | type name                                                | (don't match return)                      | Return type to search for                           |
| `-t`   | `--throws`      | type name(s)                                             | (don't match throws)                      | Exception types that must be thrown                 |                                             
| `-@`   | `--annotation`  | annotation name(s)                                       | (don't match annotations)                 | Annotations to filter on (class or method)          |
| `-s`   | `--sort`        | `access`, `closest`, `import`, `name`, `package`, `type` | `-s closest -s access -s package -s name` | Sort criteria                                       |
| `-x`   | `--permute`     | (none)                                                   | (don't permute)                           | Match permutations of supplied parameters           |    
