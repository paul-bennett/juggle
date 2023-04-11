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
              [-f=<formatterOption>] [-i=packageName] [-j=jarFilePath]
              [-m=moduleName] [-n=methodName] [-p=type,type,...] [-r=type]
              [-s=<addSortCriteria>] [-t=type,type,...]
An API search tool for Java
  -@, --annotation=type,type,...
                             Annotations
  -a, --access=private|protected|package|public
                             Minimum accessibility of members to return
  -f, --format=<formatterOption>
                             Output format
  -h, --help                 Show this help message and exit.
  -i, --import=packageName   Imported package names
  -j, --jar=jarFilePath      JAR file to include in search
  -m, --module=moduleName    Modules to search
  -n, --name=methodName      Filter by member name
  -p, --param=type,type,...  Parameter type of searched function
  -r, --return=type          Return type of searched function
  -s, --sort=<addSortCriteria>
                             Sort criteria
  -t, --throws=type,type,... Thrown types
  -V, --version              Print version information and exit.
  -x, --[no-]permute         Also match permutations of parameters
$
````

Of course we can explicitly ask for help:

````
$ juggle --help
Usage: juggle [-hVx] [-@=type,type,...] [-a=private|protected|package|public]
              [-f=<formatterOption>] [-i=packageName] [-j=jarFilePath]
              [-m=moduleName] [-n=methodName] [-p=type,type,...] [-r=type]
              [-s=<addSortCriteria>] [-t=type,type,...]
An API search tool for Java
  -@, --annotation=type,type,...
                             Annotations
  -a, --access=private|protected|package|public
                             Minimum accessibility of members to return
  -f, --format=<formatterOption>
                             Output format
  -h, --help                 Show this help message and exit.
  -i, --import=packageName   Imported package names
  -j, --jar=jarFilePath      JAR file to include in search
  -m, --module=moduleName    Modules to search
  -n, --name=methodName      Filter by member name
  -p, --param=type,type,...  Parameter type of searched function
  -r, --return=type          Return type of searched function
  -s, --sort=<addSortCriteria>
                             Sort criteria
  -t, --throws=type,type,... Thrown types
  -V, --version              Print version information and exit.
  -x, --[no-]permute         Also match permutations of parameters
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
public ResultSet PreparedStatement.executeQuery() throws SQLException
public ResultSet Statement.getGeneratedKeys() throws SQLException
public ResultSet Statement.getResultSet() throws SQLException
$
````

````
$ juggle -m java.se -i java.sql -r ResultSet -p PreparedStatement
public ResultSet PreparedStatement.executeQuery() throws SQLException
public ResultSet Statement.getGeneratedKeys() throws SQLException
public ResultSet Statement.getResultSet() throws SQLException
$
````

### [GitHub issue #32](https://github.com/paul-bennett/juggle/issues/32)

Results aren't deduplicated

````
$ juggle -n asSubclass -m java.base,java.base
public <U> Class<T> Class<T>.asSubclass(Class<T>)
$
````

