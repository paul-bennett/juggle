# Regressions

This file contains regression tests for Juggle; curiosities that show
how we expect the Juggle command-line to operate in corner cases.

The file is parsed by `com.angellane.juggle.TestSamples`. See also the
comment in [README.md](README.md) for more details about how it's parsed.
But in essence, add a test by copying one of the code blocks.

## Command-line Parsing

If we pass an invalid argument, we should get an error and the help text:

````
$ juggle --fiddle-de-dee
Unknown option: '--fiddle-de-dee'
Usage: juggle [-hVx] [-@=type,type,...] [-a=private|protected|package|public]
              [-c=className] [-f=<formatterOption>] [-i=packageName]
              [-j=jarFilePath] [-m=moduleName] [-n=methodName] [-p=type,
              type,...] [-r=type] [-s=<addSortCriteria>] [-t=type,type,...]
              [declaration...]
An API search tool for Java
      [declaration...]       A Java-style declaration to match against
  -@, --annotation=type,type,...
                             Annotations
  -a, --access=private|protected|package|public
                             Minimum accessibility of members to return
  -c, --class-name=className Filter by class name
  -f, --format=<formatterOption>
                             Output format
  -h, --help                 Show this help message and exit.
  -i, --import=packageName   Imported package names
  -j, --jar=jarFilePath      JAR file to include in search
  -m, --module=moduleName    Modules to search
  -n, --member-name=methodName
                             Filter by member name
  -p, --param=type,type,...  Parameter type of searched function
  -r, --return=type          Return type of searched function
  -s, --sort=<addSortCriteria>
                             Sort criteria
  -t, --throws=type,type,... Thrown types
  -V, --version              Print version information and exit.
  -x, --[no-]permute         Also match permutations of parameters
$
````

Of course, we can explicitly ask for help:

````
$ juggle --help
Usage: juggle [-hVx] [-@=type,type,...] [-a=private|protected|package|public]
              [-c=className] [-f=<formatterOption>] [-i=packageName]
              [-j=jarFilePath] [-m=moduleName] [-n=methodName] [-p=type,
              type,...] [-r=type] [-s=<addSortCriteria>] [-t=type,type,...]
              [declaration...]
An API search tool for Java
      [declaration...]       A Java-style declaration to match against
  -@, --annotation=type,type,...
                             Annotations
  -a, --access=private|protected|package|public
                             Minimum accessibility of members to return
  -c, --class-name=className Filter by class name
  -f, --format=<formatterOption>
                             Output format
  -h, --help                 Show this help message and exit.
  -i, --import=packageName   Imported package names
  -j, --jar=jarFilePath      JAR file to include in search
  -m, --module=moduleName    Modules to search
  -n, --member-name=methodName
                             Filter by member name
  -p, --param=type,type,...  Parameter type of searched function
  -r, --return=type          Return type of searched function
  -s, --sort=<addSortCriteria>
                             Sort criteria
  -t, --throws=type,type,... Thrown types
  -V, --version              Print version information and exit.
  -x, --[no-]permute         Also match permutations of parameters
$
````

We can ask for the version of the application, but when run from an unpacked
source tree it doesn't show anything useful: 
````
$ juggle --version
Unknown
$
````

## Previously fixed bugs

### [GitHub issue #1](https://github.com/paul-bennett/juggle/issues/1)

Searching (with -p or -r) for an array of a primitive type falls back to Object


````
$ juggle -p double[],int,int,double -r void
public static void java.util.Arrays.fill(double[],int,int,double)
$
````

### [GitHub issue #16](https://github.com/paul-bennett/juggle/issues/32)

Support module "implied readability"

Prior to fixing this issue, specifying a module using `-m` would examine the classes
directly defined within the module, but not any classes from modules which it requires
transitively.

For example, the `java.se` module requires 20 modules transitively, including `java.sql`.
So the following two executions should return the same results.  (Prior to the fix,
the second -- `-m java.se` -- showed no results.)

