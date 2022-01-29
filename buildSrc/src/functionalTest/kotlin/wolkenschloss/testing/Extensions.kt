package wolkenschloss.testing

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File


fun File.build(vararg args: String): BuildResult = GradleRunner.create()
    .withProjectDir(this)
    .withArguments(*args)
    .withPluginClasspath()
    .build()