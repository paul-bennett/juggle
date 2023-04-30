# Regressions

This file contains regression tests for Juggle; curiosities that show
how we expect the Juggle command-line to operate in corner cases.

The file is parsed by `com.angellane.juggle.TestSamples`. See also the
comment in [README.md](README.md) for more details about how it's parsed.
But in essence, add a test by copying one of the code blocks.

## Command-line Parsing

If we pass an invalid argument, we should get an error and the help text:

````
$ juggle --fiddle-de-dee
Unknown option: '--fiddle-de-dee'
Usage: juggle [-hVx] [-@=type,type,...] [-a=private|protected|package|public]
              [-f=<formatterOption>] [-i=packageName] [-j=jarFilePath]
              [-m=moduleName] [-n=methodName] [-p=type,type,...] [-r=type]
              [-s=<addSortCriteria>] [-t=type,type,...] [declaration...]
An API search tool for Java
      [declaration...]       A Java-style declaration to match against
  -@, --annotation=type,type,...
                             Annotations
  -a, --access=private|protected|package|public
                             Minimum accessibility of members to return
  -f, --format=<formatterOption>
                             Output format
  -h, --help                 Show this help message and exit.
  -i, --import=packageName   Imported package names
  -j, --jar=jarFilePath      JAR file to include in search
  -m, --module=moduleName    Modules to search
  -n, --name=methodName      Filter by member name
  -p, --param=type,type,...  Parameter type of searched function
  -r, --return=type          Return type of searched function
  -s, --sort=<addSortCriteria>
                             Sort criteria
  -t, --throws=type,type,... Thrown types
  -V, --version              Print version information and exit.
  -x, --[no-]permute         Also match permutations of parameters
$
````

Of course, we can explicitly ask for help:

````
$ juggle --help
Usage: juggle [-hVx] [-@=type,type,...] [-a=private|protected|package|public]
              [-f=<formatterOption>] [-i=packageName] [-j=jarFilePath]
              [-m=moduleName] [-n=methodName] [-p=type,type,...] [-r=type]
              [-s=<addSortCriteria>] [-t=type,type,...] [declaration...]
An API search tool for Java
      [declaration...]       A Java-style declaration to match against
  -@, --annotation=type,type,...
                             Annotations
  -a, --access=private|protected|package|public
                             Minimum accessibility of members to return
  -f, --format=<formatterOption>
                             Output format
  -h, --help                 Show this help message and exit.
  -i, --import=packageName   Imported package names
  -j, --jar=jarFilePath      JAR file to include in search
  -m, --module=moduleName    Modules to search
  -n, --name=methodName      Filter by member name
  -p, --param=type,type,...  Parameter type of searched function
  -r, --return=type          Return type of searched function
  -s, --sort=<addSortCriteria>
                             Sort criteria
  -t, --throws=type,type,... Thrown types
  -V, --version              Print version information and exit.
  -x, --[no-]permute         Also match permutations of parameters
$
````

We can ask for the version of the application, but when run from an unpacked
source tree it doesn't show anything useful: 
````
$ juggle --version
Unknown
$
````

## Development of new declaration syntax

These tests don't necessarily make sense.  I came up with them
while developing the new declaration-style syntax.  As a result
they're a bit mix-and-match of both syntax styles.

# Just showing static members

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

Or we could look at all the `static` methods returning `void`
that have been marked as `@Deprecated`:

````
$ juggle @java.lang.Deprecated static -r void
public static void Compiler.disable()
public static void Compiler.enable()
public static void System.setSecurityManager(SecurityManager)
public static void java.net.URLConnection.setDefaultRequestProperty(String,String)
public static void java.security.AccessController.checkPermission(java.security.Permission) throws java.security.AccessControlException
public static void java.security.Policy.setPolicy(java.security.Policy)
public static void sun.net.www.protocol.http.HttpURLConnection.setDefaultAuthenticator(sun.net.www.protocol.http.HttpAuthenticator)
$
````

Note that with this example we're putting the declaration first
and the old-style parameters last. Either pattern works. And in
fact, due to the way we gather the declaration, you can even put
old-style options right in the middle:

