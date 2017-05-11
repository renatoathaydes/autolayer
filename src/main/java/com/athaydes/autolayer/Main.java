package com.athaydes.autolayer;

import java.io.File;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Layer;
import java.lang.reflect.Method;
import java.lang.reflect.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Auto-layer bootstrap class.
 */
public class Main {

    private static final Logger log = Logger.getLogger( Main.class.getName() );

    public static void main( String[] args ) throws Throwable {
        if ( args.length != 2 ) {
            System.err.println( "Please provide a auto-layer descriptor and a runnable module/class as arguments." );
            return;
        }

        File descriptor = new File( args[ 0 ] );
        if ( !descriptor.isFile() ) {
            System.err.println( "Not a file: " + descriptor.getAbsolutePath() );
            return;
        }

        String[] mainModuleAndClass = args[ 1 ].split( "/" );
        String moduleName;
        String mainClass;
        String packageName;

        if ( mainModuleAndClass.length == 2 ) {
            moduleName = mainModuleAndClass[ 0 ];
            mainClass = mainModuleAndClass[ 1 ];
            packageName = extractPackageName( mainClass );

            if ( packageName.isEmpty() ) {
                System.err.println( "Invalid class name (missing package): " + mainClass );
            }
        } else {
            System.err.println( "Invalid module/class: " + args[ 2 ] );
            return;
        }

        AutoLayer[] layers = new DescriptorParser().parse( descriptor );

        AutoLayer parentLayer = null;

        int i = 1;

        Module module = null;

        for (AutoLayer autoLayer : layers) {
            if ( parentLayer == null ) {
                parentLayer = autoLayer; // root layer
                continue;
            }

            if ( log.isLoggable( Level.FINE ) ) {
                log.fine( "Layer " + i++ + ": " + autoLayer + ", modules " + autoLayer.getModuleNames() );
            }

            List<Layer> parents = new ArrayList<>( autoLayer.getParents().length );
            for (AutoLayer parent : autoLayer.getParents()) {
                parents.add( parent.getJavaLayer() );
            }

            List<Configuration> parentConfigurations = parents.stream()
                    .map( Layer::configuration )
                    .collect( Collectors.toList() );

            Configuration cf = Configuration.resolve(
                    autoLayer.getModuleFinder(), parentConfigurations, ModuleFinder.of(),
                    autoLayer.getModuleNames() );

            Layer.Controller controller = Layer.defineModulesWithOneLoader( cf, parents, parentLayer.getLoader() );
            Layer layer = controller.layer();

            autoLayer.setJavaLayer( layer );

            if ( autoLayer.getModuleNames().contains( moduleName ) ) {
                module = layer.findModule( moduleName )
                        .orElseThrow( () -> new RuntimeException( "Auto-layer contains module, " +
                                "but Java Layer does not: " + moduleName ) );

                controller.addOpens( module, packageName, Main.class.getModule() );
            }

            parentLayer = autoLayer;
        }

        Layer[] javaLayers = Arrays.stream( layers )
                .map( AutoLayer::getJavaLayer )
                .toArray( Layer[]::new );

        if ( log.isLoggable( Level.FINE ) ) {
            log.fine( "Layers: " + Arrays.toString( javaLayers ) );
        }

        if ( module == null ) {
            throw new RuntimeException( "Could not find module: " + moduleName );
        }

        Class<?> main = module.getClassLoader().loadClass( mainClass );
        Method mainMethod = main.getMethod( "main", String[].class );
        mainMethod.invoke( main, ( Object ) new String[ 0 ] );

        log.fine( "AutoLayers main() exiting" );
    }

    private static String extractPackageName( String mainClass ) {
        int index = mainClass.lastIndexOf( '.' );
        return mainClass.substring( 0, Math.max( index, 0 ) );
    }

}
