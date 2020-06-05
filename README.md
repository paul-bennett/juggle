# Juggle: an API search tool for Java

Juggle searches Java libraries for methods that match a given type signature.

For example, is there a method that when given a String returns a Class?
````
$ juggle -p String -r Class
public static java.lang.Class java.lang.Class.forName(java.lang.String) throws java.lang.ClassNotFoundException
static native java.lang.Class java.lang.Class.getPrimitiveClass(java.lang.String)
$
````

Juggle supports multiple parameter (`-p`) options but only one return (`-r`).
Non-static methods are treated as if there is a silent first parameter
whose type is the class in question.:
````
$ juggle -p String -r int
public int java.lang.String.length()
public int java.lang.String.hashCode()
byte java.lang.String.coder()
private int java.lang.String.indexOfNonWhitespace()
private int java.lang.String.lastIndexOfNonWhitespace()
````

In this example, the `coder` method is shown despite its return type not being an exact match. this is because a `byte` can be assigned to an `int` without casting (Widening Primitive Conversion).

JARs to search through can be specified using the -j flag:
````
$ juggle                                                                \
    -j mylib.jar                                                        \
...
````

Similarly, the ``-m`` flag can be used to specify JMODs to search.
(Caveats: modules must be in the current working directory;
this feature hasn't been throughly tested yet.)

To make life easier, packages can be imported with `-i` so that full class
names don't have to be written each time. As you would expect, `java.lang` is
always imported automatically.
````
$ juggle                                                                \
    -i java.net                                                         \
...
````

Juggle treats constructors as if they were methods returning an instance of the
declaring class.  In the following example you'll see a couple of constructors
and three static methods, all with broadly similar signatures:
````
$ juggle -p int -p String -r java.io.InputStream
public java.io.FileInputStream(java.lang.String) throws java.io.FileNotFoundException
public java.io.StringBufferInputStream(java.lang.String)
private static java.io.InputStream java.time.chrono.HijrahChronology.lambda$readConfigProperties$0(java.lang.String)
public static java.io.InputStream java.lang.ClassLoader.getSystemResourceAsStream(java.lang.String)
private static java.io.InputStream com.sun.java.util.jar.pack.PropMap.lambda$static$0(java.lang.String)
$
````

Finally, Juggle treats data fields as a pair of methods: a setter and a getter.
Non-static fields have an additional implicit `this` parameter:

````
$ juggle -r java.io.OutputStream
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
```

