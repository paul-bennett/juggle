<!-- 
    Juggle -- a declarative search tool for Java
   
    Copyright 2020,2024 Paul Bennett
   
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

# Open Questions

This file contains a few user interface questions that I've not resolved yet.

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
