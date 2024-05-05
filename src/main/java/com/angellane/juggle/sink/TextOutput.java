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
package com.angellane.juggle.sink;

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.candidate.MemberCandidate;
import com.angellane.juggle.candidate.TypeCandidate;
import com.angellane.juggle.formatter.Formatter;
import com.angellane.juggle.util.ClassUtils;

import java.io.PrintStream;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextOutput implements Sink {
    final Formatter f;

    PrintStream out;
    private final Set<String> imports;

    public TextOutput(List<String> importedPackageNames, PrintStream out, Formatter f) {
        this.imports = Set.copyOf(importedPackageNames);
        this.out = out;
        this.f = f;
    }

    @Override
    public void accept(Candidate candidate) {
        if (candidate instanceof TypeCandidate ct)
            out.println(decode(ct.clazz()));
        else if (candidate instanceof MemberCandidate cm)
            out.println(decode(cm.member()));
    }


    public String decode(Class<?> c) {
        StringBuilder ret = new StringBuilder();

        StringBuilder mods = new StringBuilder();
        mods.append(decodeModifiers(c.getModifiers(), false));
        if (ClassUtils.classIsSealed(c))    mods.append("sealed ");
        if (ClassUtils.classIsNonSealed(c)) mods.append("non-sealed ");

        ret.append(f.formatKeyword(mods.toString()));

        ret.append(f.formatKeyword(decodeTypeKind(c)));
        ret.append(" ");
        ret.append(f.formatClassName(decodeClass(c)));

        if (c.getSuperclass() != null
                && !c.getSuperclass().equals(Object.class)
                && !c.isRecord() && !c.isEnum()
        ) {
            ret.append(f.formatKeyword(" extends "));
            ret.append(f.formatType(decodeClass(c.getSuperclass())));
        }

        if (c.getInterfaces().length > 0 && !c.isAnnotation()) {
            ret.append(f.formatKeyword(" implements "));
            ret.append(
                    Arrays.stream(c.getInterfaces())
                            .map(this::decodeClass)
                            .map(f::formatType)
                            .collect(Collectors.joining(
                                    f.formatPunctuation(", "))
                            )
            );
        }

        if (c.getPermittedSubclasses() != null
                && c.getPermittedSubclasses().length > 0) {
            ret.append(f.formatKeyword(" permits "));
            ret.append(
                    Arrays.stream(c.getPermittedSubclasses())
                            .map(this::decodeClass)
                            .map(f::formatType)
                            .collect(Collectors.joining(
                                    f.formatPunctuation(", "))
                            )
            );
        }

        return ret.toString();
    }

    public String decode(Member m) {
        StringBuilder ret = new StringBuilder();

        // Default methods aren't flagged explicitly in modifiers
        boolean isDefault = m instanceof Method method && method.isDefault();

        ret.append(f.formatKeyword(decodeModifiers(m.getModifiers(), isDefault)));

        Executable e = (m instanceof Executable) ? (Executable)m : null;

        if (e != null)
            ret.append(decodeTypeParameters(e.getTypeParameters()));

        ret.append(decodeDeclType(m));
        ret.append(f.formatClassName(decodeType(m.getDeclaringClass())));

        ret.append('.');
        ret.append(f.formatMethodName(m instanceof Constructor ? "<init>" : m.getName()));

        if (e != null) {
            ret.append(decodeParams(e.getParameters()));
            ret.append(decodeThrows(e.getGenericExceptionTypes()));
        }

        return ret.toString();
    }

    public String decodeModifiers(int mods, boolean isDefault) {
        // Modifier.toString() includes "interface" in its output, but we
        // don't want it -- that's handled by decodeTypeKind()
        int tweakedMods = mods & (~Modifier.INTERFACE);

        StringBuilder ret = new StringBuilder(Modifier.toString(tweakedMods));

        if (isDefault) {
            if (!ret.isEmpty()) ret.append(' ');
            ret.append("default");
        }

        if (!ret.isEmpty())
            ret.append(' ');

        return ret.toString();
    }

    public String decodeTypeKind(Class<?> type) {
             if (type.isRecord())       return "record";
        else if (type.isAnnotation())   return "@interface";
        else if (type.isInterface())    return "interface";
        else if (type.isEnum())         return "enum";
        else if (!type.isPrimitive())   return "class";
        else                            return "";
    }

    public String decodeTypeParameters(TypeVariable<?>[] typeVariables) {
        if (typeVariables.length == 0)
            return "";
        else {
            StringBuilder ret = new StringBuilder("<");
            StringJoiner joiner = new StringJoiner(",");
            for (TypeVariable<?> tv : typeVariables) {
                joiner.add(tv.getName());
                // This would be a good place to add bounds and handle wildcards
            }
            ret.append(joiner);
            ret.append("> ");
            return ret.toString();
        }
    }

    public String decodeDeclType(Member m) {
        if (m instanceof Method)
            return decodeType(((Method) m).getGenericReturnType()) + ' ';
        else if (m instanceof Field)
            return decodeType(((Field) m).getGenericType()) + ' ';
        else    // m instanceof Constructor
            return "";   // Nothing to do
    }

    public String decodeType(Type t) {
        return decodeType(t, null);
    }

    public String decodeType(Type t, boolean isVarArgs) {
        return decodeType(t, null, isVarArgs);
    }

    public String decodeType(Type t, Type[] actualTypeArgs) {
        return decodeType(t, actualTypeArgs, false);
    }

    public String decodeType(Type t, Type[] actualTypeArgs, boolean isVarArgs) {
        /* In a JDK17 world...
        return switch (t) {
            case GenericArrayType   ga  -> decodeGenericArrayType(ga);
            case ParameterizedType  pt  -> decodeParameterizedType(pt);
            case TypeVariable<?>    tv  -> decodeTypeVariable(tv);
            case WildcardType       wt  -> decodeWildcardType(wt);
            case Class<?>           cl  -> decodeClass(cl, actualTypeArgs);
            default                     -> t.toString();
        };
        */
             if (t instanceof GenericArrayType)     return decodeGenericArrayType((GenericArrayType)t, isVarArgs);
        else if (t instanceof ParameterizedType)    return decodeParameterizedType((ParameterizedType)t);
        else if (t instanceof TypeVariable<?>)      return decodeTypeVariable((TypeVariable<?>)t);
        else if (t instanceof WildcardType)         return decodeWildcardType((WildcardType)t);
        else if (t instanceof Class<?>)             return decodeClass((Class<?>)t, actualTypeArgs, isVarArgs);
        else                                        return t.toString();
    }

    public String decodeGenericArrayType(GenericArrayType t, boolean isVarArgs) {
        return decodeType(t.getGenericComponentType()) + (isVarArgs ? "..." : "[]");
    }

    public String decodeParameterizedType(ParameterizedType t) {
        // We need to pass the actual type arguments down into the decoding
        // layer since otherwise the only info available is the name of the
        // type parameters

        return decodeType(t.getRawType(), t.getActualTypeArguments());
    }

    public <T extends GenericDeclaration> String decodeTypeVariable(TypeVariable<T> t) {
        // We don't need to emit bounds here; the TypeVariable is the
        // case when a type parameter is declared in a signature

        return t.getName();
    }

    private String decodeBounds(String keyword, Type[] bs) {
        if (bs != null && bs.length > 0) {
            return " " + keyword + " " +
                    Stream.of(bs)
                            .map(this::decodeType)
                            .collect(Collectors.joining(" & "));
        }
        else
            return "";
    }

    public String decodeWildcardType(WildcardType t) {
        var lb = t.getLowerBounds();
        var ub = t.getUpperBounds();

        if (ub.length == 1 && ub[0] == Object.class) ub = null;

        return "?"
                + decodeBounds("extends", ub)
                + decodeBounds("super",   lb);
    }

    private String sanitisedClassName(Class<?> c) {
        StringBuilder ret = new StringBuilder();

        String packageName = c.getPackageName();

        if (!imports.contains(packageName))
            ret.append(packageName).append('.');

        String canonicalName = c.getCanonicalName();

        // CanonicalName will be null for lambdas, etc.
        if (canonicalName == null) canonicalName = c.getName();

        // Knock off the "packageName." prefix
        // This isn't the same as getSimpleName() since that doesn't include
        // the names of enclosing outer classes

        ret.append(canonicalName.substring(packageName.length()+1));

        return ret.toString();
    }

    public String decodeClass(Class<?> c) {
        return decodeClass(c, null, false);
    }

    public String decodeClass(Class<?> c, Type[] actualTypeArguments, boolean isVarArgs) {
        // Emit the type name, taking into account generics and imports

        if (c.isPrimitive())
            return c.getName();
        else if (c.isArray())
            return decodeType(c.getComponentType()) + (isVarArgs ? "..." : "[]");
        else {
            StringBuilder ret = new StringBuilder(sanitisedClassName(c));

            if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                // This branch is hit when type arguments have been supplied
                ret
                        .append('<')
                        .append(
                            Stream.of(actualTypeArguments)
                                    .map(this::decodeType)
                                    .collect(Collectors.joining(","))
                            )
                        .append('>');
            }
            else {
                // This is hit when there are no type arguments, but there
                // may still be type parameters
                TypeVariable<?>[] typeVars = c.getTypeParameters();

                if (typeVars.length > 0) {
                    ret.append('<');
                    StringJoiner j = new StringJoiner(",");
                    for (var tv : typeVars)
                        j.add(tv.toString());
                    ret.append(j);
                    ret.append('>');
                }
            }

            return ret.toString();
        }
    }

    public String decodeParams(Parameter[] parameters) {
        return "(" +
                Arrays.stream(parameters)
                        .map(p -> decodeType(p.getParameterizedType(), p.isVarArgs()))
                        .collect(Collectors.joining(",")) +
                ")";
    }

    public String decodeThrows(Type[] exceptionTypes) {
        if (exceptionTypes.length == 0)
            return "";
        else
            return " throws " +
                    Arrays.stream(exceptionTypes)
                            .map(this::decodeType)
                            .collect(Collectors.joining(","));
    }
}
