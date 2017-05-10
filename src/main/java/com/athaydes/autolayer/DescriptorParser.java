package com.athaydes.autolayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser of Auto-Layer descriptors.
 */
public class DescriptorParser {

    public AutoLayer[] parse( File descriptor ) throws IOException {
        List<String> lines = Files.readAllLines( descriptor.toPath() );
        return parse( descriptor.getName(), lines );
    }

    public AutoLayer[] parse( String fileName, List<String> lines ) {
        List<DescriptorEntry> entries = new ArrayList<>( lines.size() );

        int lineNumber = 1;

        for (String line : lines) {
            if ( !line.isEmpty() && !line.startsWith( "//" ) ) {
                entries.add( new DescriptorEntry( fileName, line, lineNumber ) );
            }
            lineNumber++;
        }

        Pattern descriptorLinePattern = Pattern.compile(
                "(\\(\\s*(?<layers>\\d+(\\s*,\\d+\\s*)?)?\\s*\\)\\s*)?(?<files>.+)" );

        AutoLayer[] layers = new AutoLayer[ entries.size() + 1 ];

        layers[ 0 ] = AutoLayer.ROOT;

        for (int i = 0; i < entries.size(); i++) {
            DescriptorEntry entry = entries.get( i );
            Matcher matcher = descriptorLinePattern.matcher( entry.line );
            if ( matcher.matches() ) {
                int[] parentLayerIndexes = parseParentLayers( i, entry, matcher.group( "layers" ) );

                AutoLayer[] parents = new AutoLayer[ parentLayerIndexes.length ];
                int j = 0;
                for (int index : parentLayerIndexes) {
                    parents[ j ] = layers[ index ];
                    j++;
                }

                String files = matcher.group( "files" );

                AutoLayer layer = new AutoLayer( parents, files.split( "\\s*\\|\\s*" ) );
                layers[ i + 1 ] = layer;
            } else {
                throw new InvalidAutoLayerDescriptor( entry, "Not a valid layer declaration" );
            }
        }

        return layers;
    }

    private static int[] parseParentLayers( int layerIndex,
                                            DescriptorEntry entry,
                                            String layersDeclaration ) {
        if ( layersDeclaration == null || layersDeclaration.isEmpty() ) {
            // by default, the parent layer is the previously declared one (0 is the boot-layer)
            return new int[]{ layerIndex };
        }

        String[] layers = layersDeclaration.split( "\\s*,\\s*" );
        int[] result = new int[ layers.length ];

        for (int i = 0; i < result.length; i++) {
            result[ i ] = Integer.parseInt( layers[ i ] );
            if ( result[ i ] > layerIndex ) {
                throw new InvalidAutoLayerDescriptor( entry,
                        "reference to subsequent layer is not allowed. Layer " +
                                ( layerIndex + 1 ) + " refers to Layer " + result[ i ] );
            }
        }

        return result;
    }

}
