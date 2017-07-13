package io.protolang;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class Helper {
    public static URLClassLoader classloaderWithCompileElements(MavenProject project, Log logger) throws Exception {
        List<String> resourceElements = new LinkedList<>();
        for ( Resource resource : project.getResources() ) {
            resourceElements.add( resource.getDirectory() );
        }

        List<String> testCompileClasspathElements = project.getTestClasspathElements();
        Set<Artifact> dependencies = project.getDependencyArtifacts();
        List<URL> runtimeUrls = new LinkedList<>();

        for ( Artifact artifact : dependencies ) {
            String scope = artifact.getScope();
            if ( Artifact.SCOPE_COMPILE.equals( scope ) ) {
                try {
                    URL url = artifact.getFile().toURI().toURL();
                    runtimeUrls.add( url );
                    logger.debug( "adding to classloader " + url.toExternalForm() );
                }
                catch ( MalformedURLException e ) {
                    throw new RuntimeException( e );
                }
            }
        }

        for ( String next : resourceElements ) {
            URL url = new File( next ).toURI().toURL();
            runtimeUrls.add( url );
            logger.debug( "adding to classloader resource " + url.toExternalForm() );
        }

        for ( String next : testCompileClasspathElements ) {
            URL url = new File( next ).toURI().toURL();
            runtimeUrls.add( url );
            logger.debug( "adding to classloader compile classpath" + url.toExternalForm() );
        }

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        return new URLClassLoader( runtimeUrls.toArray( new URL[runtimeUrls.size()] ), oldLoader );
    }
}
