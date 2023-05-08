# Development of new declaration syntax

These tests don't necessarily make sense.  I came up with them
while developing the new declaration-style syntax.  As a result
they're a bit mix-and-match of both syntax styles.

## Just showing static members

All parts of the declaration are optional.  That means we can
use the declaration syntax to do curious things, such as filter
a search for only static members.

First, an ordinary old-style search:
````
$ juggle -r java.lang.reflect.Field        
public java.lang.reflect.Field Class<T>.getDeclaredField(String) throws NoSuchFieldException,SecurityException
public java.lang.reflect.Field Class<T>.getField(String) throws NoSuchFieldException,SecurityException
public abstract java.lang.reflect.Field jdk.internal.access.JavaLangReflectAccess.copyField(java.lang.reflect.Field)
public java.lang.reflect.Field java.lang.reflect.ReflectAccess.copyField(java.lang.reflect.Field)
public java.lang.reflect.Field jdk.internal.reflect.ConstantPool.getFieldAt(int)
public java.lang.reflect.Field jdk.internal.reflect.ConstantPool.getFieldAtIfLoaded(int)
public java.lang.reflect.Field jdk.internal.reflect.ReflectionFactory.copyField(java.lang.reflect.Field)
public static java.lang.reflect.Field sun.reflect.misc.FieldUtil.getField(Class<T>,String) throws NoSuchFieldException
$
````

Now let's do the same, but just show the `static` matches:
````
$ juggle -r java.lang.reflect.Field static
public static java.lang.reflect.Field sun.reflect.misc.FieldUtil.getField(Class<T>,String) throws NoSuchFieldException
$
````

Or we could look at all the `static` methods returning `Package`
that have been marked as `@Deprecated`:

````
$ juggle @Deprecated static -r Package
public static Package Package.getPackage(String)
$
````

Note that with this example we're putting the declaration first
and the old-style parameters last. Either pattern works. And in
fact, due to the way we gather the declaration, you can even put
old-style options right in the middle:

````
$ juggle  @Deprecated -p String static
public static boolean Compiler.compileClasses(String)
public static Package Package.getPackage(String)
public static final String jdk.internal.icu.util.VersionInfo.ICU_DATA_VERSION_PATH
public static String java.net.URLConnection.getDefaultRequestProperty(String)
public static String java.net.URLDecoder.decode(String)
public static String java.net.URLEncoder.encode(String)
public static long java.util.Date.parse(String)
public static sun.security.x509.AlgorithmId sun.security.x509.AlgorithmId.getAlgorithmId(String) throws java.security.NoSuchAlgorithmException
public static Object Compiler.command(Object)
$
````

(This is rather obtuse though, so should be discouraged.)

More example of modifiers:

````
$ juggle synchronized final
public final synchronized Throwable[] Throwable.getSuppressed()
public final synchronized void Thread.join(long) throws InterruptedException
public final synchronized void Thread.join(long,int) throws InterruptedException
public final synchronized void Thread.setName(String)
public final synchronized void Throwable.addSuppressed(Throwable)
public final synchronized void sun.security.provider.AbstractDrbg.engineSetSeed(byte[])
public final synchronized void sun.security.provider.HashDrbg.generateAlgorithm(byte[],byte[])
$

$ juggle abstract -p Number       
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
$ juggle -r boolean final native
public final native boolean Thread.isAlive()
public final transient native boolean java.lang.invoke.VarHandle.compareAndSet(Object[])
public final transient native boolean java.lang.invoke.VarHandle.weakCompareAndSet(Object[])
public final transient native boolean java.lang.invoke.VarHandle.weakCompareAndSetAcquire(Object[])
public final transient native boolean java.lang.invoke.VarHandle.weakCompareAndSetPlain(Object[])
public final transient native boolean java.lang.invoke.VarHandle.weakCompareAndSetRelease(Object[])
public final native boolean jdk.internal.misc.Unsafe.compareAndSetInt(Object,long,int,int)
public final native boolean jdk.internal.misc.Unsafe.compareAndSetLong(Object,long,long,long)
public final native boolean jdk.internal.misc.Unsafe.compareAndSetReference(Object,long,Object,Object)
$
````

## Return type

Here's a modified example from `README.md`:
````
$ juggle -i java.net -r Inet6Address
public static Inet6Address Inet6Address.getByAddress(String,byte[],int) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],NetworkInterface) throws UnknownHostException
$
````

The equivalent in the new syntax is:
````
$ juggle -i java.net Inet6Address
public static Inet6Address Inet6Address.getByAddress(String,byte[],int) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],NetworkInterface) throws UnknownHostException
$
````
(We've just omitted the `-r`.)

The above is an exact type match.

When using the `-r` option, we are implicitly setting an upper bound of the return type. Using the declaration
syntax we need to be specific if we want to use an upper bound.

For example, here are all the methods named `getByAddress` that return an `InetAddress` or one of its subclasses:
````
$ juggle -n getByAddress -i java.net -r InetAddress 
public static Inet6Address Inet6Address.getByAddress(String,byte[],int) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],NetworkInterface) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(byte[]) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(String,byte[]) throws UnknownHostException
$
````

