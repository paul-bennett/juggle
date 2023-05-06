package com.angellane.juggle.candidate;

import java.lang.reflect.Modifier;

public interface Candidate {
    int ACCESS_MODIFIERS_MASK = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;
    int OTHER_MODIFIERS_MASK  = ~ACCESS_MODIFIERS_MASK;

}
