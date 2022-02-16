package wolkenschloss.testing

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.util.*

fun Instance.build(vararg args: String): BuildResult = createRunner()
    .withArguments(*args)
    .build()

fun Instance.buildAndFail(vararg args: String): BuildResult = createRunner()
    .withArguments(*args)
    .buildAndFail()

fun Instance.createRunner(): GradleRunner = GradleRunner.create()
    .withProjectDir(this.target)
    .withPluginClasspath()

fun Instance.properties(path: String): Properties {
    val properties = Properties()

    target.resolve(path).inputStream().use {
        properties.load(it)
    }

    return properties
}


