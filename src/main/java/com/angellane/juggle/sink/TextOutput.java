package com.angellane.juggle.sink;

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.candidate.MemberCandidate;
import com.angellane.juggle.candidate.TypeCandidate;
import com.angellane.juggle.formatter.Formatter;

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

        ret.append(f.formatKeyword(decodeModifiers(c.getModifiers())));

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
                            .collect(Collectors.joining(f.formatKeyword(", ")))
            );
        }

        return ret.toString();
    }

    public String decode(Member m) {
        StringBuilder ret = new StringBuilder();

        ret.append(f.formatKeyword(decodeModifiers(m.getModifiers())));

        Executable e = (m instanceof Executable) ? (Executable)m : null;

        if (e != null)
            ret.append(decodeTypeParameters(e.getTypeParameters()));

        ret.append(decodeDeclType(m));
        ret.append(f.formatClassName(decodeType(m.getDeclaringClass())));

        ret.append('.');
        ret.append(f.formatMethodName(m instanceof Constructor ? "<init>" : m.getName()));

        if (e != null) {
            ret.append(decodeParams(e.getGenericParameterTypes()));
            ret.append(decodeThrows(e.getGenericExceptionTypes()));
        }

        return ret.toString();
    }

    public String decodeModifiers(int mods) {
        // Modifier.toString() includes "interface" in its output, but we
        // don't want it -- that's handled by decodeTypeKind()
        int tweakedMods = mods & (~Modifier.INTERFACE);

        StringBuilder ret = new StringBuilder(Modifier.toString(tweakedMods));

        if (ret.length() > 0)
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
        /* In a JDK17 world...
        return switch (t) {
            case GenericArrayType   ga  -> decodeGenericArrayType(ga);
            case ParameterizedType  pt  -> decodeParameterizedType(pt);
            case TypeVariable<?>    tv  -> decodeTypeVariable(tv);
            case WildcardType       wt  -> decodeWildcardType(wt);
            case Class<?>           cl  -> decodeClass(cl);
            default                     -> t.toString();
        };
        */
             if (t instanceof GenericArrayType)     return decodeGenericArrayType((GenericArrayType)t);
        else if (t instanceof ParameterizedType)    return decodeParameterizedType((ParameterizedType)t);
        else if (t instanceof TypeVariable<?>)      return decodeTypeVariable((TypeVariable<?>)t);
        else if (t instanceof WildcardType)         return decodeWildcardType((WildcardType)t);
        else if (t instanceof Class<?>)             return decodeClass((Class<?>)t);
        else                                        return t.toString();
    }

    public String decodeGenericArrayType(GenericArrayType t) {
        return decodeType(t.getGenericComponentType()) + "[]";
    }

    public String decodeParameterizedType(ParameterizedType t) {
        return decodeType(t.getRawType());
    }

    public <T extends GenericDeclaration> String decodeTypeVariable(TypeVariable<T> t) {
        var bs = t.getBounds();
        if (bs.length == 1 && bs[0] == Object.class) bs = null;

        return t.getName() + (bs == null || bs.length == 0 ? ""
                : " extends " + Stream.of(bs).map(this::decodeType).collect(Collectors.joining(", ")));
    }

    public String decodeWildcardType(WildcardType t) {
        var lb = t.getLowerBounds();
        var ub = t.getUpperBounds();

        if (ub.length == 1 && ub[0] == Object.class) ub = null;

        StringBuilder sb = new StringBuilder(t.getTypeName());

        if (ub != null && ub.length > 0) {
            sb.append(" extends ");
            sb.append(Stream.of(ub).map(this::decodeType).collect(Collectors.joining(", ")));
        }

        if (lb != null && lb.length > 0) {
            sb.append(" super ");
            sb.append(Stream.of(lb).map(this::decodeType).collect(Collectors.joining(", ")));
        }

        return sb.toString();
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

            if (!imports.contains(packageName))
                ret.append(packageName).append('.');

            // Knock off the "packageName." prefix
            ret.append((canonicalName != null ? canonicalName : c.getName())
                    .substring(packageName.length()+1));

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
