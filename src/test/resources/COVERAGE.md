# Coverage Tests

This file contains invocations designed to increase code coverage of the test suite

## No parameters in the query

````
$ juggle -r NoSuchMethodException -s closest
public NoSuchMethodException.<init>()
public NoSuchMethodException.<init>(String)
$
````

## ByMostSpecificType comparator

````
$ juggle -r NoSuchMethodException -s type
public NoSuchMethodException.<init>()
public NoSuchMethodException.<init>(String)
$
````

````
$ juggle -p String -r java.io.InputStream -s type
public java.io.FileInputStream.<init>(String) throws java.io.FileNotFoundException
public java.io.StringBufferInputStream.<init>(String)
public static java.io.InputStream ClassLoader.getSystemResourceAsStream(String)
$
````

## Missing module

````
$ juggle -m this.module.does.not.exist
*** Module this.module.does.not.exist not found
$
````
