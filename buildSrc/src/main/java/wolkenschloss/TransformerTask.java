package wolkenschloss;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

// Verarbeitet eine Vorlage
public abstract class TransformerTask extends DefaultTask {

    @Input
    abstract public Property<TestbedView> getView();

    @Input
    abstract public Property<TestbedPool> getPool();

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getTemplate();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void machMal() {
        getLogger().info("Running '{}' with:", this.getName());

        MustacheFactory factory = new DefaultMustacheFactory();
        var scopes = new HashMap<String, Object>();
        scopes.put("user", getView().get().getUser().get());
        scopes.put("getSshKey", getView().get().getSshKey().get());
        scopes.put("hostname", getView().get().getHostname().get());

        var callback = new HashMap<String, Object>();
        callback.put("ip", getView().get().getHostAddress().get());
        callback.put("port", getView().get().getCallbackPort().get());

        var pool = new HashMap<String, Object>();

//        pool.put("directory", getPool().get().getDir().get());
        pool.put("name", getPool().get().getName().get());

        scopes.put("callback", callback);
        scopes.put("pool", pool);

        RegularFile file = getTemplate().get();
        getLogger().info("Processing {}", file.getAsFile());

        getLogger().info("Create Directory for Output File: {}",
        getOutputFile().get().getAsFile().getParentFile().mkdirs());

            try {
                var reader = new InputStreamReader(new FileInputStream(file.getAsFile()), StandardCharsets.UTF_8);
                Mustache mustache = factory.compile(reader, file.getAsFile().getName());
                StringWriter writer = new StringWriter();
                mustache.execute(writer, scopes);
                writer.flush();


                var write = new FileWriter(getOutputFile().get().getAsFile());
                write.write(writer.toString());
                write.close();

            } catch (IOException e) {
                e.printStackTrace();
                throw new GradleScriptException("Can not process template", e);
            }
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
