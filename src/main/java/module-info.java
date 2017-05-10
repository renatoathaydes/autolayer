/**
 * Main module of the auto-layer project.
 *
 * It bootstrap a Java 9+ application using an auto-layer descriptor, generated during the build,
 * to separate the Java modules into separate layers.
 */
module autolayer.main {
    requires java.logging;
}
