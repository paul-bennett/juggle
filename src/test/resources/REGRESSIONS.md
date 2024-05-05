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

### [GitHub Issue #116](https://github.com/paul-bennett/juggle/issues/116): Output of generics is sometimes wrong

Fixing this issue changed the output of many Juggle queries,
so rather than list dozens of regressions, here's a corner
that needed to be mopped up at the very end.

First, Juggle was not correctly filtering classes used in wildcard
types for imported packages
```shell
$ juggle -i java.util.stream -i java.util.function \
    /java.util.stream.Stream.mapMulti\$/
public default <R> Stream<R> Stream<T>.mapMulti(BiConsumer<? super T,? super Consumer<R>>)
$
```

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
public abstract interface java.security.Certificate
public abstract interface java.security.DomainCombiner
public abstract static interface java.security.Policy.Parameters
public abstract interface java.util.Observer
$
```

### [GitHub Issue #102](https://github.com/paul-bennett/juggle/issues/102): Add receiver parameter support

It's now possible to list just non-static members by specifying the name
of the first parameter as `this`:
```shell
$ juggle -c none '(java.util.function.Function this,...)'
public default <V> java.util.function.Function<T,V> java.util.function.Function<T,R>.andThen(java.util.function.Function<? super R,? extends V>)
public abstract R java.util.function.Function<T,R>.apply(T)
public default <V> java.util.function.Function<V,R> java.util.function.Function<T,R>.compose(java.util.function.Function<? super V,? extends T>)
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
public final class StringBuilder extends AbstractStringBuilder implements java.io.Serializable, Comparable<T>, CharSequence
public class Object
$
```

That doesn't look right! Where's the `AbstractStringBuilder` class?

Turns out it's not `public`, so Juggle filtered it out.  Let's try again: 
```shell
$ juggle private class super StringBuilder
public final class StringBuilder extends AbstractStringBuilder implements java.io.Serializable, Comparable<T>, CharSequence
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
public abstract interface Comparable<T>
public abstract interface java.io.Serializable
$
```

As you might expect we can limit the output to direct superinterfaces only by
turning off conversions:
```shell
$ juggle -c none interface super StringBuilder
public abstract interface CharSequence
public abstract interface Comparable<T>
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
public abstract sealed class Executable extends AccessibleObject implements Member, GenericDeclaration permits Constructor<T>, Method
public final class Field extends AccessibleObject implements Member
public final class Constructor<T> extends Executable
public final class Method extends Executable
$
```

As expected, omitting `--conversions` is equivalent to `--conversions=auto`:
```shell
$ juggle --conversions=auto -i java.lang.reflect class extends AccessibleObject
public abstract sealed class Executable extends AccessibleObject implements Member, GenericDeclaration permits Constructor<T>, Method
public final class Field extends AccessibleObject implements Member
public final class Constructor<T> extends Executable
public final class Method extends Executable
$
```

And because we've not used any wildcards in the query, that's the same as
`--conversions=all`:
```shell
$ juggle --conversions=all -i java.lang.reflect class extends AccessibleObject
public abstract sealed class Executable extends AccessibleObject implements Member, GenericDeclaration permits Constructor<T>, Method
public final class Field extends AccessibleObject implements Member
public final class Constructor<T> extends Executable
public final class Method extends Executable
$
```

Specifying `--conversions=none` just matches classes that literally extend
`AccessibleObject`:
```shell
$ juggle --conversions=none -i java.lang.reflect class extends AccessibleObject
public abstract sealed class Executable extends AccessibleObject implements Member, GenericDeclaration permits Constructor<T>, Method
public final class Field extends AccessibleObject implements Member
$
```

The original clumsy syntax no longer just shows indirect subclasses, but I
don't consider that to be a great loss:
```shell
$ juggle -i java.lang.reflect class extends \? extends AccessibleObject
public abstract sealed class Executable extends AccessibleObject implements Member, GenericDeclaration permits Constructor<T>, Method
public final class Field extends AccessibleObject implements Member
public final class Constructor<T> extends Executable
public final class Method extends Executable
$
```
Because the query has a bounded wildcard, that's the same as
`--conversions=none`:
```shell
$ juggle -c none -i java.lang.reflect class extends \? extends AccessibleObject
public abstract sealed class Executable extends AccessibleObject implements Member, GenericDeclaration permits Constructor<T>, Method
public final class Field extends AccessibleObject implements Member
public final class Constructor<T> extends Executable
public final class Method extends Executable
$
```
```shell
$ juggle -c all -i java.lang.reflect class extends AccessibleObject
public abstract sealed class Executable extends AccessibleObject implements Member, GenericDeclaration permits Constructor<T>, Method
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
$
```

Now all indirect descendents too:
```shell
$ juggle -i java.util interface extends Collection
public abstract interface Deque<E> implements Queue<E>
public abstract interface List<E> implements Collection<E>
public abstract interface NavigableSet<E> implements SortedSet<E>
public abstract interface Queue<E> implements Collection<E>
public abstract interface Set<E> implements Collection<E>
public abstract interface SortedSet<E> implements Set<E>
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
public class java.util.concurrent.CopyOnWriteArrayList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable
$
```


### [GitHub Issue #86](https://github.com/paul-bennett/juggle/issues/86): Add support for sealed and non-sealed modifiers

There's a handful of `sealed` interfaces in JDK 17:
```shell
$ juggle sealed interface
public abstract sealed interface java.lang.constant.ConstantDesc permits java.lang.constant.ClassDesc, java.lang.constant.MethodHandleDesc, java.lang.constant.MethodTypeDesc, Double, java.lang.constant.DynamicConstantDesc<T>, Float, Integer, Long, String
public abstract sealed interface java.lang.constant.ClassDesc implements java.lang.constant.ConstantDesc, java.lang.invoke.TypeDescriptor.OfField<F> permits java.lang.constant.PrimitiveClassDescImpl, java.lang.constant.ReferenceClassDescImpl
public abstract sealed interface java.lang.constant.MethodHandleDesc implements java.lang.constant.ConstantDesc permits java.lang.constant.AsTypeMethodHandleDesc, java.lang.constant.DirectMethodHandleDesc
public abstract sealed interface java.lang.constant.DirectMethodHandleDesc implements java.lang.constant.MethodHandleDesc permits java.lang.constant.DirectMethodHandleDescImpl
public abstract sealed interface java.lang.constant.MethodTypeDesc implements java.lang.constant.ConstantDesc, java.lang.invoke.TypeDescriptor.OfMethod<F,M> permits java.lang.constant.MethodTypeDescImpl
$
```

And there's only one `non-sealed` class:
```shell
$ juggle non-sealed class
public abstract non-sealed class java.lang.constant.DynamicConstantDesc<T> implements java.lang.constant.ConstantDesc
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
$
```

This is the same as asking for `all` conversions:
```shell
$ juggle --conversions=all 'CharSequence (String,int,int)'
public CharSequence String.subSequence(int,int)
public abstract CharSequence CharSequence.subSequence(int,int)
public String String.substring(int,int)
public static java.nio.CharBuffer java.nio.CharBuffer.wrap(CharSequence,int,int)
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
public int String.codePointAt(int)
public int String.codePointBefore(int)
public static Integer Integer.getInteger(String,Integer)
public int String.indexOf(int)
public int String.lastIndexOf(int)
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
public static int Character.codePointAt(CharSequence,int)
public static int Character.codePointBefore(CharSequence,int)
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public volatile int String.compareTo(Object)
public abstract int Comparable<T>.compareTo(T)
public static int WeakPairMap.Pair<K1,K2>.hashCode(Object,Object)
$
```
```shell
$ juggle -c=auto 'Integer(String,int)'
public static Integer Integer.getInteger(String,int)
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public int String.codePointAt(int)
public int String.codePointBefore(int)
public static Integer Integer.getInteger(String,Integer)
public int String.indexOf(int)
public int String.lastIndexOf(int)
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
public static int Character.codePointAt(CharSequence,int)
public static int Character.codePointBefore(CharSequence,int)
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public volatile int String.compareTo(Object)
public abstract int Comparable<T>.compareTo(T)
public static int WeakPairMap.Pair<K1,K2>.hashCode(Object,Object)
$
```
```shell
$ juggle -c=all 'Integer(String,int)'
public static Integer Integer.getInteger(String,int)
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public int String.codePointAt(int)
public int String.codePointBefore(int)
public static Integer Integer.getInteger(String,Integer)
public int String.indexOf(int)
public int String.lastIndexOf(int)
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
public static int Character.codePointAt(CharSequence,int)
public static int Character.codePointBefore(CharSequence,int)
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public volatile int String.compareTo(Object)
public abstract int Comparable<T>.compareTo(T)
public static int WeakPairMap.Pair<K1,K2>.hashCode(Object,Object)
$
```

If we tell Juggle not to apply any conversions, boxing is disabled as well.
(This is equivalent to pre-fix Juggle.)
```shell
$ juggle -c=none 'Integer(String,int)'
public static Integer Integer.getInteger(String,int)
public static Integer Integer.valueOf(String,int) throws NumberFormatException
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
public java.lang.reflect.Constructor<?> Class<T>.getEnclosingConstructor() throws SecurityException
public java.lang.reflect.Method Class<T>.getEnclosingMethod() throws SecurityException
public java.lang.reflect.Method java.lang.annotation.AnnotationTypeMismatchException.element()
public java.lang.reflect.Method java.lang.reflect.RecordComponent.getAccessor()
$
```

Conversions also apply to exceptions. We used to have to include upper
bounds explicitly:
```shell
$ juggle '? encode throws ? extends java.io.IOException'
public static String java.net.URLEncoder.encode(String,String) throws java.io.UnsupportedEncodingException
public final java.nio.ByteBuffer java.nio.charset.CharsetEncoder.encode(java.nio.CharBuffer) throws java.nio.charset.CharacterCodingException
public abstract void java.security.cert.Extension.encode(java.io.OutputStream) throws java.io.IOException
$
```

But now, with `-c auto`, the bounds are set implicitly:
```shell
$ juggle '? encode throws java.io.IOException' 
public static String java.net.URLEncoder.encode(String,String) throws java.io.UnsupportedEncodingException
public final java.nio.ByteBuffer java.nio.charset.CharsetEncoder.encode(java.nio.CharBuffer) throws java.nio.charset.CharacterCodingException
public abstract void java.security.cert.Extension.encode(java.io.OutputStream) throws java.io.IOException
$
```

To show just the methods that throw a specific exception we need `-c none`:
```shell
$ juggle -c none '? encode throws java.io.IOException' 
public abstract void java.security.cert.Extension.encode(java.io.OutputStream) throws java.io.IOException
$
```


### [GitHub Issue #109](https://github.com/paul-bennett/juggle/issues/109): Reintroduce boxing conversions

Here's a few functions that could take four `int`s:
```shell
$ juggle '(int,int,int,int)'
public static java.time.LocalTime java.time.LocalTime.of(int,int,int,int)
public java.util.IntSummaryStatistics.<init>(long,int,int,long) throws IllegalArgumentException
public static java.time.temporal.ValueRange java.time.temporal.ValueRange.of(long,long,long,long)
public java.util.DoubleSummaryStatistics.<init>(long,double,double,double) throws IllegalArgumentException
public java.util.LongSummaryStatistics.<init>(long,long,long,long) throws IllegalArgumentException
public static <E> java.util.List<E> java.util.List<E>.of(E,E,E,E)
public static <K,V> java.util.Map<K,V> java.util.Map<K,V>.of(K,V,K,V)
public static <E> java.util.Set<E> java.util.Set<E>.of(E,E,E,E)
$
```

If we search for `Integer`s (or a mix of both types) we should get the same
thing:

```shell
$ juggle '(int,Integer,int,Integer)'
public static java.time.LocalTime java.time.LocalTime.of(int,int,int,int)
public java.util.IntSummaryStatistics.<init>(long,int,int,long) throws IllegalArgumentException
public static java.time.temporal.ValueRange java.time.temporal.ValueRange.of(long,long,long,long)
public java.util.DoubleSummaryStatistics.<init>(long,double,double,double) throws IllegalArgumentException
public java.util.LongSummaryStatistics.<init>(long,long,long,long) throws IllegalArgumentException
public static <E> java.util.List<E> java.util.List<E>.of(E,E,E,E)
public static <K,V> java.util.Map<K,V> java.util.Map<K,V>.of(K,V,K,V)
public static <E> java.util.Set<E> java.util.Set<E>.of(E,E,E,E)
$
```

Similarly, we should be able to go in the other direction too:
```shell
$ juggle 'int (Integer,Integer)'
public int Integer.compareTo(Integer)
public volatile int Integer.compareTo(Object)
public abstract int Comparable<T>.compareTo(T)
public static int WeakPairMap.Pair<K1,K2>.hashCode(Object,Object)
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
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
public static native byte java.lang.reflect.Array.getByte(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native char java.lang.reflect.Array.getChar(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native short java.lang.reflect.Array.getShort(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static int java.util.Objects.checkIndex(int,int)
public static int Math.floorMod(long,int)
public static int StrictMath.floorMod(long,int)
public static char Character.forDigit(int,int)
public static int Double.compare(double,double)
public static int Float.compare(float,float)
public static int Long.compare(long,long)
public static int Long.compareUnsigned(long,long)
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
public static int java.util.Objects.checkIndex(int,int)
public static int Math.floorMod(long,int)
public static int StrictMath.floorMod(long,int)
public static char Character.forDigit(int,int)
public static int Double.compare(double,double)
public static int Float.compare(float,float)
public static int Long.compare(long,long)
public static int Long.compareUnsigned(long,long)
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public int Integer.compareTo(Integer)
public static native byte java.lang.reflect.Array.getByte(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native char java.lang.reflect.Array.getChar(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native short java.lang.reflect.Array.getShort(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public volatile int Integer.compareTo(Object)
public abstract int Comparable<T>.compareTo(T)
public static int WeakPairMap.Pair<K1,K2>.hashCode(Object,Object)
$
```

Finally, we the conversions should also apply for return types:
```shell
$ juggle 'Integer(String,int)'
public static Integer Integer.getInteger(String,int)
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public int String.codePointAt(int)
public int String.codePointBefore(int)
public static Integer Integer.getInteger(String,Integer)
public int String.indexOf(int)
public int String.lastIndexOf(int)
public static int Integer.parseInt(String,int) throws NumberFormatException
public static int Integer.parseUnsignedInt(String,int) throws NumberFormatException
public static int Character.codePointAt(CharSequence,int)
public static int Character.codePointBefore(CharSequence,int)
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public volatile int String.compareTo(Object)
public abstract int Comparable<T>.compareTo(T)
public static int WeakPairMap.Pair<K1,K2>.hashCode(Object,Object)
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
public char String.charAt(int)
public static int Character.codePointAt(CharSequence,int)
public static int Character.codePointBefore(CharSequence,int)
public static byte Byte.parseByte(String,int) throws NumberFormatException
public static short Short.parseShort(String,int) throws NumberFormatException
public static native int java.lang.reflect.Array.getInt(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public abstract char CharSequence.charAt(int)
public static Integer Integer.getInteger(String,int)
public static Integer Integer.valueOf(String,int) throws NumberFormatException
public static native byte java.lang.reflect.Array.getByte(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native char java.lang.reflect.Array.getChar(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public static native short java.lang.reflect.Array.getShort(Object,int) throws IllegalArgumentException,ArrayIndexOutOfBoundsException
public volatile int String.compareTo(Object)
public static Byte Byte.valueOf(String,int) throws NumberFormatException
public static Short Short.valueOf(String,int) throws NumberFormatException
public abstract int Comparable<T>.compareTo(T)
public static Integer Integer.getInteger(String,Integer)
public static int WeakPairMap.Pair<K1,K2>.hashCode(Object,Object)
$
```

### [GitHub Issue #105](https://github.com/paul-bennett/juggle/issues/105): `-s package` should sort by package name

Modified `package` comparator now sorts packages alphabetically if they 
weren't mentioned in the import list, and the implicit `java.lang` has
been moved to the end of that list rather than the start.
```shell
$ juggle -i java.net class /Class/
public class java.security.SecureClassLoader extends ClassLoader
public class URLClassLoader extends java.security.SecureClassLoader implements java.io.Closeable
public final class Class<T> implements java.io.Serializable, java.lang.reflect.GenericDeclaration, java.lang.reflect.Type, java.lang.reflect.AnnotatedElement, java.lang.invoke.TypeDescriptor.OfField<F>, java.lang.constant.Constable
public class ClassCastException extends RuntimeException
public class ClassCircularityError extends LinkageError
public class ClassFormatError extends LinkageError
public abstract class ClassLoader
public class ClassNotFoundException extends ReflectiveOperationException
public abstract class ClassValue<T>
public class IncompatibleClassChangeError extends LinkageError
public class NoClassDefFoundError extends LinkageError
public class UnsupportedClassVersionError extends ClassFormatError
public class java.io.InvalidClassException extends java.io.ObjectStreamException
public class java.io.ObjectStreamClass implements java.io.Serializable
public class java.lang.invoke.ClassSpecializer.Factory
public abstract class java.lang.invoke.ClassSpecializer.SpeciesData
$
```

### [GitHub Issue #72](https://github.com/paul-bennett/juggle/issues/72): Don't show JDK implementation classes

Prior to fixing, this used to include two further results from non-exported packages
in `jdk.internals.*`:
```shell
$ juggle '(int,int,int,int)'
public static java.time.LocalTime java.time.LocalTime.of(int,int,int,int)
public java.util.IntSummaryStatistics.<init>(long,int,int,long) throws IllegalArgumentException
public static java.time.temporal.ValueRange java.time.temporal.ValueRange.of(long,long,long,long)
public java.util.DoubleSummaryStatistics.<init>(long,double,double,double) throws IllegalArgumentException
public java.util.LongSummaryStatistics.<init>(long,long,long,long) throws IllegalArgumentException
public static <E> java.util.List<E> java.util.List<E>.of(E,E,E,E)
public static <K,V> java.util.Map<K,V> java.util.Map<K,V>.of(K,V,K,V)
public static <E> java.util.Set<E> java.util.Set<E>.of(E,E,E,E)
$
```

### [GitHub Issue #85](https://github.com/paul-bennett/juggle/issues/85): Handle nested classes in queries

Prior to fixing, this used to throw an exception saying that it couldn't
find the `Authenticator.RequestorType` class.
```shell
$ juggle -i java.net 'PasswordAuthentication (Authenticator,String,InetAddress,int,String,String,String,URL,Authenticator.RequestorType)'
public static PasswordAuthentication Authenticator.requestPasswordAuthentication(Authenticator,String,InetAddress,int,String,String,String,URL,Authenticator.RequestorType)
public PasswordAuthentication Authenticator.requestPasswordAuthenticationInstance(String,InetAddress,int,String,String,String,URL,Authenticator.RequestorType)
$
```

### [GitHub Issue #62](https://github.com/paul-bennett/juggle/issues/62): Add ellipsis support to throws clauses

We don't need to implement this because using a wildcard in the `throws` clause does the trick.

Here are the methods that only throw `FileNotFoundException`:
```shell
$ juggle throws java.io.FileNotFoundException
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
$
```

By adding a `, ?` to the `throws` clause, we include methods that throw other exceptions as well:
```shell
$ juggle throws java.io.FileNotFoundException, \?
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
$
```

### [GitHub Issue #99](https://github.com/paul-bennett/juggle/issues/99): Exception on `juggle private`

Prior to fixing #99, this query was resulting in an uncaught exception.
```shell
$ juggle "private java.util.Optional /^lambda/"
private static java.util.Optional<T> java.util.Currency.lambda$getValidCurrencyData$0(java.util.Properties,java.util.regex.Pattern,String)
private static java.util.Optional<T> java.util.spi.ToolProvider.lambda$findFirst$1(ClassLoader,String)
private static java.util.Optional<T> java.util.stream.Collectors.lambda$reducing$48(java.util.stream.Collectors$1OptionalBox)
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
$
```

### [GitHub Issue #65](https://github.com/paul-bennett/juggle/issues/65): Handle ellipsis in parameter lists

For this issue we're going to focus on methods whose name ends with the word
`search` (ignoring case):

```shell
$ juggle '/search$/i'                                                
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
public static <T> int java.util.Arrays.binarySearch(T[],int,int,T,java.util.Comparator<? super T>)
public static int java.util.Arrays.binarySearch(Object[],Object)
public static <T> int java.util.Arrays.binarySearch(T[],T,java.util.Comparator<? super T>)
public static int java.util.Arrays.binarySearch(long[],int,int,long)
public static int java.util.Arrays.binarySearch(long[],long)
public static int java.util.Arrays.binarySearch(short[],int,int,short)
public static int java.util.Arrays.binarySearch(short[],short)
public static <T> int java.util.Collections.binarySearch(java.util.List<? extends Comparable<? super T>>,T)
public static <T> int java.util.Collections.binarySearch(java.util.List<? extends T>,T,java.util.Comparator<? super T>)
public synchronized int java.util.Stack<E>.search(Object)
public <U> U java.util.concurrent.ConcurrentHashMap<K,V>.search(long,java.util.function.BiFunction<? super K,? super V,? extends U>)
$
```


Omitting parentheses as above indicates that we don't want to filter on
parameters at all.  Including parentheses but nothing between them matches
zero-arg methods.  There are none that match the name filter in this case:

```shell
$ juggle '/search$/i ()'
$
```

If we put a single ellipsis in the parameter list we're saying that we
want methods with zero or more parameters, so we get the same results
as when we omitted parentheses altogether:

```shell
$ juggle '/search$/i (...)'                                                
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
public static <T> int java.util.Arrays.binarySearch(T[],int,int,T,java.util.Comparator<? super T>)
public static int java.util.Arrays.binarySearch(Object[],Object)
public static <T> int java.util.Arrays.binarySearch(T[],T,java.util.Comparator<? super T>)
public static int java.util.Arrays.binarySearch(long[],int,int,long)
public static int java.util.Arrays.binarySearch(long[],long)
public static int java.util.Arrays.binarySearch(short[],int,int,short)
public static int java.util.Arrays.binarySearch(short[],short)
public static <T> int java.util.Collections.binarySearch(java.util.List<? extends Comparable<? super T>>,T)
public static <T> int java.util.Collections.binarySearch(java.util.List<? extends T>,T,java.util.Comparator<? super T>)
public synchronized int java.util.Stack<E>.search(Object)
public <U> U java.util.concurrent.ConcurrentHashMap<K,V>.search(long,java.util.function.BiFunction<? super K,? super V,? extends U>)
$
```

Now let's just specify the first parameter. That drops us down to three candidates:
```shell
$ juggle '/search$/i (? extends java.util.Collection,...)'                                              
public static <T> int java.util.Collections.binarySearch(java.util.List<? extends Comparable<? super T>>,T)
public static <T> int java.util.Collections.binarySearch(java.util.List<? extends T>,T,java.util.Comparator<? super T>)
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
public static int java.util.Arrays.binarySearch(Object[],int,int,Object)
public static int java.util.Arrays.binarySearch(Object[],Object)
public static <T> int java.util.Collections.binarySearch(java.util.List<? extends Comparable<? super T>>,T)
public synchronized int java.util.Stack<E>.search(Object)
$
```
```shell
$ juggle '/search$/i (..., String)'                                                
public static int java.util.Arrays.binarySearch(Object[],int,int,Object)
public static int java.util.Arrays.binarySearch(Object[],Object)
public static <T> int java.util.Collections.binarySearch(java.util.List<? extends Comparable<? super T>>,T)
public synchronized int java.util.Stack<E>.search(Object)
$
```

Let's put the ellipsis in the middle, missing out all but the first and last arg:
```shell
$ juggle '/search$/i (java.util.List, ..., java.util.Comparator)'                                                
public static <T> int java.util.Collections.binarySearch(java.util.List<? extends T>,T,java.util.Comparator<? super T>)
public static <T> int java.util.Collections.binarySearch(java.util.List<? extends Comparable<? super T>>,T)
$
```

And now the opposite: a param in the middle:
```shell
$ juggle '/search$/i (..., int, ...)'                                                
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
public <U> U java.util.concurrent.ConcurrentHashMap<K,V>.search(long,java.util.function.BiFunction<? super K,? super V,? extends U>)
public static int java.util.Arrays.binarySearch(Object[],int,int,Object)
public static <T> int java.util.Arrays.binarySearch(T[],int,int,T,java.util.Comparator<? super T>)
public static int java.util.Arrays.binarySearch(Object[],Object)
public static <T> int java.util.Arrays.binarySearch(T[],T,java.util.Comparator<? super T>)
public static <T> int java.util.Collections.binarySearch(java.util.List<? extends Comparable<? super T>>,T)
public static <T> int java.util.Collections.binarySearch(java.util.List<? extends T>,T,java.util.Comparator<? super T>)
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
public abstract sealed class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration permits java.lang.reflect.Constructor<T>, java.lang.reflect.Method
public final class java.lang.reflect.Field extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member
$
```

To show classes that indirectly implement an interface, allow conversions:
```shell
$ juggle class implements java.lang.reflect.Member
public abstract sealed class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration permits java.lang.reflect.Constructor<T>, java.lang.reflect.Method
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
public abstract sealed class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration permits java.lang.reflect.Constructor<T>, java.lang.reflect.Method
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
public abstract sealed class java.lang.reflect.Executable extends java.lang.reflect.AccessibleObject implements java.lang.reflect.Member, java.lang.reflect.GenericDeclaration permits java.lang.reflect.Constructor<T>, java.lang.reflect.Method
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
public static boolean StringUTF16.contentEquals(byte[],byte[],int)
public static boolean StringUTF16.contentEquals(byte[],CharSequence,int)
public static boolean java.util.Arrays.deepEquals(Object[],Object[])
public static boolean java.util.Objects.deepEquals(Object,Object)
$
```

Similarly, there's only one class called `StringBuffer`:
```shell
$ juggle 'class StringBuffer'
public final class StringBuffer extends AbstractStringBuilder implements java.io.Serializable, Comparable<T>, CharSequence
$
```
But there are two that contain the letters `StringBuffer`:
```shell
$ juggle 'class /StringBuffer/'
public final class StringBuffer extends AbstractStringBuilder implements java.io.Serializable, Comparable<T>, CharSequence
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
public static transient Stream<Path> Files.find(Path,int,java.util.function.BiPredicate<Path,java.nio.file.attribute.BasicFileAttributes>,FileVisitOption[]) throws java.io.IOException
public static transient Stream<Path> Files.walk(Path,int,FileVisitOption[]) throws java.io.IOException
public static transient Stream<Path> Files.walk(Path,FileVisitOption[]) throws java.io.IOException
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
public volatile int StringBuilder.compareTo(Object)
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
public <U> Class<? extends U> Class<T>.asSubclass(Class<U>)
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
$
```
