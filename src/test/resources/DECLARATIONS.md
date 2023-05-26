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
# Development of new declaration syntax

These tests don't necessarily make sense.  I came up with them
while developing the new declaration-style syntax.  As a result
they're a bit mix-and-match of both syntax styles.

## Just showing static members

All parts of the declaration are optional.  That means we can
use the declaration syntax to do curious things, such as filter
a search for only static members.

First, just looking for the return type:
````
$ juggle java.io.OutputStream
public abstract java.io.OutputStream Process.getOutputStream()
public java.io.OutputStream ProcessImpl.getOutputStream()
public java.io.OutputStream.<init>()
public static java.io.OutputStream java.io.OutputStream.nullOutputStream()
public abstract java.io.OutputStream java.net.CacheRequest.getBody() throws java.io.IOException
public java.io.OutputStream java.net.Socket.getOutputStream() throws java.io.IOException
public java.io.OutputStream java.net.URLConnection.getOutputStream() throws java.io.IOException
public static java.io.OutputStream java.nio.channels.Channels.newOutputStream(java.nio.channels.AsynchronousByteChannel)
public static java.io.OutputStream java.nio.channels.Channels.newOutputStream(java.nio.channels.WritableByteChannel)
public static transient java.io.OutputStream java.nio.file.Files.newOutputStream(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
public transient java.io.OutputStream java.nio.file.spi.FileSystemProvider.newOutputStream(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
public java.io.OutputStream java.util.Base64.Encoder.wrap(java.io.OutputStream)
$
````

Now let's do the same, but just show the `static` matches:
````
$ juggle static java.io.OutputStream
public static java.io.OutputStream java.io.OutputStream.nullOutputStream()
public static java.io.OutputStream java.nio.channels.Channels.newOutputStream(java.nio.channels.AsynchronousByteChannel)
public static java.io.OutputStream java.nio.channels.Channels.newOutputStream(java.nio.channels.WritableByteChannel)
public static transient java.io.OutputStream java.nio.file.Files.newOutputStream(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
$
````

Or we could look at all the `static` methods returning `Package`
that have been marked as `@Deprecated`:

````
$ juggle @Deprecated static Package 
public static Package Package.getPackage(String)
$
````

More example of modifiers:

````
$ juggle synchronized final
public final synchronized void Throwable.addSuppressed(Throwable)
public final synchronized Throwable[] Throwable.getSuppressed()
public final synchronized void Thread.join(long) throws InterruptedException
public final synchronized void Thread.join(long,int) throws InterruptedException
public final synchronized void Thread.setName(String)
$

$ juggle abstract '(Number)'       
public abstract double Number.doubleValue()
public abstract float Number.floatValue()
public abstract int Number.intValue()
public abstract long Number.longValue()
$
````

Since `strictfp` became the default implementation in Java 17,
we find no methods where it's explicitly specified:
````
$ juggle strictfp
$
````

And here are all the `final native` methods returning a `boolean`:
````
$ juggle final native boolean
public final native boolean Thread.isAlive()
public final transient native boolean java.lang.invoke.VarHandle.compareAndSet(Object[])
public final transient native boolean java.lang.invoke.VarHandle.weakCompareAndSet(Object[])
public final transient native boolean java.lang.invoke.VarHandle.weakCompareAndSetAcquire(Object[])
public final transient native boolean java.lang.invoke.VarHandle.weakCompareAndSetPlain(Object[])
public final transient native boolean java.lang.invoke.VarHandle.weakCompareAndSetRelease(Object[])
$
````

## Return type

Here's a modified example from `README.md`:
````
$ juggle -i java.net Inet6Address
public static Inet6Address Inet6Address.getByAddress(String,byte[],int) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],NetworkInterface) throws UnknownHostException
$
````
The above is an exact type match.

When using the `-r` option, we are implicitly setting an upper bound of the return type. Using the declaration
syntax we need to be specific if we want to use an upper bound.

For example, here are all the methods named `getByAddress` that return an `InetAddress` or one of its subclasses:
````
$ juggle -i java.net '? extends InetAddress getByAddress' 
public static Inet6Address Inet6Address.getByAddress(String,byte[],int) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],NetworkInterface) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(byte[]) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(String,byte[]) throws UnknownHostException
$
````

If we omit the `? extends` wildcard, we only get the methods that return exactly that type: 
````
$ juggle -i java.net InetAddress getByAddress 
public static InetAddress InetAddress.getByAddress(byte[]) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(String,byte[]) throws UnknownHostException
$
````

We can specify lower bounds instead.  For example, which `getByAddress` methods return an `Inet6Address` or one
of its superclasses?
````
$ juggle -i java.net \? super Inet6Address getByAddress 
public static Inet6Address Inet6Address.getByAddress(String,byte[],int) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],NetworkInterface) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(byte[]) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(String,byte[]) throws UnknownHostException
$
````

