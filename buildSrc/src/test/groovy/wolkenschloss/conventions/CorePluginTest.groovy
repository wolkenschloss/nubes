package wolkenschloss.conventions

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.TempDir

class CorePluginTest extends Specification {

    public static final String PROJECT_PROPERTIES_FILE = "build/resources/main/project.properties"
    public static final String SHA = "ffac537e6cbbf934b08745a378932722df287a53"
    public static final String REF = "refs/heads/feature-branch-1"

    @TempDir File testProjectDir
    File settingsFile
    File buildFile
    File testFile
    File srcFile

    @SuppressWarnings('unused')
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

    private void initGitRepositoryWithTag(String tag) {
        git("init")
        git("add .")
        git("commit -m 'initial'")
        git("tag -a $tag -m 'message'")
    }

    private int git(String cmd) {
        def process = "git $cmd".execute(null, testProjectDir)
        return process.waitFor()
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
        initGitRepositoryWithTag("v1.0")

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
        initGitRepositoryWithTag("v1.0")

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
        initGitRepositoryWithTag("v1.0")

        when: "running gradle build"
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build")
                .withPluginClasspath()
                .build()

        then: "build failed"
        result.task(':compileJava').outcome == TaskOutcome.SUCCESS
    }

    @Ignore("issue: #180, not yet implemented")
    def "build with tagged git repository should contain git tag in project.properties"() {
        given: "a git project with tag"
        initGitRepositoryWithTag("v1.0")

        when: "running gradle build"
        def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("classes")
        .withPluginClasspath()
        .build()

        def properties = readProperties(PROJECT_PROPERTIES_FILE)

        then: "project.properties contains git tag"
        result.task(":projectProperties").outcome == TaskOutcome.SUCCESS
        println result.output
        properties."project.version" == "v1.0"
    }

    def "build without git repository should fail"() {
        when: "running gradle build"
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build")
                .withPluginClasspath()
                .buildAndFail()

        then:
        result.tasks == []
    }

    def "build without git repository should not fail if environment variables are set"() {

        when: "running gradle build"

         def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build")
                .withEnvironment(["GITHUB_SHA": SHA, "GITHUB_REF": REF])
                .withPluginClasspath()
                .build()
        def properties = readProperties(PROJECT_PROPERTIES_FILE)
        println result.output

        then:
        result.task(":projectProperties").outcome == TaskOutcome.SUCCESS
        properties."project.sha" == SHA
        properties."project.ref" == REF
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