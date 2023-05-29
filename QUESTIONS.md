# Open Questions

This file contains a few user interface questions that I've not resolved yet.

## List all subclasses

There's no way at present to list all subclasses of a class or interface.  The
obvious query to try only lists classes that directly extend the named class:
````
$ juggle class extends MyClass
````

On the other hand, indirect subclasses are shown by this query:
````
$ juggle class extends ? extends MyClass
````

> **Question**: Should Juggle include indirect subclasses in the
> first query shown, `$ juggle class extends MyClass`?

> **Question**: If the answer to the previous question is "yes",
> should there be an option to revert to the original (specific)
> behaviour?  If so, what should it be? (`-c none`?)

> **Question**: Should the words `class` and `interface`, and the
> words `extends` and `implements` be interchangable in queries?
> I.E. Should they be treated as upper bounds on type queries?

> **Question**: Should the queried class be included in the results?


## List all superclasses

What's the lower bound equivalent to the subclass query?

Proposal:
````
$ juggle class super MyClass
````

> **Question**: Should this query (which isn't legal Java) list
> all direct and indirect superclasses and superinterfaces of
> the named class?

> **Question**: How should such a query work with other query
> facets, such as modifier and names?


## Negative searches

How should Juggle express negative searches in queries, e.g.
names that _don't_ match a particular regular expression, or
methods that _don't_ have a specific modifier set?

> **Question**: Is `!` a suitable syntactic element for "not"?
> E.G. `$ juggle !/foo/` and `$ juggle !static`?  Does this
> cause problems due to its use in some shells?

> **Question**: Should the grammar require there to be no
> whitespace between the negation operator and the expression?


## Might throw other types

The original design for `throws` clause matches used the
ellipsis to indicate "may throw other things", i.e.
`$ juggle throws FileNotFoundException, ...` would only list
methods that throw `FileNotFoundException` but those methods
might throw other exceptions too, whereas `$ juggle throws
FileNotFoundException` would show only those methods that
throw the one specific class.

A side-effect of implementing wildcard types is that the
question mark carries the same meaning in this context, i.e.
`$ juggle throws FileNotFoundException, ?`.

> **Question**: Is the wildcard syntax obvious, memorable and
> appropriate in this context, or should we implement the
> ellipsis as originally planned?
