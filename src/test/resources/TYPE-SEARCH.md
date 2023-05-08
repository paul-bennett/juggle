# Type Search

This file contains samples of Juggle's new type search feature.

Which classes directly extend `InetAddress`?
````
$ juggle -i java.net class extends InetAddress
class java.net.Inet4Address
class java.net.Inet6Address
$
````

What about classes that implement `java.util.Collection`?
````
$ juggle class implements java.util.Collection
class java.util.AbstractCollection
class java.util.Collections$CheckedCollection
class java.util.Collections$SynchronizedCollection
class java.util.Collections$UnmodifiableCollection
class java.util.concurrent.ConcurrentHashMap$CollectionView
class java.util.concurrent.ConcurrentHashMap$ValuesView
$
````

## Searching by type flavour

There's only one record class in the JDK at the moment; it's in the `jdk.net` 
module.  However, the JDK leaks some other classes.  Here's what I'd like to
get to:
````
$ juggle -m jdk.net -i jdk.net -i java.nio.file.attribute record /Unix/
class jdk.net.UnixDomainPrincipal
$
````

In reality, Juggle presently says:
````
$ juggle -m jdk.net -i jdk.net,java.nio.file.attribute record
class sun.security.pkcs.SignerInfo$AlgorithmInfo
class sun.nio.ch.IOUtil$LinkedRunnable
class sun.nio.ch.IOUtil$Releaser
class apple.security.KeychainStore$LocalAttr
class jdk.net.UnixDomainPrincipal
$
````

## Classes

There should be no classes that are both `final` and `abstract`:
````
$ juggle abstract final class
$
````

Here's a bit of a "Christmas Tree" query (all the bits turned on):
````
$ juggle '@Deprecated package abstract static strictfp  class /asd/'
$
````

## Interfaces

Another Christmas tree
````
$ juggle "@Deprecated abstract public interface /Obs/"  
interface java.util.Observer
$
````

## Annotations

So many of these are almost identical that there's little to be tested.
However, not all annotations are themselves annotated `@Documented`, so
that's a little thing we can prove.
````
$ juggle '@java.lang.annotation.Documented @interface'       
interface java.lang.Deprecated
interface java.lang.FunctionalInterface
interface java.lang.SafeVarargs
interface java.lang.annotation.Documented
interface java.lang.annotation.Inherited
interface java.lang.annotation.Native
interface java.lang.annotation.Repeatable
interface java.lang.annotation.Retention
interface java.lang.annotation.Target
$
````

## Enums

Enumerations can implement interfaces.
````
$ juggle enum implements java.time.temporal.TemporalAccessor
class java.time.DayOfWeek
class java.time.Month
$
````

## Records

````
$ juggle -m jdk.net -i jdk.net -i java.nio.file.attribute record "(UserPrincipal, GroupPrincipal)"
class jdk.net.UnixDomainPrincipal
$
````
