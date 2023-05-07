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
% juggle -m jdk.net -i jdk.net,java.nio.file.attribute record
public record UnixDomainPrincipal(UserPrincipal user, GroupPrincipal group) extends Record
%
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