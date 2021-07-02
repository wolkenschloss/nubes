package wolkenschloss.task;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import wolkenschloss.TestbedPool;
import wolkenschloss.TestbedView;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

// Verarbeitet eine Vorlage
@CacheableTask
public abstract class Transform extends DefaultTask {

    @Input
    abstract public Property<String> getRootImageName();

    @Input
    abstract public Property<String> getCidataImageName();

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

        var scopes = new HashMap<String, Object>();
        scopes.put("user", getView().get().getUser().get());
        scopes.put("getSshKey", getView().get().getSshKey().get());
        scopes.put("hostname", getView().get().getHostname().get());
        scopes.put("fqdn", getView().get().getFqdn().get());
        scopes.put("locale", getView().get().getLocale().get());

        var callback = new HashMap<String, Object>();
        callback.put("ip", getView().get().getHostAddress().get());
        callback.put("port", getView().get().getCallbackPort().get());

        var disks = new HashMap<String, Object>();
        disks.put("root", getRootImageName().get());
        disks.put("cidata", getCidataImageName().get());

        scopes.put("callback", callback);
        scopes.put("pool", getPool().get().toTemplateScope());
        scopes.put("disks", disks);

        RegularFile file = getTemplate().get();
        getLogger().info("Processing {}", file.getAsFile());

        getLogger().info("Create Directory for Output File: {}",
        getOutputFile().get().getAsFile().getParentFile().mkdirs());

        try(var input = new FileInputStream(file.getAsFile())) {
            try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {

                final MustacheFactory factory = new DefaultMustacheFactory();
                final Mustache mustache = factory.compile(reader, file.getAsFile().getName());

                try (var writer = new FileWriter(getOutputFile().get().getAsFile()))
                {
                    mustache.execute(writer, scopes);
                    writer.flush();
                }
            }
        } catch (IOException e) {
            throw new GradleScriptException("Can not process template", e);
        }
    }
}
