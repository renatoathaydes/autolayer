
package com.modB;

import com.google.common.collect.Iterators;

import java.util.Iterator;
import java.util.List;

public class B {

    public List<String> getMoreStrings() {
        return List.of( "d", "e", "f" );
    }

    public Iterator<String> empty() {
        return Iterators.emptyIterator();
    }

}