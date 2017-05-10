package com.athaydes.autolayer;

/**
 * A single descriptor entry in a Auto-Layer descriptor.
 */
class DescriptorEntry {
    final String fileName;
    final String line;
    final int lineNumber;

    public DescriptorEntry( String fileName, String line, int lineNumber ) {
        this.fileName = fileName;
        this.line = line;
        this.lineNumber = lineNumber;
    }

    public String location() {
        return fileName + ":" + lineNumber;
    }
}
