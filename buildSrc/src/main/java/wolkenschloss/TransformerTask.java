package wolkenschloss;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public abstract class TransformerTask extends DefaultTask {

    @Input
    abstract public Property<TestbedView> getView();

    @InputFile
    @PathSensitive(PathSensitivity.ABSOLUTE)
    abstract public RegularFileProperty getSshKeyFile();

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public Property<FileCollection> getTemplates();

    @OutputDirectory
    public abstract Property<FileSystemLocation> getOutputDir();

    @TaskAction
    public void machMal() {
        getLogger().info("Running 'hello' with:");

        MustacheFactory factory = new DefaultMustacheFactory();
        var scopes = new HashMap<String, Object>();
        scopes.put("user", getView().get().getUser().get());
        scopes.put("getSshKey", getView().get().getSshKey().get());
        scopes.put("hostname", getView().get().getHostname().get());

        getTemplates().get().forEach(file -> {
            getLogger().info("Processing {}", file.getPath());

            getOutputDir().get().getAsFile().mkdirs();

            try {
                var reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                Mustache mustache = factory.compile(reader, file.getName());
                StringWriter writer = new StringWriter();
                mustache.execute(writer, scopes);
                writer.flush();


                var src = getProject().file("src").getAbsolutePath();
                var rel = new File(src).toURI().relativize(file.toURI()).getPath();
                rel = stripExtension(rel);
                getLogger().info("Write file to {}", rel);


                var dest = getProject().getLayout().getBuildDirectory().dir(rel);

                getLogger().info("Destination {}", dest);
                getLogger().info("REL: {}", rel);


                dest.get().getAsFile().getParentFile().mkdirs();

                var write = new FileWriter(dest.get().getAsFile());
                write.write(writer.toString());
                write.close();

            } catch (IOException e) {
                e.printStackTrace();
                throw new GradleScriptException("Can not process template", e);
            }

        });
    }

    private String stripExtension(String rel) {
        var last = rel.lastIndexOf('.');
        if (last > 0) {
            return rel.substring(0, last);
        } else {
            return rel;
        }
    }
}
