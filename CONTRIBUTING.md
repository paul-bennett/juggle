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

# Contributions Welcome!

Contributions are most certainly welcome!  Either submit a pull request, or
email opensource@angellane.com

I'd also appreciate any feedback on usefulness of this tool.

## Most Useful

The most useful contributions at the moment would be:

* Did Juggle do what you expected?
  - If you submitted a query to Juggle
    but can't work out why it responded in the way it did, then I've
    probably not got the user interface quite right.
  - Let me know what query you used, what results you saw, and what
    results you expected to see.

* Did Juggle barf?
  - Juggle should never experience an unhandled exception or emit
    unintelligible output.  If it outputs junk I'd like to know.
  - What query did you use? What did you see?

* Do you find the query syntax confusing or clumsy?
  - My intent is to keep Juggle's query syntax as close as possible
    to Java declarations.
  - Ideally you should be able to take a declaration line from a
    Java source file and use that as a query.  Juggle should include
    the original declaration in its results.
  - If you omit any component from a declaration, Juggle should still
    include the original query in its results.
  - Please let me know if it doesn't behave this way, or you find you
    are having to use strange syntax for the query.