package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@DisplayName("Groups Resource")
@TestHTTPEndpoint(GroupsResource.class)
public class GroupResourceTest {

    @ParameterizedTest
    @DisplayName("GET /units/groups")
    @ValueSource(strings = {"Volumes", "Weights", "Kitchen Terms", "Common Terms"})
    public void listGroupsTest(String group) {
        RestAssured.given()
                .when()
                .get()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("groups.name", hasItem(group));
    }

    enum Testcase {
        VOLUMES(Group.VOLUME),
        WEIGHTS(Group.WEIGHT),
        KITCHEN(Group.KITCHEN),
        COMMON(Group.COMMON);

        private final Group group;

        Testcase(Group group) {
            this.group = group;
        }

        public String selector() {
            return String.format("groups.findAll {group -> group.name == '%s' }.units.name.flatten().size()", group.name);
        }

        public Matcher<?> expectation() {
            return is(group.units.length);
        }
    }

    @ParameterizedTest
    @EnumSource
    public void unitsPerGroupTest(Testcase testcase) {
        RestAssured.when()
                .get()
                .then()
                .body(testcase.selector(), testcase.expectation());
    }
}
