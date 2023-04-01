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
 [-@ (--annotation) type,type,...] [-a (--access) [PRIVATE | PROTECTED | PACKAGE | PUBLIC]] [-h (--help)] [-i (--import) packageName] [-j (--jar) jarFilePath] [-m (--module) moduleName] [-p (--param) type,type,...] [-r (--return) type] [-s (--sort) [ACCESS | TYPE | CLOSEST | PACKAGE | NAME]] [-t (--throws) type,type,...]
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
 -p (--param) type,type,...             : Parameter type of searched function
 -r (--return) type                     : Return type of searched function
 -s (--sort) [ACCESS | TYPE | CLOSEST   : Sort criteria
 | PACKAGE | NAME]                         
 -t (--throws) type,type,...            : Thrown types
$
````

