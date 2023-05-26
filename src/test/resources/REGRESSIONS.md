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

# Regressions

This file contains regression tests for Juggle; curiosities that show
how we expect the Juggle command-line to operate in corner cases.

The file is parsed by `com.angellane.juggle.TestSamples`. See also the
comment in [README.md](README.md) for more details about how it's parsed.
But in essence, add a test by copying one of the code blocks.

## Previously fixed bugs

(Most recently fixed first.)

### [GitHub Issue #105](https://github.com/paul-bennett/juggle/issues/105): `-s package` should sort by package name

Modified `package` comparator now sorts packages alphabetically if they 
weren't mentioned in the import list, and the implicit `java.lang` has
been moved to the end of that list rather than the start.
````
$ juggle -i java.net class /Class/
public class URLClassLoader extends java.security.SecureClassLoader implements java.io.Closeable
public final class Class<T> implements java.io.Serializable, java.lang.reflect.GenericDeclaration, java.lang.reflect.Type, java.lang.reflect.AnnotatedElement, java.lang.invoke.TypeDescriptor.OfField<F>, java.lang.constant.Constable
public class ClassCastException extends RuntimeException
public class ClassCircularityError extends LinkageError
public class ClassFormatError extends LinkageError
public abstract class ClassLoader
public class ClassNotFoundException extends ReflectiveOperationException
public abstract class ClassValue<T>
public class IncompatibleClassChangeError extends LinkageError
public class NoClassDefFoundError extends LinkageError
public class UnsupportedClassVersionError extends ClassFormatError
public class java.io.InvalidClassException extends java.io.ObjectStreamException
public class java.io.ObjectStreamClass implements java.io.Serializable
public class java.security.SecureClassLoader extends ClassLoader
$
````

### [GitHub Issue #72](https://github.com/paul-bennett/juggle/issues/72): Don't show JDK implementation classes

Prior to fixing, this used to include two further results from non-exported packages
in `jdk.internals.*`:
````
$ juggle '(int,int,int,int)'
public static java.time.LocalTime java.time.LocalTime.of(int,int,int,int)
$
````

### [GitHub Issue #85](https://github.com/paul-bennett/juggle/issues/85): Handle nested classes in queries

Prior to fixing, this used to throw an exception saying that it couldn't
find the `Authenticator.RequestorType` class.
````
$ juggle -i java.net 'PasswordAuthentication (Authenticator,String,InetAddress,int,String,String,String,URL,Authenticator.RequestorType)'
public static PasswordAuthentication Authenticator.requestPasswordAuthentication(Authenticator,String,InetAddress,int,String,String,String,URL,Authenticator.RequestorType)
public PasswordAuthentication Authenticator.requestPasswordAuthenticationInstance(String,InetAddress,int,String,String,String,URL,Authenticator.RequestorType)
$
````

### [GitHub Issue #62](https://github.com/paul-bennett/juggle/issues/62): Add ellipsis support to throws clauses

We don't need to implement this because using a wildcard in the `throws` clause does the trick.

Here are the methods that only throw `FileNotFoundException`:
````
$ juggle throws java.io.FileNotFoundException
public java.io.FileInputStream.<init>(java.io.File) throws java.io.FileNotFoundException
public java.io.FileInputStream.<init>(String) throws java.io.FileNotFoundException
public java.io.FileOutputStream.<init>(java.io.File) throws java.io.FileNotFoundException
public java.io.FileOutputStream.<init>(java.io.File,boolean) throws java.io.FileNotFoundException
public java.io.FileOutputStream.<init>(String) throws java.io.FileNotFoundException
public java.io.FileOutputStream.<init>(String,boolean) throws java.io.FileNotFoundException
public java.io.FileReader.<init>(java.io.File) throws java.io.FileNotFoundException
public java.io.FileReader.<init>(String) throws java.io.FileNotFoundException
public java.io.PrintStream.<init>(java.io.File) throws java.io.FileNotFoundException
public java.io.PrintStream.<init>(String) throws java.io.FileNotFoundException
public java.io.PrintWriter.<init>(java.io.File) throws java.io.FileNotFoundException
public java.io.PrintWriter.<init>(String) throws java.io.FileNotFoundException
public java.io.RandomAccessFile.<init>(java.io.File,String) throws java.io.FileNotFoundException
public java.io.RandomAccessFile.<init>(String,String) throws java.io.FileNotFoundException
public java.util.Formatter.<init>(java.io.File) throws java.io.FileNotFoundException
public java.util.Formatter.<init>(String) throws java.io.FileNotFoundException
public java.util.Scanner.<init>(java.io.File) throws java.io.FileNotFoundException
public java.util.Scanner.<init>(java.io.File,String) throws java.io.FileNotFoundException
$
````

