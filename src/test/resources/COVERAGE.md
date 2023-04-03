# Coverage Tests

This file contains invocations designed to increase code coverage of the test suite

## No parameters in the query

````
$ juggle -r NoSuchMethodException -s closest
public NoSuchMethodException.<init>()
public NoSuchMethodException.<init>(String)
$
````

## ByMostSpecificType comparator

````
$ juggle -r NoSuchMethodException -s type
public NoSuchMethodException.<init>()
public NoSuchMethodException.<init>(String)
$
````

````
$ juggle -p String -r java.io.InputStream -s type
public java.io.FileInputStream.<init>(String) throws java.io.FileNotFoundException
public java.io.StringBufferInputStream.<init>(String)
public static java.io.InputStream ClassLoader.getSystemResourceAsStream(String)
$
````

## Missing module

````
$ juggle -m this.module.does.not.exist
*** Module this.module.does.not.exist not found
$
````


## Unknown type

````
$ juggle -p ThisTypeDoesNotExist -r boolean
*** Couldn't find type: ThisTypeDoesNotExist; using class java.lang.Object instead
public static boolean Thread.holdsLock(Object)
public static boolean java.lang.invoke.MethodHandleProxies.isWrapperInstance(Object)
public static boolean java.util.Objects.isNull(Object)
public static boolean java.util.Objects.nonNull(Object)
$
````

## Methods that don't throw

````
$ juggle -p java.io.InputStream -r String -t ''
public String Object.toString()
public static String String.valueOf(Object)
public static String java.util.Objects.toString(Object)
public static String sun.invoke.util.BytecodeDescriptor.unparse(Object)
$
````

If it wasn't for the `-t ''` we'd expect the above query to also include 
`String java.net.URLConnection.guessContentTypeFromStream(java.io.InputStream)` in its results.


## Trying to find WildcardType

The biggest area that presently lacks test coverage is `TextOutput.decodeWildcardType()`.  This function
doesn't seem to be called at all, even when I explicitly search for a method that the JavaDoc suggests
returns a wildcard type:

````
$ juggle -n asSubclass
public <U> Class<T> Class<T>.asSubclass(Class<T>)
$
````

The `Class.asSubclass` method is declared to return `Class<? extends U>`. I suspect the wildcard is eliminated
at runtime due to type erasure, in which case it may be worth stripping this method from the source altogether.


