# Declaration-style tests from README

This file contains test equivalent to those in the original `README.md`
but re-expressed in the new declaration-style syntax.  The original
query is shown commented out on the line that precedes the new invocation.

Look for the word _BUG_ in this file to see tests which didn't provide
identical output.  These have been commented out by switching the shell
prompt to `%`.

````
$ #juggle -p java.time.Clock -r java.time.LocalTime
$ juggle java.time.LocalTime (java.time.Clock)
public static java.time.LocalTime java.time.LocalTime.now(java.time.Clock)
$
````
````
$ #juggle -p double[] -p int -p int -p double -r void
$ juggle void '(double[], int, int, double)' 
public static void java.util.Arrays.fill(double[],int,int,double)
$
````
````
$ #juggle -p double[] -p int -p int -p double
$ juggle '(double[], int, int, double)' 
public static int java.util.Arrays.binarySearch(double[],int,int,double)
public static void java.util.Arrays.fill(double[],int,int,double)
$
````
````
$ #juggle -r java.net.Inet6Address
$ juggle java.net.Inet6Address
public static java.net.Inet6Address java.net.Inet6Address.getByAddress(String,byte[],int) throws java.net.UnknownHostException
public static java.net.Inet6Address java.net.Inet6Address.getByAddress(String,byte[],java.net.NetworkInterface) throws java.net.UnknownHostException
$
````
BUG: should be fixed by #58: Add --conversions option
````
$ #juggle -p java.util.regex.Matcher -p java.lang.String -r java.lang.String
% juggle 'java.lang.String (java.util.regex.Matcher, java.lang.String)' 
$ juggle 'java.lang.String (? super java.util.regex.Matcher, java.lang.String)' 
public String java.util.regex.Matcher.group(String)
public String java.util.regex.Matcher.replaceAll(String)
public String java.util.regex.Matcher.replaceFirst(String)
public static String java.util.Objects.toString(Object,String)
$
````
BUG: #96: Sort order of declaration syntax output not same as original
````
$ #juggle -r int -p java.io.InterruptedIOException
% juggle 'int (java.io.InterruptedIOException)'
% juggle 'int (java.io.InterruptedIOException)'
public int java.io.InterruptedIOException.bytesTransferred
public native int Object.hashCode()
public static native int System.identityHashCode(Object)
public static int java.util.Objects.hashCode(Object)
public static int sun.invoke.util.ValueConversions.widenSubword(Object)
public static native int java.lang.reflect.Array.getLength(Object) throws IllegalArgumentException
$
````
````
$ #juggle -t java.net.URISyntaxException
$ juggle throws java.net.URISyntaxException
public java.net.URI java.net.URI.parseServerAuthority() throws java.net.URISyntaxException
public java.net.URI java.net.URL.toURI() throws java.net.URISyntaxException
public java.net.URI.<init>(String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String,int,String,String,String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String,String) throws java.net.URISyntaxException
public java.net.URI.<init>(String,String,String,String,String) throws java.net.URISyntaxException
$
````
````
$ #juggle -@ FunctionalInterface -r int
$ juggle @FunctionalInterface int
public abstract int java.util.Comparator<T>.compare(T,T)
public abstract int java.util.function.DoubleToIntFunction.applyAsInt(double)
public abstract int java.util.function.IntBinaryOperator.applyAsInt(int,int)
public abstract int java.util.function.IntSupplier.getAsInt()
public abstract int java.util.function.IntUnaryOperator.applyAsInt(int)
public abstract int java.util.function.LongToIntFunction.applyAsInt(long)
public abstract int java.util.function.ToIntBiFunction<T,U>.applyAsInt(T,U)
public abstract int java.util.function.ToIntFunction<T>.applyAsInt(T)
$
````
````
$ #juggle -n substring
$ juggle /substring/i
public String AbstractStringBuilder.substring(int)
public String AbstractStringBuilder.substring(int,int)
public String String.substring(int)
public String String.substring(int,int)
public volatile String StringBuilder.substring(int)
public volatile String StringBuilder.substring(int,int)
public synchronized String StringBuffer.substring(int)
public synchronized String StringBuffer.substring(int,int)
$
````
BUG: this fails due to #39: Classes loaded by inconsistent loaders (visible only in test)
````
$ #juggle -j build/libs/testLib.jar -r com.angellane.juggle.testinput.lib.Lib -a package
% juggle -j build/libs/testLib.jar package com.angellane.juggle.testinput.lib.Lib
% juggle -j build/libs/testLib.jar package com.angellane.juggle.testinput.lib.Lib
public static com.angellane.juggle.testinput.lib.Lib com.angellane.juggle.testinput.lib.Lib.libFactory()
com.angellane.juggle.testinput.lib.Lib.<init>()
$
````
````
$ #juggle -m java.sql -r java.sql.CallableStatement
$ juggle -m java.sql java.sql.CallableStatement
public abstract java.sql.CallableStatement java.sql.Connection.prepareCall(String) throws java.sql.SQLException
public abstract java.sql.CallableStatement java.sql.Connection.prepareCall(String,int,int) throws java.sql.SQLException
public abstract java.sql.CallableStatement java.sql.Connection.prepareCall(String,int,int,int) throws java.sql.SQLException
$
````
````
% #juggle                                                                \
    -j build/libs/juggle-1.0-SNAPSHOT.jar                               \
    -i com.angellane.juggle                                             \
    -i java.util                                                        \
    -r CartesianProduct
% juggle                                                                \
    -j build/libs/juggle-1.0-SNAPSHOT.jar                               \
    -i com.angellane.juggle                                             \
    -i java.util                                                        \
    CartesianProduct
public CartesianProduct<T>.<init>(List<E>[])
public static <T> CartesianProduct<T> CartesianProduct<T>.of(List<E>[])
%
````
BUG: should be fixed by #58: Add --conversions option
````
$ #juggle -p String -r java.io.InputStream
% juggle 'java.io.InputStream (String)'
$ juggle '? extends java.io.InputStream (String)'
public static java.io.InputStream ClassLoader.getSystemResourceAsStream(String)
public java.io.FileInputStream.<init>(String) throws java.io.FileNotFoundException
public java.io.StringBufferInputStream.<init>(String)
$
````
BUG: #96: Sort order of declaration syntax output not same as original
````
$# juggle -r java.io.OutputStream -p '' -a protected
% juggle 'protected java.io.OutputStream ()'
% juggle -a protected ' ? extends java.io.OutputStream ()'
public java.io.OutputStream.<init>()
public static java.io.OutputStream java.io.OutputStream.nullOutputStream()
public static final java.io.PrintStream System.err
public static final java.io.PrintStream System.out
public java.io.ByteArrayOutputStream.<init>()
public java.io.PipedOutputStream.<init>()
public sun.net.www.http.PosterOutputStream.<init>()
public sun.security.util.DerOutputStream.<init>()
protected java.io.ObjectOutputStream.<init>() throws java.io.IOException,SecurityException
$
````
````
$ #juggle -p double[],int,double,int -r void
$ juggle "void (double[],int,double,int)
$
````
BUG: #97: Permutation with new declaration syntax very slow
````
$ #juggle -p double[],int,double,int -r void -x
% juggle -x "void (double[],int,double,int)"
public static void java.util.Arrays.fill(double[],int,int,double)
$
````
