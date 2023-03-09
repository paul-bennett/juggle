package com.angellane.juggle;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MemberDecoder {
    private final Set<String> imports;

    public MemberDecoder(List<String> importedPackageNames) {
        this.imports = Set.copyOf(importedPackageNames);
    }

    public String decode(Member m) {
        StringBuilder ret = new StringBuilder();

        Executable e = (m instanceof Executable) ? (Executable)m : null;

        ret.append(decodeModifiers(m.getModifiers()));

        if (e != null)
            ret.append(decodeTypeParameters(e.getTypeParameters()));

        ret.append(decodeDeclType(m));
        ret.append(decodeType(m.getDeclaringClass()));

        ret.append('.');
        ret.append(m instanceof Constructor ? "<init>" : m.getName());

        if (e != null) {
            ret.append(decodeParams(e.getGenericParameterTypes()));
            ret.append(decodeThrows(e.getGenericExceptionTypes()));
        }

        return ret.toString();
    }

    public String decodeModifiers(int mods) {
        // To reduce clutter we're only interested in a subset of the modifiers
        StringBuilder ret = new StringBuilder(Modifier.toString(
                mods & (Modifier.STATIC
                        | Modifier.PUBLIC
                        | Modifier.PROTECTED
                        | Modifier.PRIVATE
                )
        ));

        if (ret.length() > 0)
            ret.append(' ');

        return ret.toString();
    }

    public String decodeTypeParameters(TypeVariable<?>[] typeVariables) {
        if (typeVariables == null || typeVariables.length == 0)
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
        if (t instanceof GenericArrayType) {
            var ga = (GenericArrayType)t;
            return decodeType(ga.getGenericComponentType()) + "[]";
        }
        else if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)t;
            return decodeType(pt.getRawType());
        }
        else if (t instanceof TypeVariable<?>) {
            var tv = (TypeVariable<?>)t;
            var bs = tv.getBounds();
            if (bs.length == 1 && bs[0] == Object.class) bs = null;

            return tv.getName() + (bs == null || bs.length == 0 ? ""
                    : " extends " + Stream.of(bs).map(this::decodeType).collect(Collectors.joining(", ")));
        }
        else if (t instanceof WildcardType) {
            var wt = (WildcardType)t;
            return "WILDCARD(" + wt + ")";
        }
        else if (t instanceof Class<?>)
            return decodeClass((Class<?>)t);
        else
            return t.toString();
    }

    public String decodeClass(Class<?> c) {
        // Emit the type name, taking into account generics and imports

        if (c.isPrimitive())
            return c.getName();
        else if (c.isArray())
            return decodeType(c.getComponentType()) + "[]";
        else {
            StringBuilder ret = new StringBuilder();

            String canonicalName = c.getCanonicalName();
            String packageName = c.getPackageName();

            if (canonicalName == null)
                System.out.println("/// " + c);
            else if (imports.contains(packageName))
                // Knock off the "packageName." prefix
                ret.append(canonicalName.substring(packageName.length()+1));
            else
                ret.append(canonicalName);

            TypeVariable<?>[] typeVars = c.getTypeParameters();

            if (typeVars.length > 0) {
                ret.append('<');
                StringJoiner j = new StringJoiner(",");
                for (var tv : typeVars)
                    j.add(tv.toString());
                ret.append(j);
                ret.append('>');
            }

            return ret.toString();
        }
    }

    public String decodeParams(Type[] parameterTypes) {
        if (parameterTypes == null)
            return "";
        else
            return "(" +
                    Arrays.stream(parameterTypes)
                            .map(this::decodeType)
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
