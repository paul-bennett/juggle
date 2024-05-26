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

# Juggle: a declarative search tool for Java

Juggle searches Java libraries for types and members that match a given declaration.

## Installation

Since _Juggle_ is aimed at Java developers, I don't provide a binary release; 
I assume you're comfortable building and running Java applications.

To install (you'll need to have JDK17 or later on your path):

```shell
$ git clone https://github.com/paul-bennett/juggle.git
$ cd juggle
$ ./gradlew jar
$
```

This will result in a `juggle-*.jar` in the `build/libs` subdirectory.

_Juggle_ ships with a helpful shell script that sets an alias in Bourne shell
derivatives to provide simple execution.
```shell
$ source juggle-alias.sh
$
```

Now, to execute a query just use the `juggle` alias:
```shell
$ juggle class InetAddress
public sealed class java.net.InetAddress implements java.io.Serializable permits java.net.Inet4Address, java.net.Inet6Address
$
```

## Examples

For much more information, including lots of example invocations, see the full
documentation at [src/main/resources/README.md](src/main/resources/README.md).

## Feedback

I love feedback. Please let me know if you find _Juggle_ useful or frustrating.
Send it to [opensource@angellane.com](mailto:opensource@angellane.com).

Thanks!

-Paul