Because classes can implement multiple interfaces, it's possible to specify multiple lower bounds.
How do I get hold of an instance of a class that implements both the `List` and `Queue` interfaces?
````
$ juggle -i java.util \? extends Queue \& List
public LinkedList<E>.<init>()
public LinkedList<E>.<init>(Collection<E>)
$
````

(Note that multiple lower bounds a separated by a `&` character, just like in Java declarations.  And of course
being a shell metacharacter this likely needs escaping in your shell.)

Finally, a question mark on its own represents an unbounded wildcard type.  Unlike in Java this also matches the 
`void` type and is equivalent to omitting an `-r` option.

Here are all the methods called `checkAccess`:
````
$ juggle /checkAccess/
public final void Thread.checkAccess()
public final void ThreadGroup.checkAccess()
public void SecurityManager.checkAccess(Thread)
public void SecurityManager.checkAccess(ThreadGroup)
public abstract boolean java.io.FileSystem.checkAccess(java.io.File,int)
public native boolean java.io.UnixFileSystem.checkAccess(java.io.File,int)
public abstract transient void java.nio.file.spi.FileSystemProvider.checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
$
````

And here they are again, specifying an unbounded return type:
````
$ juggle \? checkAccess
public final void Thread.checkAccess()
public final void ThreadGroup.checkAccess()
public void SecurityManager.checkAccess(Thread)
public void SecurityManager.checkAccess(ThreadGroup)
public abstract boolean java.io.FileSystem.checkAccess(java.io.File,int)
public native boolean java.io.UnixFileSystem.checkAccess(java.io.File,int)
public abstract transient void java.nio.file.spi.FileSystemProvider.checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
$
````

### Arrays

Are there any functions that return an array of arrays of `String`s?
````
$ juggle 'String[][]'           
public String[][] java.text.DateFormatSymbols.getZoneStrings()
public String[][] javax.security.auth.PrivateCredentialPermission.getPrincipals()
$
````

