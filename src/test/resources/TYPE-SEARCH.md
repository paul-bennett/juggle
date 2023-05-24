# Type Search

This file contains samples of Juggle's new type search feature.

Which classes directly extend `InetAddress`?
````
$ juggle -i java.net class extends InetAddress
public final class Inet4Address extends InetAddress
public final class Inet6Address extends InetAddress
$
````

What about classes that implement `java.util.Collection`?
````
$ juggle class implements java.util.Collection
public abstract class java.util.AbstractCollection<E> implements java.util.Collection<E>
$
````

## Searching by type flavour

There's only one record class in the JDK at the moment; it's in the `jdk.net` 
module.  However, the JDK leaks some other classes.  Here's what I'd like to
get to:
````
$ juggle -m jdk.net -i jdk.net -i java.nio.file.attribute record /Unix/
public final record UnixDomainPrincipal
$
````

In reality, Juggle presently says:
````
$ juggle -m jdk.net -i jdk.net,java.nio.file.attribute record
public final record jdk.net.UnixDomainPrincipal
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
$ juggle '@Deprecated package abstract static strictfp class /asd/'
$
````

## Interfaces

Another Christmas tree
````
$ juggle "@Deprecated abstract public interface /Obs/"  
public abstract interface java.util.Observer
$
````

## Annotations

So many of these are almost identical that there's little to be tested.
However, not all annotations are themselves annotated `@Documented`, so
that's a little thing we can prove.
````
$ juggle '@java.lang.annotation.Documented @interface'       
public abstract @interface Deprecated
public abstract @interface FunctionalInterface
public abstract @interface SafeVarargs
public abstract @interface java.lang.annotation.Documented
public abstract @interface java.lang.annotation.Inherited
public abstract @interface java.lang.annotation.Native
public abstract @interface java.lang.annotation.Repeatable
public abstract @interface java.lang.annotation.Retention
public abstract @interface java.lang.annotation.Target
$
````

## Enums

Enumerations can implement interfaces.
````
$ juggle enum implements java.time.temporal.TemporalAccessor
public final enum java.time.DayOfWeek implements java.time.temporal.TemporalAccessor, java.time.temporal.TemporalAdjuster
public final enum java.time.Month implements java.time.temporal.TemporalAccessor, java.time.temporal.TemporalAdjuster
$
````

## Records

````
$ juggle -m jdk.net -i jdk.net -i java.nio.file.attribute record "(UserPrincipal, GroupPrincipal)"
public final record UnixDomainPrincipal
$
````