````
$ juggle -m java.sql -i java.sql -r ResultSet -p PreparedStatement
public abstract ResultSet PreparedStatement.executeQuery() throws SQLException
public abstract ResultSet Statement.getGeneratedKeys() throws SQLException
public abstract ResultSet Statement.getResultSet() throws SQLException
$
````

````
$ juggle -m java.se -i java.sql -r ResultSet -p PreparedStatement
public abstract ResultSet PreparedStatement.executeQuery() throws SQLException
public abstract ResultSet Statement.getGeneratedKeys() throws SQLException
public abstract ResultSet Statement.getResultSet() throws SQLException
$
````

### [GitHub issue #32](https://github.com/paul-bennett/juggle/issues/32)

Results aren't deduplicated

````
$ juggle -n asSubclass -m java.base,java.base
public <U> Class<T> Class<T>.asSubclass(Class<T>)
$
````

### [GitHub issue #70](https://github.com/paul-bennett/juggle/issues/70): Add `transient` and `volatile` member modifiers

There are about 400 `transient` methods in the JDK. Here are a few.
````
$ juggle -i java.util.stream -i java.nio.file transient Stream
public static transient <T> Stream<T> Stream<T>.of(T[])
public static transient Stream<T> Files.find(Path,int,java.util.function.BiPredicate<T,U>,FileVisitOption[]) throws java.io.IOException
public static transient Stream<T> Files.walk(Path,int,FileVisitOption[]) throws java.io.IOException
public static transient Stream<T> Files.walk(Path,FileVisitOption[]) throws java.io.IOException
$
````
And here are some of the 2000 `volatile` methods:
````
$ juggle "volatile (StringBuilder,?)"
public volatile char StringBuilder.charAt(int)
public volatile int StringBuilder.codePointAt(int)
public volatile int StringBuilder.codePointBefore(int)
public volatile int StringBuilder.compareTo(Object)
public volatile AbstractStringBuilder StringBuilder.append(boolean)
public volatile AbstractStringBuilder StringBuilder.append(char)
public volatile AbstractStringBuilder StringBuilder.append(char[])
public volatile AbstractStringBuilder StringBuilder.append(double)
public volatile AbstractStringBuilder StringBuilder.append(float)
public volatile AbstractStringBuilder StringBuilder.append(int)
public volatile AbstractStringBuilder StringBuilder.append(CharSequence)
public volatile AbstractStringBuilder StringBuilder.append(Object)
public volatile AbstractStringBuilder StringBuilder.append(String)
public volatile AbstractStringBuilder StringBuilder.append(StringBuffer)
public volatile AbstractStringBuilder StringBuilder.append(long)
public volatile AbstractStringBuilder StringBuilder.appendCodePoint(int)
public volatile AbstractStringBuilder StringBuilder.deleteCharAt(int)
public volatile Appendable StringBuilder.append(char) throws java.io.IOException
public volatile Appendable StringBuilder.append(CharSequence) throws java.io.IOException
public volatile String StringBuilder.substring(int)
public volatile void StringBuilder.ensureCapacity(int)
public volatile void StringBuilder.setLength(int)
$
````

## [GitHub Issue #74](https://github.com/paul-bennett/juggle/issues/74): Search by exact name is broken (substring)

There are no methods in the JDK called `Equals`:
````
$ juggle '? Equals'
$
````

But there are some that contain the word `Equals`:
````
$ juggle '? /Equals/'
public boolean String.contentEquals(CharSequence)
public boolean String.contentEquals(StringBuffer)
public static boolean StringUTF16.contentEquals(byte[],byte[],int)
public static boolean StringUTF16.contentEquals(byte[],CharSequence,int)
public static boolean java.util.Arrays.deepEquals(Object[],Object[])
public static boolean java.util.Objects.deepEquals(Object,Object)
$
````

Similarly, there's only one class called `StringBuffer`:
````
$ juggle 'class StringBuffer'
class java.lang.StringBuffer
$
````
But there are two that contain the letters `StringBuffer`:
````
$ juggle 'class /StringBuffer/'
class java.lang.StringBuffer
class java.io.StringBufferInputStream
$
````

(Prior to fixing this issue, string literals were interpreted as
case-sensitive substring matches.)