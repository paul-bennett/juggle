package com.angellane.juggle.parser;

import com.angellane.juggle.Juggler;
import com.angellane.juggle.query.Query;
import com.angellane.juggle.query.QueryFactory;
import com.angellane.juggle.query.TypeFlavour;
import com.angellane.juggle.query.TypeQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TypeParserTest {
    Juggler juggler = new Juggler();
    QueryFactory factory = new QueryFactory(juggler);

    private TypeQuery typeQueryFor(final String decl) {
        Query q = factory.createQuery(decl);

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
        expectedQuery.flavour = TypeFlavour.CLASS;

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testClassName() {
        TypeQuery actualQuery = typeQueryFor("class String");

        TypeQuery expectedQuery = new TypeQuery();
        expectedQuery.flavour = TypeFlavour.CLASS;
        expectedQuery.setNameExact("String");

        assertEquals(expectedQuery, actualQuery);
    }


}
