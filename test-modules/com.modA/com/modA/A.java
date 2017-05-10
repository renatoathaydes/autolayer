package com.modA;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class A {

    public List<String> getStrings() {
        return ImmutableList.of( "a", "b", "c" );
    }

    public void handle( Throwable throwable ) {
        Throwables.throwIfInstanceOf( throwable, RuntimeException.class );
    }

}