If we omit the `-r` and specify a return type of `InetAddress`, we only get the methods that return exactly that type: 
````
$ juggle -n getByAddress -i java.net InetAddress 
public static InetAddress InetAddress.getByAddress(byte[]) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(String,byte[]) throws UnknownHostException
$
````

We can get back to the full list with the new syntax by explicitly requesting an upper bound:
````
$ juggle -n getByAddress -i java.net \? extends InetAddress 
public static Inet6Address Inet6Address.getByAddress(String,byte[],int) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],NetworkInterface) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(byte[]) throws UnknownHostException
public static InetAddress InetAddress.getByAddress(String,byte[]) throws UnknownHostException
$
````

We can specify lower bounds instead.  For example, which `getByAddress` methods return an `Inet6Address` or one
of its superclasses?
````
$ juggle -n getByAddress -i java.net \? super Inet6Address 
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
public LinkedList<E> jdk.internal.util.jar.JarIndex.get(String)
public LinkedList<E> sun.security.provider.PolicyParser.GrantEntry.principals
$
````

(Note that multiple lower bounds a separated by a `&` character, just like in Java declarations.  And of course
being a shell metacharacter this likely needs escaping in your shell.)

Finally, a question mark on its own represents an unbounded wildcard type.  Unlike in Java this also matches the 
`void` type and is equivalent to omitting an `-r` option.

Here are all the methods called `checkAccess`:
````
$ juggle -n checkAccess
public final void Thread.checkAccess()
public final void ThreadGroup.checkAccess()
public void SecurityManager.checkAccess(Thread)
public void SecurityManager.checkAccess(ThreadGroup)
public abstract boolean java.io.FileSystem.checkAccess(java.io.File,int)
public abstract transient void java.nio.file.spi.FileSystemProvider.checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
public abstract void jdk.internal.access.foreign.MemoryAddressProxy.checkAccess(long,long,boolean)
public abstract void jdk.internal.access.foreign.MemorySegmentProxy.checkAccess(long,long,boolean)
public native boolean java.io.UnixFileSystem.checkAccess(java.io.File,int)
public transient void jdk.internal.jrtfs.JrtFileSystemProvider.checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
public transient void sun.nio.fs.UnixFileSystemProvider.checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
$
````

And here they are again, specifying an unbounded return type:
````
$ juggle -n checkAccess \?
public final void Thread.checkAccess()
public final void ThreadGroup.checkAccess()
public void SecurityManager.checkAccess(Thread)
public void SecurityManager.checkAccess(ThreadGroup)
public abstract boolean java.io.FileSystem.checkAccess(java.io.File,int)
public abstract transient void java.nio.file.spi.FileSystemProvider.checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
public abstract void jdk.internal.access.foreign.MemoryAddressProxy.checkAccess(long,long,boolean)
public abstract void jdk.internal.access.foreign.MemorySegmentProxy.checkAccess(long,long,boolean)
public native boolean java.io.UnixFileSystem.checkAccess(java.io.File,int)
public transient void jdk.internal.jrtfs.JrtFileSystemProvider.checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
public transient void sun.nio.fs.UnixFileSystemProvider.checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
$
````

### Arrays

Are there any functions that return an array of arrays of `String`s?
````
$ juggle 'String[][]'           
public String[][] java.text.DateFormatSymbols.getZoneStrings()
public String[][] javax.security.auth.PrivateCredentialPermission.getPrincipals()
public static String[][] sun.util.locale.provider.TimeZoneNameUtility.getZoneStrings(java.util.Locale)
$
````

