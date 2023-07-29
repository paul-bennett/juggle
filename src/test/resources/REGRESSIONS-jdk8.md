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

### [GitHub Issue #42](https://github.com/paul-bennett/juggle/issues/42): Warn on impossible queries

Juggle now warns on things which won't match anything.

#### Annotations
For example, non-annotation classes used in annotation contexts:
```shell
$ juggle '@String class'
*** Warning: `java.lang.String' is not an annotation interface; won't match anything
$
```

#### Throwables
Juggle also now checks that types in `throws` clauses are appropriate.

A class that appears directly must extend `Throwable`:
```shell
$ juggle throws String
*** Warning: `java.lang.String' is not a Throwable; won't match anything
$
```

The same restriction exists on classes in the upper bound of the thrown
type:
```shell
$ juggle throws ? extends String
*** Warning: `java.lang.String' is not a Throwable; won't match anything
$
```

But note that interfaces in the upper bound are unrestricted:
```shell
$ juggle throws ? extends java.util.Collection
$
```

Finally, things that appear in the lower bound must be throwable classes.
```shell
$ juggle throws ? super String
*** Warning: `java.lang.String' is not a Throwable; won't match anything
$
```

#### `void`

```shell
$ juggle 'void []'
*** Error: Can't have an array with element type `void'
$
```

```shell
$ juggle '(void)'
*** Error: Can't use `void' in a parameter or record component type
$
```

### [GitHub Issue #41](https://github.com/paul-bennett/juggle/issues/41): Warn if searching for features inaccessible to reflection

The `@Override` annotation has a `RetentionPolicy` of `SOURCE`, so trying to
use it in a Juggle query will always produce no results.  Following this fix,
Juggle is now explicit about this.  (See also `@SuppressWarnings` and other
annotation interfaces.)

```shell
$ juggle @Override class
*** Warning: `@interface java.lang.Override' has `SOURCE' retention policy; won't match anything (only `RUNTIME' policy works)
$
```

Conversely, `@Deprecated` has `RUNTIME` retention so no warning is emitted
in this case:
```shell
$ juggle '@Deprecated interface'
public abstract interface com.apple.eawt.ApplicationListener implements java.util.EventListener
public abstract interface com.sun.java.swing.Painter<T> implements javax.swing.Painter<T>
public abstract interface com.sun.net.ssl.HostnameVerifier
public abstract interface com.sun.net.ssl.KeyManager
public abstract interface com.sun.net.ssl.TrustManager
public abstract interface com.sun.net.ssl.X509KeyManager implements com.sun.net.ssl.KeyManager
public abstract interface com.sun.net.ssl.X509TrustManager implements com.sun.net.ssl.TrustManager
public abstract interface com.sun.org.apache.xml.internal.security.utils.ElementChecker
public abstract interface com.sun.org.omg.CORBA.portable.ValueHelper implements org.omg.CORBA.portable.BoxedValueHelper
public abstract interface java.rmi.registry.RegistryHandler
public abstract interface java.rmi.server.LoaderHandler
public abstract interface java.rmi.server.RemoteCall
public abstract interface java.rmi.server.ServerRef implements java.rmi.server.RemoteRef
public abstract interface java.rmi.server.Skeleton
public abstract interface java.security.Certificate
public abstract interface org.omg.CORBA.DynAny implements org.omg.CORBA.Object
public abstract interface org.omg.CORBA.DynArray implements org.omg.CORBA.Object, org.omg.CORBA.DynAny
public abstract interface org.omg.CORBA.DynEnum implements org.omg.CORBA.Object, org.omg.CORBA.DynAny
public abstract interface org.omg.CORBA.DynFixed implements org.omg.CORBA.Object, org.omg.CORBA.DynAny
public abstract interface org.omg.CORBA.DynSequence implements org.omg.CORBA.Object, org.omg.CORBA.DynAny
public abstract interface org.omg.CORBA.DynStruct implements org.omg.CORBA.Object, org.omg.CORBA.DynAny
public abstract interface org.omg.CORBA.DynUnion implements org.omg.CORBA.Object, org.omg.CORBA.DynAny
public abstract interface org.omg.CORBA.DynValue implements org.omg.CORBA.Object, org.omg.CORBA.DynAny
public abstract interface sun.misc.VMNotification
public abstract static interface sun.net.idn.UCharacterEnums.ECharacterCategory
public abstract static interface sun.net.idn.UCharacterEnums.ECharacterDirection
public abstract interface sun.net.www.protocol.http.HttpAuthenticator
public abstract interface sun.text.normalizer.SymbolTable
$
```

### [GitHub Issue #102](https://github.com/paul-bennett/juggle/issues/102): Add receiver parameter support

It's now possible to list just non-static members by specifying the name
of the first parameter as `this`:
```shell
$ juggle -c none '(java.util.function.Function this,...)'
public <V> java.util.function.Function<T,R> java.util.function.Function<T,R>.andThen(java.util.function.Function<T,R>)
public abstract R java.util.function.Function<T,R>.apply(T)
public <V> java.util.function.Function<T,R> java.util.function.Function<T,R>.compose(java.util.function.Function<T,R>)
$
```

(Prior to this change, the above query would've listed all the members of 
`Function`, including the `static` members, mainly because most JDK builds
don't include parameter metadata in which case Juggle ignores all parameter
names.)


### [GitHub Issue #15](https://github.com/paul-bennett/juggle/issues/15): Handle module path

We can now load classes from modules, searched on the specified module path.
```shell
$ juggle -p build/libs -m juggle.testLib /someFunction/
public static void com.angellane.juggle.testinput.lib.Lib.someFunction(int,String)
$
```


### [GitHub Issue #43](https://github.com/paul-bennett/juggle/issues/43): Search by parameter name/annotation

This fix allows methods (and record types) to be matched by parameter
(component) metadata such as per-parameter annotations, modifiers and name.

However, annotations are restricted to those with the `RUNTIME` retention 
policy, and modifiers and names can only be checked against classes which
were compiled with `javac -parameters`.  In particular, I've yet to find a
JDK which was built in this way.

Consequently most of the tests here use the following method from the
`com.angellane.juggle.testinput.lib` package:
```java
class Lib {
    //...
    public static void someFunction(
            @SourceAnnotation @ClassAnnotation @RuntimeAnnotation int foo,
            final String bar) {}
}
```

First let's check that we can locate the method with a wildcard query:
```shell
$ juggle -cp build/libs/testLib.jar -i com.angellane.juggle.testinput.lib '? someFunction(,)'
public static void Lib.someFunction(int,String)
$
```

Let's try annotations first.  This should be an exact match:
```shell
$ juggle -cp build/libs/testLib.jar -i com.angellane.juggle.testinput.lib '? someFunction(@RuntimeAnnotation,)'
public static void Lib.someFunction(int,String)
$
```

But this one should fail:
```shell
$ juggle -cp build/libs/testLib.jar -i com.angellane.juggle.testinput.lib '? someFunction(,@RuntimeAnnotation)'
$
```

Turning to modifiers, this should match:
```shell
$ juggle -cp build/libs/testLib.jar -i com.angellane.juggle.testinput.lib '? someFunction(,final)'
public static void Lib.someFunction(int,String)
$
```

But this should fail:
```shell
$ juggle -cp build/libs/testLib.jar -i com.angellane.juggle.testinput.lib '? someFunction(final,)'
$
```

And for names, here's a success:
```shell
$ juggle -cp build/libs/testLib.jar -i com.angellane.juggle.testinput.lib '? someFunction(? foo,)'
public static void Lib.someFunction(int,String)
$
```

And a failure:
```shell
$ juggle -cp build/libs/testLib.jar -i com.angellane.juggle.testinput.lib '? someFunction(? bar,)'
$
```

Putting it all together:
```shell
% juggle -cp build/libs/testLib.jar -i com.angellane.juggle.testinput.lib '? someFunction(@RuntimeAnnotation ? foo, final ? bar)'
public static void Lib.someFunction(int,String)
%
```

(Annoyingly I've had to disable this last test. It works, but only if the
previous two tests that mention `@RuntimeAnnotation` are disabled.  This
isn't a surprise; it's a recurrence of [GitHub issue #39: _Classes loaded by
inconsistent loaders (visible only in test)_](https://github.com/paul-bennett/juggle/issues/39).)


### [GitHub Issue #115](https://github.com/paul-bennett/juggle/issues/115): Allow qnames in type clauses

Prior to this fix, it wasn't possible to use qualified names to look up
classes or interfaces, so including a package name gave a syntax error.

Instead, the package had to be mentioned in an `--import` option:
```shell
$ juggle --import java.net class InetAddress
public class InetAddress implements java.io.Serializable
$
```

After this fix we can use the more natural query:
```shell
$ juggle class java.net.InetAddress
public class java.net.InetAddress implements java.io.Serializable
$
```

The same issue used to make it impossible to query inner classes,
but after the fix we can now do so:
```shell
$ juggle interface java.util.Map.Entry
public abstract static interface java.util.Map.Entry<K,V>
$
```

A partial name can be specified using a regex:
```shell
$ juggle interface /Map.Entry/
public abstract static interface java.util.Map.Entry<K,V>
$
```

### [GitHub Issue #87](https://github.com/paul-bennett/juggle/issues/87): Add ability to list supertypes

What are the superclasses of `StringBuilder`?
```shell
$ juggle class super StringBuilder
public final class StringBuilder extends AbstractStringBuilder implements java.io.Serializable, CharSequence
public class Object
$
```

That doesn't look right! Where's the `AbstractStringBuilder` class?

Turns out it's not `public`, so Juggle filtered it out.  Let's try again: 
```shell
$ juggle private class super StringBuilder
public final class StringBuilder extends AbstractStringBuilder implements java.io.Serializable, CharSequence
public class Object
abstract class AbstractStringBuilder implements Appendable, CharSequence
$
```

That's better!

We can also show all the interfaces that `StringBuilder` implements:
```shell
$ juggle interface super StringBuilder
public abstract interface Appendable
public abstract interface CharSequence
public abstract interface java.io.Serializable
$
```

As you might expect we can limit the output to direct superinterfaces only by
turning off conversions:
```shell
$ juggle -c none interface super StringBuilder
public abstract interface CharSequence
public abstract interface java.io.Serializable
$
```
```shell
$ juggle -c none private class super StringBuilder
abstract class AbstractStringBuilder implements Appendable, CharSequence
$
```

### [GitHub Issue #84](https://github.com/paul-bennett/juggle/issues/84): List all classes that directly or indirectly implement an interface

We can now list all direct and indirect subclasses:
```shell
$ juggle -i java.lang.reflect class extends AccessibleObject
public abstract class Executable extends AccessibleObject implements Member, GenericDeclaration
public final class Field extends AccessibleObject implements Member
public final class Constructor<T> extends Executable
public final class Method extends Executable
$
```

As expected, omitting `--conversions` is equivalent to `--conversions=auto`:
```shell
$ juggle --conversions=auto -i java.lang.reflect class extends AccessibleObject
public abstract class Executable extends AccessibleObject implements Member, GenericDeclaration
public final class Field extends AccessibleObject implements Member
public final class Constructor<T> extends Executable
public final class Method extends Executable
$
```

And because we've not used any wildcards in the query, that's the same as
`--conversions=all`:
```shell
$ juggle --conversions=all -i java.lang.reflect class extends AccessibleObject
public abstract class Executable extends AccessibleObject implements Member, GenericDeclaration
public final class Field extends AccessibleObject implements Member
public final class Constructor<T> extends Executable
public final class Method extends Executable
$
```

Specifying `--conversions=none` just matches classes that literally extend
`AccessibleObject`:
```shell
$ juggle --conversions=none -i java.lang.reflect class extends AccessibleObject
public abstract class Executable extends AccessibleObject implements Member, GenericDeclaration
public final class Field extends AccessibleObject implements Member
$
```

The original clumsy syntax no longer just shows indirect subclasses, but I
don't consider that to be a great loss:
```shell
$ juggle -i java.lang.reflect class extends \? extends AccessibleObject
public abstract class Executable extends AccessibleObject implements Member, GenericDeclaration
public final class Field extends AccessibleObject implements Member
public final class Constructor<T> extends Executable
public final class Method extends Executable
$
```
Because the query has a bounded wildcard, that's the same as
`--conversions=none`:
```shell
$ juggle -c none -i java.lang.reflect class extends \? extends AccessibleObject
public abstract class Executable extends AccessibleObject implements Member, GenericDeclaration
public final class Field extends AccessibleObject implements Member
public final class Constructor<T> extends Executable
public final class Method extends Executable
$
```
```shell
$ juggle -c all -i java.lang.reflect class extends AccessibleObject
public abstract class Executable extends AccessibleObject implements Member, GenericDeclaration
public final class Field extends AccessibleObject implements Member
public final class Constructor<T> extends Executable
public final class Method extends Executable
$
```

Here's a similar sequence with interfaces.  First, the direct descendents
of `Collection`:
```shell
$ juggle -c none -i java.util interface extends Collection
public abstract interface List<E> implements Collection<E>
public abstract interface Queue<E> implements Collection<E>
public abstract interface Set<E> implements Collection<E>
public abstract interface java.beans.beancontext.BeanContext implements java.beans.beancontext.BeanContextChild, Collection<E>, java.beans.DesignMode, java.beans.Visibility
$
```

Now all indirect descendents too:
```shell
$ juggle -i java.util interface extends Collection
public abstract interface List<E> implements Collection<E>
public abstract interface Queue<E> implements Collection<E>
public abstract interface Deque<E> implements Queue<E>
public abstract interface Set<E> implements Collection<E>
public abstract interface SortedSet<E> implements Set<E>
public abstract interface NavigableSet<E> implements SortedSet<E>
public abstract interface com.sun.corba.se.impl.orbutil.graph.Graph implements Set<E>
public abstract interface com.sun.corba.se.spi.ior.IOR implements List<E>, com.sun.corba.se.spi.ior.Writeable, com.sun.corba.se.spi.ior.MakeImmutable
public abstract interface com.sun.corba.se.spi.ior.IORTemplate implements List<E>, com.sun.corba.se.spi.ior.IORFactory, com.sun.corba.se.spi.ior.MakeImmutable
public abstract interface com.sun.corba.se.spi.ior.IORTemplateList implements List<E>, com.sun.corba.se.spi.ior.IORFactory, com.sun.corba.se.spi.ior.MakeImmutable
public abstract interface com.sun.corba.se.spi.ior.TaggedProfileTemplate implements List<E>, com.sun.corba.se.spi.ior.Identifiable, com.sun.corba.se.spi.ior.WriteContents, com.sun.corba.se.spi.ior.MakeImmutable
public abstract interface com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate implements com.sun.corba.se.spi.ior.TaggedProfileTemplate
public abstract interface com.sun.org.apache.xerces.internal.xs.LSInputList implements List<E>
public abstract interface com.sun.org.apache.xerces.internal.xs.ShortList implements List<E>
public abstract interface com.sun.org.apache.xerces.internal.xs.StringList implements List<E>
public abstract interface com.sun.org.apache.xerces.internal.xs.XSNamespaceItemList implements List<E>
public abstract interface com.sun.org.apache.xerces.internal.xs.XSObjectList implements List<E>
public abstract interface com.sun.org.apache.xerces.internal.xs.datatypes.ByteList implements List<E>
public abstract interface com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList implements List<E>
public abstract interface java.beans.beancontext.BeanContext implements java.beans.beancontext.BeanContextChild, Collection<E>, java.beans.DesignMode, java.beans.Visibility
public abstract interface java.beans.beancontext.BeanContextServices implements java.beans.beancontext.BeanContext, java.beans.beancontext.BeanContextServicesListener
public abstract interface java.util.concurrent.BlockingQueue<E> implements Queue<E>
public abstract interface java.util.concurrent.BlockingDeque<E> implements java.util.concurrent.BlockingQueue<E>, Deque<E>
public abstract interface java.util.concurrent.TransferQueue<E> implements java.util.concurrent.BlockingQueue<E>
$
```

Finally, how about classes that implement interfaces?
Direct implementation of an interface:
```shell
$ juggle -c none -i java.util class implements Collection
public abstract class AbstractCollection<E> implements Collection<E>
$
```

And indirect implementation via class:
```shell
$ juggle -i java.util class implements List
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E>
public abstract class AbstractSequentialList<E> extends AbstractList<E>
public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable
public class LinkedList<E> extends AbstractSequentialList<E> implements List<E>, Deque<E>, Cloneable, java.io.Serializable
public class Vector<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable
public class Stack<E> extends Vector<E>
public class com.sun.corba.se.impl.ior.FreezableList extends AbstractList<E>
public class com.sun.corba.se.spi.ior.IdentifiableContainerBase extends com.sun.corba.se.impl.ior.FreezableList
public class com.sun.corba.se.impl.ior.IORImpl extends com.sun.corba.se.spi.ior.IdentifiableContainerBase implements com.sun.corba.se.spi.ior.IOR
public class com.sun.corba.se.impl.ior.IORTemplateImpl extends com.sun.corba.se.spi.ior.IdentifiableContainerBase implements com.sun.corba.se.spi.ior.IORTemplate
public class com.sun.corba.se.impl.ior.IORTemplateListImpl extends com.sun.corba.se.impl.ior.FreezableList implements com.sun.corba.se.spi.ior.IORTemplateList
public abstract class com.sun.corba.se.spi.ior.TaggedProfileTemplateBase extends com.sun.corba.se.spi.ior.IdentifiableContainerBase implements com.sun.corba.se.spi.ior.TaggedProfileTemplate
public class com.sun.corba.se.impl.ior.iiop.IIOPProfileTemplateImpl extends com.sun.corba.se.spi.ior.TaggedProfileTemplateBase implements com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate
public final class com.sun.istack.internal.FinalArrayList<T> extends ArrayList<E>
public static final class com.sun.java.util.jar.pack.ConstantPool.Index extends AbstractList<E>
public class com.sun.jmx.remote.internal.ArrayQueue<T> extends AbstractList<E>
public class com.sun.jmx.snmp.SnmpVarBindList extends Vector<E>
public final class com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringStack extends Stack<E>
public class com.sun.org.apache.xerces.internal.impl.dv.util.ByteListImpl extends AbstractList<E> implements com.sun.org.apache.xerces.internal.xs.datatypes.ByteList
public final class com.sun.org.apache.xerces.internal.impl.xs.XSModelImpl extends AbstractList<E> implements com.sun.org.apache.xerces.internal.xs.XSModel, com.sun.org.apache.xerces.internal.xs.XSNamespaceItemList
public final class com.sun.org.apache.xerces.internal.impl.xs.util.LSInputListImpl extends AbstractList<E> implements com.sun.org.apache.xerces.internal.xs.LSInputList
public final class com.sun.org.apache.xerces.internal.impl.xs.util.ObjectListImpl extends AbstractList<E> implements com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
public final class com.sun.org.apache.xerces.internal.impl.xs.util.ShortListImpl extends AbstractList<E> implements com.sun.org.apache.xerces.internal.xs.ShortList
public final class com.sun.org.apache.xerces.internal.impl.xs.util.StringListImpl extends AbstractList<E> implements com.sun.org.apache.xerces.internal.xs.StringList
public class com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl extends AbstractList<E> implements com.sun.org.apache.xerces.internal.xs.XSObjectList
public static final class com.sun.xml.internal.bind.v2.runtime.reflect.Lister.Pack<ItemT> extends ArrayList<E>
public final class com.sun.xml.internal.bind.v2.util.CollisionCheckStack<E> extends AbstractList<E>
public final class com.sun.xml.internal.messaging.saaj.util.FinalArrayList extends ArrayList<E>
public class com.sun.xml.internal.ws.api.message.HeaderList extends ArrayList<E> implements com.sun.xml.internal.ws.api.message.MessageHeaders
public abstract class com.sun.xml.internal.ws.transport.http.HttpAdapterList<T> extends AbstractList<E> implements com.sun.xml.internal.ws.transport.http.DeploymentDescriptorParser.AdapterFactory<A>
public class com.sun.xml.internal.ws.transport.http.server.ServerAdapterList extends com.sun.xml.internal.ws.transport.http.HttpAdapterList<T>
public class java.util.concurrent.CopyOnWriteArrayList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable
public class javax.management.AttributeList extends ArrayList<E>
public class javax.management.relation.RoleList extends ArrayList<E>
public class javax.management.relation.RoleUnresolvedList extends ArrayList<E>
public class sun.awt.util.IdentityArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess
public class sun.awt.util.IdentityLinkedList<E> extends AbstractSequentialList<E> implements List<E>, Deque<E>
public class sun.security.x509.AttributeNameEnumeration extends Vector<E>
public class sun.swing.BakedArrayList extends ArrayList<E>
$
```


### [GitHub Issue #86](https://github.com/paul-bennett/juggle/issues/86): Add support for sealed and non-sealed modifiers

JDK 11 doesn't have `sealed` or `non-sealed` classes:
```shell
$ juggle sealed interface
$
```

```shell
$ juggle non-sealed class
$
```

More recent JDKs have many more; these examples will need updating as the
minimum requirements of Juggle increased.


### [GitHub Issue #112](https://github.com/paul-bennett/juggle/issues/112): Fail on parse error

This query uses a symbol that doesn't pass the lexer:
```shell
$ juggle :
line 1:0 token recognition error at: ':'
*** Error: Couldn't parse query
$
```

This query passes the lexer but fails the parser:
```shell
$ juggle \)
*** Error: Couldn't parse query at 1:0
)
^

$
```

And again:
```shell
$ juggle interface interface
*** Error: Couldn't parse query at 1:10
interface interface
          ^^^^^^^^^

$
```

Let's try splitting across a few lines:
```shell
$ juggle "public            \
       class                \
this should fail            \
on the second word above"
*** Error: Couldn't parse query at 3:5
public            
       class                
this should fail            
     ^^^^^^
on the second word above

$
```
(Unfortunately the parser in `TestSamples` doesn't propagate the newlines
down into arguments.)

This exercises a different branch of the error handling:
```shell
$ juggle 'int (int'
line 1:8 no viable alternative at input 'int'
*** Error: Couldn't parse query at 1:5
int (int
     ^^^

$
```

### [GitHub Issue #58](https://github.com/paul-bennett/juggle/issues/58): Add `--conversions` option

The new `--conversions` option lets us dictate when Juggle automatically
applies relevant boxing and reference conversions for us.

Here are four methods that we'll play with. We can find them using explicit wildcards.
(This query worked before the fix.)
```shell
$ juggle '? extends CharSequence (? super String,int,int)'
public CharSequence String.subSequence(int,int)
public abstract CharSequence CharSequence.subSequence(int,int)
public String String.substring(int,int)
public static java.nio.CharBuffer java.nio.CharBuffer.wrap(CharSequence,int,int)
$
```

If we don't specify any wildcards in the query, Juggle infers them for us:
```shell
$ juggle 'CharSequence (String,int,int)'
public CharSequence String.subSequence(int,int)
public abstract CharSequence CharSequence.subSequence(int,int)
public String String.substring(int,int)
public static java.nio.CharBuffer java.nio.CharBuffer.wrap(CharSequence,int,int)
public static String com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary.substringF(String,double,double)
public static String com.sun.xml.internal.bind.marshaller.Messages.format(String,Object,Object)
public static String com.sun.xml.internal.bind.unmarshaller.Messages.format(String,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0024_SPI_FAIL_SERVICE_URL_LINE_MSG(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0029_SERVICE_PORT_OPERATION_PARAM_MUST_NOT_BE_NULL(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0061_METHOD_INVOCATION_FAILED(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0071_ERROR_MULTIPLE_ASSERTION_CREATORS_FOR_NAMESPACE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0074_CANNOT_CREATE_ASSERTION_BAD_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0089_EXPECTED_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0091_END_ELEMENT_NO_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0092_CHARACTER_DATA_UNEXPECTED(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.FAILED_TO_PARSE_WITH_MEX(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_INTEGER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_LONG(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ProviderApiMessages.NOTFOUND_PORT_IN_WSDL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.NOT_KNOW_HTTP_CONTEXT_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_SOAPBINDING_CONFLICT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_MISSING_ATTRIBUTE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_MULTIPLEBINDING(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.UNSUPPORTED_OPERATION(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.UtilMessages.UTIL_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.WsdlmodelMessages.WSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLREADER_UNEXPECTED_STATE_MESSAGE(Object,Object,Object)
$
```

This is the same as asking for `all` conversions:
```shell
$ juggle --conversions=all 'CharSequence (String,int,int)'
public CharSequence String.subSequence(int,int)
public abstract CharSequence CharSequence.subSequence(int,int)
public String String.substring(int,int)
public static java.nio.CharBuffer java.nio.CharBuffer.wrap(CharSequence,int,int)
public static String com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary.substringF(String,double,double)
public static String com.sun.xml.internal.bind.marshaller.Messages.format(String,Object,Object)
public static String com.sun.xml.internal.bind.unmarshaller.Messages.format(String,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0024_SPI_FAIL_SERVICE_URL_LINE_MSG(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0029_SERVICE_PORT_OPERATION_PARAM_MUST_NOT_BE_NULL(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0061_METHOD_INVOCATION_FAILED(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0071_ERROR_MULTIPLE_ASSERTION_CREATORS_FOR_NAMESPACE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0074_CANNOT_CREATE_ASSERTION_BAD_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0089_EXPECTED_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0091_END_ELEMENT_NO_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0092_CHARACTER_DATA_UNEXPECTED(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.FAILED_TO_PARSE_WITH_MEX(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_INTEGER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_LONG(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ProviderApiMessages.NOTFOUND_PORT_IN_WSDL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.NOT_KNOW_HTTP_CONTEXT_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_SOAPBINDING_CONFLICT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_MISSING_ATTRIBUTE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_MULTIPLEBINDING(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.UNSUPPORTED_OPERATION(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.UtilMessages.UTIL_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.WsdlmodelMessages.WSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLREADER_UNEXPECTED_STATE_MESSAGE(Object,Object,Object)
$
```

And in this case, because we don't have any explicit wildcards in the query,
it's equivalent to `auto` conversions:
```shell
$ juggle --conversions=auto 'CharSequence (String,int,int)'
public CharSequence String.subSequence(int,int)
public abstract CharSequence CharSequence.subSequence(int,int)
public String String.substring(int,int)
public static java.nio.CharBuffer java.nio.CharBuffer.wrap(CharSequence,int,int)
public static String com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary.substringF(String,double,double)
public static String com.sun.xml.internal.bind.marshaller.Messages.format(String,Object,Object)
public static String com.sun.xml.internal.bind.unmarshaller.Messages.format(String,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0024_SPI_FAIL_SERVICE_URL_LINE_MSG(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0029_SERVICE_PORT_OPERATION_PARAM_MUST_NOT_BE_NULL(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0061_METHOD_INVOCATION_FAILED(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0071_ERROR_MULTIPLE_ASSERTION_CREATORS_FOR_NAMESPACE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0074_CANNOT_CREATE_ASSERTION_BAD_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0089_EXPECTED_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0091_END_ELEMENT_NO_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0092_CHARACTER_DATA_UNEXPECTED(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.FAILED_TO_PARSE_WITH_MEX(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_INTEGER(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.INVALID_PROPERTY_VALUE_LONG(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ProviderApiMessages.NOTFOUND_PORT_IN_WSDL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.NOT_KNOW_HTTP_CONTEXT_TYPE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIMEMODELER_INVALIDANNOTATION_ON_IMPL(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIMEMODELER_INVALID_SOAPBINDING_ON_METHOD(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_ONEWAY_OPERATION_NO_CHECKED_EXCEPTIONS(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ModelerMessages.RUNTIME_MODELER_SOAPBINDING_CONFLICT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_MISSING_ATTRIBUTE(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ServerMessages.RUNTIME_PARSER_WSDL_MULTIPLEBINDING(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.UNSUPPORTED_OPERATION(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.UtilMessages.UTIL_PARSER_WRONG_ELEMENT(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.WsdlmodelMessages.WSDL_PORTADDRESS_EPRADDRESS_NOT_MATCH(Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.StreamingMessages.XMLREADER_UNEXPECTED_STATE_MESSAGE(Object,Object,Object)
$
```

If we didn't want Juggle to apply any conversions we can ask for `none`:
(This is what Juggle used to respond with prior to fixing #58.)
```shell
$ juggle --conversions=none 'CharSequence (String,int,int)'
public CharSequence String.subSequence(int,int)
$
```

Something similar happens with boxing conversions.  If we apply `auto`
conversions and use no wildcards, that's equivalent to `all`:
```shell
$ juggle 'Integer(String,int)'
public static Integer Integer.getInteger(String,int)
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public static Integer sun.net.NetProperties.getInteger(String,int)
public int String.codePointAt(int)
public int String.codePointBefore(int)
public static Integer Integer.getInteger(String,Integer)
public int String.indexOf(int)
public int String.lastIndexOf(int)
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
public static int sun.text.normalizer.UTF16.charAt(String,int)
public static int sun.text.normalizer.Utility.skipWhitespace(String,int)
public static int Character.codePointAt(CharSequence,int)
public static int Character.codePointBefore(CharSequence,int)
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static int jdk.xml.internal.JdkXmlUtils.getValue(Object,int)
public static int sun.misc.PerformanceLogger.setTime(String,long)
public static int sun.swing.SwingUtilities2.getUIDefaultsInt(Object,int)
public volatile int String.compareTo(Object)
public abstract int Comparable<T>.compareTo(T)
$
```
```shell
$ juggle -c=auto 'Integer(String,int)'
public static Integer Integer.getInteger(String,int)
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public static Integer sun.net.NetProperties.getInteger(String,int)
public int String.codePointAt(int)
public int String.codePointBefore(int)
public static Integer Integer.getInteger(String,Integer)
public int String.indexOf(int)
public int String.lastIndexOf(int)
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
public static int sun.text.normalizer.UTF16.charAt(String,int)
public static int sun.text.normalizer.Utility.skipWhitespace(String,int)
public static int Character.codePointAt(CharSequence,int)
public static int Character.codePointBefore(CharSequence,int)
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static int jdk.xml.internal.JdkXmlUtils.getValue(Object,int)
public static int sun.misc.PerformanceLogger.setTime(String,long)
public static int sun.swing.SwingUtilities2.getUIDefaultsInt(Object,int)
public volatile int String.compareTo(Object)
public abstract int Comparable<T>.compareTo(T)
$
```
```shell
$ juggle -c=all 'Integer(String,int)'
public static Integer Integer.getInteger(String,int)
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public static Integer sun.net.NetProperties.getInteger(String,int)
public int String.codePointAt(int)
public int String.codePointBefore(int)
public static Integer Integer.getInteger(String,Integer)
public int String.indexOf(int)
public int String.lastIndexOf(int)
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
public static int sun.text.normalizer.UTF16.charAt(String,int)
public static int sun.text.normalizer.Utility.skipWhitespace(String,int)
public static int Character.codePointAt(CharSequence,int)
public static int Character.codePointBefore(CharSequence,int)
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static int jdk.xml.internal.JdkXmlUtils.getValue(Object,int)
public static int sun.misc.PerformanceLogger.setTime(String,long)
public static int sun.swing.SwingUtilities2.getUIDefaultsInt(Object,int)
public volatile int String.compareTo(Object)
public abstract int Comparable<T>.compareTo(T)
$
```

If we tell Juggle not to apply any conversions, boxing is disabled as well.
(This is equivalent to pre-fix Juggle.)
```shell
$ juggle -c=none 'Integer(String,int)'
public static Integer Integer.getInteger(String,int)
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public static Integer sun.net.NetProperties.getInteger(String,int)
$
```

Further tests I've used during development of this feature:
```shell
$ juggle -c=none 'java.lang.reflect.Executable(?)'
public java.lang.reflect.Executable java.lang.reflect.Parameter.getDeclaringExecutable()
$
```

```shell
$ juggle 'java.lang.reflect.Executable(?)'
public java.lang.reflect.Executable java.lang.reflect.Parameter.getDeclaringExecutable()
public java.lang.reflect.Constructor<T> Class<T>.getEnclosingConstructor() throws SecurityException
public java.lang.reflect.Method Class<T>.getEnclosingMethod() throws SecurityException
public abstract java.lang.reflect.Method com.oracle.webservices.internal.api.databinding.JavaCallInfo.getMethod()
public static java.lang.reflect.Method com.sun.beans.finder.MethodFinder.findAccessibleMethod(java.lang.reflect.Method) throws NoSuchMethodException
public java.lang.reflect.Method com.sun.corba.se.impl.presentation.rmi.DynamicMethodMarshallerImpl.getMethod()
public java.lang.reflect.Method com.sun.corba.se.impl.presentation.rmi.IDLNameTranslatorImpl.IDLMethodInfo.method
public abstract java.lang.reflect.Method com.sun.corba.se.spi.presentation.rmi.DynamicMethodMarshaller.getMethod()
public java.lang.reflect.Method com.sun.xml.internal.bind.v2.model.impl.ClassInfoImpl<T,C,F,M>.getFactoryMethod()
public java.lang.reflect.Method com.sun.xml.internal.bind.v2.model.impl.RuntimeClassInfoImpl.getFactoryMethod()
public abstract java.lang.reflect.Method com.sun.xml.internal.bind.v2.model.runtime.RuntimeClassInfo.getFactoryMethod()
public final java.lang.reflect.Method com.sun.xml.internal.bind.v2.runtime.reflect.Accessor.GetterSetterReflection<BeanT,ValueT>.getter
public final java.lang.reflect.Method com.sun.xml.internal.bind.v2.runtime.reflect.Accessor.GetterSetterReflection<BeanT,ValueT>.setter
public abstract java.lang.reflect.Method com.sun.xml.internal.ws.api.databinding.ClientCallBridge.getMethod()
public java.lang.reflect.Method com.sun.xml.internal.ws.api.databinding.JavaCallInfo.getMethod()
public abstract java.lang.reflect.Method com.sun.xml.internal.ws.api.model.JavaMethod.getMethod()
public abstract java.lang.reflect.Method com.sun.xml.internal.ws.api.model.JavaMethod.getSEIMethod()
public java.lang.reflect.Method com.sun.xml.internal.ws.client.sei.StubHandler.getMethod()
public java.lang.reflect.Method com.sun.xml.internal.ws.model.JavaMethodImpl.getMethod()
public java.lang.reflect.Method com.sun.xml.internal.ws.model.JavaMethodImpl.getSEIMethod()
public java.lang.reflect.Method com.sun.xml.internal.ws.server.sei.TieHandler.getMethod()
public java.lang.reflect.Method com.sun.xml.internal.ws.spi.db.MethodGetter.getMethod()
public java.lang.reflect.Method com.sun.xml.internal.ws.spi.db.MethodSetter.getMethod()
public synchronized java.lang.reflect.Method java.beans.EventSetDescriptor.getAddListenerMethod()
public synchronized java.lang.reflect.Method java.beans.EventSetDescriptor.getGetListenerMethod()
public synchronized java.lang.reflect.Method java.beans.IndexedPropertyDescriptor.getIndexedReadMethod()
public synchronized java.lang.reflect.Method java.beans.IndexedPropertyDescriptor.getIndexedWriteMethod()
public synchronized java.lang.reflect.Method java.beans.MethodDescriptor.getMethod()
public synchronized java.lang.reflect.Method java.beans.PropertyDescriptor.getReadMethod()
public synchronized java.lang.reflect.Method java.beans.EventSetDescriptor.getRemoveListenerMethod()
public synchronized java.lang.reflect.Method java.beans.PropertyDescriptor.getWriteMethod()
public java.lang.reflect.Method java.lang.annotation.AnnotationTypeMismatchException.element()
public java.lang.reflect.Method javax.swing.JOptionPane.ModalPrivilegedAction.run()
$
```

Conversions also apply to exceptions. We used to have to include upper
bounds explicitly:
```shell
$ juggle '? encode throws ? extends java.io.IOException'
public static final String com.sun.jndi.toolkit.url.UrlUtil.encode(String,String) throws java.io.UnsupportedEncodingException
public static String com.sun.org.apache.bcel.internal.classfile.Utility.encode(byte[],boolean) throws java.io.IOException
public abstract com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.api.databinding.Databinding.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public abstract com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.api.pipe.Codec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.db.DatabindingImpl.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.MimeCodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.MtomCodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.SOAPBindingCodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public volatile com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.SwACodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.XMLHTTPBindingCodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public static String java.net.URLEncoder.encode(String,String) throws java.io.UnsupportedEncodingException
public final java.nio.ByteBuffer java.nio.charset.CharsetEncoder.encode(java.nio.CharBuffer) throws java.nio.charset.CharacterCodingException
public abstract void java.security.cert.Extension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.misc.CharacterEncoder.encode(byte[],java.io.OutputStream) throws java.io.IOException
public void sun.misc.CharacterEncoder.encode(java.io.InputStream,java.io.OutputStream) throws java.io.IOException
public void sun.misc.CharacterEncoder.encode(java.nio.ByteBuffer,java.io.OutputStream) throws java.io.IOException
public int sun.security.jgss.GSSHeader.encode(java.io.OutputStream) throws java.io.IOException
public abstract byte[] sun.security.jgss.krb5.InitialToken.encode() throws java.io.IOException
public abstract void sun.security.jgss.krb5.MessageToken_v2.encode(java.io.OutputStream) throws java.io.IOException
public byte[] sun.security.jgss.krb5.MicToken_v2.encode() throws java.io.IOException
public byte[] sun.security.jgss.krb5.WrapToken_v2.encode() throws java.io.IOException
public final byte[] sun.security.jgss.krb5.AcceptSecContextToken.encode() throws java.io.IOException
public final byte[] sun.security.jgss.krb5.InitSecContextToken.encode() throws java.io.IOException
public final void sun.security.jgss.krb5.MessageToken.MessageTokenHeader.encode(java.io.OutputStream) throws java.io.IOException
public final void sun.security.jgss.krb5.MessageToken_v2.MessageTokenHeader.encode(java.io.OutputStream) throws java.io.IOException
public int sun.security.jgss.krb5.MicToken_v2.encode(byte[],int) throws java.io.IOException
public int sun.security.jgss.krb5.WrapToken_v2.encode(byte[],int) throws java.io.IOException
public void sun.security.jgss.krb5.MicToken_v2.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.jgss.krb5.WrapToken_v2.encode(java.io.OutputStream) throws java.io.IOException
public final void sun.security.pkcs.PKCS8Key.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.pkcs.ContentInfo.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.pkcs.PKCS9Attributes.encode(byte,java.io.OutputStream) throws java.io.IOException
public void sun.security.pkcs.SignerInfo.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.pkcs10.PKCS10Attributes.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.provider.certpath.CertId.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public byte[] sun.security.timestamp.TSRequest.encode() throws java.io.IOException
public void sun.security.util.DerValue.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public abstract void sun.security.x509.GeneralNameInterface.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public final byte[] sun.security.x509.AlgorithmId.encode() throws java.io.IOException
public final void sun.security.x509.AlgorithmId.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public final void sun.security.x509.X509Key.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.AVA.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.AccessDescription.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.AuthorityInfoAccessExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.AuthorityKeyIdentifierExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.BasicConstraintsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CRLDistributionPointsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CRLNumberExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CRLReasonCodeExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateAlgorithmId.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateIssuerExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateIssuerName.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificatePoliciesExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificatePolicyId.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.CertificatePolicyMap.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.CertificatePolicySet.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.CertificateSerialNumber.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateSubjectName.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateValidity.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateVersion.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateX509Key.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.DNSName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.DeltaCRLIndicatorExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.DistributionPoint.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.DistributionPointName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.EDIPartyName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.ExtendedKeyUsageExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.Extension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.Extension.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.FreshestCRLExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.GeneralName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.GeneralNames.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.GeneralSubtree.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.GeneralSubtrees.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.IPAddressName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.InhibitAnyPolicyExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.InvalidityDateExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.IssuerAlternativeNameExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.IssuingDistributionPointExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.KeyUsageExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.NameConstraintsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.NetscapeCertTypeExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.OIDName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.OtherName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.PolicyConstraintsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.PolicyInformation.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.PolicyMappingsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.PrivateKeyUsageExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.RFC822Name.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.ReasonFlags.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.SerialNumber.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.SubjectAlternativeNameExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.SubjectInfoAccessExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.SubjectKeyIdentifierExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.URIName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.UniqueIdentity.encode(sun.security.util.DerOutputStream,byte) throws java.io.IOException
public void sun.security.x509.X400Address.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.X500Name.encode(sun.security.util.DerOutputStream) throws java.io.IOException
$
```

But now, with `-c auto`, the bounds are set implicitly:
```shell
$ juggle '? encode throws java.io.IOException' 
public static final String com.sun.jndi.toolkit.url.UrlUtil.encode(String,String) throws java.io.UnsupportedEncodingException
public static String com.sun.org.apache.bcel.internal.classfile.Utility.encode(byte[],boolean) throws java.io.IOException
public abstract com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.api.databinding.Databinding.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public abstract com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.api.pipe.Codec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.db.DatabindingImpl.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.MimeCodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.MtomCodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.SOAPBindingCodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public volatile com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.SwACodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.XMLHTTPBindingCodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public static String java.net.URLEncoder.encode(String,String) throws java.io.UnsupportedEncodingException
public final java.nio.ByteBuffer java.nio.charset.CharsetEncoder.encode(java.nio.CharBuffer) throws java.nio.charset.CharacterCodingException
public abstract void java.security.cert.Extension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.misc.CharacterEncoder.encode(byte[],java.io.OutputStream) throws java.io.IOException
public void sun.misc.CharacterEncoder.encode(java.io.InputStream,java.io.OutputStream) throws java.io.IOException
public void sun.misc.CharacterEncoder.encode(java.nio.ByteBuffer,java.io.OutputStream) throws java.io.IOException
public int sun.security.jgss.GSSHeader.encode(java.io.OutputStream) throws java.io.IOException
public abstract byte[] sun.security.jgss.krb5.InitialToken.encode() throws java.io.IOException
public abstract void sun.security.jgss.krb5.MessageToken_v2.encode(java.io.OutputStream) throws java.io.IOException
public byte[] sun.security.jgss.krb5.MicToken_v2.encode() throws java.io.IOException
public byte[] sun.security.jgss.krb5.WrapToken_v2.encode() throws java.io.IOException
public final byte[] sun.security.jgss.krb5.AcceptSecContextToken.encode() throws java.io.IOException
public final byte[] sun.security.jgss.krb5.InitSecContextToken.encode() throws java.io.IOException
public final void sun.security.jgss.krb5.MessageToken.MessageTokenHeader.encode(java.io.OutputStream) throws java.io.IOException
public final void sun.security.jgss.krb5.MessageToken_v2.MessageTokenHeader.encode(java.io.OutputStream) throws java.io.IOException
public int sun.security.jgss.krb5.MicToken_v2.encode(byte[],int) throws java.io.IOException
public int sun.security.jgss.krb5.WrapToken_v2.encode(byte[],int) throws java.io.IOException
public void sun.security.jgss.krb5.MicToken_v2.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.jgss.krb5.WrapToken_v2.encode(java.io.OutputStream) throws java.io.IOException
public final void sun.security.pkcs.PKCS8Key.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.pkcs.ContentInfo.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.pkcs.PKCS9Attributes.encode(byte,java.io.OutputStream) throws java.io.IOException
public void sun.security.pkcs.SignerInfo.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.pkcs10.PKCS10Attributes.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.provider.certpath.CertId.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public byte[] sun.security.timestamp.TSRequest.encode() throws java.io.IOException
public void sun.security.util.DerValue.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public abstract void sun.security.x509.GeneralNameInterface.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public final byte[] sun.security.x509.AlgorithmId.encode() throws java.io.IOException
public final void sun.security.x509.AlgorithmId.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public final void sun.security.x509.X509Key.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.AVA.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.AccessDescription.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.AuthorityInfoAccessExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.AuthorityKeyIdentifierExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.BasicConstraintsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CRLDistributionPointsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CRLNumberExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CRLReasonCodeExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateAlgorithmId.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateIssuerExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateIssuerName.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificatePoliciesExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificatePolicyId.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.CertificatePolicyMap.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.CertificatePolicySet.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.CertificateSerialNumber.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateSubjectName.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateValidity.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateVersion.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateX509Key.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.DNSName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.DeltaCRLIndicatorExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.DistributionPoint.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.DistributionPointName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.EDIPartyName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.ExtendedKeyUsageExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.Extension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.Extension.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.FreshestCRLExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.GeneralName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.GeneralNames.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.GeneralSubtree.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.GeneralSubtrees.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.IPAddressName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.InhibitAnyPolicyExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.InvalidityDateExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.IssuerAlternativeNameExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.IssuingDistributionPointExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.KeyUsageExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.NameConstraintsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.NetscapeCertTypeExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.OIDName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.OtherName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.PolicyConstraintsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.PolicyInformation.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.PolicyMappingsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.PrivateKeyUsageExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.RFC822Name.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.ReasonFlags.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.SerialNumber.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.SubjectAlternativeNameExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.SubjectInfoAccessExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.SubjectKeyIdentifierExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.URIName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.UniqueIdentity.encode(sun.security.util.DerOutputStream,byte) throws java.io.IOException
public void sun.security.x509.X400Address.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.X500Name.encode(sun.security.util.DerOutputStream) throws java.io.IOException
$
```

To show just the methods that throw a specific exception we need `-c none`:
```shell
$ juggle -c none '? encode throws java.io.IOException' 
public static String com.sun.org.apache.bcel.internal.classfile.Utility.encode(byte[],boolean) throws java.io.IOException
public abstract com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.api.databinding.Databinding.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public abstract com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.api.pipe.Codec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.db.DatabindingImpl.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.MimeCodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.MtomCodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.SOAPBindingCodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public volatile com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.SwACodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public com.sun.xml.internal.ws.api.pipe.ContentType com.sun.xml.internal.ws.encoding.XMLHTTPBindingCodec.encode(com.sun.xml.internal.ws.api.message.Packet,java.io.OutputStream) throws java.io.IOException
public abstract void java.security.cert.Extension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.misc.CharacterEncoder.encode(byte[],java.io.OutputStream) throws java.io.IOException
public void sun.misc.CharacterEncoder.encode(java.io.InputStream,java.io.OutputStream) throws java.io.IOException
public void sun.misc.CharacterEncoder.encode(java.nio.ByteBuffer,java.io.OutputStream) throws java.io.IOException
public int sun.security.jgss.GSSHeader.encode(java.io.OutputStream) throws java.io.IOException
public abstract byte[] sun.security.jgss.krb5.InitialToken.encode() throws java.io.IOException
public abstract void sun.security.jgss.krb5.MessageToken_v2.encode(java.io.OutputStream) throws java.io.IOException
public byte[] sun.security.jgss.krb5.MicToken_v2.encode() throws java.io.IOException
public byte[] sun.security.jgss.krb5.WrapToken_v2.encode() throws java.io.IOException
public final byte[] sun.security.jgss.krb5.AcceptSecContextToken.encode() throws java.io.IOException
public final byte[] sun.security.jgss.krb5.InitSecContextToken.encode() throws java.io.IOException
public final void sun.security.jgss.krb5.MessageToken.MessageTokenHeader.encode(java.io.OutputStream) throws java.io.IOException
public final void sun.security.jgss.krb5.MessageToken_v2.MessageTokenHeader.encode(java.io.OutputStream) throws java.io.IOException
public int sun.security.jgss.krb5.MicToken_v2.encode(byte[],int) throws java.io.IOException
public int sun.security.jgss.krb5.WrapToken_v2.encode(byte[],int) throws java.io.IOException
public void sun.security.jgss.krb5.MicToken_v2.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.jgss.krb5.WrapToken_v2.encode(java.io.OutputStream) throws java.io.IOException
public final void sun.security.pkcs.PKCS8Key.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.pkcs.ContentInfo.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.pkcs.PKCS9Attributes.encode(byte,java.io.OutputStream) throws java.io.IOException
public void sun.security.pkcs.SignerInfo.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.pkcs10.PKCS10Attributes.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.provider.certpath.CertId.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public byte[] sun.security.timestamp.TSRequest.encode() throws java.io.IOException
public void sun.security.util.DerValue.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public abstract void sun.security.x509.GeneralNameInterface.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public final byte[] sun.security.x509.AlgorithmId.encode() throws java.io.IOException
public final void sun.security.x509.AlgorithmId.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public final void sun.security.x509.X509Key.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.AVA.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.AccessDescription.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.AuthorityInfoAccessExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.AuthorityKeyIdentifierExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.BasicConstraintsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CRLDistributionPointsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CRLNumberExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CRLReasonCodeExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateAlgorithmId.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateIssuerExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateIssuerName.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificatePoliciesExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificatePolicyId.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.CertificatePolicyMap.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.CertificatePolicySet.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.CertificateSerialNumber.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateSubjectName.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateValidity.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateVersion.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.CertificateX509Key.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.DNSName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.DeltaCRLIndicatorExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.DistributionPoint.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.DistributionPointName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.EDIPartyName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.ExtendedKeyUsageExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.Extension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.Extension.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.FreshestCRLExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.GeneralName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.GeneralNames.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.GeneralSubtree.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.GeneralSubtrees.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.IPAddressName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.InhibitAnyPolicyExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.InvalidityDateExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.IssuerAlternativeNameExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.IssuingDistributionPointExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.KeyUsageExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.NameConstraintsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.NetscapeCertTypeExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.OIDName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.OtherName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.PolicyConstraintsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.PolicyInformation.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.PolicyMappingsExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.PrivateKeyUsageExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.RFC822Name.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.ReasonFlags.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.SerialNumber.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.SubjectAlternativeNameExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.SubjectInfoAccessExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.SubjectKeyIdentifierExtension.encode(java.io.OutputStream) throws java.io.IOException
public void sun.security.x509.URIName.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.UniqueIdentity.encode(sun.security.util.DerOutputStream,byte) throws java.io.IOException
public void sun.security.x509.X400Address.encode(sun.security.util.DerOutputStream) throws java.io.IOException
public void sun.security.x509.X500Name.encode(sun.security.util.DerOutputStream) throws java.io.IOException
$
```


### [GitHub Issue #109](https://github.com/paul-bennett/juggle/issues/109): Reintroduce boxing conversions

Here's a few functions that could take four `int`s:
```shell
$ juggle '(int,int,int,int)'
public com.sun.corba.se.impl.transport.ReadTCPTimeoutsImpl.<init>(int,int,int,int)
public static int com.sun.java.util.jar.pack.Coding.codeMax(int,int,int,int)
public static int com.sun.java.util.jar.pack.Coding.codeMin(int,int,int,int)
public com.sun.org.apache.bcel.internal.classfile.CodeException.<init>(int,int,int,int)
public com.sun.org.apache.bcel.internal.classfile.InnerClass.<init>(int,int,int,int)
public static javax.xml.datatype.XMLGregorianCalendar com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl.createDate(int,int,int,int)
public static javax.xml.datatype.XMLGregorianCalendar com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl.createTime(int,int,int,int)
public com.sun.xml.internal.fastinfoset.util.ContiguousCharArrayArray.<init>(int,int,int,int)
public java.awt.Color.<init>(int,int,int,int)
public java.awt.DisplayMode.<init>(int,int,int,int)
public java.awt.GridLayout.<init>(int,int,int,int)
public java.awt.Insets.<init>(int,int,int,int)
public java.awt.Rectangle.<init>(int,int,int,int)
public java.awt.image.BandedSampleModel.<init>(int,int,int,int)
public java.awt.image.CropImageFilter.<init>(int,int,int,int)
public java.awt.image.DirectColorModel.<init>(int,int,int,int)
public java.awt.image.MultiPixelPackedSampleModel.<init>(int,int,int,int)
public java.awt.image.SampleModel.<init>(int,int,int,int)
public static java.time.LocalTime java.time.LocalTime.of(int,int,int,int)
public javax.smartcardio.CommandAPDU.<init>(int,int,int,int)
public javax.sound.midi.ShortMessage.<init>(int,int,int,int) throws javax.sound.midi.InvalidMidiDataException
public static javax.swing.border.Border javax.swing.BorderFactory.createEmptyBorder(int,int,int,int)
public javax.swing.DefaultBoundedRangeModel.<init>(int,int,int,int)
public javax.swing.JSlider.<init>(int,int,int,int)
public javax.swing.SpinnerNumberModel.<init>(int,int,int,int)
public javax.swing.border.EmptyBorder.<init>(int,int,int,int)
public javax.swing.plaf.BorderUIResource.EmptyBorderUIResource.<init>(int,int,int,int)
public javax.swing.plaf.InsetsUIResource.<init>(int,int,int,int)
public sun.java2d.loops.ProcessPath.DrawHandler.<init>(int,int,int,int)
public static sun.java2d.pipe.Region sun.java2d.pipe.Region.getInstanceXYWH(int,int,int,int)
public static sun.java2d.pipe.Region sun.java2d.pipe.Region.getInstanceXYXY(int,int,int,int)
public sun.java2d.xr.XRColor.<init>(int,int,int,int)
public sun.swing.MenuItemLayoutHelper.RectSize.<init>(int,int,int,int)
public static sun.text.normalizer.VersionInfo sun.text.normalizer.VersionInfo.getInstance(int,int,int,int)
public javax.swing.SizeRequirements.<init>(int,int,int,float)
public java.awt.BasicStroke.<init>(float,int,int,float)
public com.sun.java_cup.internal.runtime.Symbol.<init>(int,int,int,Object)
public com.sun.org.apache.xerces.internal.impl.xs.models.XSCMLeaf.<init>(int,Object,int,int)
public javax.swing.event.ListDataEvent.<init>(Object,int,int,int)
public com.sun.jmx.snmp.SnmpIpAddress.<init>(long,long,long,long)
public com.sun.jmx.snmp.SnmpOid.<init>(long,long,long,long)
public java.awt.Color.<init>(float,float,float,float)
public java.awt.font.TextLine.TextLineMetrics.<init>(float,float,float,float)
public static double java.awt.geom.Point2D.distance(double,double,double,double)
public static double java.awt.geom.Point2D.distanceSq(double,double,double,double)
public static java.awt.geom.AffineTransform java.awt.geom.AffineTransform.getRotateInstance(double,double,double,double)
public java.awt.geom.Ellipse2D.Double.<init>(double,double,double,double)
public java.awt.geom.Ellipse2D.Float.<init>(float,float,float,float)
public java.awt.geom.Line2D.Double.<init>(double,double,double,double)
public java.awt.geom.Line2D.Float.<init>(float,float,float,float)
public java.awt.geom.Rectangle2D.Double.<init>(double,double,double,double)
public java.awt.geom.Rectangle2D.Float.<init>(float,float,float,float)
public java.lang.management.MemoryUsage.<init>(long,long,long,long)
public static java.time.temporal.ValueRange java.time.temporal.ValueRange.of(long,long,long,long)
public javax.swing.SpinnerNumberModel.<init>(double,double,double,double)
public static double sun.awt.geom.Order2.TforY(double,double,double,double)
public sun.awt.geom.Crossings.<init>(double,double,double,double)
public sun.awt.geom.Crossings.EvenOdd.<init>(double,double,double,double)
public sun.awt.geom.Crossings.NonZero.<init>(double,double,double,double)
public sun.security.krb5.internal.ccache.Tag.<init>(int,int,Integer,Integer)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0030_SERVICE_PORT_OPERATION_FAULT_MSG_PARAM_MUST_NOT_BE_NULL(Object,Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0030_SERVICE_PORT_OPERATION_FAULT_MSG_PARAM_MUST_NOT_BE_NULL(Object,Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.AddressingMessages.NON_UNIQUE_OPERATION_SIGNATURE(Object,Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.RUNTIME_WSDLPARSER_INVALID_WSDL(Object,Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.AddressingMessages.localizableNON_UNIQUE_OPERATION_SIGNATURE(Object,Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ClientMessages.localizableRUNTIME_WSDLPARSER_INVALID_WSDL(Object,Object,Object,Object)
public javax.swing.SpinnerNumberModel.<init>(Number,Comparable<T>,Comparable<T>,Number)
$
```

If we search for `Integer`s (or a mix of both types) we should get the same
thing:

```shell
$ juggle '(int,Integer,int,Integer)'
public com.sun.java_cup.internal.runtime.Symbol.<init>(int,int,int,Object)
public com.sun.org.apache.xerces.internal.impl.xs.models.XSCMLeaf.<init>(int,Object,int,int)
public com.sun.corba.se.impl.transport.ReadTCPTimeoutsImpl.<init>(int,int,int,int)
public static int com.sun.java.util.jar.pack.Coding.codeMax(int,int,int,int)
public static int com.sun.java.util.jar.pack.Coding.codeMin(int,int,int,int)
public com.sun.org.apache.bcel.internal.classfile.CodeException.<init>(int,int,int,int)
public com.sun.org.apache.bcel.internal.classfile.InnerClass.<init>(int,int,int,int)
public static javax.xml.datatype.XMLGregorianCalendar com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl.createDate(int,int,int,int)
public static javax.xml.datatype.XMLGregorianCalendar com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl.createTime(int,int,int,int)
public com.sun.xml.internal.fastinfoset.util.ContiguousCharArrayArray.<init>(int,int,int,int)
public java.awt.Color.<init>(int,int,int,int)
public java.awt.DisplayMode.<init>(int,int,int,int)
public java.awt.GridLayout.<init>(int,int,int,int)
public java.awt.Insets.<init>(int,int,int,int)
public java.awt.Rectangle.<init>(int,int,int,int)
public java.awt.image.BandedSampleModel.<init>(int,int,int,int)
public java.awt.image.CropImageFilter.<init>(int,int,int,int)
public java.awt.image.DirectColorModel.<init>(int,int,int,int)
public java.awt.image.MultiPixelPackedSampleModel.<init>(int,int,int,int)
public java.awt.image.SampleModel.<init>(int,int,int,int)
public static java.time.LocalTime java.time.LocalTime.of(int,int,int,int)
public javax.smartcardio.CommandAPDU.<init>(int,int,int,int)
public javax.sound.midi.ShortMessage.<init>(int,int,int,int) throws javax.sound.midi.InvalidMidiDataException
public static javax.swing.border.Border javax.swing.BorderFactory.createEmptyBorder(int,int,int,int)
public javax.swing.DefaultBoundedRangeModel.<init>(int,int,int,int)
public javax.swing.JSlider.<init>(int,int,int,int)
public javax.swing.SpinnerNumberModel.<init>(int,int,int,int)
public javax.swing.border.EmptyBorder.<init>(int,int,int,int)
public javax.swing.plaf.BorderUIResource.EmptyBorderUIResource.<init>(int,int,int,int)
public javax.swing.plaf.InsetsUIResource.<init>(int,int,int,int)
public sun.java2d.loops.ProcessPath.DrawHandler.<init>(int,int,int,int)
public static sun.java2d.pipe.Region sun.java2d.pipe.Region.getInstanceXYWH(int,int,int,int)
public static sun.java2d.pipe.Region sun.java2d.pipe.Region.getInstanceXYXY(int,int,int,int)
public sun.java2d.xr.XRColor.<init>(int,int,int,int)
public sun.security.krb5.internal.ccache.Tag.<init>(int,int,Integer,Integer)
public sun.swing.MenuItemLayoutHelper.RectSize.<init>(int,int,int,int)
public static sun.text.normalizer.VersionInfo sun.text.normalizer.VersionInfo.getInstance(int,int,int,int)
public javax.swing.SizeRequirements.<init>(int,int,int,float)
public java.awt.BasicStroke.<init>(float,int,int,float)
public javax.swing.event.ListDataEvent.<init>(Object,int,int,int)
public com.sun.jmx.snmp.SnmpIpAddress.<init>(long,long,long,long)
public com.sun.jmx.snmp.SnmpOid.<init>(long,long,long,long)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0030_SERVICE_PORT_OPERATION_FAULT_MSG_PARAM_MUST_NOT_BE_NULL(Object,Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0030_SERVICE_PORT_OPERATION_FAULT_MSG_PARAM_MUST_NOT_BE_NULL(Object,Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.AddressingMessages.NON_UNIQUE_OPERATION_SIGNATURE(Object,Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.RUNTIME_WSDLPARSER_INVALID_WSDL(Object,Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.AddressingMessages.localizableNON_UNIQUE_OPERATION_SIGNATURE(Object,Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ClientMessages.localizableRUNTIME_WSDLPARSER_INVALID_WSDL(Object,Object,Object,Object)
public java.awt.Color.<init>(float,float,float,float)
public java.awt.font.TextLine.TextLineMetrics.<init>(float,float,float,float)
public static double java.awt.geom.Point2D.distance(double,double,double,double)
public static double java.awt.geom.Point2D.distanceSq(double,double,double,double)
public static java.awt.geom.AffineTransform java.awt.geom.AffineTransform.getRotateInstance(double,double,double,double)
public java.awt.geom.Ellipse2D.Double.<init>(double,double,double,double)
public java.awt.geom.Ellipse2D.Float.<init>(float,float,float,float)
public java.awt.geom.Line2D.Double.<init>(double,double,double,double)
public java.awt.geom.Line2D.Float.<init>(float,float,float,float)
public java.awt.geom.Rectangle2D.Double.<init>(double,double,double,double)
public java.awt.geom.Rectangle2D.Float.<init>(float,float,float,float)
public java.lang.management.MemoryUsage.<init>(long,long,long,long)
public static java.time.temporal.ValueRange java.time.temporal.ValueRange.of(long,long,long,long)
public javax.swing.SpinnerNumberModel.<init>(double,double,double,double)
public javax.swing.SpinnerNumberModel.<init>(Number,Comparable<T>,Comparable<T>,Number)
public static double sun.awt.geom.Order2.TforY(double,double,double,double)
public sun.awt.geom.Crossings.<init>(double,double,double,double)
public sun.awt.geom.Crossings.EvenOdd.<init>(double,double,double,double)
public sun.awt.geom.Crossings.NonZero.<init>(double,double,double,double)
$
```

Similarly, we should be able to go in the other direction too:
```shell
$ juggle 'int (Integer,Integer)'
public int Integer.compareTo(Integer)
public volatile int Integer.compareTo(Object)
public abstract int Comparable<T>.compareTo(T)
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static int jdk.xml.internal.JdkXmlUtils.getValue(Object,int)
public static int sun.swing.SwingUtilities2.getUIDefaultsInt(Object,int)
public static int Math.addExact(int,int)
public static int StrictMath.addExact(int,int)
public static int Integer.compare(int,int)
public static int Integer.compareUnsigned(int,int)
public static int Character.digit(int,int)
public static int Integer.divideUnsigned(int,int)
public static int Math.floorDiv(int,int)
public static int StrictMath.floorDiv(int,int)
public static int Math.floorMod(int,int)
public static int StrictMath.floorMod(int,int)
public static int Integer.max(int,int)
public static int Math.max(int,int)
public static int StrictMath.max(int,int)
public static int Integer.min(int,int)
public static int Math.min(int,int)
public static int StrictMath.min(int,int)
public static int Math.multiplyExact(int,int)
public static int StrictMath.multiplyExact(int,int)
public static int Integer.remainderUnsigned(int,int)
public static int Integer.rotateLeft(int,int)
public static int Integer.rotateRight(int,int)
public static int Math.subtractExact(int,int)
public static int StrictMath.subtractExact(int,int)
public static int Integer.sum(int,int)
public static int com.sun.jmx.snmp.agent.SnmpRequestTree.mapGetException(int,int) throws com.sun.jmx.snmp.SnmpStatusException
public static int com.sun.jmx.snmp.agent.SnmpRequestTree.mapSetException(int,int) throws com.sun.jmx.snmp.SnmpStatusException
public static final int com.sun.org.apache.bcel.internal.classfile.Utility.clearBit(int,int)
public static final int com.sun.org.apache.bcel.internal.classfile.Utility.setBit(int,int)
public static final int com.sun.xml.internal.fastinfoset.util.KeyIntMap.indexFor(int,int)
public static native byte java.lang.reflect.Array.getByte(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native char java.lang.reflect.Array.getChar(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native short java.lang.reflect.Array.getShort(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static int sun.awt.dnd.SunDragSourceContextPeer.convertModifiersToDropAction(int,int)
public static int sun.java2d.pipe.Region.clipAdd(int,int)
public static int sun.java2d.pipe.Region.dimAdd(int,int)
public static int sun.text.normalizer.UCharacter.digit(int,int)
public static final int sun.text.normalizer.NormalizerImpl.quickCheck(int,int)
public static final int sun.util.calendar.CalendarUtils.amod(int,int)
public static final int sun.util.calendar.CalendarUtils.floorDivide(int,int)
public static final int sun.util.calendar.CalendarUtils.mod(int,int)
public static char Character.forDigit(int,int)
public static int jdk.management.resource.internal.ResourceNatives.setThreadResourceContext(long,int)
public static native int jdk.management.resource.internal.ResourceNatives.setThreadResourceContext0(long,int)
public static int sun.java2d.pipe.Region.clipScale(int,double)
public static int Double.compare(double,double)
public static int Float.compare(float,float)
public static int Long.compare(long,long)
public static int Long.compareUnsigned(long,long)
public static int com.sun.jndi.dns.ResourceRecord.compareSerialNumbers(long,long)
public static int sun.awt.geom.Curve.orderof(double,double)
$
```
```shell
$ juggle 'int (int,int)'
public static int Math.addExact(int,int)
public static int StrictMath.addExact(int,int)
public static int Integer.compare(int,int)
public static int Integer.compareUnsigned(int,int)
public static int Character.digit(int,int)
public static int Integer.divideUnsigned(int,int)
public static int Math.floorDiv(int,int)
public static int StrictMath.floorDiv(int,int)
public static int Math.floorMod(int,int)
public static int StrictMath.floorMod(int,int)
public static int Integer.max(int,int)
public static int Math.max(int,int)
public static int StrictMath.max(int,int)
public static int Integer.min(int,int)
public static int Math.min(int,int)
public static int StrictMath.min(int,int)
public static int Math.multiplyExact(int,int)
public static int StrictMath.multiplyExact(int,int)
public static int Integer.remainderUnsigned(int,int)
public static int Integer.rotateLeft(int,int)
public static int Integer.rotateRight(int,int)
public static int Math.subtractExact(int,int)
public static int StrictMath.subtractExact(int,int)
public static int Integer.sum(int,int)
public static int com.sun.jmx.snmp.agent.SnmpRequestTree.mapGetException(int,int) throws com.sun.jmx.snmp.SnmpStatusException
public static int com.sun.jmx.snmp.agent.SnmpRequestTree.mapSetException(int,int) throws com.sun.jmx.snmp.SnmpStatusException
public static final int com.sun.org.apache.bcel.internal.classfile.Utility.clearBit(int,int)
public static final int com.sun.org.apache.bcel.internal.classfile.Utility.setBit(int,int)
public static final int com.sun.xml.internal.fastinfoset.util.KeyIntMap.indexFor(int,int)
public static int sun.awt.dnd.SunDragSourceContextPeer.convertModifiersToDropAction(int,int)
public static int sun.java2d.pipe.Region.clipAdd(int,int)
public static int sun.java2d.pipe.Region.dimAdd(int,int)
public static int sun.text.normalizer.UCharacter.digit(int,int)
public static final int sun.text.normalizer.NormalizerImpl.quickCheck(int,int)
public static final int sun.util.calendar.CalendarUtils.amod(int,int)
public static final int sun.util.calendar.CalendarUtils.floorDivide(int,int)
public static final int sun.util.calendar.CalendarUtils.mod(int,int)
public static char Character.forDigit(int,int)
public static int jdk.management.resource.internal.ResourceNatives.setThreadResourceContext(long,int)
public static native int jdk.management.resource.internal.ResourceNatives.setThreadResourceContext0(long,int)
public static int sun.java2d.pipe.Region.clipScale(int,double)
public static int Double.compare(double,double)
public static int Float.compare(float,float)
public static int Long.compare(long,long)
public static int Long.compareUnsigned(long,long)
public static int com.sun.jndi.dns.ResourceRecord.compareSerialNumbers(long,long)
public static int sun.awt.geom.Curve.orderof(double,double)
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static int jdk.xml.internal.JdkXmlUtils.getValue(Object,int)
public static int sun.swing.SwingUtilities2.getUIDefaultsInt(Object,int)
public int Integer.compareTo(Integer)
public static native byte java.lang.reflect.Array.getByte(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native char java.lang.reflect.Array.getChar(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native short java.lang.reflect.Array.getShort(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public volatile int Integer.compareTo(Object)
public abstract int Comparable<T>.compareTo(T)
$
```

Finally, we the conversions should also apply for return types:
```shell
$ juggle 'Integer(String,int)'
public static Integer Integer.getInteger(String,int)
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public static Integer sun.net.NetProperties.getInteger(String,int)
public int String.codePointAt(int)
public int String.codePointBefore(int)
public static Integer Integer.getInteger(String,Integer)
public int String.indexOf(int)
public int String.lastIndexOf(int)
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
public static int sun.text.normalizer.UTF16.charAt(String,int)
public static int sun.text.normalizer.Utility.skipWhitespace(String,int)
public static int Character.codePointAt(CharSequence,int)
public static int Character.codePointBefore(CharSequence,int)
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static int jdk.xml.internal.JdkXmlUtils.getValue(Object,int)
public static int sun.misc.PerformanceLogger.setTime(String,long)
public static int sun.swing.SwingUtilities2.getUIDefaultsInt(Object,int)
public volatile int String.compareTo(Object)
public abstract int Comparable<T>.compareTo(T)
$
```
```shell
$ juggle 'int(String,int)'
public int String.codePointAt(int)
public int String.codePointBefore(int)
public int String.indexOf(int)
public int String.lastIndexOf(int)
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
public static int sun.text.normalizer.UTF16.charAt(String,int)
public static int sun.text.normalizer.Utility.skipWhitespace(String,int)
public char String.charAt(int)
public static int Character.codePointAt(CharSequence,int)
public static int Character.codePointBefore(CharSequence,int)
public static byte Byte.parseByte(String,int) throws NumberFormatException
public static short Short.parseShort(String,int) throws NumberFormatException
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static int jdk.xml.internal.JdkXmlUtils.getValue(Object,int)
public static int sun.misc.PerformanceLogger.setTime(String,long)
public static int sun.swing.SwingUtilities2.getUIDefaultsInt(Object,int)
public abstract char CharSequence.charAt(int)
public static Integer Integer.getInteger(String,int)
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public static native byte java.lang.reflect.Array.getByte(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native char java.lang.reflect.Array.getChar(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native short java.lang.reflect.Array.getShort(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static Integer sun.net.NetProperties.getInteger(String,int)
public volatile int String.compareTo(Object)
public static Byte Byte.valueOf(String,int) throws NumberFormatException
public static Short Short.valueOf(String,int) throws NumberFormatException
public abstract int Comparable<T>.compareTo(T)
public static Integer Integer.getInteger(String,Integer)
$
```

### [GitHub Issue #105](https://github.com/paul-bennett/juggle/issues/105): `-s package` should sort by package name

Modified `package` comparator now sorts packages alphabetically if they 
weren't mentioned in the import list, and the implicit `java.lang` has
been moved to the end of that list rather than the start.
```shell
$ juggle -i java.net class /Class/
public class ClassCircularityError extends LinkageError
public class ClassFormatError extends LinkageError
public abstract class ClassLoader
public class java.security.SecureClassLoader extends ClassLoader
public class URLClassLoader extends java.security.SecureClassLoader implements java.io.Closeable
public final class Class<T> implements java.io.Serializable, java.lang.reflect.GenericDeclaration, java.lang.reflect.Type, java.lang.reflect.AnnotatedElement
public class ClassCastException extends RuntimeException
public class ClassNotFoundException extends ReflectiveOperationException
public abstract class ClassValue<T>
public class IncompatibleClassChangeError extends LinkageError
public class NoClassDefFoundError extends LinkageError
public class UnsupportedClassVersionError extends ClassFormatError
public final class com.sun.beans.finder.ClassFinder
public class com.sun.corba.se.impl.io.ObjectStreamClass implements java.io.Serializable
public final class com.sun.corba.se.impl.orbutil.ObjectStreamClassUtil_1_3
public class com.sun.corba.se.impl.orbutil.ObjectStreamClass_1_3_1 implements java.io.Serializable
public class com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel extends com.sun.java.swing.plaf.windows.WindowsLookAndFeel
public final class com.sun.java.util.jar.pack.Package.Class extends com.sun.java.util.jar.pack.Attribute.Holder implements Comparable<T>
public static class com.sun.java.util.jar.pack.ConstantPool.ClassEntry extends com.sun.java.util.jar.pack.ConstantPool.Entry
public abstract class com.sun.java.util.jar.pack.Package.Class.Member extends com.sun.java.util.jar.pack.Attribute.Holder implements Comparable<T>
public class com.sun.java.util.jar.pack.Package.Class.Field extends com.sun.java.util.jar.pack.Package.Class.Member
public class com.sun.java.util.jar.pack.Package.Class.Method extends com.sun.java.util.jar.pack.Package.Class.Member
public class com.sun.jmx.remote.util.ClassLoaderWithRepository extends ClassLoader
public class com.sun.jmx.remote.util.ClassLogger
public class com.sun.jmx.remote.util.OrderClassLoaders extends ClassLoader
public class com.sun.org.apache.bcel.internal.classfile.ClassFormatException extends RuntimeException
public final class com.sun.org.apache.bcel.internal.classfile.ClassParser
public final class com.sun.org.apache.bcel.internal.classfile.ConstantClass extends com.sun.org.apache.bcel.internal.classfile.Constant implements com.sun.org.apache.bcel.internal.classfile.ConstantObject
public final class com.sun.org.apache.bcel.internal.classfile.InnerClass implements Cloneable, com.sun.org.apache.bcel.internal.classfile.Node
public final class com.sun.org.apache.bcel.internal.classfile.InnerClasses extends com.sun.org.apache.bcel.internal.classfile.Attribute
public class com.sun.org.apache.bcel.internal.classfile.JavaClass extends com.sun.org.apache.bcel.internal.classfile.AccessFlags implements Cloneable, com.sun.org.apache.bcel.internal.classfile.Node
public final class com.sun.org.apache.bcel.internal.classfile.PMGClass extends com.sun.org.apache.bcel.internal.classfile.Attribute
public class com.sun.org.apache.bcel.internal.generic.ClassGen extends com.sun.org.apache.bcel.internal.classfile.AccessFlags implements Cloneable
public class com.sun.org.apache.bcel.internal.generic.ClassGenException extends RuntimeException
public class com.sun.org.apache.bcel.internal.util.Class2HTML implements com.sun.org.apache.bcel.internal.Constants
public class com.sun.org.apache.bcel.internal.util.ClassLoader extends ClassLoader
public class com.sun.org.apache.bcel.internal.util.ClassLoaderRepository implements com.sun.org.apache.bcel.internal.util.Repository
public class com.sun.org.apache.bcel.internal.util.ClassPath implements java.io.Serializable
public class com.sun.org.apache.bcel.internal.util.ClassQueue implements java.io.Serializable
public class com.sun.org.apache.bcel.internal.util.ClassSet implements java.io.Serializable
public class com.sun.org.apache.bcel.internal.util.ClassStack implements java.io.Serializable
public class com.sun.org.apache.bcel.internal.util.ClassVector implements java.io.Serializable
public class com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator extends com.sun.org.apache.bcel.internal.generic.ClassGen
public abstract class com.sun.xml.internal.bind.api.ClassResolver
public final class com.sun.xml.internal.bind.v2.ClassFactory
public final class com.sun.xml.internal.bind.v2.bytecode.ClassTailor
public class com.sun.xml.internal.bind.v2.model.annotation.ClassLocatable<C> implements com.sun.xml.internal.bind.v2.model.annotation.Locatable
public class com.sun.xml.internal.bind.v2.model.impl.ClassInfoImpl<T,C,F,M> extends com.sun.xml.internal.bind.v2.model.impl.TypeInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> implements com.sun.xml.internal.bind.v2.model.core.ClassInfo<T,C>, com.sun.xml.internal.bind.v2.model.core.Element<T,C>
public final class com.sun.xml.internal.bind.v2.runtime.ClassBeanInfoImpl<BeanT> extends com.sun.xml.internal.bind.v2.runtime.JaxBeanInfo<BeanT> implements com.sun.xml.internal.bind.v2.runtime.AttributeAccessor<BeanT>
public class com.sun.xml.internal.ws.org.objectweb.asm.ClassAdapter implements com.sun.xml.internal.ws.org.objectweb.asm.ClassVisitor
public class com.sun.xml.internal.ws.org.objectweb.asm.ClassReader
public class com.sun.xml.internal.ws.org.objectweb.asm.ClassWriter implements com.sun.xml.internal.ws.org.objectweb.asm.ClassVisitor
public class java.io.InvalidClassException extends java.io.ObjectStreamException
public class java.io.ObjectStreamClass implements java.io.Serializable
public final class java.lang.instrument.ClassDefinition
public class java.lang.instrument.IllegalClassFormatException extends Exception
public class java.lang.instrument.UnmodifiableClassException extends Exception
public class java.rmi.server.RMIClassLoader
public abstract class java.rmi.server.RMIClassLoaderSpi
public class javax.naming.NameClassPair implements java.io.Serializable
public class javax.rmi.CORBA.ClassDesc implements java.io.Serializable
public final class jdk.internal.instrumentation.ClassInstrumentation
public class jdk.internal.org.objectweb.asm.ClassReader
public abstract class jdk.internal.org.objectweb.asm.ClassVisitor
public class jdk.internal.org.objectweb.asm.ClassWriter extends jdk.internal.org.objectweb.asm.ClassVisitor
public class jdk.internal.org.objectweb.asm.commons.RemappingClassAdapter extends jdk.internal.org.objectweb.asm.ClassVisitor
public class jdk.internal.org.objectweb.asm.tree.ClassNode extends jdk.internal.org.objectweb.asm.ClassVisitor
public class jdk.internal.org.objectweb.asm.tree.InnerClassNode
public class jdk.internal.org.objectweb.asm.util.CheckClassAdapter extends jdk.internal.org.objectweb.asm.ClassVisitor
public final class jdk.internal.org.objectweb.asm.util.TraceClassVisitor extends jdk.internal.org.objectweb.asm.ClassVisitor
public class sun.applet.AppletClassLoader extends URLClassLoader
public class sun.management.snmp.jvminstr.JvmClassLoadingImpl implements sun.management.snmp.jvmmib.JvmClassLoadingMBean
public class sun.management.snmp.jvminstr.JvmRTBootClassPathEntryImpl implements sun.management.snmp.jvmmib.JvmRTBootClassPathEntryMBean, java.io.Serializable
public class sun.management.snmp.jvminstr.JvmRTBootClassPathTableMetaImpl extends sun.management.snmp.jvmmib.JvmRTBootClassPathTableMeta
public class sun.management.snmp.jvminstr.JvmRTClassPathEntryImpl implements sun.management.snmp.jvmmib.JvmRTClassPathEntryMBean, java.io.Serializable
public class sun.management.snmp.jvminstr.JvmRTClassPathTableMetaImpl extends sun.management.snmp.jvmmib.JvmRTClassPathTableMeta
public class sun.management.snmp.jvmmib.EnumJvmClassesVerboseLevel extends com.sun.jmx.snmp.Enumerated implements java.io.Serializable
public class sun.management.snmp.jvmmib.EnumJvmRTBootClassPathSupport extends com.sun.jmx.snmp.Enumerated implements java.io.Serializable
public class sun.management.snmp.jvmmib.JvmClassLoadingMeta extends com.sun.jmx.snmp.agent.SnmpMibGroup implements java.io.Serializable, com.sun.jmx.snmp.agent.SnmpStandardMetaServer
public class sun.management.snmp.jvmmib.JvmRTBootClassPathEntryMeta extends com.sun.jmx.snmp.agent.SnmpMibEntry implements java.io.Serializable, com.sun.jmx.snmp.agent.SnmpStandardMetaServer
public class sun.management.snmp.jvmmib.JvmRTBootClassPathTableMeta extends com.sun.jmx.snmp.agent.SnmpMibTable implements java.io.Serializable
public class sun.management.snmp.jvmmib.JvmRTClassPathEntryMeta extends com.sun.jmx.snmp.agent.SnmpMibEntry implements java.io.Serializable, com.sun.jmx.snmp.agent.SnmpStandardMetaServer
public class sun.management.snmp.jvmmib.JvmRTClassPathTableMeta extends com.sun.jmx.snmp.agent.SnmpMibTable implements java.io.Serializable
public final class sun.management.snmp.util.SnmpLoadedClassData extends sun.management.snmp.util.SnmpCachedData
public abstract class sun.misc.ClassFileTransformer
public class sun.misc.ClassLoaderUtil
public class sun.misc.URLClassPath
public class sun.print.AttributeClass
public class sun.reflect.generics.repository.ClassRepository extends sun.reflect.generics.repository.GenericDeclRepository<S>
public class sun.reflect.generics.scope.ClassScope extends sun.reflect.generics.scope.AbstractScope<D> implements sun.reflect.generics.scope.Scope
public class sun.reflect.generics.tree.ClassSignature implements sun.reflect.generics.tree.Signature
public class sun.reflect.generics.tree.ClassTypeSignature implements sun.reflect.generics.tree.FieldTypeSignature
public class sun.reflect.generics.tree.SimpleClassTypeSignature implements sun.reflect.generics.tree.FieldTypeSignature
public abstract class sun.rmi.server.WeakClassHashMap<V>
public class sun.swing.plaf.windows.ClassicSortArrowIcon implements javax.swing.Icon, javax.swing.plaf.UIResource, java.io.Serializable
$
```

### [GitHub Issue #72](https://github.com/paul-bennett/juggle/issues/72): Don't show JDK implementation classes

Prior to fixing, this used to include two further results from non-exported packages
in `jdk.internals.*`:
```shell
$ juggle '(int,int,int,int)'
public com.sun.corba.se.impl.transport.ReadTCPTimeoutsImpl.<init>(int,int,int,int)
public static int com.sun.java.util.jar.pack.Coding.codeMax(int,int,int,int)
public static int com.sun.java.util.jar.pack.Coding.codeMin(int,int,int,int)
public com.sun.org.apache.bcel.internal.classfile.CodeException.<init>(int,int,int,int)
public com.sun.org.apache.bcel.internal.classfile.InnerClass.<init>(int,int,int,int)
public static javax.xml.datatype.XMLGregorianCalendar com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl.createDate(int,int,int,int)
public static javax.xml.datatype.XMLGregorianCalendar com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl.createTime(int,int,int,int)
public com.sun.xml.internal.fastinfoset.util.ContiguousCharArrayArray.<init>(int,int,int,int)
public java.awt.Color.<init>(int,int,int,int)
public java.awt.DisplayMode.<init>(int,int,int,int)
public java.awt.GridLayout.<init>(int,int,int,int)
public java.awt.Insets.<init>(int,int,int,int)
public java.awt.Rectangle.<init>(int,int,int,int)
public java.awt.image.BandedSampleModel.<init>(int,int,int,int)
public java.awt.image.CropImageFilter.<init>(int,int,int,int)
public java.awt.image.DirectColorModel.<init>(int,int,int,int)
public java.awt.image.MultiPixelPackedSampleModel.<init>(int,int,int,int)
public java.awt.image.SampleModel.<init>(int,int,int,int)
public static java.time.LocalTime java.time.LocalTime.of(int,int,int,int)
public javax.smartcardio.CommandAPDU.<init>(int,int,int,int)
public javax.sound.midi.ShortMessage.<init>(int,int,int,int) throws javax.sound.midi.InvalidMidiDataException
public static javax.swing.border.Border javax.swing.BorderFactory.createEmptyBorder(int,int,int,int)
public javax.swing.DefaultBoundedRangeModel.<init>(int,int,int,int)
public javax.swing.JSlider.<init>(int,int,int,int)
public javax.swing.SpinnerNumberModel.<init>(int,int,int,int)
public javax.swing.border.EmptyBorder.<init>(int,int,int,int)
public javax.swing.plaf.BorderUIResource.EmptyBorderUIResource.<init>(int,int,int,int)
public javax.swing.plaf.InsetsUIResource.<init>(int,int,int,int)
public sun.java2d.loops.ProcessPath.DrawHandler.<init>(int,int,int,int)
public static sun.java2d.pipe.Region sun.java2d.pipe.Region.getInstanceXYWH(int,int,int,int)
public static sun.java2d.pipe.Region sun.java2d.pipe.Region.getInstanceXYXY(int,int,int,int)
public sun.java2d.xr.XRColor.<init>(int,int,int,int)
public sun.swing.MenuItemLayoutHelper.RectSize.<init>(int,int,int,int)
public static sun.text.normalizer.VersionInfo sun.text.normalizer.VersionInfo.getInstance(int,int,int,int)
public javax.swing.SizeRequirements.<init>(int,int,int,float)
public java.awt.BasicStroke.<init>(float,int,int,float)
public com.sun.java_cup.internal.runtime.Symbol.<init>(int,int,int,Object)
public com.sun.org.apache.xerces.internal.impl.xs.models.XSCMLeaf.<init>(int,Object,int,int)
public javax.swing.event.ListDataEvent.<init>(Object,int,int,int)
public com.sun.jmx.snmp.SnmpIpAddress.<init>(long,long,long,long)
public com.sun.jmx.snmp.SnmpOid.<init>(long,long,long,long)
public java.awt.Color.<init>(float,float,float,float)
public java.awt.font.TextLine.TextLineMetrics.<init>(float,float,float,float)
public static double java.awt.geom.Point2D.distance(double,double,double,double)
public static double java.awt.geom.Point2D.distanceSq(double,double,double,double)
public static java.awt.geom.AffineTransform java.awt.geom.AffineTransform.getRotateInstance(double,double,double,double)
public java.awt.geom.Ellipse2D.Double.<init>(double,double,double,double)
public java.awt.geom.Ellipse2D.Float.<init>(float,float,float,float)
public java.awt.geom.Line2D.Double.<init>(double,double,double,double)
public java.awt.geom.Line2D.Float.<init>(float,float,float,float)
public java.awt.geom.Rectangle2D.Double.<init>(double,double,double,double)
public java.awt.geom.Rectangle2D.Float.<init>(float,float,float,float)
public java.lang.management.MemoryUsage.<init>(long,long,long,long)
public static java.time.temporal.ValueRange java.time.temporal.ValueRange.of(long,long,long,long)
public javax.swing.SpinnerNumberModel.<init>(double,double,double,double)
public static double sun.awt.geom.Order2.TforY(double,double,double,double)
public sun.awt.geom.Crossings.<init>(double,double,double,double)
public sun.awt.geom.Crossings.EvenOdd.<init>(double,double,double,double)
public sun.awt.geom.Crossings.NonZero.<init>(double,double,double,double)
public sun.security.krb5.internal.ccache.Tag.<init>(int,int,Integer,Integer)
public static String com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.WSP_0030_SERVICE_PORT_OPERATION_FAULT_MSG_PARAM_MUST_NOT_BE_NULL(Object,Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.policy.privateutil.LocalizationMessages.localizableWSP_0030_SERVICE_PORT_OPERATION_FAULT_MSG_PARAM_MUST_NOT_BE_NULL(Object,Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.AddressingMessages.NON_UNIQUE_OPERATION_SIGNATURE(Object,Object,Object,Object)
public static String com.sun.xml.internal.ws.resources.ClientMessages.RUNTIME_WSDLPARSER_INVALID_WSDL(Object,Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.AddressingMessages.localizableNON_UNIQUE_OPERATION_SIGNATURE(Object,Object,Object,Object)
public static com.sun.istack.internal.localization.Localizable com.sun.xml.internal.ws.resources.ClientMessages.localizableRUNTIME_WSDLPARSER_INVALID_WSDL(Object,Object,Object,Object)
public javax.swing.SpinnerNumberModel.<init>(Number,Comparable<T>,Comparable<T>,Number)
$
```

### [GitHub Issue #85](https://github.com/paul-bennett/juggle/issues/85): Handle nested classes in queries

Prior to fixing, this used to throw an exception saying that it couldn't
find the `Authenticator.RequestorType` class.
```shell
$ juggle -i java.net 'PasswordAuthentication (Authenticator,String,InetAddress,int,String,String,String,URL,Authenticator.RequestorType)'
$
```

### [GitHub Issue #62](https://github.com/paul-bennett/juggle/issues/62): Add ellipsis support to throws clauses

We don't need to implement this because using a wildcard in the `throws` clause does the trick.

Here are the methods that only throw `FileNotFoundException`:
```shell
$ juggle throws java.io.FileNotFoundException
public static String com.apple.eio.FileManager.findFolder(int) throws java.io.FileNotFoundException
public static String com.apple.eio.FileManager.findFolder(short,int) throws java.io.FileNotFoundException
public static String com.apple.eio.FileManager.findFolder(short,int,boolean) throws java.io.FileNotFoundException
public static String com.apple.eio.FileManager.getResource(String) throws java.io.FileNotFoundException
public static String com.apple.eio.FileManager.getResource(String,String) throws java.io.FileNotFoundException
public static String com.apple.eio.FileManager.getResource(String,String,String) throws java.io.FileNotFoundException
public static boolean com.apple.eio.FileManager.moveToTrash(java.io.File) throws java.io.FileNotFoundException
public static boolean com.apple.eio.FileManager.revealInFinder(java.io.File) throws java.io.FileNotFoundException
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
public sun.awt.shell.ShellFolder sun.awt.shell.ShellFolderManager.createShellFolder(java.io.File) throws java.io.FileNotFoundException
public abstract sun.awt.shell.ShellFolder sun.awt.shell.ShellFolder.getLinkLocation() throws java.io.FileNotFoundException
public static sun.awt.shell.ShellFolder sun.awt.shell.ShellFolder.getShellFolder(java.io.File) throws java.io.FileNotFoundException
$
```

By adding a `, ?` to the `throws` clause, we include methods that throw other exceptions as well:
```shell
$ juggle throws java.io.FileNotFoundException, \?
public static String com.apple.eio.FileManager.findFolder(int) throws java.io.FileNotFoundException
public static String com.apple.eio.FileManager.findFolder(short,int) throws java.io.FileNotFoundException
public static String com.apple.eio.FileManager.findFolder(short,int,boolean) throws java.io.FileNotFoundException
public static String com.apple.eio.FileManager.getResource(String) throws java.io.FileNotFoundException
public static String com.apple.eio.FileManager.getResource(String,String) throws java.io.FileNotFoundException
public static String com.apple.eio.FileManager.getResource(String,String,String) throws java.io.FileNotFoundException
public static boolean com.apple.eio.FileManager.moveToTrash(java.io.File) throws java.io.FileNotFoundException
public static boolean com.apple.eio.FileManager.revealInFinder(java.io.File) throws java.io.FileNotFoundException
public static byte[] com.sun.org.apache.xml.internal.security.utils.JavaUtils.getBytesFromFile(String) throws java.io.FileNotFoundException,java.io.IOException
public com.sun.org.apache.xml.internal.security.utils.resolver.implementations.ResolverAnonymous.<init>(String) throws java.io.FileNotFoundException,java.io.IOException
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
public javax.imageio.stream.FileImageInputStream.<init>(java.io.File) throws java.io.FileNotFoundException,java.io.IOException
public javax.imageio.stream.FileImageOutputStream.<init>(java.io.File) throws java.io.FileNotFoundException,java.io.IOException
public sun.awt.shell.ShellFolder sun.awt.shell.ShellFolderManager.createShellFolder(java.io.File) throws java.io.FileNotFoundException
public abstract sun.awt.shell.ShellFolder sun.awt.shell.ShellFolder.getLinkLocation() throws java.io.FileNotFoundException
public static sun.awt.shell.ShellFolder sun.awt.shell.ShellFolder.getShellFolder(java.io.File) throws java.io.FileNotFoundException
public sun.rmi.log.ReliableLog.LogFile.<init>(String,String) throws java.io.FileNotFoundException,java.io.IOException
$
```

### [GitHub Issue #99](https://github.com/paul-bennett/juggle/issues/99): Exception on `juggle private`

Prior to fixing #99, this query was resulting in an uncaught exception.
```shell
$ juggle "private java.util.Optional /^lambda/"
private static java.util.Optional<T> java.util.stream.Collectors.lambda$reducing$40(java.util.stream.Collectors$1OptionalBox)
$
```

It may be the case that these methods will all be hidden from a future version of Juggle because they're private
lambdas (and therefore of no use to external code).

The exception was thrown when trying to emit the parameter type of the last of these,
i.e. `java.util.stream.Collectors$1OptionalBox`.

### [GitHub Issue #98](https://github.com/paul-bennett/juggle/issues/98): Access modifiers follow the usual pattern of specifying minimum accessibility:
```shell
$ juggle private java.net.Inet6Address 
public static java.net.Inet6Address java.net.Inet6Address.getByAddress(String,byte[],int) throws java.net.UnknownHostException
public static java.net.Inet6Address java.net.Inet6Address.getByAddress(String,byte[],java.net.NetworkInterface) throws java.net.UnknownHostException
java.net.Inet6Address.<init>()
java.net.Inet6Address.<init>(String,byte[])
java.net.Inet6Address.<init>(String,byte[],int)
java.net.Inet6Address.<init>(String,byte[],String) throws java.net.UnknownHostException
java.net.Inet6Address.<init>(String,byte[],java.net.NetworkInterface) throws java.net.UnknownHostException
final java.net.Inet6Address java.net.Inet6Address.Inet6AddressHolder.this$0
$
```

### [GitHub Issue #65](https://github.com/paul-bennett/juggle/issues/65): Handle ellipsis in parameter lists

For this issue we're going to focus on methods whose name ends with the word
`search` (ignoring case):

```shell
$ juggle '/search$/i'                                                
public com.apple.laf.AquaTextFieldSearch.<init>()
public javax.naming.NamingEnumeration<T> com.sun.jndi.dns.DnsContext.c_search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls,com.sun.jndi.toolkit.ctx.Continuation) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.dns.DnsContext.c_search(javax.naming.Name,String,javax.naming.directory.SearchControls,com.sun.jndi.toolkit.ctx.Continuation) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.dns.DnsContext.c_search(javax.naming.Name,javax.naming.directory.Attributes,String[],com.sun.jndi.toolkit.ctx.Continuation) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public com.sun.jndi.toolkit.dir.DirSearch.<init>()
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public static javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.DirSearch.search(javax.naming.directory.DirContext,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public static javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.DirSearch.search(javax.naming.directory.DirContext,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public static javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.DirSearch.search(javax.naming.directory.DirContext,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public final java.util.Iterator<E> com.sun.org.apache.bcel.internal.util.InstructionFinder.search(String)
public final java.util.Iterator<E> com.sun.org.apache.bcel.internal.util.InstructionFinder.search(String,com.sun.org.apache.bcel.internal.generic.InstructionHandle)
public final java.util.Iterator<E> com.sun.org.apache.bcel.internal.util.InstructionFinder.search(String,com.sun.org.apache.bcel.internal.generic.InstructionHandle,com.sun.org.apache.bcel.internal.util.InstructionFinder.CodeConstraint)
public final java.util.Iterator<E> com.sun.org.apache.bcel.internal.util.InstructionFinder.search(String,com.sun.org.apache.bcel.internal.util.InstructionFinder.CodeConstraint)
public String com.sun.org.apache.xml.internal.security.transforms.params.XPathFilterCHGPContainer.getExcludeButSearch()
public org.w3c.dom.Node com.sun.org.apache.xml.internal.security.transforms.params.XPathFilterCHGPContainer.getHereContextNodeExcludeButSearch()
public org.w3c.dom.Node com.sun.org.apache.xml.internal.security.transforms.params.XPathFilterCHGPContainer.getHereContextNodeIncludeButSearch()
public String com.sun.org.apache.xml.internal.security.transforms.params.XPathFilterCHGPContainer.getIncludeButSearch()
public int com.sun.org.apache.xml.internal.utils.IntStack.search(int)
public int com.sun.org.apache.xml.internal.utils.ObjectStack.search(Object)
public abstract void java.lang.instrument.Instrumentation.appendToBootstrapClassLoaderSearch(java.util.jar.JarFile)
public abstract void java.lang.instrument.Instrumentation.appendToSystemClassLoaderSearch(java.util.jar.JarFile)
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
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public void sun.instrument.InstrumentationImpl.appendToBootstrapClassLoaderSearch(java.util.jar.JarFile)
public void sun.instrument.InstrumentationImpl.appendToSystemClassLoaderSearch(java.util.jar.JarFile)
$
```


Omitting parentheses as above indicates that we don't want to filter on
parameters at all.  Including parentheses but nothing between them matches
zero-arg methods.  There are none that match the name filter in this case:

```shell
$ juggle '/search$/i ()'
public com.apple.laf.AquaTextFieldSearch.<init>()
public com.sun.jndi.toolkit.dir.DirSearch.<init>()
$
```

If we put a single ellipsis in the parameter list we're saying that we
want methods with zero or more parameters, so we get the same results
as when we omitted parentheses altogether:

```shell
$ juggle '/search$/i (...)'                                                
public com.apple.laf.AquaTextFieldSearch.<init>()
public javax.naming.NamingEnumeration<T> com.sun.jndi.dns.DnsContext.c_search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls,com.sun.jndi.toolkit.ctx.Continuation) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.dns.DnsContext.c_search(javax.naming.Name,String,javax.naming.directory.SearchControls,com.sun.jndi.toolkit.ctx.Continuation) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.dns.DnsContext.c_search(javax.naming.Name,javax.naming.directory.Attributes,String[],com.sun.jndi.toolkit.ctx.Continuation) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.ldap.LdapReferralContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.ctx.PartialCompositeDirContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public com.sun.jndi.toolkit.dir.DirSearch.<init>()
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.HierMemDirCtx.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public static javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.DirSearch.search(javax.naming.directory.DirContext,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public static javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.DirSearch.search(javax.naming.directory.DirContext,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public static javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.dir.DirSearch.search(javax.naming.directory.DirContext,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.toolkit.url.GenericURLDirContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> com.sun.jndi.url.ldap.ldapURLContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public final java.util.Iterator<E> com.sun.org.apache.bcel.internal.util.InstructionFinder.search(String)
public final java.util.Iterator<E> com.sun.org.apache.bcel.internal.util.InstructionFinder.search(String,com.sun.org.apache.bcel.internal.generic.InstructionHandle)
public final java.util.Iterator<E> com.sun.org.apache.bcel.internal.util.InstructionFinder.search(String,com.sun.org.apache.bcel.internal.generic.InstructionHandle,com.sun.org.apache.bcel.internal.util.InstructionFinder.CodeConstraint)
public final java.util.Iterator<E> com.sun.org.apache.bcel.internal.util.InstructionFinder.search(String,com.sun.org.apache.bcel.internal.util.InstructionFinder.CodeConstraint)
public String com.sun.org.apache.xml.internal.security.transforms.params.XPathFilterCHGPContainer.getExcludeButSearch()
public org.w3c.dom.Node com.sun.org.apache.xml.internal.security.transforms.params.XPathFilterCHGPContainer.getHereContextNodeExcludeButSearch()
public org.w3c.dom.Node com.sun.org.apache.xml.internal.security.transforms.params.XPathFilterCHGPContainer.getHereContextNodeIncludeButSearch()
public String com.sun.org.apache.xml.internal.security.transforms.params.XPathFilterCHGPContainer.getIncludeButSearch()
public int com.sun.org.apache.xml.internal.utils.IntStack.search(int)
public int com.sun.org.apache.xml.internal.utils.ObjectStack.search(Object)
public abstract void java.lang.instrument.Instrumentation.appendToBootstrapClassLoaderSearch(java.util.jar.JarFile)
public abstract void java.lang.instrument.Instrumentation.appendToSystemClassLoaderSearch(java.util.jar.JarFile)
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
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public abstract javax.naming.NamingEnumeration<T> javax.naming.directory.DirContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.directory.InitialDirContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(String,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(String,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(String,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(String,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(javax.naming.Name,String,Object[],javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(javax.naming.Name,String,javax.naming.directory.SearchControls) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(javax.naming.Name,javax.naming.directory.Attributes) throws javax.naming.NamingException
public javax.naming.NamingEnumeration<T> javax.naming.spi.ContinuationDirContext.search(javax.naming.Name,javax.naming.directory.Attributes,String[]) throws javax.naming.NamingException
public void sun.instrument.InstrumentationImpl.appendToBootstrapClassLoaderSearch(java.util.jar.JarFile)
public void sun.instrument.InstrumentationImpl.appendToSystemClassLoaderSearch(java.util.jar.JarFile)
$
```

Now let's just specify the first parameter. That drops us down to three candidates:
```shell
$ juggle '/search$/i (? extends java.util.Collection,...)'                                              
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T,java.util.Comparator<T>)
public synchronized int java.util.Stack<E>.search(Object)
$
```
Note how the last of these is a non-static member, so the "first"
parameter is actually the target class.

If we specify something outlandish as our first parameter we get no results:
```shell
$ juggle '/search$/i (java.net.InetAddress,...)'                                                
$
```

Here's the same but with the last parameter:
```shell
$ juggle '/search$/i (...,double)'                                                
public static int java.util.Arrays.binarySearch(double[],double)
public static int java.util.Arrays.binarySearch(double[],int,int,double)
public int com.sun.org.apache.xml.internal.utils.ObjectStack.search(Object)
public static int java.util.Arrays.binarySearch(Object[],int,int,Object)
public static int java.util.Arrays.binarySearch(Object[],Object)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T)
public synchronized int java.util.Stack<E>.search(Object)
$
```
```shell
$ juggle '/search$/i (..., String)'                                                
public final java.util.Iterator<E> com.sun.org.apache.bcel.internal.util.InstructionFinder.search(String)
public int com.sun.org.apache.xml.internal.utils.ObjectStack.search(Object)
public static int java.util.Arrays.binarySearch(Object[],int,int,Object)
public static int java.util.Arrays.binarySearch(Object[],Object)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T)
public synchronized int java.util.Stack<E>.search(Object)
$
```

Let's put the ellipsis in the middle, missing out all but the first and last arg:
```shell
$ juggle '/search$/i (java.util.List, ..., java.util.Comparator)'                                                
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T,java.util.Comparator<T>)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T)
$
```

And now the opposite: a param in the middle:
```shell
$ juggle '/search$/i (..., int, ...)'                                                
public int com.sun.org.apache.xml.internal.utils.IntStack.search(int)
public static int java.util.Arrays.binarySearch(byte[],int,int,byte)
public static int java.util.Arrays.binarySearch(char[],int,int,char)
public static int java.util.Arrays.binarySearch(int[],int)
public static int java.util.Arrays.binarySearch(int[],int,int,int)
public static int java.util.Arrays.binarySearch(short[],int,int,short)
public static int java.util.Arrays.binarySearch(double[],double)
public static int java.util.Arrays.binarySearch(double[],int,int,double)
public static int java.util.Arrays.binarySearch(float[],float)
public static int java.util.Arrays.binarySearch(float[],int,int,float)
public static int java.util.Arrays.binarySearch(long[],int,int,long)
public static int java.util.Arrays.binarySearch(long[],long)
public <U> U java.util.concurrent.ConcurrentHashMap<K,V>.search(long,java.util.function.BiFunction<T,U,R>)
public int com.sun.org.apache.xml.internal.utils.ObjectStack.search(Object)
public static int java.util.Arrays.binarySearch(Object[],int,int,Object)
public static <T> int java.util.Arrays.binarySearch(T[],int,int,T,java.util.Comparator<T>)
public static int java.util.Arrays.binarySearch(Object[],Object)
public static <T> int java.util.Arrays.binarySearch(T[],T,java.util.Comparator<T>)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T)
public static <T> int java.util.Collections.binarySearch(java.util.List<E>,T,java.util.Comparator<T>)
public synchronized int java.util.Stack<E>.search(Object)
$
```

Finally, ellipses all over the place:
```shell
$ juggle '/search$/i (..., long[], ..., int, ...)'                                                
public static int java.util.Arrays.binarySearch(long[],int,int,long)
public static int java.util.Arrays.binarySearch(long[],long)
$
```
(The second result here is included because `int` can be promoted to `long`.)


### [GitHub Issue #48](https://github.com/paul-bennett/juggle/issues/48): Implemented-By Index

Juggle can show you all classes that directly implement a specific interface:
```shell
$ juggle -c none class implements java.lang.reflect.Member                  
public abstract class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration
public final class java.lang.reflect.Field extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member
$
```

To show classes that indirectly implement an interface, allow conversions:
```shell
$ juggle class implements java.lang.reflect.Member
public abstract class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration
public final class java.lang.reflect.Constructor<T> extends java.lang.reflect.Executable
public final class java.lang.reflect.Field extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member
public final class java.lang.reflect.Method extends java.lang.reflect.Executable
$
```

Juggle doesn't presently offer a mechanism to list all classes that
directly or indirectly implement an interface.
(See [GitHub Issue #84](https://github.com/paul-bennett/juggle/issues/84).)

### [GitHub issue #47](https://github.com/paul-bennett/juggle/issues/47): Subclass Index

Here's how to find the direct subclasses of a class:
```shell
$ juggle -c none class extends java.lang.reflect.AccessibleObject
public abstract class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration
public final class java.lang.reflect.Field extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member
$
```

`java.lang.reflect.Executable` itself has two subclasses that weren't listed
above because they're _indirect_ subclasses of `AccessibleObject`:
```shell
$ juggle class extends java.lang.reflect.Executable
public final class java.lang.reflect.Constructor<T> extends java.lang.reflect.Executable
public final class java.lang.reflect.Method extends java.lang.reflect.Executable
$
```

To show all subclasses, enable conversions (`-c auto` would do the same here):
```shell
$ juggle -c all class extends java.lang.reflect.AccessibleObject
public abstract class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration
public final class java.lang.reflect.Field extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member
public final class java.lang.reflect.Constructor<T> extends java.lang.reflect.Executable
public final class java.lang.reflect.Method extends java.lang.reflect.Executable
$
```

### [GitHub Issue #45](https://github.com/paul-bennett/juggle/issues/45): JavaNut 27 (Class Defined-In Index)

Juggle now answers this question directly:
```shell
$ juggle class FileNotFoundException
public class java.io.FileNotFoundException extends java.io.IOException
$
```

### [GitHub Issue #74](https://github.com/paul-bennett/juggle/issues/74): Search by exact name is broken (substring)

There are no methods in the JDK called `Equals`:
```shell
$ juggle '? Equals'
$
```

But there are some that contain the word `Equals`:
```shell
$ juggle '? /Equals/'
public boolean String.contentEquals(CharSequence)
public boolean String.contentEquals(StringBuffer)
public boolean com.sun.corba.se.impl.io.ObjectStreamField.typeEquals(com.sun.corba.se.impl.io.ObjectStreamField)
public boolean com.sun.corba.se.impl.orbutil.ObjectStreamField.typeEquals(com.sun.corba.se.impl.orbutil.ObjectStreamField)
public final boolean com.sun.java.util.jar.pack.ConstantPool.Entry.tagEquals(int)
public boolean com.sun.jmx.remote.internal.ProxyRef.remoteEquals(java.rmi.server.RemoteRef)
public boolean com.sun.org.apache.regexp.internal.RETestCase.assertEquals(StringBuffer,String,int,int)
public boolean com.sun.org.apache.regexp.internal.RETestCase.assertEquals(StringBuffer,String,String,String)
public void com.sun.org.apache.regexp.internal.RETest.assertEquals(String,int,int)
public void com.sun.org.apache.regexp.internal.RETest.assertEquals(String,String,String)
public abstract boolean com.sun.org.apache.xpath.internal.Expression.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.axes.AxesWalker.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.axes.DescendantIterator.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.axes.FilterExprIterator.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.axes.FilterExprIteratorSimple.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.axes.FilterExprWalker.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.axes.OneStepIterator.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.axes.OneStepIteratorForward.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.axes.UnionPathIterator.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.axes.WalkingIterator.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.functions.Function.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.functions.Function2Args.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.functions.Function3Args.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.functions.FunctionMultiArgs.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.functions.FunctionOneArg.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.objects.XObject.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.objects.XNodeSet.notEquals(com.sun.org.apache.xpath.internal.objects.XObject) throws javax.xml.transform.TransformerException
public boolean com.sun.org.apache.xpath.internal.objects.XObject.notEquals(com.sun.org.apache.xpath.internal.objects.XObject) throws javax.xml.transform.TransformerException
public boolean com.sun.org.apache.xpath.internal.operations.Equals.bool(com.sun.org.apache.xpath.internal.XPathContext) throws javax.xml.transform.TransformerException
public com.sun.org.apache.xpath.internal.operations.Equals.<init>()
public com.sun.org.apache.xpath.internal.operations.NotEquals.<init>()
public boolean com.sun.org.apache.xpath.internal.operations.Operation.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.operations.UnaryOperation.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.operations.Variable.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public com.sun.org.apache.xpath.internal.objects.XObject com.sun.org.apache.xpath.internal.operations.Equals.operate(com.sun.org.apache.xpath.internal.objects.XObject,com.sun.org.apache.xpath.internal.objects.XObject) throws javax.xml.transform.TransformerException
public com.sun.org.apache.xpath.internal.objects.XObject com.sun.org.apache.xpath.internal.operations.NotEquals.operate(com.sun.org.apache.xpath.internal.objects.XObject,com.sun.org.apache.xpath.internal.objects.XObject) throws javax.xml.transform.TransformerException
public boolean com.sun.org.apache.xpath.internal.patterns.NodeTest.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.patterns.StepPattern.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public boolean com.sun.org.apache.xpath.internal.patterns.UnionPattern.deepEquals(com.sun.org.apache.xpath.internal.Expression)
public abstract boolean java.rmi.server.RemoteRef.remoteEquals(java.rmi.server.RemoteRef)
public static boolean java.util.Arrays.deepEquals(Object[],Object[])
public static boolean java.util.Objects.deepEquals(Object,Object)
public abstract boolean javax.lang.model.element.Name.contentEquals(CharSequence)
public boolean javax.print.attribute.ResolutionSyntax.lessThanOrEquals(javax.print.attribute.ResolutionSyntax)
public boolean javax.swing.text.ChangedCharSetException.keyEqualsCharSet()
public static boolean sun.lwawt.macosx.LWCToolkit.doEquals(Object,Object,java.awt.Component)
public boolean sun.rmi.server.ActivatableRef.remoteEquals(java.rmi.server.RemoteRef)
public boolean sun.rmi.server.UnicastRef.remoteEquals(java.rmi.server.RemoteRef)
public boolean sun.rmi.transport.LiveRef.remoteEquals(Object)
$
```

Similarly, there's only one class called `StringBuffer`:
```shell
$ juggle 'class StringBuffer'
public final class StringBuffer extends AbstractStringBuilder implements java.io.Serializable, CharSequence
$
```
But there are two that contain the letters `StringBuffer`:
```shell
$ juggle 'class /StringBuffer/'
public final class StringBuffer extends AbstractStringBuilder implements java.io.Serializable, CharSequence
public class com.sun.org.apache.xerces.internal.util.XMLStringBuffer extends com.sun.org.apache.xerces.internal.xni.XMLString
public class com.sun.org.apache.xml.internal.utils.FastStringBuffer
public class com.sun.org.apache.xml.internal.utils.StringBufferPool
public class java.io.StringBufferInputStream extends java.io.InputStream
$
```

(Prior to fixing this issue, string literals were interpreted as
case-sensitive substring matches.)

### [GitHub issue #70](https://github.com/paul-bennett/juggle/issues/70): Add `transient` and `volatile` member modifiers

There are about 400 `transient` methods in the JDK. Here are a few.
```shell
$ juggle -i java.util.stream -i java.nio.file transient Stream
public static transient <T> Stream<T> Stream<T>.of(T[])
public static transient Stream<T> Files.find(Path,int,java.util.function.BiPredicate<T,U>,FileVisitOption[]) throws java.io.IOException
public static transient Stream<T> Files.walk(Path,int,FileVisitOption[]) throws java.io.IOException
public static transient Stream<T> Files.walk(Path,FileVisitOption[]) throws java.io.IOException
$
```
And here are some of the 2000 `volatile` methods:
```shell
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
public volatile AbstractStringBuilder StringBuilder.deleteCharAt(int)
public volatile void StringBuilder.ensureCapacity(int)
public volatile void StringBuilder.setLength(int)
public volatile String StringBuilder.substring(int)
public volatile Appendable AbstractStringBuilder.append(char) throws java.io.IOException
public volatile Appendable AbstractStringBuilder.append(CharSequence) throws java.io.IOException
$
```

### [GitHub issue #32](https://github.com/paul-bennett/juggle/issues/32)

Results aren't deduplicated

```shell
# juggle -n asSubclass -m java.base,java.base
$ juggle /asSubclass/ -m java.base,java.base
public <U> Class<T> Class<T>.asSubclass(Class<T>)
$
```

### [GitHub issue #16](https://github.com/paul-bennett/juggle/issues/32)

Support module "implied readability"

Prior to fixing this issue, specifying a module using `-m` would examine the classes
directly defined within the module, but not any classes from modules which it requires
transitively.

For example, the `java.se` module requires 20 modules transitively, including `java.sql`.
So the following two executions should return the same results.  (Prior to the fix,
the second -- `-m java.se` -- showed no results.)

```shell
# juggle -m java.sql -i java.sql -r ResultSet -p PreparedStatement
$ juggle -m java.sql -i java.sql ResultSet '(? super PreparedStatement)'
public abstract ResultSet PreparedStatement.executeQuery() throws SQLException
public abstract ResultSet Statement.getGeneratedKeys() throws SQLException
public abstract ResultSet Statement.getResultSet() throws SQLException
$
```

```shell
# juggle -m java.se -i java.sql -r ResultSet -p PreparedStatement
$ juggle -m java.se -i java.sql ResultSet '(? super PreparedStatement)'
public abstract ResultSet PreparedStatement.executeQuery() throws SQLException
public abstract ResultSet Statement.getGeneratedKeys() throws SQLException
public abstract ResultSet Statement.getResultSet() throws SQLException
$
```

### [GitHub issue #1](https://github.com/paul-bennett/juggle/issues/1)

Searching (with -p or -r) for an array of a primitive type falls back to Object

```shell
# juggle -p double[],int,int,double -r void
$ juggle void '(double[],int,int,double)'
public static void java.util.Arrays.fill(double[],int,int,double)
public static void com.sun.media.sound.EmergencySoundbank.complexGaussianDist(double[],double,double,double)
$
```
