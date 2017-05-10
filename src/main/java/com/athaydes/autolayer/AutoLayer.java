package com.athaydes.autolayer;

import java.lang.module.ModuleFinder;
import java.lang.reflect.Layer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * A Java 9+ Auto-Layer.
 */
public class AutoLayer {

    private final AutoLayer[] parents;
    private final Path[] jars;
    private Layer javaLayer;
    private ClassLoader loader;
    private ModuleFinder moduleFinder;

    public static final AutoLayer ROOT = new AutoLayer( Layer.boot(), ClassLoader.getSystemClassLoader() ) {
        @Override
        public void setJavaLayer( Layer javaLayer ) {
            throw new UnsupportedOperationException( "Root module cannot change layer" );
        }

        @Override
        public String toString() {
            return "AutoLayer{ROOT}";
        }
    };

    static AutoLayer root() {
        return ROOT;
    }

    private AutoLayer( Layer layer, ClassLoader loader ) {
        this.parents = new AutoLayer[ 0 ];
        this.jars = new Path[ 0 ];
        this.javaLayer = layer;
        this.loader = loader;
        this.moduleFinder = ModuleFinder.of( jars );
    }

    public AutoLayer( AutoLayer[] parents, String[] jars ) {
        this.parents = parents;
        this.jars = Stream.of( jars ).map( jar -> Paths.get( jar ) ).toArray( Path[]::new );
        this.moduleFinder = ModuleFinder.of( this.jars );
    }

    public Path[] getJars() {
        return this.jars;
    }

    public Set<String> getModuleNames() {
        return moduleFinder.findAll().stream()
                .map( it -> it.descriptor().name() )
                .collect( Collectors.toSet() );
    }

    public Layer getJavaLayer() {
        return javaLayer;
    }

    public void setJavaLayer( Layer javaLayer ) {
        this.javaLayer = javaLayer;
        if ( javaLayer.modules().isEmpty() ) {
            throw new RuntimeException( "Empty Java layer cannot be created as an auto-layer" );
        }
        this.loader = javaLayer.modules().iterator().next().getClassLoader();
    }

    public ClassLoader getLoader() {
        return this.loader;
    }

    public ModuleFinder getModuleFinder() {
        return moduleFinder;
    }

    public AutoLayer[] getParents() {
        return parents;
    }

    @Override
    public boolean equals( Object other ) {
        if ( this == other ) return true;
        if ( other == null || getClass() != other.getClass() ) return false;

        AutoLayer autoLayer = ( AutoLayer ) other;

        return Arrays.equals( parents, autoLayer.parents ) &&
                Arrays.equals( jars, autoLayer.jars );
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode( parents );
        result = 31 * result + Arrays.hashCode( jars );
        return result;
    }

    @Override
    public String toString() {
        String parentsString = ( parents.length == 1 && parents[ 0 ] == ROOT ) ? "ROOT" :
                Arrays.stream( parents )
                        .flatMap( it -> Arrays.stream( it.jars ).map( Object::toString ) )
                        .collect( joining( "," ) );

        return "AutoLayer{" +
                "parents=[" + parentsString +
                "], jars=" + Arrays.toString( jars ) +
                '}';
    }
}
