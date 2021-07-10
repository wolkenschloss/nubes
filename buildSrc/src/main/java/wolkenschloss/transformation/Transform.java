package wolkenschloss.transformation;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.*;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Füllt die Platzhalter einer Vorlage mit Werten und gibt
 * das Ergebnis in einer Datei aus.
 */
@CacheableTask
public abstract class Transform extends DefaultTask {

    /**
     * Platzhalter Werte, mit denen die Vorlage gefüllt wird.
     * @return Werte, mit denen die Platzhalter einer Vorlage gefüllt werden.
     */
    @Internal
    abstract public MapProperty<String, Object> getScope();

    /**
     * @return Eine Datei, die die Vorlage enthält
     */
    @InputFile
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getTemplate();

    /**
     * @return Eine Datei, in welche die Ausgabe geschrieben wird.
     */
    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void transform() {
        RegularFile file = getTemplate().get();
        getLogger().info("Transforming {}", file.getAsFile());

        //noinspection ResultOfMethodCallIgnored
        getOutputFile().get().getAsFile().getParentFile().mkdirs();

        try(var input = new FileInputStream(file.getAsFile())) {
            try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {

                final MustacheFactory factory = new DefaultMustacheFactory();
                final Mustache mustache = factory.compile(reader, file.getAsFile().getName());

                try (var writer = new FileWriter(getOutputFile().get().getAsFile()))
                {
                    mustache.execute(writer, getScope().get());
                    writer.flush();
                }
            }
        } catch (IOException e) {
            throw new GradleScriptException("Can not process template", e);
        }
    }
}
