package io.protolang;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "gen")
public class ProtolangGenMojo extends AbstractMojo {
    @Parameter(property = "gen.input", defaultValue = "${project.basedir}/src/main/protolang")
    private File[] inputs;

    @Parameter(property = "gen.output", defaultValue = "${project.build.directory}/generated-sources")
    private File output;

    @Parameter(property = "gen.debug", defaultValue = "false")
    private Boolean debug;

    @Parameter(property = "gen.version", defaultValue = "${project.version}")
    private String version;

    @Parameter(property = "gen.failOnErrors", defaultValue = "true")
    private Boolean failOnErrors;

    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            List<String> resourceElements = new LinkedList<>();
            for ( Resource resource : project.getResources() ) {
                resourceElements.add( resource.getDirectory() );
            }

            List<String> compileClasspathElements = project.getCompileClasspathElements();
            Set<Artifact> dependencies = project.getDependencyArtifacts();
            List<URL> runtimeUrls = new LinkedList<>();

            for ( Artifact artifact : dependencies ) {
                String scope = artifact.getScope();
                if ( Artifact.SCOPE_COMPILE.equals( scope ) ) {
                    try {
                        URL url = artifact.getFile().toURI().toURL();
                        runtimeUrls.add( url );
                        getLog().debug( "adding to classloader " + url.toExternalForm() );
                    }
                    catch ( MalformedURLException e ) {
                        throw new RuntimeException( e );
                    }
                }
            }

            for ( String next : resourceElements ) {
                URL url = new File( next ).toURI().toURL();
                runtimeUrls.add( url );
                getLog().debug( "adding to classloader resource " + url.toExternalForm() );
            }

            for ( String next : compileClasspathElements ) {
                URL url = new File( next ).toURI().toURL();
                runtimeUrls.add( url );
                getLog().debug( "adding to classloader compile classpath" + url.toExternalForm() );
            }

            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
            URLClassLoader newLoader = new URLClassLoader( runtimeUrls.toArray( new URL[runtimeUrls.size()] ), oldLoader );
            Thread.currentThread().setContextClassLoader( newLoader );

            getLog().info( String.format( "output=%s, inputs=%s", output, Arrays.toString( inputs ) ) );

            try {
                output.mkdirs();
                Generator gen = new Generator( output, inputs, debug, version, failOnErrors );
                gen.run();
            }
            finally {
                Thread.currentThread().setContextClassLoader( oldLoader );
            }
        }
        catch ( Exception e ) {
            getLog().error( e );
            throw new MojoFailureException( "failed to generate sources", e );
        }
    }
}
