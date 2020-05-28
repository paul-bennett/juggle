# Juggle: an API search tool for Java

Juggle searches Java libraries for methods that match a given type signature.

For example, is there a method that when given a String returns a Class?
````
$ java -classpath .:../../../lib/commons-cli-1.4.jar                      \
    -p java.lang.String -r java.lang.Class
public static java.lang.Class java.lang.Class.forName(java.lang.String) throws java.lang.ClassNotFoundException
static native java.lang.Class java.lang.Class.getPrimitiveClass(java.lang.String)
$
````

Juggle supports multiple parameter (`-p`) options but only one return (`-r`).
Non-static methods are treated as if there is a silent first parameter
whose type is the class in question.:
````
$ java -classpath .:../../../lib/commons-cli-1.4.jar                      \
    -p java.lang.String -r int
public int java.lang.String.length()
public int java.lang.String.hashCode()
private int java.lang.String.indexOfNonWhitespace()
private int java.lang.String.lastIndexOfNonWhitespace()
````

JARs to search through can be specified using the -j flag:
````
$ java -classpath .:../../../lib/common<s-cli-1.4.jar                   \
    -j mylib.jar                                                        \
...
````

In practice, Juggle only searches classes found in the JAR files, plus the
single class that's the type of the first parameter.  In practise this gives
good results, but does mean that static methods from the JDK may be overlooked.

