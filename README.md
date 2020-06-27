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
  
  See the src/test/bin/run-e2e-tests.sh and src/test/bin/split-tests.awk
  scripts for more info.

  To document examples that shouldn't be run with the test script,
  use an alternative shell prompt such as %
-->

For example, is there a method that when given a `java.time.Clock` returns a `java.time.LocalTime`?
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
$ juggle -r java.net.URLPermission
public java.net.URLPermission(String)
public java.net.URLPermission(String,String)
$
````
So the only ways of getting a `URLPermission` is by using either of its two
constructors.  (Of course methods which declare their return type to be
a superclass of `URLPermission` may in fact return a `URLPermission` object,
but Juggle can't be certain that they will so doesn't include them in its
output.)

Omitting `-p` and also omitting `-r` will list all methods in the JDK.
While marginally interesting, the output is rather too long to be helpful!

Non-static methods are treated as if
there is a silent first parameter whose type is the class in question:
````
$ juggle -p java.util.regex.Matcher -p java.lang.String -r java.lang.String
public String java.util.regex.Matcher.group(String)
public String java.util.regex.Matcher.replaceAll(String)
public String java.util.regex.Matcher.replaceFirst(String)
public static String java.util.Objects.toString(Object,String)
$
````````
In the above note how the first argument to `Objects.toString` is an `Object`, not a `String` (as specified with `-p`).
Juggle includes this method in the result set because Java allows instances of a subclass to be passed to
a function that is expecting a parent class (Widening Reference Conversion).

Juggle treats data fields as a pair of methods: a setter (which takes an
argument of the field's type and returns `void`) and a getter (which takes
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

To list static methods which take no arguments (along with static fields
by virtue of Juggle treating fields as having zero-arg pseudo-getters),
use `-p ""`.

## Where to look

You can tell Juggle to which JARs to include in the search by using the `-j` option:
````
% juggle                                                                \
    -j mylib.jar                                                        \
````

The `-m` flag can be used to specify JMODs to search.
(Caveats: modules must be in the current working directory;
this feature hasn't been throughly tested yet.)

At present there's no support for scanning an unpacked JAR or a directory of class files.

## Sorting the results

Juggle can sort its output in a number of ways. Sort criteria are specified using `-s`
and are cumulative.  The output is first sorted by the first criteria; ties are resolved
by any subsequent criteria.

| Option       | Description                                                     |
|--------------|-----------------------------------------------------------------|
| `-s access`  | Shows members by access, with `public` first and `private` last |
| `-s name`    | Sorts results by name alphabetically                            |
| `-s package` | Orders members from imported (`-i`) packages before others      |
| `-s type`    | Presents more specific types before less specific ones          |
|              |                                                                 |

The default sort criteria are equivalent to specifying `-s type -s access -s package -s name`.
The intent is that this default causes Juggle to list the "best" matches first. If that's not
what's happening in practice, I'd like to hear about it! 
 
## Extra goodies

To make life easier, packages can be imported with `-i` so that fully qualified class
names don't have to be written out each time. As you would expect, `java.lang` is
always imported automatically.  Juggle omits imported package names in its output.
````
% juggle                                                                \
    -i java.net                                                         \
````

Juggle treats constructors as if they were methods returning an instance of the
declaring class.  In the following example you'll see a couple of constructors
and a static method, all with broadly similar signatures:
````
$ juggle -p String -r java.io.InputStream
public java.io.FileInputStream(String) throws java.io.FileNotFoundException
public java.io.StringBufferInputStream(String)
public static java.io.InputStream ClassLoader.getSystemResourceAsStream(String)
$
````
(We see another example of Reference Widening here too: `FileInputStream` and `StringBufferInputStream`
are both descendent classes of `InputStream`, so objects of those first two types can be assigned to a
variable of the latter type.)

By default Juggle will only show `public` members. Use the `-a` option to set an alternative minimum level of accessibility (`public`, `package`, `protected`, or `private`).

````
$ juggle -r java.io.OutputStream -p '' -a private
public static java.io.PrintStream System.err
public static java.io.PrintStream System.out
public java.io.PipedOutputStream()
public sun.net.www.http.PosterOutputStream()
public sun.security.util.DerOutputStream()
public java.io.ByteArrayOutputStream()
static ProcessBuilder.NullOutputStream ProcessBuilder.NullOutputStream.INSTANCE
com.sun.java.util.jar.pack.CodingChooser.Sizer()
static java.io.PrintStream jdk.internal.logger.SimpleConsoleLogger.outputStream()
protected java.io.ObjectOutputStream() throws java.io.IOException,SecurityException
private ProcessBuilder.NullOutputStream()
private static java.io.PrintStream sun.launcher.LauncherHelper.ostream
public java.io.OutputStream()
public static java.io.OutputStream java.io.OutputStream.nullOutputStream()
$
````

Of course `private` members can't be used, so `-a protected` is likely the most nosey you should be.

This output also shows that Juggle is inspecting the runtime and not the specification. That can result
in some pseudo-private members or classes (such as `sun.security.util.DerOutputStream` above) leaking
into output. Just because you _can_ call a method doesn't mean you _should_.

## Command-line summary

Each command-line option has a long name equivalent. This table summarises all options.

| Option | Long Equivalent | Argument | Default | Description |
|--------|-----------------|----------|----------|-------------|
| `-a`   | `--access`      | `private`, `protected`, `package`, `public` | `-a public` | Minimum accessibility |
| `-i`   | `--import`      | package name |  | Packages to import (`java.lang` is always searched) |
| `-j`   | `--jar`         | file path | | JAR files to search |
| `-m`   | `--module`      | module name | | JMODs to search (must be in current directory) |
| `-p`   | `--param`       | type name | (don't match parameters) | Type of parameters to search for |
| `-r`   | `--return`      | type name | (don't match return)     | Return type to search for |
| `-s`   | `--sort`        | `import`, `name`, `package`, `type` | `-s type -s access -s package -s name` | Sort criteria