````
$ juggle  @java.lang.Deprecated -p char static
public static boolean Character.isJavaLetter(char)
public static boolean Character.isJavaLetterOrDigit(char)
public static boolean Character.isSpace(char)
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

I'm unclear why no methods are flagged as `strictfp`:
````
$ juggle strictfp
$
````

Equally, I'm surprised at how few are showing up as `native`:
````
$ juggle native
public boolean Class<T>.isArray()
public boolean Class<T>.isAssignableFrom(Class<T>)
public boolean Class<T>.isHidden()
public boolean Class<T>.isInstance(Object)
public boolean Class<T>.isInterface()
public boolean Class<T>.isPrimitive()
public int Class<T>.getModifiers()
public int Object.hashCode()
public int Runtime.availableProcessors()
public Class<T> Class<T>.getSuperclass()
public Object[] Class<T>.getSigners()
public String String.intern()
public long Runtime.freeMemory()
public long Runtime.maxMemory()
public long Runtime.totalMemory()
public void Runtime.gc()
public boolean java.io.UnixFileSystem.checkAccess(java.io.File,int)
public boolean java.io.UnixFileSystem.createDirectory(java.io.File)
public boolean java.io.UnixFileSystem.createFileExclusively(String) throws java.io.IOException
public boolean java.io.UnixFileSystem.setLastModifiedTime(java.io.File,long)
public boolean java.io.UnixFileSystem.setPermission(java.io.File,int,boolean,boolean)
public boolean java.io.UnixFileSystem.setReadOnly(java.io.File)
public boolean jdk.internal.misc.Unsafe.getBoolean(Object,long)
public boolean jdk.internal.misc.Unsafe.getBooleanVolatile(Object,long)
public byte jdk.internal.misc.Unsafe.getByte(Object,long)
public byte jdk.internal.misc.Unsafe.getByteVolatile(Object,long)
public char jdk.internal.misc.Unsafe.getChar(Object,long)
public char jdk.internal.misc.Unsafe.getCharVolatile(Object,long)
public double jdk.internal.misc.Unsafe.getDouble(Object,long)
public double jdk.internal.misc.Unsafe.getDoubleVolatile(Object,long)
public float jdk.internal.misc.Unsafe.getFloat(Object,long)
public float jdk.internal.misc.Unsafe.getFloatVolatile(Object,long)
public int java.io.UnixFileSystem.getBooleanAttributes0(java.io.File)
public int jdk.internal.misc.Unsafe.getInt(Object,long)
public int jdk.internal.misc.Unsafe.getIntVolatile(Object,long)
public Class<T> jdk.internal.misc.Unsafe.defineClass0(String,byte[],int,int,ClassLoader,java.security.ProtectionDomain)
public Object jdk.internal.misc.Unsafe.allocateInstance(Class<T>) throws InstantiationException
public Object jdk.internal.misc.Unsafe.getReference(Object,long)
public Object jdk.internal.misc.Unsafe.getReferenceVolatile(Object,long)
public Object jdk.internal.misc.Unsafe.getUncompressedObject(long)
public String java.net.Inet4AddressImpl.getHostByAddr(byte[]) throws java.net.UnknownHostException
public String java.net.Inet4AddressImpl.getLocalHostName() throws java.net.UnknownHostException
public String java.net.Inet6AddressImpl.getHostByAddr(byte[]) throws java.net.UnknownHostException
public String java.net.Inet6AddressImpl.getLocalHostName() throws java.net.UnknownHostException
public String[] java.io.UnixFileSystem.list(java.io.File)
public java.net.InetAddress[] java.net.Inet4AddressImpl.lookupAllHostAddr(String) throws java.net.UnknownHostException
public java.net.InetAddress[] java.net.Inet6AddressImpl.lookupAllHostAddr(String) throws java.net.UnknownHostException
public java.nio.ByteBuffer jdk.internal.perf.Perf.createByteArray(String,int,int,byte[],int)
public java.nio.ByteBuffer jdk.internal.perf.Perf.createLong(String,int,int,long)
public long java.io.RandomAccessFile.getFilePointer() throws java.io.IOException
public long java.io.RandomAccessFile.length() throws java.io.IOException
public long java.io.UnixFileSystem.getLastModifiedTime(java.io.File)
public long java.io.UnixFileSystem.getLength(java.io.File)
public long java.io.UnixFileSystem.getSpace(java.io.File,int)
public long jdk.internal.misc.Unsafe.getLong(Object,long)
public long jdk.internal.misc.Unsafe.getLongVolatile(Object,long)
public long jdk.internal.perf.Perf.highResCounter()
public long jdk.internal.perf.Perf.highResFrequency()
public short jdk.internal.misc.Unsafe.getShort(Object,long)
public short jdk.internal.misc.Unsafe.getShortVolatile(Object,long)
public void java.io.FileDescriptor.sync() throws java.io.SyncFailedException
public void java.io.RandomAccessFile.setLength(long) throws java.io.IOException
public void jdk.internal.misc.Unsafe.fullFence()
public void jdk.internal.misc.Unsafe.loadFence()
public void jdk.internal.misc.Unsafe.park(boolean,long)
public void jdk.internal.misc.Unsafe.putBoolean(Object,long,boolean)
public void jdk.internal.misc.Unsafe.putBooleanVolatile(Object,long,boolean)
public void jdk.internal.misc.Unsafe.putByte(Object,long,byte)
public void jdk.internal.misc.Unsafe.putByteVolatile(Object,long,byte)
public void jdk.internal.misc.Unsafe.putChar(Object,long,char)
public void jdk.internal.misc.Unsafe.putCharVolatile(Object,long,char)
public void jdk.internal.misc.Unsafe.putDouble(Object,long,double)
public void jdk.internal.misc.Unsafe.putDoubleVolatile(Object,long,double)
public void jdk.internal.misc.Unsafe.putFloat(Object,long,float)
public void jdk.internal.misc.Unsafe.putFloatVolatile(Object,long,float)
public void jdk.internal.misc.Unsafe.putInt(Object,long,int)
public void jdk.internal.misc.Unsafe.putIntVolatile(Object,long,int)
public void jdk.internal.misc.Unsafe.putLong(Object,long,long)
public void jdk.internal.misc.Unsafe.putLongVolatile(Object,long,long)
public void jdk.internal.misc.Unsafe.putReference(Object,long,Object)
public void jdk.internal.misc.Unsafe.putReferenceVolatile(Object,long,Object)
public void jdk.internal.misc.Unsafe.putShort(Object,long,short)
public void jdk.internal.misc.Unsafe.putShortVolatile(Object,long,short)
public void jdk.internal.misc.Unsafe.storeFence()
public void jdk.internal.misc.Unsafe.throwException(Throwable)
public void jdk.internal.misc.Unsafe.unpark(Object)
$
````

## Previously fixed bugs

### [GitHub issue #1](https://github.com/paul-bennett/juggle/issues/1)

Searching (with -p or -r) for an array of a primitive type falls back to Object


````
$ juggle -p double[],int,int,double -r void
public static void java.util.Arrays.fill(double[],int,int,double)
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
$ juggle -m java.sql -i java.sql -r ResultSet -p PreparedStatement
public ResultSet PreparedStatement.executeQuery() throws SQLException
public ResultSet Statement.getGeneratedKeys() throws SQLException
public ResultSet Statement.getResultSet() throws SQLException
$
````

````
$ juggle -m java.se -i java.sql -r ResultSet -p PreparedStatement
public ResultSet PreparedStatement.executeQuery() throws SQLException
public ResultSet Statement.getGeneratedKeys() throws SQLException
public ResultSet Statement.getResultSet() throws SQLException
$
````

### [GitHub issue #32](https://github.com/paul-bennett/juggle/issues/32)

Results aren't deduplicated

````
$ juggle -n asSubclass -m java.base,java.base
public <U> Class<T> Class<T>.asSubclass(Class<T>)
$
````