This can also be expressed using an ellipsis (even though that's not valid Java):
````
$ juggle 'String[]...'           
public String[][] java.text.DateFormatSymbols.getZoneStrings()
public String[][] javax.security.auth.PrivateCredentialPermission.getPrincipals()
public static String[][] sun.util.locale.provider.TimeZoneNameUtility.getZoneStrings(java.util.Locale)
$
````

## Matching member names

The new syntax lets us match members by name, either literal match
or by regular expression.

Here's what the old syntax allowed: a case-insensitive literal match:
````
$ juggle -n isjavaletterordigit
public static boolean Character.isJavaLetterOrDigit(char)
$
````

(There's an ambiguity in the raw grammar: an IDENT on its own might
be interpreted either as a return type or a member name.  In time
Juggle will resolve this by favouring a return type if a type of
that name can be found. For now though, I'm forcing the parser's
hand by including a type of `?`. TODO: remove these `?`s and `'`s.)

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
public Thread.<init>()
public static native Thread Thread.currentThread()
$
````
Answer: it shows methods with no parameters.  This feels natural, but does
raise the question of how to show methods with a single parameter.

The solution is to use an explicit wildcard, `(?)`:
````
$ juggle 'Thread (?)'
public Thread.<init>(Runnable)
public Thread.<init>(String)
public abstract Thread jdk.internal.misc.ScopedMemoryAccess.Scope.ownerThread()
public final Thread java.util.concurrent.locks.AbstractQueuedLongSynchronizer.getFirstQueuedThread()
public final Thread java.util.concurrent.locks.AbstractQueuedSynchronizer.getFirstQueuedThread()
public static Thread jdk.internal.misc.InnocuousThread.newSystemThread(Runnable)
public static Thread jdk.internal.misc.InnocuousThread.newThread(Runnable)
$
````

Which methods meet the general contract of the `Comparator` interface?
````
$ juggle "int (?,? extends Object, ? extends Object)"
public volatile int String.CaseInsensitiveComparator.compare(Object,Object)
public int String.CaseInsensitiveComparator.compare(String,String)
public abstract int com.sun.crypto.provider.GCM.doFinal(java.nio.ByteBuffer,java.nio.ByteBuffer)
public abstract int com.sun.crypto.provider.GCM.update(java.nio.ByteBuffer,java.nio.ByteBuffer)
public abstract int java.io.FileSystem.compare(java.io.File,java.io.File)
public abstract int java.nio.channels.DatagramChannel.send(java.nio.ByteBuffer,java.net.SocketAddress) throws java.io.IOException
public abstract int java.nio.file.attribute.UserDefinedFileAttributeView.read(String,java.nio.ByteBuffer) throws java.io.IOException
public abstract int java.nio.file.attribute.UserDefinedFileAttributeView.write(String,java.nio.ByteBuffer) throws java.io.IOException
public abstract int java.text.Collator.compare(String,String)
public abstract int java.util.Comparator<T>.compare(T,T)
public abstract int java.util.function.ToIntBiFunction<T,U>.applyAsInt(T,U)
public abstract int jdk.internal.org.xml.sax.Attributes.getIndex(String,String)
public final int java.util.concurrent.atomic.AtomicIntegerFieldUpdater<T>.getAndUpdate(T,java.util.function.IntUnaryOperator)
public final int java.util.concurrent.atomic.AtomicIntegerFieldUpdater<T>.updateAndGet(T,java.util.function.IntUnaryOperator)
public final int javax.crypto.Cipher.doFinal(java.nio.ByteBuffer,java.nio.ByteBuffer) throws javax.crypto.ShortBufferException,javax.crypto.IllegalBlockSizeException,javax.crypto.BadPaddingException
public final int javax.crypto.Cipher.update(java.nio.ByteBuffer,java.nio.ByteBuffer) throws javax.crypto.ShortBufferException
public final int sun.security.util.ByteArrayLexOrder.compare(byte[],byte[])
public final int sun.security.util.ByteArrayTagOrder.compare(byte[],byte[])
public int com.sun.crypto.provider.GCTR.doFinal(java.nio.ByteBuffer,java.nio.ByteBuffer)
public int com.sun.crypto.provider.GCTR.update(java.nio.ByteBuffer,java.nio.ByteBuffer)
public int com.sun.crypto.provider.GHASH.doFinal(java.nio.ByteBuffer,java.nio.ByteBuffer)
public int com.sun.crypto.provider.GHASH.update(java.nio.ByteBuffer,java.nio.ByteBuffer)
public int com.sun.crypto.provider.GaloisCounterMode.GCMDecrypt.doFinal(java.nio.ByteBuffer,java.nio.ByteBuffer) throws javax.crypto.IllegalBlockSizeException,javax.crypto.AEADBadTagException,javax.crypto.ShortBufferException
public int com.sun.crypto.provider.GaloisCounterMode.GCMDecrypt.doUpdate(java.nio.ByteBuffer,java.nio.ByteBuffer) throws javax.crypto.ShortBufferException
public int com.sun.crypto.provider.GaloisCounterMode.GCMEncrypt.doFinal(java.nio.ByteBuffer,java.nio.ByteBuffer) throws javax.crypto.IllegalBlockSizeException,javax.crypto.ShortBufferException
public int com.sun.crypto.provider.GaloisCounterMode.GCMEncrypt.doUpdate(java.nio.ByteBuffer,java.nio.ByteBuffer) throws javax.crypto.ShortBufferException
public int com.sun.crypto.provider.GaloisCounterMode.GCTRGHASH.doFinal(java.nio.ByteBuffer,java.nio.ByteBuffer)
public int com.sun.crypto.provider.GaloisCounterMode.GCTRGHASH.update(java.nio.ByteBuffer,java.nio.ByteBuffer)
public int java.io.UnixFileSystem.compare(java.io.File,java.io.File)
public volatile int java.net.CookieManager.CookieComparator.compare(Object,Object)
public int java.net.CookieManager.CookieComparator.compare(java.net.HttpCookie,java.net.HttpCookie)
public int java.text.Collator.compare(Object,Object)
public int java.util.Arrays.NaturalOrder.compare(Object,Object)
public int java.util.Base64.Decoder.decode(byte[],byte[])
public int java.util.Base64.Encoder.encode(byte[],byte[])
public int java.util.Collections.ReverseComparator.compare(Comparable<T>,Comparable<T>)
public volatile int java.util.Collections.ReverseComparator.compare(Object,Object)
public int java.util.Collections.ReverseComparator2<T>.compare(T,T)
public int java.util.Comparators.NaturalOrderComparator.compare(Comparable<T>,Comparable<T>)
public volatile int java.util.Comparators.NaturalOrderComparator.compare(Object,Object)
public int java.util.Comparators.NullComparator<T>.compare(T,T)
public int java.util.concurrent.SubmissionPublisher<T>.offer(T,java.util.function.BiPredicate<T,U>)
public int jdk.internal.icu.text.UnicodeSet.span(CharSequence,jdk.internal.icu.text.UnicodeSet.SpanCondition)
public int jdk.internal.math.FDBigInteger.addAndCmp(jdk.internal.math.FDBigInteger,jdk.internal.math.FDBigInteger)
public int jdk.internal.org.objectweb.asm.ClassWriter.newNameType(String,String)
public int jdk.internal.util.xml.impl.Attrs.getIndex(String,String)
public volatile int sun.launcher.LauncherHelper.JrtFirstComparator.compare(Object,Object)
public int sun.launcher.LauncherHelper.JrtFirstComparator.compare(java.lang.module.ModuleReference,java.lang.module.ModuleReference)
public int sun.nio.ch.DatagramChannelImpl.send(java.nio.ByteBuffer,java.net.SocketAddress) throws java.io.IOException
public volatile int sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl.send(java.nio.ByteBuffer,java.net.SocketAddress) throws java.io.IOException
public int sun.nio.fs.UnixUserDefinedFileAttributeView.read(String,java.nio.ByteBuffer) throws java.io.IOException
public int sun.nio.fs.UnixUserDefinedFileAttributeView.write(String,java.nio.ByteBuffer) throws java.io.IOException
public volatile int sun.security.provider.certpath.ForwardBuilder.PKIXCertComparator.compare(Object,Object)
public int sun.security.provider.certpath.ForwardBuilder.PKIXCertComparator.compare(java.security.cert.X509Certificate,java.security.cert.X509Certificate)
public volatile int sun.security.provider.certpath.PKIX.CertStoreComparator.compare(Object,Object)
public int sun.security.provider.certpath.PKIX.CertStoreComparator.compare(java.security.cert.CertStore,java.security.cert.CertStore)
public volatile int sun.security.util.ByteArrayLexOrder.compare(Object,Object)
public volatile int sun.security.util.ByteArrayTagOrder.compare(Object,Object)
public volatile int sun.security.x509.AVAComparator.compare(Object,Object)
public int sun.security.x509.AVAComparator.compare(sun.security.x509.AVA,sun.security.x509.AVA)
public volatile int sun.util.locale.provider.CalendarNameProviderImpl.LengthBasedComparator.compare(Object,Object)
public int sun.util.locale.provider.CalendarNameProviderImpl.LengthBasedComparator.compare(String,String)
public static <T> int java.util.Arrays.binarySearch(T[],T,java.util.Comparator<T>)
public static <T> int java.util.Arrays.compare(T[],T[],java.util.Comparator<T>)
public static <T> int java.util.Arrays.mismatch(T[],T[],java.util.Comparator<T>)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T,java.util.Comparator<T>)
public static <T> int java.util.Objects.compare(T,T,java.util.Comparator<T>)
public static native int sun.nio.ch.Net.accept(java.io.FileDescriptor,java.io.FileDescriptor,java.net.InetSocketAddress[]) throws java.io.IOException
public synchronized int java.text.RuleBasedCollator.compare(String,String)
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
public static int jdk.internal.icu.text.UTF16.charAt(String,int)
public static int jdk.internal.jimage.ImageStringsReader.hashCode(String,int)
public static int jdk.internal.jimage.ImageStringsReader.unmaskedHashCode(String,int)
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
public static int jdk.internal.icu.text.UTF16.charAt(String,int)
public static int jdk.internal.jimage.ImageStringsReader.hashCode(String,int)
public static int jdk.internal.jimage.ImageStringsReader.unmaskedHashCode(String,int)
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



