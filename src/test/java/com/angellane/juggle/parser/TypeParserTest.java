package com.angellane.juggle.parser;

import com.angellane.juggle.Juggler;
import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.query.*;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TypeParserTest {
    Juggler juggler = new Juggler();
    QueryFactory factory = new QueryFactory(juggler);

    private TypeQuery typeQueryFor(final String decl) {
        Query<?> q = factory.createQuery(decl);

        if (q instanceof TypeQuery typeQuery)
            return typeQuery;
        else {
            fail("Query is not a TypeQuery");
            return null;
        }
    }

    @Test
    public void testTypeFlavour() {
        TypeQuery actualQuery = typeQueryFor("class");

        TypeQuery expectedQuery = new TypeQuery();
        expectedQuery.setAccessibility(Accessibility.PUBLIC);
        expectedQuery.flavour = TypeFlavour.CLASS;

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testClassName() {
        TypeQuery actualQuery = typeQueryFor("class String");

        TypeQuery expectedQuery = new TypeQuery();
        expectedQuery.setAccessibility(Accessibility.PUBLIC);
        expectedQuery.flavour = TypeFlavour.CLASS;
        expectedQuery.setNameExact("String");

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testClassExactExtends() {
        TypeQuery actualQuery = typeQueryFor("class extends String");

        TypeQuery expectedQuery = new TypeQuery();
        expectedQuery.setAccessibility(Accessibility.PUBLIC);
        expectedQuery.flavour = TypeFlavour.CLASS;
        expectedQuery.setSupertype(BoundedType.exactType(String.class));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testClassBoundedExtends() {
        TypeQuery actualQuery = typeQueryFor("class extends ? super String");

        TypeQuery expectedQuery = new TypeQuery();
        expectedQuery.setAccessibility(Accessibility.PUBLIC);
        expectedQuery.flavour = TypeFlavour.CLASS;
        expectedQuery.setSupertype(BoundedType.supertypeOf(String.class));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testClassImplements() {
        TypeQuery actualQuery = typeQueryFor(
                "class implements java.io.Serializable, " +
                "? extends java.lang.reflect.AnnotatedType");

        TypeQuery expectedQuery = new TypeQuery();
        expectedQuery.flavour = TypeFlavour.CLASS;
        expectedQuery.setAccessibility(Accessibility.PUBLIC);
        expectedQuery.setSuperInterfaces(Set.of(
                BoundedType.exactType(java.io.Serializable.class),
                BoundedType.subtypeOf(java.lang.reflect.AnnotatedType.class)
                )
        );

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testClassPermits() {
        TypeQuery actualQuery = typeQueryFor(
                "class permits Integer, ? extends Long");

        TypeQuery expectedQuery = new TypeQuery();
        expectedQuery.flavour = TypeFlavour.CLASS;
        expectedQuery.setAccessibility(Accessibility.PUBLIC);
        expectedQuery.setPermittedSubtypes(Set.of(
                BoundedType.exactType(java.lang.Integer.class),
                BoundedType.subtypeOf(java.lang.Long.class)
        ));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testInterfaceExtends() {
        TypeQuery actualQuery = typeQueryFor(
                "interface extends java.lang.reflect.AnnotatedType," +
                " ? super java.lang.reflect.TypeVariable," +
                " ? extends java.lang.reflect.AnnotatedElement," +
                " ?");

        TypeQuery expectedQuery = new TypeQuery();
        expectedQuery.flavour = TypeFlavour.INTERFACE;
        expectedQuery.setSuperInterfaces(Set.of(
                BoundedType.exactType(java.lang.reflect.AnnotatedType.class),
                BoundedType.supertypeOf(java.lang.reflect.TypeVariable.class),
                BoundedType.subtypeOf(java.lang.reflect.AnnotatedElement.class),
                BoundedType.unboundedWildcardType()
                )
        );

        assertEquals(expectedQuery, actualQuery);
    }


    @Test
    public void testInterfacePermits() {
        TypeQuery actualQuery = typeQueryFor(
                "interface permits java.lang.reflect.AnnotatedType");

        TypeQuery expectedQuery = new TypeQuery();
        expectedQuery.flavour = TypeFlavour.INTERFACE;
        expectedQuery.setPermittedSubtypes(Set.of(
                BoundedType.exactType(java.lang.reflect.AnnotatedType.class)
        ));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testAnnotationDecl() {
        TypeQuery actualQuery = typeQueryFor(
                "@interface Deprecated");

        TypeQuery expectedQuery = new TypeQuery();
        expectedQuery.flavour = TypeFlavour.ANNOTATION;
        expectedQuery.setNameExact("Deprecated");

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testMetaAnnotation() {
        TypeQuery actualQuery = typeQueryFor(
                "@Deprecated @interface");

        TypeQuery expectedQuery = new TypeQuery();
        expectedQuery.flavour = TypeFlavour.ANNOTATION;
        expectedQuery.addAnnotationType(Deprecated.class);

        assertEquals(expectedQuery, actualQuery);
    }

}
