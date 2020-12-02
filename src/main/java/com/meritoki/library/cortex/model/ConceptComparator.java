package com.meritoki.library.cortex.model;

import java.util.Comparator;

public class ConceptComparator implements Comparator<Concept> {
    @Override
    public int compare(Concept o1, Concept o2) {
        return new Double(o1.rank).compareTo(new Double(o2.rank));
    }
}
