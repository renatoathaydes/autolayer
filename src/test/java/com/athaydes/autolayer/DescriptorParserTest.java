package com.athaydes.autolayer;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DescriptorParserTest {

    @Test
    public void canParseOneLineDescriptors() {
        List<String> lines = Collections.singletonList( "abc.jar" );

        AutoLayer[] layers = new DescriptorParser().parse( "descriptor.file", lines );

        assertEquals( 2, layers.length );
        assertEquals( AutoLayer.ROOT, layers[ 0 ] );
        assertArrayEquals( new Path[]{ Paths.get( "abc.jar" ) }, layers[ 1 ].getJars() );
        assertArrayEquals( new AutoLayer[]{ AutoLayer.ROOT }, layers[ 1 ].getParents() );
    }

    @Test
    public void canParseMultiLineDescriptors() {
        List<String> lines = Arrays.asList( "abc.jar", "def.jar|ghi.jar" );

        AutoLayer[] layers = new DescriptorParser().parse( "descriptor.file", lines );

        assertEquals( 3, layers.length );
        assertEquals( AutoLayer.ROOT, layers[ 0 ] );
        assertArrayEquals( new Path[]{ Paths.get( "abc.jar" ) }, layers[ 1 ].getJars() );
        assertArrayEquals( new AutoLayer[]{ AutoLayer.ROOT }, layers[ 1 ].getParents() );

        assertArrayEquals( new Path[]{ Paths.get( "def.jar" ), Paths.get( "ghi.jar" ) }, layers[ 2 ].getJars() );
        assertArrayEquals( new AutoLayer[]{ layers[ 1 ] }, layers[ 2 ].getParents() );
    }

    @Test
    public void canParseExplicitLayerDeclarations() {
        List<String> lines = Arrays.asList(
                "(0)abc.jar",
                "(1)def.jar|ghi.jar",
                "(1)jkl.jar",
                "(2, 3)mno.jar" );

        AutoLayer[] layers = new DescriptorParser().parse( "descriptor.file", lines );

        assertEquals( 5, layers.length );
        assertEquals( AutoLayer.ROOT, layers[ 0 ] );

        assertArrayEquals( new Path[]{ Paths.get( "abc.jar" ) }, layers[ 1 ].getJars() );
        assertArrayEquals( new AutoLayer[]{ AutoLayer.ROOT }, layers[ 1 ].getParents() );

        assertArrayEquals( new Path[]{ Paths.get( "def.jar" ), Paths.get( "ghi.jar" ) }, layers[ 2 ].getJars() );
        assertArrayEquals( new AutoLayer[]{ layers[ 1 ] }, layers[ 2 ].getParents() );

        assertArrayEquals( new Path[]{ Paths.get( "jkl.jar" ) }, layers[ 3 ].getJars() );
        assertArrayEquals( new AutoLayer[]{ layers[ 1 ] }, layers[ 3 ].getParents() );

        assertArrayEquals( new Path[]{ Paths.get( "mno.jar" ) }, layers[ 4 ].getJars() );
        assertArrayEquals( new AutoLayer[]{ layers[ 2 ], layers[ 3 ] }, layers[ 4 ].getParents() );
    }


}