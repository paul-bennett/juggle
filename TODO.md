# TODO for Juggle

* Add more tests
  
* Does Juggle solve my original two questions?
  1. "I have a `Foo`. What can I do with it?"
  2. "How do I get a `Bar`?"
      
* Implement constraints due to generic type parameters.
  This needs careful thought!
  - Currently Juggle treats type parameters as unconstrained `Object`
    + By not evaluating constraints, Juggle may inappropriately include (or exclude) some entries from the results
    + Consider the method `boolean java.util.Map<K,V>.replace(K, V, V)`.
      Because the last two parameters have the same generic type, this method
      should not be included in the result set of the query
      `juggle -p java.util.Map -p Long -p String -p Long -r boolean`
      because a Long and a String aren't assignment compatible.
      This is because Juggle is blind to the generic types and instead treats
      the method as `boolean java.util.Map.replace(Object, Object, Object)`. 
    + Consider the return type of the `V java.util.Map<K,V>.putIfAbsent(K, V)`
      should match the second parameter.  Clearly this should match the query
      `juggle -p java.util.Map -p Long -p String -r` but Juggle excludes it from the output
      because it treats the method as `Object java.util.Map.putIfAbsent(Object, Object)`, 
      and is unable to deduce the correct return type of `String`. Consequently
      because a `Object` return type can't be assigned to a `String` variable,
      the method is excluded from the result set.
    + Finally, consider type constraints such as `? extends Foo` or `? super Bar`.
      These need complicate the matching process further.
      
  - It looks like it should be possible to implement this feature using:
    + Interfaces `java.lang.reflect.GenericDeclaration`; `java.lang.reflect.Type` and its subinterfaces
      `java.lang.reflect.GenericArrayType`, `java.lang.reflect.ParameterizedType`,
      `java.lang.reflect.TypeVariable` and `java.lang.reflect.WildcardType`
    + `java.lang.Class`'s `getGenericInterfaces()`, `getGenericSuperclass()`and `getTypeParameters()` methods
    + `java.lang.reflect.Executable`'s `getGenericExceptionTypes()`, `getGenericParameterTypes()` and 
      `getTypeParameters()` methods
      (`java.lang.reflect.Method` and `java.lang.reflect.Constructor` both extend `Executable`.)
    + `java.lang.reflect.Method`'s `getGenericReturnType()` method
    + `java.lang.reflect.Field`'s `getGenericType()` method
  
  - There may be standard methods (somewhere in the `java.compiler` package?)
    that fully implement type inference algorithm, which would save a lot of
    work!
    
* Allow type variables in the queries -- see also Generics above
  - New option `-g` introduces a type variable
    + e.g. show me functions that take two arguments of some type `Foo`
      and a third argument of a potentially different type returning an
      argument of the first type:`juggle -g Foo -g Bar -p Foo -p Foo -p Bar -r Foo`
  - Type variables follow the usual Java rules:
    + `-g A` introduces type variable `A`
    + `-g A extends Foo` introduces type variable `A` that is `Foo` or
      a subclass of `Foo`. (In this case `Foo` is either a concrete
      type, or another type variable.)
    + `-g A super Foo` introduces type variable `A` that is `Foo` or
      a superclass/interface of `Foo`
  - Type variables can be used in `-p` and `-r` too:
    + `-p ?` means a parameter of some unknown type
    + `-r ?` means any return type (including primitive and `void`)
    + `-p ? extends Foo` means a parameter that is `Foo` or a subclass of `Foo`.
    
* Currently an absence of a `-r` is treated as `-r void`. Maybe instead
  it should be treated as `-r ?`.  (I.E. any type)
* Add support for module path (``-M``?)
 
* Add support for directories of class files
     
* Should a warning be emitted for `-p void` or `void[]`?

* Consider refactoring the permutation invocation (maybe it should be 
  done in main?)     
* What about currying?
  - a search for: `Foo -> Bar -> Baz` might match `Foo -> (Bar -> Baz)`
  - Java kind of allows this return type with Functional Interfaces
       
* Try alternative syntax for command-line
  - i.e. express query in a more natural way, rather than:
    `-p foo -p bar -r baz`
  - maybe like a Java declaration
    `baz myfunc(foo f, bar b)`
    where "myfunc", "f" and "b" are ignored bits of text
  - or would a lambda-inspired syntax work?
    `(foo, bar) -> baz`
  - Haskell-like?
    `foo -> bar -> baz`
          
* Check type compatibility matches Java Language Spec chapter 5
  - See methods isTypeCompatibleForInvocation/isTypeCompatibleForAssignment

* When emitting decls, omit the default package name (maybe last -or first?- `-i`)
     
* Should sort results naturally
  - Exact matches first
  - Permutations of parameters and partial matches later
  - What about supertype params and subtype returns?
  - Box/unbox cost?
  - Prefer member funs over statics?
  - Sort in order of specificity e.g. in calling semantics
     
* Optional matching algorithm?
  - exact match vs assignment-compatible
  - configurable as to whether to ignore optional args

* Review code for more TODOs

* Consider back-porting to JDK9
  - biggest problem is Class.arrayType(), from JDK12