By adding a `, ?` to the `throws` clause, we include methods that throw other exceptions as well:
````
$ juggle throws java.io.FileNotFoundException, \?
public java.io.FileInputStream.<init>(java.io.File) throws java.io.FileNotFoundException
public java.io.FileInputStream.<init>(String) throws java.io.FileNotFoundException
public java.io.FileOutputStream.<init>(java.io.File) throws java.io.FileNotFoundException
public java.io.FileOutputStream.<init>(java.io.File,boolean) throws java.io.FileNotFoundException
public java.io.FileOutputStream.<init>(String) throws java.io.FileNotFoundException
public java.io.FileOutputStream.<init>(String,boolean) throws java.io.FileNotFoundException
public java.io.FileReader.<init>(java.io.File) throws java.io.FileNotFoundException
public java.io.FileReader.<init>(String) throws java.io.FileNotFoundException
public java.io.PrintStream.<init>(java.io.File) throws java.io.FileNotFoundException
public java.io.PrintStream.<init>(java.io.File,String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
public java.io.PrintStream.<init>(String) throws java.io.FileNotFoundException
public java.io.PrintStream.<init>(String,String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
public java.io.PrintWriter.<init>(java.io.File) throws java.io.FileNotFoundException
public java.io.PrintWriter.<init>(java.io.File,String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
public java.io.PrintWriter.<init>(String) throws java.io.FileNotFoundException
public java.io.PrintWriter.<init>(String,String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
public java.io.RandomAccessFile.<init>(java.io.File,String) throws java.io.FileNotFoundException
public java.io.RandomAccessFile.<init>(String,String) throws java.io.FileNotFoundException
public java.util.Formatter.<init>(java.io.File) throws java.io.FileNotFoundException
public java.util.Formatter.<init>(java.io.File,String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
public java.util.Formatter.<init>(java.io.File,String,java.util.Locale) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
public java.util.Formatter.<init>(String) throws java.io.FileNotFoundException
public java.util.Formatter.<init>(String,String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
public java.util.Formatter.<init>(String,String,java.util.Locale) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
public java.util.Scanner.<init>(java.io.File) throws java.io.FileNotFoundException
public java.util.Scanner.<init>(java.io.File,String) throws java.io.FileNotFoundException
$
````

### [GitHub Issue #99](https://github.com/paul-bennett/juggle/issues/99): Exception on `juggle private`

Prior to fixing #99, this query was resulting in an uncaught exception.
````
$ juggle "private java.util.Optional /^lambda/"
private static java.util.Optional<T> java.util.Currency.lambda$getValidCurrencyData$0(java.util.Properties,java.util.regex.Pattern,String)
private static java.util.Optional<T> java.util.spi.ToolProvider.lambda$findFirst$1(ClassLoader,String)
private static java.util.Optional<T> java.util.stream.Collectors.lambda$reducing$48(java.util.stream.Collectors$1OptionalBox)
$
````

It may be the case that these methods will all be hidden from a future version of Juggle because they're private
lambdas (and therefore of no use to external code).

The exception was thrown when trying to emit the parameter type of the last of these,
i.e. `java.util.stream.Collectors$1OptionalBox`.

### [GitHub Issue #98](https://github.com/paul-bennett/juggle/issues/98): Access modifiers follow the usual pattern of specifying minimum accessibility:
````
$ juggle private java.net.Inet6Address 
public static java.net.Inet6Address java.net.Inet6Address.getByAddress(String,byte[],int) throws java.net.UnknownHostException
public static java.net.Inet6Address java.net.Inet6Address.getByAddress(String,byte[],java.net.NetworkInterface) throws java.net.UnknownHostException
java.net.Inet6Address.<init>()
java.net.Inet6Address.<init>(String,byte[])
java.net.Inet6Address.<init>(String,byte[],int)
java.net.Inet6Address.<init>(String,byte[],String) throws java.net.UnknownHostException
java.net.Inet6Address.<init>(String,byte[],java.net.NetworkInterface) throws java.net.UnknownHostException
$
````

### [GitHub Issue #65](https://github.com/paul-bennett/juggle/issues/65): Handle ellipsis in parameter lists

For this issue we're going to focus on methods whose name ends with the word
`search` (ignoring case):

````
$ juggle '/search$/i'                                                
public static int java.util.Arrays.binarySearch(byte[],byte)
public static int java.util.Arrays.binarySearch(byte[],int,int,byte)
public static int java.util.Arrays.binarySearch(char[],char)
public static int java.util.Arrays.binarySearch(char[],int,int,char)
public static int java.util.Arrays.binarySearch(double[],double)
public static int java.util.Arrays.binarySearch(double[],int,int,double)
public static int java.util.Arrays.binarySearch(float[],float)
public static int java.util.Arrays.binarySearch(float[],int,int,float)
public static int java.util.Arrays.binarySearch(int[],int)
public static int java.util.Arrays.binarySearch(int[],int,int,int)
public static int java.util.Arrays.binarySearch(Object[],int,int,Object)
public static <T> int java.util.Arrays.binarySearch(T[],int,int,T,java.util.Comparator<T>)
public static int java.util.Arrays.binarySearch(Object[],Object)
public static <T> int java.util.Arrays.binarySearch(T[],T,java.util.Comparator<T>)
public static int java.util.Arrays.binarySearch(long[],int,int,long)
public static int java.util.Arrays.binarySearch(long[],long)
public static int java.util.Arrays.binarySearch(short[],int,int,short)
public static int java.util.Arrays.binarySearch(short[],short)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T,java.util.Comparator<T>)
public synchronized int java.util.Stack<E>.search(Object)
public <U> U java.util.concurrent.ConcurrentHashMap<K,V>.search(long,java.util.function.BiFunction<T,U,R>)
$
````


Omitting parentheses as above indicates that we don't want to filter on
parameters at all.  Including parentheses but nothing between them matches
zero-arg methods.  There are none that match the name filter in this case:

````
$ juggle '/search$/i ()'
$
````

If we put a single ellipsis in the parameter list we're saying that we
want methods with zero or more parameters, so we get the same results
as when we omitted parentheses altogether:

````
$ juggle '/search$/i (...)'                                                
public static int java.util.Arrays.binarySearch(byte[],byte)
public static int java.util.Arrays.binarySearch(byte[],int,int,byte)
public static int java.util.Arrays.binarySearch(char[],char)
public static int java.util.Arrays.binarySearch(char[],int,int,char)
public static int java.util.Arrays.binarySearch(double[],double)
public static int java.util.Arrays.binarySearch(double[],int,int,double)
public static int java.util.Arrays.binarySearch(float[],float)
public static int java.util.Arrays.binarySearch(float[],int,int,float)
public static int java.util.Arrays.binarySearch(int[],int)
public static int java.util.Arrays.binarySearch(int[],int,int,int)
public static int java.util.Arrays.binarySearch(Object[],int,int,Object)
public static <T> int java.util.Arrays.binarySearch(T[],int,int,T,java.util.Comparator<T>)
public static int java.util.Arrays.binarySearch(Object[],Object)
public static <T> int java.util.Arrays.binarySearch(T[],T,java.util.Comparator<T>)
public static int java.util.Arrays.binarySearch(long[],int,int,long)
public static int java.util.Arrays.binarySearch(long[],long)
public static int java.util.Arrays.binarySearch(short[],int,int,short)
public static int java.util.Arrays.binarySearch(short[],short)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T,java.util.Comparator<T>)
public synchronized int java.util.Stack<E>.search(Object)
public <U> U java.util.concurrent.ConcurrentHashMap<K,V>.search(long,java.util.function.BiFunction<T,U,R>)
$
````

Now let's just specify the first parameter. That drops us down to three candidates:
````
$ juggle '/search$/i (? extends java.util.Collection,...)'                                                
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T,java.util.Comparator<T>)
public synchronized int java.util.Stack<E>.search(Object)
$
````
Note how the last of these is a non-static member, so the "first"
parameter is actually the target class.

If we specify something outlandish as our first parameter we get no results:
````
$ juggle '/search$/i (java.net.InetAddress,...)'                                                
$
````

Here's the same but with the last parameter:
````
$ juggle '/search$/i (...,double)'                                                
public static int java.util.Arrays.binarySearch(double[],double)
public static int java.util.Arrays.binarySearch(double[],int,int,double)
$
````
````
$ juggle '/search$/i (..., String)'                                                
$
````

Let's put the ellipsis in the middle, missing out all but the first and last arg:
````
$ juggle '/search$/i (java.util.List, ..., java.util.Comparator)'                                                
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T,java.util.Comparator<T>)
$
````

And now the opposite: a param in the middle:
````
$ juggle '/search$/i (..., int, ...)'                                                
public static int java.util.Arrays.binarySearch(byte[],int,int,byte)
public static int java.util.Arrays.binarySearch(char[],int,int,char)
public static int java.util.Arrays.binarySearch(double[],int,int,double)
public static int java.util.Arrays.binarySearch(float[],int,int,float)
public static int java.util.Arrays.binarySearch(int[],int)
public static int java.util.Arrays.binarySearch(int[],int,int,int)
public static int java.util.Arrays.binarySearch(Object[],int,int,Object)
public static <T> int java.util.Arrays.binarySearch(T[],int,int,T,java.util.Comparator<T>)
public static int java.util.Arrays.binarySearch(long[],int,int,long)
public static int java.util.Arrays.binarySearch(short[],int,int,short)
$
````

Finally, ellipses all over the place:
````
$ juggle '/search$/i (..., long[], ..., int, ...)'                                                
public static int java.util.Arrays.binarySearch(long[],int,int,long)
$
````

### [GitHub Issue #48](https://github.com/paul-bennett/juggle/issues/48): Implemented-By Index

Juggle can show you all classes that directly implement a specific interface:
````
$ juggle class implements java.lang.reflect.Member                  
public abstract class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration
public final class java.lang.reflect.Field extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member
$
````

To show classes that indirectly implement an interface, use a type bound:
````
$ juggle class extends \? extends java.lang.reflect.Member
public final class java.lang.reflect.Constructor<T> extends java.lang.reflect.Executable
public final class java.lang.reflect.Method extends java.lang.reflect.Executable
$
````

Juggle doesn't presently offer a mechanism to list all classes that
directly or indirectly implement an interface.
(See [GitHub Issue #84](https://github.com/paul-bennett/juggle/issues/84).)

### [GitHub issue #47](https://github.com/paul-bennett/juggle/issues/47): Subclass Index

Here's how to find the direct subclasses of a class:
````
$ juggle class extends java.lang.reflect.AccessibleObject
public abstract class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration
public final class java.lang.reflect.Field extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member
$
````

`java.lang.reflect.Executable` itself has two subclasses that weren't listed
above because they're _indirect_ subclasses of `AccessibleObject`:
````
$ juggle class extends java.lang.reflect.Executable
public final class java.lang.reflect.Constructor<T> extends java.lang.reflect.Executable
public final class java.lang.reflect.Method extends java.lang.reflect.Executable
$
````

To show all subclasses, including indirect ones we can specify a type bound:
````
$ juggle class extends \? extends java.lang.reflect.AccessibleObject
public final class java.lang.reflect.Constructor<T> extends java.lang.reflect.Executable
public abstract class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration
public final class java.lang.reflect.Field extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member
public final class java.lang.reflect.Method extends java.lang.reflect.Executable
$
````

### [GitHub Issue #45](https://github.com/paul-bennett/juggle/issues/45): JavaNut 27 (Class Defined-In Index)

Juggle now answers this question directly:
````
$ juggle class FileNotFoundException
public class java.io.FileNotFoundException extends java.io.IOException
$
````

### [GitHub Issue #74](https://github.com/paul-bennett/juggle/issues/74): Search by exact name is broken (substring)

There are no methods in the JDK called `Equals`:
````
$ juggle '? Equals'
$
````

But there are some that contain the word `Equals`:
````
$ juggle '? /Equals/'
public boolean String.contentEquals(CharSequence)
public boolean String.contentEquals(StringBuffer)
public static boolean StringUTF16.contentEquals(byte[],byte[],int)
public static boolean StringUTF16.contentEquals(byte[],CharSequence,int)
public static boolean java.util.Arrays.deepEquals(Object[],Object[])
public static boolean java.util.Objects.deepEquals(Object,Object)
$
````

Similarly, there's only one class called `StringBuffer`:
````
$ juggle 'class StringBuffer'
public final class StringBuffer extends AbstractStringBuilder implements java.io.Serializable, Comparable<T>, CharSequence
$
````
But there are two that contain the letters `StringBuffer`:
````
$ juggle 'class /StringBuffer/'
public final class StringBuffer extends AbstractStringBuilder implements java.io.Serializable, Comparable<T>, CharSequence
public class java.io.StringBufferInputStream extends java.io.InputStream
$
````

(Prior to fixing this issue, string literals were interpreted as
case-sensitive substring matches.)

### [GitHub issue #70](https://github.com/paul-bennett/juggle/issues/70): Add `transient` and `volatile` member modifiers

There are about 400 `transient` methods in the JDK. Here are a few.
````
$ juggle -i java.util.stream -i java.nio.file transient Stream
public static transient <T> Stream<T> Stream<T>.of(T[])
public static transient Stream<T> Files.find(Path,int,java.util.function.BiPredicate<T,U>,FileVisitOption[]) throws java.io.IOException
public static transient Stream<T> Files.walk(Path,int,FileVisitOption[]) throws java.io.IOException
public static transient Stream<T> Files.walk(Path,FileVisitOption[]) throws java.io.IOException
$
````
And here are some of the 2000 `volatile` methods:
````
$ juggle "volatile (StringBuilder,?)"
public volatile AbstractStringBuilder StringBuilder.append(boolean)
public volatile AbstractStringBuilder StringBuilder.append(char)
public volatile AbstractStringBuilder StringBuilder.append(char[])
public volatile AbstractStringBuilder StringBuilder.append(double)
public volatile AbstractStringBuilder StringBuilder.append(float)
public volatile AbstractStringBuilder StringBuilder.append(int)
public volatile AbstractStringBuilder StringBuilder.append(CharSequence)
public volatile AbstractStringBuilder StringBuilder.append(Object)
public volatile AbstractStringBuilder StringBuilder.append(String)
public volatile AbstractStringBuilder StringBuilder.append(StringBuffer)
public volatile AbstractStringBuilder StringBuilder.append(long)
public volatile Appendable StringBuilder.append(char) throws java.io.IOException
public volatile Appendable StringBuilder.append(CharSequence) throws java.io.IOException
public volatile AbstractStringBuilder StringBuilder.appendCodePoint(int)
public volatile char StringBuilder.charAt(int)
public volatile int StringBuilder.codePointAt(int)
public volatile int StringBuilder.codePointBefore(int)
public volatile int StringBuilder.compareTo(Object)
public volatile AbstractStringBuilder StringBuilder.deleteCharAt(int)
public volatile void StringBuilder.ensureCapacity(int)
public volatile void StringBuilder.setLength(int)
public volatile String StringBuilder.substring(int)
$
````

### [GitHub issue #32](https://github.com/paul-bennett/juggle/issues/32)

Results aren't deduplicated

````
% juggle -n asSubclass -m java.base,java.base
$ juggle /asSubclass/ -m java.base,java.base
public <U> Class<T> Class<T>.asSubclass(Class<T>)
$
````

### [GitHub issue #16](https://github.com/paul-bennett/juggle/issues/32)

Support module "implied readability"

Prior to fixing this issue, specifying a module using `-m` would examine the classes
directly defined within the module, but not any classes from modules which it requires
transitively.

For example, the `java.se` module requires 20 modules transitively, including `java.sql`.
So the following two executions should return the same results.  (Prior to the fix,
the second -- `-m java.se` -- showed no results.)

````
% juggle -m java.sql -i java.sql -r ResultSet -p PreparedStatement
$ juggle -m java.sql -i java.sql ResultSet '(? super PreparedStatement)'
public abstract ResultSet PreparedStatement.executeQuery() throws SQLException
public abstract ResultSet Statement.getGeneratedKeys() throws SQLException
public abstract ResultSet Statement.getResultSet() throws SQLException
$
````

````
% juggle -m java.se -i java.sql -r ResultSet -p PreparedStatement
$ juggle -m java.se -i java.sql ResultSet '(? super PreparedStatement)'
public abstract ResultSet PreparedStatement.executeQuery() throws SQLException
public abstract ResultSet Statement.getGeneratedKeys() throws SQLException
public abstract ResultSet Statement.getResultSet() throws SQLException
$
````

### [GitHub issue #1](https://github.com/paul-bennett/juggle/issues/1)

Searching (with -p or -r) for an array of a primitive type falls back to Object

````
% juggle -p double[],int,int,double -r void
$ juggle void '(double[],int,int,double)'
public static void java.util.Arrays.fill(double[],int,int,double)
$
````