This can also be expressed using an ellipsis (even though that's not valid Java):
````
$ juggle 'String[]...'           
public String[][] java.text.DateFormatSymbols.getZoneStrings()
public String[][] javax.security.auth.PrivateCredentialPermission.getPrincipals()
$
````

## Matching member names

The new syntax lets us match members by name, either literal match
or by regular expression.

Here's what the old syntax allowed: a case-insensitive literal match:
````
$ juggle /isjavaletterordigit/i
public static boolean Character.isJavaLetterOrDigit(char)
$
````

(There's an ambiguity in the raw grammar: an IDENT on its own might
be interpreted either as a return type or a member name.  In time
Juggle will resolve this by favouring a return type if a type of
that name can be found. For now though, I'm forcing the parser's
hand by including a type of `?`.)

Just dropping the `-n` adopts the new syntax, but literals are matched
case-sensitively, so our first attempt fails: 
````
$ juggle '? isjavaletterordigit'
$
````

Getting the case right works:
````
$ juggle '? isJavaLetterOrDigit'
public static boolean Character.isJavaLetterOrDigit(char)
$
````

We can switch to using a regular expression by surrounding in `/`
characters. REs aren't to the ends of the member, so this matches
two methods:
````
$ juggle /isJavaLetter/
public static boolean Character.isJavaLetter(char)
public static boolean Character.isJavaLetterOrDigit(char)
$
````

Using `^` and `$` ties it. (Note: both of these characters are
usually interpreted by the shell, so we additionally need quote
marks.)
````
$ juggle '/^isJavaLetter$/'
public static boolean Character.isJavaLetter(char)
$
````

Adding a `i` after the closing `/` makes the match case-insensitive.
Combined with the anchors this gives us the same as the old `-n`.
````
$ juggle '/^isjavaletterordigit$/i'
public static boolean Character.isJavaLetterOrDigit(char)
$
````

## Parameters

Are there any functions that take twenty parameters? Juggle knows (19 commas 
separate 20 parameters) ...
````
$ juggle "(,,,,,,,,,,,,,,,,,,,)"
public static <K,V> java.util.Map<K,V> java.util.Map<K,V>.of(K,V,K,V,K,V,K,V,K,V,K,V,K,V,K,V,K,V,K,V)
$
````

If `(,)` shows methods with two unknown parameters, what does `()` show?
````
$ juggle 'Thread ()'
public static native Thread Thread.currentThread()
public Thread.<init>()
$
````
Answer: it shows methods with no parameters.  This feels natural, but does
raise the question of how to show methods with a single parameter.

The solution is to use an explicit wildcard, `(?)`:
````
$ juggle 'Thread (?)'
public Thread.<init>(Runnable)
public Thread.<init>(String)
public final Thread java.util.concurrent.locks.AbstractQueuedLongSynchronizer.getFirstQueuedThread()
public final Thread java.util.concurrent.locks.AbstractQueuedSynchronizer.getFirstQueuedThread()
$
````

Which methods meet the general contract of the `Comparator` interface?
````
$ juggle "int (?,? extends Object, ? extends Object)"
public volatile int String.CaseInsensitiveComparator.compare(Object,Object)
public volatile int java.net.CookieManager.CookieComparator.compare(Object,Object)
public int java.text.Collator.compare(Object,Object)
public abstract int java.util.Comparator<T>.compare(T,T)
public int java.util.Arrays.NaturalOrder.compare(Object,Object)
public volatile int java.util.Collections.ReverseComparator.compare(Object,Object)
public int java.util.Collections.ReverseComparator2<T>.compare(T,T)
public volatile int java.util.Comparators.NaturalOrderComparator.compare(Object,Object)
public int java.util.Comparators.NullComparator<T>.compare(T,T)
public abstract int java.util.function.ToIntBiFunction<T,U>.applyAsInt(T,U)
public static <T> int java.util.Arrays.binarySearch(T[],T,java.util.Comparator<T>)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T,java.util.Comparator<T>)
public static <T> int java.util.Objects.compare(T,T,java.util.Comparator<T>)
public int java.util.concurrent.SubmissionPublisher<T>.offer(T,java.util.function.BiPredicate<T,U>)
public final int java.util.concurrent.atomic.AtomicIntegerFieldUpdater<T>.getAndUpdate(T,java.util.function.IntUnaryOperator)
public final int java.util.concurrent.atomic.AtomicIntegerFieldUpdater<T>.updateAndGet(T,java.util.function.IntUnaryOperator)
public int String.CaseInsensitiveComparator.compare(String,String)
public abstract int java.io.FileSystem.compare(java.io.File,java.io.File)
public int java.io.UnixFileSystem.compare(java.io.File,java.io.File)
public int java.net.CookieManager.CookieComparator.compare(java.net.HttpCookie,java.net.HttpCookie)
public abstract int java.nio.channels.DatagramChannel.send(java.nio.ByteBuffer,java.net.SocketAddress) throws java.io.IOException
public abstract int java.nio.file.attribute.UserDefinedFileAttributeView.read(String,java.nio.ByteBuffer) throws java.io.IOException
public abstract int java.nio.file.attribute.UserDefinedFileAttributeView.write(String,java.nio.ByteBuffer) throws java.io.IOException
public abstract int java.text.Collator.compare(String,String)
public synchronized int java.text.RuleBasedCollator.compare(String,String)
public int java.util.Collections.ReverseComparator.compare(Comparable<T>,Comparable<T>)
public int java.util.Comparators.NaturalOrderComparator.compare(Comparable<T>,Comparable<T>)
public static <T> int java.util.Arrays.compare(T[],T[],java.util.Comparator<T>)
public int java.util.Base64.Decoder.decode(byte[],byte[])
public int java.util.Base64.Encoder.encode(byte[],byte[])
public static <T> int java.util.Arrays.mismatch(T[],T[],java.util.Comparator<T>)
public final int javax.crypto.Cipher.doFinal(java.nio.ByteBuffer,java.nio.ByteBuffer) throws javax.crypto.ShortBufferException,javax.crypto.IllegalBlockSizeException,javax.crypto.BadPaddingException
public final int javax.crypto.Cipher.update(java.nio.ByteBuffer,java.nio.ByteBuffer) throws javax.crypto.ShortBufferException
$
````
A fair few! Note that not all of these really qualify though, since to qualify
as a `Comparator` lambda, the method needs to be the only one in its class.
Juggle can't tell you that.

Lower type bounds come into their own with parameter queries.  Imagine I have a
`Inet6Address`. What methods can I use to get a `NetworkInterface` from it?
````
$ juggle -i java.net NetworkInterface (? super Inet6Address)
public NetworkInterface Inet6Address.getScopedInterface()
public static NetworkInterface NetworkInterface.getByInetAddress(InetAddress) throws SocketException
$
````

## Exceptions

If we don't specify a `throws` clause, Juggle shows members that throw along
with those that don't:
````
$ juggle 'int (String,int)'
public int String.codePointAt(int)
public int String.codePointBefore(int)
public int String.indexOf(int)
public int String.lastIndexOf(int)
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
$
````

If we specify the `throws` keyword but don't follow it with any classes,
Juggle only lists the members that _don't_ throw any types:
````
$ juggle 'int (String,int) throws'
public int String.codePointAt(int)
public int String.codePointBefore(int)
public int String.indexOf(int)
public int String.lastIndexOf(int)
$
````

Conversely, specifying a type after the `throws` only lists members that throw
that particular type:
````
$ juggle 'int (String,int) throws NumberFormatException'
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
$
````

As with return and parameter types, exception types are matched precisely
which is why we see no results when we try to match on a superclass:
````
$ juggle 'int (String,int) throws RuntimeException'
$
````

But we can use an upper bound if we wanted to match any class that is lower in
the exception hierarchy:
````
$ juggle 'int (String,int) throws ? extends NumberFormatException'
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
$
````

Or even a wildcard if we don't care what class might be thrown:
````
$ juggle 'int (String,int) throws ?'
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
$
````

Lower bounds are possible too:
````
$ juggle 'int (String,int) throws ? super NumberFormatException'
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
$
````



