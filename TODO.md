# TODO for Juggle

* Adopt a decent project structure
  - Add tests
  - Remove dependency on IntelliJ
  
* Does Juggle solve my original two questions?
  1. "I have a `Foo`. What can I do with it?"
  2. "How do I get a `Bar`?"
     
* Add support for Java 9 Modules

* Figure out how to get a list of classes in the JDK

  - presently only searches classes named with `-p` and `-r` or in `-j` files
  - perhaps this will come with `-m` module support
 
* Consider generics -- is it even possible due to type erasure?
 
* Are member variables really fns?
  - get :: ClassType -> MemberType
  - set :: ClassType -> MemberType -> void
     
* Add support for constructors
  - `Class.getConstructors()` provides a list
  - Treat them as funcs from c'tor args to declaring object type.
  - Be careful of inner classes (implicit args?)
   
* Access; private, protected, package
  - maybe this should be a command-line flag, default = only public
  - then again, `grep`ing the results is effective
     
* Allow type placeholders & "don't care" types
  - In `-p` and `-r`, `_` means "I don't care about the type"
  - Anything else beginning with an underscore introduces a placeholder
  - With the exception of a single underscore, placeholders with identical
    names match the same type
  - e.g. show me funs that take two args of same type, and a third of
    a potentially different type, but return a Foo: 
     `-p _a -p _a -p _b -r Foo`
     
* Should a warning be emitted for `-p void` or `void[]`?
     
* Matches should work with any permutation of parameters
 
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
     
* Check behaviour for non-static inner classes
  - I expect an additional constructor argument for containing class
  - This is probably a compiler transformation, explicit in class file
    (i.e. nothing for us to do in Juggle.)
 
* Optional matching algorithm?
  - exact match vs assignment-compatible
  - configurable as to whether to ignore optional args
