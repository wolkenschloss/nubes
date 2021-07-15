import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir
import groovy.io.FileType

class BuildLogicFunctionalTest extends Specification {

    @TempDir File testProjectDir
    File settingsFile
    File buildFile
    File testFile

    def setup() {
        settingsFile = new File(testProjectDir, 'settings.gradle')
        buildFile = new File(testProjectDir, 'build.gradle')

        testFile = new File(testProjectDir, 'src/test/java/UnitTest.java')
        testFile.parentFile.mkdirs()
    }

    def "hello world task prints hello world"() {
        given:
        settingsFile << "rootProject.name = 'hello-world'"
        buildFile << """
            task helloWorld {
                doLast {
                  println 'Hello World!'                
                }
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('helloWorld')
            .build()

        then:
        result.output.contains('Hello World!')
        result.task(":helloWorld").outcome == TaskOutcome.SUCCESS
    }

    def "core-conventions plugin kann verwendet werden"() {
        given:
        settingsFile << "rootProject.name = 'using-core'"
        buildFile << """
        plugins {
          id 'wolkenschloss.core-conventions'
        }
        """

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


        testProjectDir.eachFileRecurse (FileType.FILES) { file ->
            println(file.absolutePath)
        }

        when:
        def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("test")
        .withPluginClasspath()
        .build()

        then:
        result.task(':test').outcome == TaskOutcome.SUCCESS
    }
}