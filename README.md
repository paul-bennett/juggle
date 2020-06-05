# Juggle: an API search tool for Java

Juggle searches Java libraries for methods that match a given type signature.

For example, is there a method that when given a `java.time.Clock` returns a `java.time.LocalTime`?
````
$ juggle -p java.time.Clock -r java.time.LocalTime
public static java.time.LocalTime java.time.LocalTime.now(java.time.Clock)
$
````
Answer: yes, the `now()` method of the `java.time.LocalTime` class. Juggle shows the full signature for the method.

Juggle supports multiple parameter `-p` options but only one return `-r`.
Non-static methods are treated as if there is a silent first parameter
whose type is the class in question:
````
$ juggle -p java.util.regex.Matcher -p java.lang.String -r java.lang.String
public static java.lang.String java.util.Objects.toString(java.lang.Object,java.lang.String)
public java.lang.String java.util.regex.Matcher.group(java.lang.String)
public java.lang.String java.util.regex.Matcher.replaceFirst(java.lang.String)
public java.lang.String java.util.regex.Matcher.replaceAll(java.lang.String)
$
````
In the above note how the first argument to `Objects.toString` is an `Object`, not a `String` (as specified with `-p`).
Juggle includes this method in the result set because Java allows instances of a subclass to be passed to
a function that is expecting a parent class (Widening Reference Conversion).

JARs to search through can be specified using the `-j` option:
````
$ juggle                                                                \
    -j mylib.jar                                                        \
...
````

The ``-m`` flag can be used to specify JMODs to search.
(Caveats: modules must be in the current working directory;
this feature hasn't been throughly tested yet.)

At present there's no support for scanning an unpacked JAR or a directory of class files.

To make life easier, packages can be imported with `-i` so that fully qualified class
names don't have to be written out each time. As you would expect, `java.lang` is
always imported automatically.
````
$ juggle                                                                \
    -i java.net                                                         \
...
````

Juggle treats constructors as if they were methods returning an instance of the
declaring class.  In the following example you'll see a couple of constructors
and a static method, all with broadly similar signatures:
````
$ juggle -p int -p String -r java.io.InputStream
public java.io.FileInputStream(java.lang.String) throws java.io.FileNotFoundException
public java.io.StringBufferInputStream(java.lang.String)
public static java.io.InputStream java.lang.ClassLoader.getSystemResourceAsStream(java.lang.String)
$
````
(We see another example of Reference Widening here too: `FileInputStream` and `StringBufferInputStream`
are both descendent classes of `InputStream`, so objects of those first two types can be assigned to a
variable of the latter type.)

Finally, Juggle treats data fields as a pair of methods: a setter and a getter.
Non-static fields have an additional implicit `this` parameter:

````
$ juggle -r java.io.OutputStream
public static final java.io.PrintStream java.lang.System.out
public static final java.io.PrintStream java.lang.System.err
public sun.security.util.DerOutputStream()
public sun.net.www.http.PosterOutputStream()
public java.io.ByteArrayOutputStream()
public java.io.OutputStream()
public java.io.PipedOutputStream()
public static java.io.OutputStream java.io.OutputStream.nullOutputStream()
$
````

By default Juggle will only show `public` members. Use the `-a` option to set an alternative minimum level of accessibility (`public`, `package`, `protected`, or `private`).

````
$ juggle -r java.io.OutputStream -a private
private static java.io.PrintStream sun.launcher.LauncherHelper.ostream
static final java.lang.ProcessBuilder$NullOutputStream java.lang.ProcessBuilder$NullOutputStream.INSTANCE
public static final java.io.PrintStream java.lang.System.out
public static final java.io.PrintStream java.lang.System.err
public sun.security.util.DerOutputStream()
public sun.net.www.http.PosterOutputStream()
private java.lang.ProcessBuilder$NullOutputStream()
public java.io.ByteArrayOutputStream()
protected java.io.ObjectOutputStream() throws java.io.IOException,java.lang.SecurityException
java.io.OutputStream$1()
public java.io.OutputStream()
public java.io.PipedOutputStream()
com.sun.java.util.jar.pack.CodingChooser$Sizer()
static java.io.PrintStream jdk.internal.logger.SimpleConsoleLogger.outputStream()
public static java.io.OutputStream java.io.OutputStream.nullOutputStream()
$
````

Of course `private` members can't be used, so `-a protected` is likely the most nosey you should be.

This output also shows that Juggle is inspecting the runtime and not the specification. That can result
in some pseudo-private members or classes (such as `sun.security.util.DerOutputStream` above) leaking
into output. Just because you _can_ call a method doesn't mean you _should_.  
