package com.athaydes.autolayer;

import java.io.File;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Layer;
import java.lang.reflect.Method;
import java.lang.reflect.Module;
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
        if ( args.length == 1 && ( args[ 0 ].equals( "-h" ) || args[ 0 ].equals( "--help" ) ) ) {
            printUsage();
            return;
        }

        if ( args.length < 2 ) {
            System.err.println( "Missing arguments" );
            printUsage();
            System.exit( 1 );
        }

        File descriptor = new File( args[ 0 ] );
        if ( !descriptor.isFile() ) {
            System.err.println( "Not a file: " + descriptor.getAbsolutePath() );
            System.exit( 2 );
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
            System.err.println( "Invalid module/class: " + args[ 1 ] );
            System.exit( 3 );
            return;
        }

        AutoLayer[] layers = new DescriptorParser().parse( descriptor );

        Module module = createJavaLayersAndGetModule( moduleName, packageName, layers );

        Layer[] javaLayers = Arrays.stream( layers )
                .map( AutoLayer::getJavaLayer )
                .toArray( Layer[]::new );

        if ( log.isLoggable( Level.FINE ) ) {
            log.fine( "Layers: " + Arrays.toString( javaLayers ) );
        }

        if ( module == null ) {
            System.err.println( "Could not find module: " + moduleName );
            return;
        }

        String[] programArgs = Arrays.stream( args ).skip( 2 ).toArray( String[]::new );

        runMainClass( mainClass, module, programArgs );

        log.fine( "AutoLayers main() exiting" );
    }

    private static void printUsage() {
        System.out.println( "Usage:" );
        System.out.println();
        System.out.println( Main.class.getName() + " --help -h" );
        System.out.println( "    Show help/usage." );
        System.out.println();
        System.out.println( Main.class.getName() + " layers-descriptor module-name/main-class [program-args]" );
        System.out.println( "    Using the provided layers-descriptor file, run module-name/main-class, passing" +
                " any further arguments to the program." );
    }

    private static String extractPackageName( String mainClass ) {
        int index = mainClass.lastIndexOf( '.' );
        return mainClass.substring( 0, Math.max( index, 0 ) );
    }

    private static Module createJavaLayersAndGetModule( String moduleName,
                                                        String packageName,
                                                        AutoLayer[] layers ) {
        Module module = null;

        for (int index = 1; index < layers.length; index++) {
            AutoLayer autoLayer = layers[ index ];

            if ( log.isLoggable( Level.FINE ) ) {
                log.fine( "Layer " + index + ": " + autoLayer + ", modules " + autoLayer.getModuleNames() );
            }

            List<Layer> parents = Arrays.stream( autoLayer.getParents() )
                    .map( AutoLayer::getJavaLayer )
                    .collect( Collectors.toList() );

            List<Configuration> parentConfigurations = parents.stream()
                    .map( Layer::configuration )
                    .collect( Collectors.toList() );

            Configuration cf = Configuration.resolve(
                    autoLayer.getModuleFinder(), parentConfigurations, ModuleFinder.of(),
                    autoLayer.getModuleNames() );

            Layer.Controller controller = Layer.defineModulesWithOneLoader(
                    cf, parents, layers[ index - 1 ].getLoader() );

            Layer layer = controller.layer();

            autoLayer.setJavaLayer( layer );

            if ( autoLayer.getModuleNames().contains( moduleName ) ) {
                module = layer.findModule( moduleName )
                        .orElseThrow( () -> new RuntimeException( "Auto-layer contains module, " +
                                "but Java Layer does not: " + moduleName ) );

                controller.addOpens( module, packageName, Main.class.getModule() );
            }
        }

        return module;
    }

    private static void runMainClass( String mainClass,
                                      Module module,
                                      String[] programArgs ) throws Exception {
        Class<?> main = module.getClassLoader().loadClass( mainClass );
        Method mainMethod = main.getMethod( "main", String[].class );
        mainMethod.invoke( main, ( Object ) programArgs );
    }

}
