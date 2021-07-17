package wolkenschloss.conventions

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

class WebappPluginTest extends Specification {

    File testProjectDir


    def setup() {
        testProjectDir = new File("../fx")
    }

    def cleanup() {
       assert new File(testProjectDir, "build").deleteDir() == true
    }

    def "build task should build jar file"() {
        when: "running gradle :build task"
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build")
                .withPluginClasspath()
                .build()

        then: "the outcome of task :build is SUCCESS"
        result.task(':vue').outcome == TaskOutcome.SUCCESS

        and: "the outcome of task :vue is SUCCESS"
        result.task(':vue').outcome == TaskOutcome.SUCCESS

        and: "webapp-example.jar exists"
        def jar = new File(testProjectDir, "build/libs/webapp-example.jar")
        jar.exists()
    }

    def "vue task should build the vue app"() {
        when: "running gradle :build task"
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("vue")
                .withPluginClasspath()
                .build()

        then: "the outcome of task :buildVueApp is SUCCESS"
        result.task(':vue').outcome == TaskOutcome.SUCCESS

        and: "output is written into META-INF"
        def dir = new File(testProjectDir, "build/classes/java/main/META-INF/resources")
        dir.exists()
        new File(dir, "index.html").isFile()
        new File(dir, "js").isDirectory()
    }

    def "unit task should run unit tests"() {
        when: "running gradle:test"
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("unit")
            .withPluginClasspath()
            .build()

        then: "the outcome of task test is SUCCESS"
        result.task(":unit").outcome == TaskOutcome.SUCCESS
    }
}
