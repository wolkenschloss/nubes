package wolkenschloss.conventions

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

class CorePluginTest extends Specification {

    @TempDir File testProjectDir
    File settingsFile
    File buildFile
    File testFile
    File srcFile

    def setup() {
        settingsFile = new File(testProjectDir, 'settings.gradle')
        buildFile = new File(testProjectDir, 'build.gradle')

        testFile = new File(testProjectDir, 'src/test/java/UnitTest.java')
        testFile.parentFile.mkdirs()

        srcFile = new File(testProjectDir, "src/main/java/CodeWithJava11.java")
        srcFile.parentFile.mkdirs()

        setup: "create gradle project"
        settingsFile << "rootProject.name = 'hello-world'"

        given: "a build file using core-conventions Plugin"
        buildFile << """
        plugins {
          id 'wolkenschloss.conventions.core'
        }
        """
    }

    def "Unit tests can use the JUnit 5 framework"() {
        given: "A unit test using junit 5"
        testFile << """
            package wolkenschloss.cookbook.core;

            import org.junit.jupiter.api.Assertions;
            import org.junit.jupiter.api.Test;

            public class UnitTest {
            @Test
                public void canExecuteJunitUnitTest() {
                    Assertions.assertTrue(true);
                }
            }
        """

        when: "running gradle :test task"
        def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("test")
        .withPluginClasspath()
        .build()

        then: "the outcome of task :test is SUCCESS"
        result.task(':test').outcome == TaskOutcome.SUCCESS
    }

    def "Source code can be written in Java with language level 11"() {
        given: "a java source file using language level 11"
        srcFile << """
            package wolkenschloss.cookbook.core;

            public class CodeWithJava11 {
                public void methodUsingLocalVar() {
                    var i = 1;
                }
            }
        """

        when: "running gradle build"
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build")
                .withPluginClasspath()
                .build()

        then: "the outcome of build task is SUCCESS"
        result.task(':build').outcome == TaskOutcome.SUCCESS
    }

    def "Source code can be written in Java with language level 16"() {
        given:
        srcFile << """
            package wolkenschloss.cookbook.core;
            record MinMax(int min, int max) {}
        """

        when: "running gradle build"
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build")
                .withPluginClasspath()
                .build()

        then: "build failed"
        result.task(':compileJava').outcome == TaskOutcome.SUCCESS

    }
}