# Coverage Tests

This file contains invocations designed to increase code coverage of the test suite.  Many of the samples here
don't actually do anything useful, but instead the combination of command-line parameters have been carefully
selected in order to drive up the JaCoCo coverage metrics.

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


## Empty -m or -@

````
$ juggle -m '' -@ '' -n getUpperBound"
public java.lang.reflect.Type[] java.lang.reflect.WildcardType.getUpperBounds()
public java.lang.reflect.Type[] sun.reflect.generics.reflectiveObjects.WildcardTypeImpl.getUpperBounds()
public sun.reflect.generics.tree.FieldTypeSignature[] sun.reflect.generics.tree.Wildcard.getUpperBounds()
$
````


## Multiple -t options

````
$ juggle -t java.io.NotActiveException -t java.io.InvalidObjectException
public void java.io.ObjectInputStream.registerValidation(java.io.ObjectInputValidation,int) throws java.io.NotActiveException,java.io.InvalidObjectException
$
````

## Explicit value of -x

By default, boolean arguments in picocli carry no value.  If you specify them on the command-line, the value `true`
is passed to the corresponding function.  

It feels wrong within the setter function to not use the value of the boolean parameter, even though we know it
will only ever take the value `true`.  That means JaCoCo will always present one path in an `if` statement as not
followed.

````
$ juggle -p String,ClassLoader,boolean
$
````

````
$ juggle -x -p String,ClassLoader,boolean
public static Class<T> Class<T>.forName(String,boolean,ClassLoader) throws ClassNotFoundException
public void ClassLoader.setClassAssertionStatus(String,boolean)
public void ClassLoader.setPackageAssertionStatus(String,boolean)
$
````

But there's a workaround... add `negatable=true` to the `@Option` annotation, and 
suddenly the long option name can be prefixed with `no-` on the command-line.

````
$ juggle --permute -p String,ClassLoader,boolean
public static Class<T> Class<T>.forName(String,boolean,ClassLoader) throws ClassNotFoundException
public void ClassLoader.setClassAssertionStatus(String,boolean)
public void ClassLoader.setPackageAssertionStatus(String,boolean)
$
````

````
$ juggle --no-permute -p String,ClassLoader,boolean
$
````

## Missing dependency

The (contrived) App class from testApp uses the Lib class from testLib in its interface, but doesn't include these
dependent classes in the JAR (it's not an uberjar).  This means trying to load the App class fails.  

````
$ juggle -j build/libs/testApp.jar -r com.angellane.juggle.testinput.app.App -p void            
*** Ignoring class com.angellane.juggle.testinput.app.App: java.lang.NoClassDefFoundError: Lcom/angellane/juggle/testinput/lib/Lib;
*** Couldn't find type: com.angellane.juggle.testinput.app.App; using class java.lang.Object instead
*** Ignoring class com.angellane.juggle.testinput.app.App: java.lang.NoClassDefFoundError: Lcom/angellane/juggle/testinput/lib/Lib;
$
````

## Methods with no modifiers

Curiously this test fails here, but works in README.md.  See GitHub issue #39.
````
% juggle -j build/libs/testLib.jar -r com.angellane.juggle.testinput.lib.Lib -a package
public static com.angellane.juggle.testinput.lib.Lib com.angellane.juggle.testinput.lib.Lib.libFactory()
com.angellane.juggle.testinput.lib.Lib.<init>()
%
````

