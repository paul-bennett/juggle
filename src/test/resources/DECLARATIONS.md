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
public java.lang.reflect.Field jdk.internal.access.JavaLangReflectAccess.copyField(java.lang.reflect.Field)
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
public static String jdk.internal.icu.util.VersionInfo.ICU_DATA_VERSION_PATH
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
public Throwable[] Throwable.getSuppressed()
public void Thread.join(long) throws InterruptedException
public void Thread.join(long,int) throws InterruptedException
public void Thread.setName(String)
public void Throwable.addSuppressed(Throwable)
public void sun.security.provider.AbstractDrbg.engineSetSeed(byte[])
public void sun.security.provider.HashDrbg.generateAlgorithm(byte[],byte[])
$

$ juggle abstract -p Number       
public double Number.doubleValue()
public float Number.floatValue()
public int Number.intValue()
public long Number.longValue()
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
public boolean Thread.isAlive()
public boolean java.lang.invoke.VarHandle.compareAndSet(Object[])
public boolean java.lang.invoke.VarHandle.weakCompareAndSet(Object[])
public boolean java.lang.invoke.VarHandle.weakCompareAndSetAcquire(Object[])
public boolean java.lang.invoke.VarHandle.weakCompareAndSetPlain(Object[])
public boolean java.lang.invoke.VarHandle.weakCompareAndSetRelease(Object[])
public boolean jdk.internal.misc.Unsafe.compareAndSetInt(Object,long,int,int)
public boolean jdk.internal.misc.Unsafe.compareAndSetLong(Object,long,long,long)
public boolean jdk.internal.misc.Unsafe.compareAndSetReference(Object,long,Object,Object)
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
% juggle -i java.net Inet6Address
public static Inet6Address Inet6Address.getByAddress(String,byte[],int) throws UnknownHostException
public static Inet6Address Inet6Address.getByAddress(String,byte[],NetworkInterface) throws UnknownHostException
%
````
(We've just omitted the `-r`.)



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
usually interpreted by the shell so we additionally need quote
marks.)
````
$ juggle '/^isJavaLetter$/'
public static boolean Character.isJavaLetter(char)
$
````

Adding a `i` after the closing `/` makes the match case insensitive.
Combined with the anchors this gives us the same as the old `-n`.
````
$ juggle '/^isjavaletterordigit$/i'
public static boolean Character.isJavaLetterOrDigit(char)
$
````

