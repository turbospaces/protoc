package io.protolang;

import java.io.File;
import java.net.URLClassLoader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

abstract class ProtolangBaseGenMojo extends AbstractMojo {
    @Parameter(property = "gen.output", defaultValue = "${project.build.directory}/generated-sources")
    private File output;
    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String java = "java";
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();

        try {
            URLClassLoader newClassLoader = Helper.classloaderWithCompileElements( project, getLog() );
            Thread.currentThread().setContextClassLoader( newClassLoader );

            for ( File f : FileUtils.listFiles( output, new String[] { java }, true ) ) {
                String subpath = output.toURI().relativize( f.toURI() ).getPath();

                String className = FilenameUtils.removeExtension( subpath ).replace( File.separatorChar, '.' );
                getLog().debug( "generating avro schema for " + f.getPath() );

                Class<?> clazz = newClassLoader.loadClass( className );
                if ( !clazz.isInterface() && !Exception.class.isAssignableFrom( clazz ) ) {
                    generate( clazz );
                }
            }
        }
        catch ( Exception e ) {
            getLog().error( e );
            throw new MojoFailureException( "unable to load class", e );
        }
        finally {
            Thread.currentThread().setContextClassLoader( oldLoader );
        }
    }
    protected abstract void generate(Class<?> clazz) throws Exception;
}
