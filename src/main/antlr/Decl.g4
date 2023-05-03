/*
 * This grammar defines an approximation of the Java method and field syntax.
 *
 * We're not using the actual Java syntax for a few reasons:
 *   1. Need to be able to allow things which Java denies (e.g. missing names)
 *   2. Need to deny some things Java allows (e.g. values in annotations)
 *   3. Want to steer clear of the intellectual property of the Java Language Specification
 *
 * The grammar will grow over time.  The aim is to start small and build out.  Big-ticket items such as
 * generics will have to come later.
 *
 * Intended order of implementation:
 *   a) Annotations by name; equivalent to `juggle -@ Foo`
 *   b) Access modifiers; equivalent to `juggle -a private`
 *   c) Member names; equivalent to `juggle -n myMethodName`
 *   d) Return type; equivalent to `juggle -r Bar`
 *   e) Parameter types; equivalent to `juggle -p Foo,Bar`
 *   f) Parameter subsets; currently not supported by Juggle
 *   g) Generics; currently not fully supported by Juggle
 *
 * At each point of the implementation newly introduced features of the grammar will be tested both in isolation
 * as well as in combination with all previously added features (in any combination).
 *
 *
 * There are ambiguities in this grammar.
 *
 * For example, is "foo" an unnamed member of type foo, or a member of name foo with unknown type?
 * Another example: if "foo(,)" is a function called "foo" that takes two args, does foo() take 0 or 1 arg?
 *
 * Both of these cases can be resolved by using "_" as an explicit wildcard.  We resolve the first case "foo" by
 * looking for a type named foo (possibly prefixed by imported package names).  If a type is found, then "foo" is
 * taken to mean "foo _" (i.e. a member of type foo with unknown name).  If there's no matching type, "foo" is
 * interpreted as "_ foo": a member of unknown type called foo.  If there's a dot in the name (i.e. "foo.bar"),
 * it can only be a qname – a type name.
 *
 * In the second example, "foo()" is interpreted as a method taking no arguments, and "foo(_)" is a method taking
 * a single unknown argument.  (To provide an unambiguous way of expressing the first of these options we also
 * allow the C-like "foo(void)".)
 */
grammar Decl;

//@header {
//    package com.angellane.juggle.parser;
//}

oneDecl
    :   decl EOF
    ;

decl
    :   annotation*
        // TODO: add generic_introducer <T>
        modifier*
        returnType?
        methodName?
        params?
    ;

annotation
    :   '@' qname
    ;

modifier
    : 'private' | 'protected' | 'package' | 'public'
    | 'abstract' | 'static' | 'final' | 'native' | 'strictfp' | 'synchronized'
    ;

returnType
    : type
    ;

methodName
    : uname
    ;

uname
    :   REGEX
    |   IDENT
    ;

qname
    :   (IDENT DOT)* IDENT
    ;

// TODO: consider how to disable ELLIPSIS in return types

type
    :   qname dim* ELLIPSIS?                # exactType
    |   '?'                                 # unboundedType
    |   '?' 'extends' qname ('&' qname)*    # upperBoundedType  // TODO: consider is qname right here?
    |   '?' 'super'   qname                 # lowerBoundedType  // TODO: consider is qname right here?
    // TODO: add type parameters, e.g. "List<Foo>"
    ;

dim : '[' ']' ;

params
    :   '(' ')'
    |   '(' param (',' param)* ')'
    ;

param
    :   type uname?     #unnamedParam   // potentially unnamed type
    |   type? uname     #untypedParam   // potentially untyped name
    // TODO: add parameter @annotations and modifiers (`final`)
    |   ELLIPSIS        #ellipsisParam  // an unknown number of params      (extension to Java)
    |                   #unknownParam   // unnamed, untyped (i.e. wildcard)
    ;

WS      : [\r\n\t ]+  -> skip;

ELLIPSIS    : '...' ;
DOT         : '.'   ;

REGEX   : '/' (ESC | ~[/\\])* '/' 'i'?;
fragment ESC : '\\' . ;                 // Allow escape of any char, but only / is meaningful

IDENT   : ID_START ID_PART* ;

// Java allows a much wider selection of chars in an identifier, but we're being lazy for now.
// JLS refers to methods on java.lang.Character, but their description is imprecise.

fragment ID_START  : [a-zA-Z_];         // Should be java.lang.Character.isJavaIdentifierStart(int)
fragment ID_PART   : [a-zA-Z_0-9];      // Should be java.lang.Character.isJavaIdentifierPart(int)

// TODO: align identifier definition with JLS... or can we just call Character.isJavaIdentifier{Start|Part}?