package wolkenschloss.testing

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.util.*

fun File.build(vararg args: String): BuildResult = createRunner()
    .withArguments(*args)
    .build()

fun File.createRunner(): GradleRunner = GradleRunner.create()
    .withProjectDir(this)
    .withPluginClasspath()

fun File.properties(path: String): Properties {
    val properties = Properties()

    resolve(path).inputStream().use {
        properties.load(it)
    }

    return properties
}