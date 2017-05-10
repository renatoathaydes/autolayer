package com.athaydes.autolayer;

/**
 * Exception used to indicate an invalid Auto-Layer descriptor.
 */
public class InvalidAutoLayerDescriptor extends RuntimeException {

    public InvalidAutoLayerDescriptor( DescriptorEntry entry, String message ) {
        super( entry.location() + " " + message + ":\n" + entry.line );
    }

}
