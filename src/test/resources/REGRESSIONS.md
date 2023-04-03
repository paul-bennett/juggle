# Regressions

This file contains regression tests for Juggle; curiosities that show
how we expect the Juggle command-line to operate in corner cases.

The file is parsed by `com.angellane.juggle.TestSamples`. See also the
comment in [README.md](README.md) for more details about how it's parsed.
But in essence, add a test by copying one of the code blocks.

## Command-line Parsing

If we pass an invalid argument, we should get one line of help:

````
$ juggle --fiddle-de-dee
"--fiddle-de-dee" is not a valid option
 [-@ (--annotation) type,type,...] [-a (--access) [PRIVATE | PROTECTED | PACKAGE | PUBLIC]] [-h (--help)] [-i (--import) packageName] [-j (--jar) jarFilePath] [-m (--module) moduleName] [-n (--name) methodName] [-p (--param) type,type,...] [-r (--return) type] [-s (--sort) [ACCESS | TYPE | CLOSEST | PACKAGE | NAME]] [-t (--throws) type,type,...] [-x (--permute)]
$
````

Of course we can get full command-line help:

````
$ juggle --help
 -@ (--annotation) type,type,...        : Annotations
 -a (--access) [PRIVATE | PROTECTED |   : Minimum accessibility of members to
 PACKAGE | PUBLIC]                        return (default: PUBLIC)
 -i (--import) packageName              : Imported package names
 -j (--jar) jarFilePath                 : JAR file to include in search
 -m (--module) moduleName               : Modules to search
 -n (--name) methodName                 : Filter by member name
 -p (--param) type,type,...             : Parameter type of searched function
 -r (--return) type                     : Return type of searched function
 -s (--sort) [ACCESS | TYPE | CLOSEST   : Sort criteria
 | PACKAGE | NAME]                         
 -t (--throws) type,type,...            : Thrown types
 -x (--permute)                         : Also match permutations of parameters
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

### [GitHub issue #32](https://github.com/paul-bennett/juggle/issues/32)

Results aren't deduplicated

````
$ juggle -n asSubclass -m java.base,java.base
public <U> Class<T> Class<T>.asSubclass(Class<T>)
$
````
