package wolkenschloss.conventions

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.TempDir

class CorePluginTest extends Specification {

    public static final String PROJECT_PROPERTIES_FILE = "build/resources/main/project.properties"
    public static final String COMMIT_ID = "ffac537e6cbbf934b08745a378932722df287a53"
    public static final String REF = "refs/heads/feature-branch-1"

    @TempDir File testProjectDir
    File settingsFile
    File buildFile
    File testFile
    File srcFile
    File propertiesFile

    @SuppressWarnings('unused')
    def setup() {
        settingsFile = new File(testProjectDir, 'settings.gradle')
        buildFile = new File(testProjectDir, 'build.gradle')

        testFile = new File(testProjectDir, 'src/test/java/UnitTest.java')
        testFile.parentFile.mkdirs()

        srcFile = new File(testProjectDir, "src/main/java/CodeWithJava11.java")
        srcFile.parentFile.mkdirs()

        propertiesFile = new File(testProjectDir, "gradle.properties")

        setup: "create gradle project"
        settingsFile << """
            rootProject.name = 'core-plugin-test'
        """

        propertiesFile << """
            # Defaults
            version = 999-SNAPSHOT
            group = family.haschka.wolkenschloss.conventions
            vcs.commit = unspecified
            vcs.ref = unspecified            
        """

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

        println result.output

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

    def "build with tagged git repository should contain git tag in project.properties"() {

        when: "running gradle build with project property version"
        def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("classes", "--project-prop", "version=v1.0")
        .withPluginClasspath()
        .build()

        def properties = readProperties(PROJECT_PROPERTIES_FILE)

        then: "project.properties file contains project version"
        result.task(":projectProperties").outcome == TaskOutcome.SUCCESS
        println result.output
        properties."project.version" == "v1.0"
    }


    def "build should process vcs information"() {

        when: "running gradle build"

         def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build",
                        "--project-prop", "vcs.commit=$COMMIT_ID",
                        "--project-prop", "vcs.ref=$REF",
                        "--project-prop", "version=123")
                .withPluginClasspath()
                .build()

        println result.output
        def properties = readProperties(PROJECT_PROPERTIES_FILE)
        println properties

        then:
        result.task(":projectProperties").outcome == TaskOutcome.SUCCESS
        properties."vcs.commit" == COMMIT_ID
        properties."vcs.ref" == REF
        properties."project.version" == "123"
        properties."project.group" == "family.haschka.wolkenschloss.conventions"
        properties."project.name" == "core-plugin-test"
    }

    Properties readProperties(String path) {
        def properties = new Properties()
        def file =  new File(testProjectDir, path)

        file.withDataInputStream {
            properties.load(it)
        }

        return  properties
    }
}