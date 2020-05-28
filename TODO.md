# TODO for Juggle

* Adopt a decent project structure
  - Add tests
  - Remove dependency on IntelliJ
  
* Does Juggle solve my original two questions?
  1. "I have a `Foo`. What can I do with it?"
  2. "How do I get a `Bar`?"
     
* Add support for Java 9 Modules
 
* Consider generics -- is it even possible due to type erasure?
 
* Consider arrays
 
* Are member variables really fns?
  - get :: ClassType -> MemberType
  - set :: ClassType -> MemberType -> void
     
* How to handle constructors?  As funcs that return an object of that type?
 
* Access; private, protected, package
  - maybe this should be a command-line flag, default = only public
     
* Allow type placeholders & "don't care" types
  - In `-p` and `-r`, `_` means "I don't care about the type"
  - Anything else beginning with an underscore introduces a placeholder
  - With the exception of a single underscore, placeholders with identical
    names match the same type
  - e.g. show me funs that take two args of same type, and a third of
    a potentially different type, but return a Foo: 
     `-p _a -p _a -p _b -r Foo`
     
* Is `void` really a type?
  - Check that `-r void` works
  - `-p void` should be disallowed
     
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
          
* Java automatically converts between boxed/unboxed types. Should Juggle?
  - e.g. `long` is compatible with `Long`

* Add an option for package 'import's
  - e.g. `-i java.net`
  - If classname not found 'naked', try in the specified package
  - When emitting decls, omit the default package name (maybe last `-i`)
  - Could specify multiple imports
  - Perhaps always implicitly import `java.lang`
     
* Should sort results naturally
     - Exact matches first
     - Permutations of parameters and partial matches later
     - What about supertype params and subtype returns?
     - Box/unbox cost?
     - Prefer member funs over statics?
     
* How should inner classes be searched?
     - Are there additional implicit args (one per containing class?)
 
* Optional matching algorithm?
     - exact match vs assignment-compatible
     - configurable as to whether to ignore optional args
     
