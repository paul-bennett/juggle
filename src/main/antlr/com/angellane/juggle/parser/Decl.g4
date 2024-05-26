/*
 *  Juggle -- an API search tool for Java
 *
 *  Copyright 2020,2023 Paul Bennett
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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

oneDecl
    :   decl EOF ;

decl
    :   classDecl
    |   interfaceDecl
    |   annotationDecl
    |   enumDecl
    |   recordDecl
    |   memberDecl
    ;

classDecl
    :   classModifiers
        'class' typeDeclName?
        (superClause? |
        classExtendsClause?
        implementsClause?
        )
        permitsClause?
    ;

superClause
    :   ('super' type)
    ;

classExtendsClause
    :   ('extends' type)
    ;

implementsClause
    :   ('implements' type (',' type)*)
    ;

interfaceDecl
    :   interfaceModifiers
        'interface' typeDeclName?
        (superClause? | interfaceExtendsClause?)
        permitsClause?
    ;

interfaceExtendsClause
    :   ('extends' type (',' type)*)
    ;


annotationDecl
    :   annotationModifiers
        '@' 'interface' typeDeclName?
    ;

enumDecl
    :   classModifiers
        'enum' typeDeclName?
        implementsClause?
    ;

recordDecl
    :   classModifiers
        'record' typeDeclName?
        recordComps?
        implementsClause?
    ;

classModifiers: classModifier*;
classModifier
    :   annotation
    |   'private' | 'protected' | 'package' | 'public'
    |   'abstract' | 'static' | 'final' | 'sealed' | 'non-sealed' | 'strictfp'
    ;

interfaceModifiers: interfaceModifier*;
interfaceModifier
    :   annotation
    |   'private' | 'protected' | 'package' | 'public'
    |   'abstract' | 'static' | 'final' | 'sealed' | 'non-sealed' | 'strictfp'
    ;

annotationModifiers: annotationModifier*;
annotationModifier
    :   annotation
    |   'public' | 'abstract'
    ;

permitsClause
    :   'permits' type (',' type)*
    ;

memberDecl
    :   memberModifiers
        returnType?
        memberDeclName?
        params?
        throwsClause?
    ;

annotation
    :   '@' qname
    ;

memberModifiers: memberModifier*;
memberModifier
    : annotation
    | 'private' | 'protected' | 'package' | 'public'
    | 'abstract' | 'static' | 'final' | 'native' | 'strictfp' | 'synchronized'
    | 'default'
    | 'transient' | 'volatile'
    ;

returnType
    : type
    ;

typeDeclName
    : NEGATE? REGEX
    | (IDENT DOT)* IDENT
    ;

memberDeclName
    : uname
    ;

uname
    :   NEGATE? REGEX
    |   IDENT
    ;

qname
    :   (IDENT DOT)* IDENT
    ;

type
    :   qname dim* ELLIPSIS?                # exactType
    |   '?'                                 # unboundedType
    |   '?' 'extends' qname ('&' qname)*    # upperBoundedType
    |   '?' 'super'   qname                 # lowerBoundedType
    ;

dim : '[' ']' ;

recordComps
    :   '(' ')'
    |   '(' recordComp (',' recordComp)* ')'
    ;

recordComp
    : recordCompModifiers typeAndName
    ;

recordCompModifiers: recordCompModifier*;
recordCompModifier
    :   annotation
    ;

params
    :   '(' ')'
    |   '(' param (',' param)* ')'
    ;

paramModifiers: paramModifier*;
paramModifier
    :   annotation
    |   'final'
    ;

param
    : paramModifiers typeAndName
    ;

typeAndName
    :   type uname?     #unnamedType    // potentially unnamed type
    |   type? uname     #untypedName    // potentially untyped name
    |   ELLIPSIS        #ellipsisType   // an unknown number of params
    |                   #wildcardType   // unnamed, untyped (i.e. wildcard)
    ;

throwsClause
    :   'throws' (exception (',' exception)* )?
    ;

exception : type ;

WS      : [\r\n\t ]+  -> skip;

ELLIPSIS    : '...' ;
DOT         : '.'   ;
NEGATE      : '!'   ;

REGEX   : '/' (ESC | ~[/\\])* '/' 'i'?;
fragment ESC : '\\' . ;                 // Allow escape of any char, but only / is meaningful

IDENT   : ID_START ID_PART* ;

// Java allows a much wider selection of chars in an identifier, but we're being lazy for now.
// JLS refers to methods on java.lang.Character, but their description is imprecise.

fragment ID_START  : [a-zA-Z_];         // Should be java.lang.Character.isJavaIdentifierStart(int)
fragment ID_PART   : [a-zA-Z_0-9];      // Should be java.lang.Character.isJavaIdentifierPart(int)
