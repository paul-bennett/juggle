# TODO for Juggle

* Add more tests
  
* Does Juggle solve my original two questions?
  1. "I have a `Foo`. What can I do with it?"
  2. "How do I get a `Bar`?"
      
* Consider generics -- is it even possible due to type erasure?

* Add support for module path (``-M``?)
 
* Add support for directories of class files
     
* Allow type placeholders & "don't care" types
  - In `-p` and `-r`, `_` means "I don't care about the type"
  - Anything else beginning with an underscore introduces a placeholder
  - With the exception of a single underscore, placeholders with identical
    names match the same type
  - e.g. show me funs that take two args of same type, and a third of
    a potentially different type, but return a Foo: 
     `-p _a -p _a -p _b -r Foo`
     
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