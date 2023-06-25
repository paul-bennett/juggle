# Open Questions

This file contains a few user interface questions that I've not resolved yet.

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
