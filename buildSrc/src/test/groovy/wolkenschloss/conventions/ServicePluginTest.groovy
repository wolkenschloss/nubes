package wolkenschloss.conventions

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

class ServicePluginTest extends Specification {
    @TempDir File testProjectDir
    File settings
    File build
    File test
    File source
    File properties

    def setup() {
        settings = new File(testProjectDir, "settings.gradle")
        build = new File(testProjectDir, "build.gradle")

        properties = new File(testProjectDir, "gradle.properties")

        settings << """
            rootProject.name = "service-plugin-test"
        """

        properties << """
            quarkusPlatformGroupId=io.quarkus
            quarkusPlatformArtifactId=quarkus-bom
            quarkusPlatformVersion=2.6.1.Final
            version = 999-SNAPSHOT
            group = family.haschka.wolkenschloss.conventions
            vcs.commit = unspecified
            vcs.ref = unspecified 
        """

        build << """
            plugins {
                id 'wolkenschloss.conventions.service'
            }
            
            dependencies {
                implementation("io.quarkus:quarkus-resteasy")
            }
        """

        source = new File(testProjectDir, "src/main/java/GreetingResource.java")
        source.parentFile.mkdirs()
        source << """
            package family.haschka.wolkenschloss.convention.service;
            
            import javax.inject.Inject;
            import javax.ws.rs.GET;
            import javax.ws.rs.Path;
            import javax.ws.rs.core.MediaType;
            import javax.ws.rs.Produces;
            
            @Path("/greeting")
            public class GreetingResource {
                                   
                @GET
                @Produces(MediaType.TEXT_PLAIN)
                public String greet() {
                    return "hello";
                }            
            }
        """
    }

    def "should compile quarkus service"() {

        when: "running gradle build"
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("build")
            .withPluginClasspath()
            .build()

        then:
        result.task(":build").outcome == TaskOutcome.SUCCESS
    }

    def "should run quarkus unit test"() {
        given: "quarkus unit test"
        test = new File(testProjectDir, "src/test/java/GreetingResourceTest.java")
        test.parentFile.mkdirs()
        test << """
        package family.haschka.wolkenschloss.convention.service;
        
        import io.quarkus.test.common.http.TestHTTPEndpoint;
        import io.quarkus.test.common.http.TestHTTPResource;
        import io.quarkus.test.junit.QuarkusTest;
        import io.restassured.RestAssured;
        import org.junit.jupiter.api.Test;
        import javax.ws.rs.core.Response;
        import java.net.URL;
        
        @QuarkusTest
        public class GreetingResourceTest {
            @TestHTTPEndpoint(GreetingResource.class)
            @TestHTTPResource
            URL url;
            
            @Test
            public void getGreetingTest() {
                RestAssured.given()
                .when()
                .get(url)
                .then()
                .statusCode(Response.Status.OK.getStatusCode()); 
            }
        }
        """

        when: "running gradle build"
        def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("build")
        .withPluginClasspath()
        .build()

        then:
        result.task(":test").outcome == TaskOutcome.SUCCESS
    }

    def "should run integration test"() {
        given: "integration test"
        def integrationTest = new File(testProjectDir, "src/integrationTest/java/GreetingTest.java")
        integrationTest.parentFile.mkdirs()
        integrationTest << """
        package family.haschka.wolkenschloss.convention.service;
        
        import io.quarkus.test.junit.QuarkusIntegrationTest;
        import io.restassured.RestAssured;
        import org.junit.jupiter.api.Test;
        import javax.ws.rs.core.Response;
        
        @QuarkusIntegrationTest
        public class GreetingTest {
        
            @Test
            public void getGreeting() {
                String port = System.getProperty("quarkus.http.port");
                String url = String.format("http://localhost:%s/greeting", port);
                
                RestAssured.given()
                    .when()
                    .get(url)
                    .then()
                    .statusCode(Response.Status.OK.getStatusCode());
            }
        }
        """

        when: "running gradle build"
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("build")
            .withPluginClasspath()
            .build()

        then:
        result.task(":integrationTest").outcome == TaskOutcome.SUCCESS

    }
}
