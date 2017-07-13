package io.protolang;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Arrays;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "gen", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ProtolangGenMojo extends AbstractMojo {
    @Parameter(property = "gen.input", defaultValue = "${project.basedir}/src/main/protolang")
    private File[] inputs;

    @Parameter(property = "gen.output", defaultValue = "${project.build.directory}/generated-sources")
    private File output;

    @Parameter(property = "gen.debug", defaultValue = "false")
    private Boolean debug;

    @Parameter(property = "gen.failOnParseErrors", defaultValue = "true")
    private Boolean failOnParseErrors;

    @Parameter(property = "gen.version", defaultValue = "${project.version}")
    private String version;

    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info( String.format( "output=%s, inputs=%s", output, Arrays.toString( inputs ) ) );

            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
            URLClassLoader newClassLoader = Helper.classloaderWithCompileElements( project, getLog() );
            Thread.currentThread().setContextClassLoader( newClassLoader );

            try {
                output.mkdirs();

                Generator gen = new Generator( inputs, output, version );
                gen.setDebug( debug );
                gen.setFailOnParseErrors( failOnParseErrors );
                gen.generate();
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
