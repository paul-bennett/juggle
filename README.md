# Juggle: an API search tool for Java

Juggle searches Java libraries for methods that match a given type signature.

For example, is there a method that when given a String returns a Class?
````
$ java -classpath .:../../../lib/commons-cli-1.4.jar                      \
    com.angellane.juggle.Main                                             \
    -p String -r Class
public static java.lang.Class java.lang.Class.forName(java.lang.String) throws java.lang.ClassNotFoundException
static native java.lang.Class java.lang.Class.getPrimitiveClass(java.lang.String)
$
````

Juggle supports multiple parameter (`-p`) options but only one return (`-r`).
Non-static methods are treated as if there is a silent first parameter
whose type is the class in question.:
````
$ java -classpath .:../../../lib/commons-cli-1.4.jar                      \
    com.angellane.juggle.Main                                             \
    -p String -r int
public int java.lang.String.length()
public int java.lang.String.hashCode()
byte java.lang.String.coder()
private int java.lang.String.indexOfNonWhitespace()
private int java.lang.String.lastIndexOfNonWhitespace()
````

In this example, the `coder` method is shown despite its return type not being an exact match. this is because a `byte` can be assigned to an `int` without casting (Widening Primitive Conversion).

JARs to search through can be specified using the -j flag:
````
$ java -classpath .:../../../lib/common<s-cli-1.4.jar                   \
    com.angellane.juggle.Main                                           \
    -j mylib.jar                                                        \
...
````

To make life easier, packages can be imported with `-i` so that full class
names don't have to be written each time. As you would expect, `java.lang` is
always imported automatically.
````
$ java -classpath .:../../../lib/common<s-cli-1.4.jar                   \
    com.angellane.juggle.Main                                           \
    -i java.net                                                         \
...
````

In practice, Juggle only searches classes found in the JAR files, plus any
named in `-p` and `-r` options.  In practise this gives good results, but
does mean that static methods from the JDK may be overlooked. For example
Juggle will never find `public static double java.lang.Math.sin(double)`.
````
$ juggle -p double -r double
$ 
````

