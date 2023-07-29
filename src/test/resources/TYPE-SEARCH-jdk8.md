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
# Type Search

This file contains samples of Juggle's new type search feature.

Which classes directly extend `InetAddress`?
```shell
$ juggle -i java.net class extends InetAddress
public final class Inet4Address extends InetAddress
public final class Inet6Address extends InetAddress
$
```

What about classes that implement `java.util.Collection`?
```shell
$ juggle -c none class implements java.util.Collection
public abstract class java.util.AbstractCollection<E> implements java.util.Collection<E>
$
```

## Searching by type flavour

(In JDK 11 there are no record classes, so these tests return nothing. But
they don't crash Juggle.)

There's only one record class in the JDK at the moment; it's in the `jdk.net` 
module.  However, the JDK leaks some other classes.  Here's what I'd like to
get to:
```shell
$ juggle -m jdk.net -i jdk.net -i java.nio.file.attribute record /Unix/
$
```

In reality, Juggle presently says:
```shell
$ juggle -m jdk.net -i jdk.net,java.nio.file.attribute record
$
```

## Classes

There should be no classes that are both `final` and `abstract`:
```shell
$ juggle abstract final class
$
```

Here's a bit of a "Christmas Tree" query (all the bits turned on):
```shell
$ juggle '@Deprecated package abstract static strictfp class /asd/'
$
```

## Interfaces

Another Christmas tree
```shell
$ juggle "@Deprecated abstract public interface /Obs/"  
$
```

## Annotations

So many of these are almost identical that there's little to be tested.
However, not all annotations are themselves annotated `@Documented`, so
that's a little thing we can prove.
```shell
$ juggle '@java.lang.annotation.Documented @interface'       
public abstract @interface Deprecated
public abstract @interface FunctionalInterface
public abstract @interface SafeVarargs
public abstract @interface com.sun.istack.internal.Interned
public abstract @interface com.sun.istack.internal.NotNull
public abstract @interface com.sun.istack.internal.Nullable
public abstract @interface com.sun.org.glassfish.external.arc.Taxonomy
public abstract @interface com.sun.org.glassfish.gmbal.AMXMetadata
public abstract @interface com.sun.org.glassfish.gmbal.Description
public abstract @interface com.sun.org.glassfish.gmbal.DescriptorFields
public abstract @interface com.sun.org.glassfish.gmbal.DescriptorKey
public abstract @interface com.sun.org.glassfish.gmbal.IncludeSubclass
public abstract @interface com.sun.org.glassfish.gmbal.InheritedAttribute
public abstract @interface com.sun.org.glassfish.gmbal.InheritedAttributes
public abstract @interface com.sun.org.glassfish.gmbal.ManagedAttribute
public abstract @interface com.sun.org.glassfish.gmbal.ManagedData
public abstract @interface com.sun.org.glassfish.gmbal.ManagedObject
public abstract @interface com.sun.org.glassfish.gmbal.ManagedOperation
public abstract @interface com.sun.org.glassfish.gmbal.NameValue
public abstract @interface com.sun.org.glassfish.gmbal.ParameterNames
public abstract @interface com.sun.xml.internal.ws.api.server.InstanceResolverAnnotation
public abstract @interface com.sun.xml.internal.ws.developer.MemberSubmissionAddressing
public abstract @interface com.sun.xml.internal.ws.developer.SchemaValidation
public abstract @interface com.sun.xml.internal.ws.developer.Serialization
public abstract @interface com.sun.xml.internal.ws.developer.StreamingAttachment
public abstract @interface com.sun.xml.internal.ws.developer.UsesJAXBContext
public abstract @interface java.beans.ConstructorProperties
public abstract @interface java.lang.annotation.Documented
public abstract @interface java.lang.annotation.Inherited
public abstract @interface java.lang.annotation.Native
public abstract @interface java.lang.annotation.Repeatable
public abstract @interface java.lang.annotation.Retention
public abstract @interface java.lang.annotation.Target
public abstract @interface javax.annotation.Generated
public abstract @interface javax.annotation.PostConstruct
public abstract @interface javax.annotation.PreDestroy
public abstract @interface javax.annotation.Resources
public abstract @interface javax.annotation.processing.SupportedAnnotationTypes
public abstract @interface javax.annotation.processing.SupportedOptions
public abstract @interface javax.annotation.processing.SupportedSourceVersion
public abstract @interface javax.management.DescriptorKey
public abstract @interface javax.management.MXBean
public abstract @interface javax.xml.ws.Action
public abstract @interface javax.xml.ws.BindingType
public abstract @interface javax.xml.ws.FaultAction
public abstract @interface javax.xml.ws.RequestWrapper
public abstract @interface javax.xml.ws.RespectBinding
public abstract @interface javax.xml.ws.ResponseWrapper
public abstract @interface javax.xml.ws.ServiceMode
public abstract @interface javax.xml.ws.WebEndpoint
public abstract @interface javax.xml.ws.WebFault
public abstract @interface javax.xml.ws.WebServiceClient
public abstract @interface javax.xml.ws.WebServiceProvider
public abstract @interface javax.xml.ws.WebServiceRef
public abstract @interface javax.xml.ws.WebServiceRefs
public abstract @interface javax.xml.ws.soap.Addressing
public abstract @interface javax.xml.ws.soap.MTOM
public abstract @interface javax.xml.ws.spi.WebServiceFeatureAnnotation
public abstract @interface jdk.Exported
$
```

## Enums

Enumerations can implement interfaces.
```shell
$ juggle enum implements java.time.temporal.TemporalAccessor
public final enum java.time.DayOfWeek implements java.time.temporal.TemporalAccessor, java.time.temporal.TemporalAdjuster
public final enum java.time.Month implements java.time.temporal.TemporalAccessor, java.time.temporal.TemporalAdjuster
public final enum java.time.chrono.HijrahEra implements java.time.chrono.Era
public final enum java.time.chrono.IsoEra implements java.time.chrono.Era
public final enum java.time.chrono.MinguoEra implements java.time.chrono.Era
public final enum java.time.chrono.ThaiBuddhistEra implements java.time.chrono.Era
$
```

## Records

```shell
$ juggle -m jdk.net -i jdk.net -i java.nio.file.attribute record "(UserPrincipal, GroupPrincipal)"
$
```
