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
# Coverage Tests

This file contains invocations designed to increase code coverage of the test suite.  Many of the samples here
don't actually do anything useful, but instead the combination of command-line parameters have been carefully
selected in order to drive up the JaCoCo coverage metrics.

## No parameters in the query

````
$ juggle NoSuchMethodException
public NoSuchMethodException.<init>()
public NoSuchMethodException.<init>(String)
$
````

## Missing module

````
$ juggle -m this.module.does.not.exist
*** Module this.module.does.not.exist not found
$
````

## Unknown type

````
$ juggle 'boolean (ThisTypeDoesNotExist)'
*** Couldn't find type: ThisTypeDoesNotExist; using class java.lang.Object instead
public static native boolean Thread.holdsLock(Object)
public static boolean jdk.internal.vm.vector.VectorSupport.isNonCapturingLambda(Object)
public static boolean java.util.Objects.isNull(Object)
public static boolean java.lang.invoke.MethodHandleProxies.isWrapperInstance(Object)
public static boolean java.util.Objects.nonNull(Object)
$
````

## Methods that don't throw

````
$ juggle 'String (? super java.io.InputStream) throws'
public String Object.toString()
public static String String.valueOf(Object)
public static String java.util.Objects.toString(Object)
public static String sun.invoke.util.BytecodeDescriptor.unparse(Object)
$
````

If it wasn't for the `throws` we'd expect the above query to also include 
`String java.net.URLConnection.guessContentTypeFromStream(java.io.InputStream)` in its results.


## Trying to find WildcardType

The biggest area that presently lacks test coverage is `TextOutput.decodeWildcardType()`.  This function
doesn't seem to be called at all, even when I explicitly search for a method that the JavaDoc suggests
returns a wildcard type:

````
$ juggle /asSubclass/
public <U> Class<T> Class<T>.asSubclass(Class<T>)
$
````

The `Class.asSubclass` method is declared to return `Class<? extends U>`. I suspect the wildcard is eliminated
at runtime due to type erasure, in which case it may be worth stripping this method from the source altogether.


## Empty -m

````
$ juggle -m '' /getUpperBound/
public abstract java.lang.reflect.Type[] java.lang.reflect.WildcardType.getUpperBounds()
public java.lang.reflect.Type[] sun.reflect.generics.reflectiveObjects.WildcardTypeImpl.getUpperBounds()
public sun.reflect.generics.tree.FieldTypeSignature[] sun.reflect.generics.tree.Wildcard.getUpperBounds()
$
````


## Multiple -t options

````
$ juggle throws java.io.NotActiveException, java.io.InvalidObjectException
public void java.io.ObjectInputStream.registerValidation(java.io.ObjectInputValidation,int) throws java.io.NotActiveException,java.io.InvalidObjectException
$
````

## Explicit value of -x

By default, boolean arguments in picocli carry no value.  If you specify them on the command-line, the value `true`
is passed to the corresponding function.  

It feels wrong within the setter function to not use the value of the boolean parameter, even though we know it
will only ever take the value `true`.  That means JaCoCo will always present one path in an `if` statement as not
followed.

````
$ juggle '(String,ClassLoader,boolean)'
$
````

````
$ juggle -x '(String,ClassLoader,boolean)'
public static Class<T> Class<T>.forName(String,boolean,ClassLoader) throws ClassNotFoundException
public void ClassLoader.setClassAssertionStatus(String,boolean)
public void ClassLoader.setPackageAssertionStatus(String,boolean)
$
````

But there's a workaround... add `negatable=true` to the `@Option` annotation, and 
suddenly the long option name can be prefixed with `no-` on the command-line.

````
$ juggle --permute '(String,ClassLoader,boolean)'
public static Class<T> Class<T>.forName(String,boolean,ClassLoader) throws ClassNotFoundException
public void ClassLoader.setClassAssertionStatus(String,boolean)
public void ClassLoader.setPackageAssertionStatus(String,boolean)
$
````

````
$ juggle --no-permute '(String,ClassLoader,boolean)'
$
````

## Missing dependency

The (contrived) App class from testApp uses the Lib class from testLib in its interface, but doesn't include these
dependent classes in the JAR (it's not an uberjar).  This means trying to load the App class fails.  

````
$ juggle -j build/libs/testApp.jar 'com.angellane.juggle.testinput.app.App()'            
*** Ignoring class com.angellane.juggle.testinput.app.App: java.lang.NoClassDefFoundError: com/angellane/juggle/testinput/lib/Lib
*** Couldn't find type: com.angellane.juggle.testinput.app.App; using class java.lang.Object instead
*** Ignoring class com.angellane.juggle.testinput.app.App: java.lang.NoClassDefFoundError: com/angellane/juggle/testinput/lib/Lib
public Object.<init>()
public static final Object sun.security.validator.ValidatorException.T_ALGORITHM_DISABLED
public static final Object sun.security.validator.ValidatorException.T_CA_EXTENSIONS
public static final Object sun.security.validator.ValidatorException.T_CERT_EXPIRED
public static final Object sun.security.validator.ValidatorException.T_EE_EXTENSIONS
public static final Object sun.security.validator.ValidatorException.T_NAME_CHAINING
public static final Object sun.security.validator.ValidatorException.T_NO_TRUST_ANCHOR
public static final Object sun.security.validator.ValidatorException.T_SIGNATURE_ERROR
public static final Object sun.security.validator.ValidatorException.T_UNTRUSTED_CERT
public static Object sun.security.jca.Providers.startJarVerification()
$
````

## Methods with no modifiers

Curiously this test fails here, but works in README.md.  See GitHub issue #39.
````
% juggle -j build/libs/testLib.jar package com.angellane.juggle.testinput.lib.Lib
public static com.angellane.juggle.testinput.lib.Lib com.angellane.juggle.testinput.lib.Lib.libFactory()
com.angellane.juggle.testinput.lib.Lib.<init>()
%
````

## Dry-Run and Show-Query options

````
$ juggle --dry-run --show-query record
QUERY: ClassQuery{flavour=RECORD, annotationTypes=null, accessibility=PUBLIC, modifierMask=0, modifiers=0, declarationPattern=null, supertype=null, superInterfaces=null, permittedSubtypes=null, recordComponents=null}
$ 
````
````
$ juggle --dry-run --show-query '()'      
QUERY: DeclQuery{annotationTypes=null, accessibility=PUBLIC, modifierMask=0, modifiers=0, returnType=null, declarationPattern=null, params=[], exceptions=null}
$ 
````