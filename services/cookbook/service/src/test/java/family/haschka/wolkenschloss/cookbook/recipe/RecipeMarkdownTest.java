package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.util.Optional;

@QuarkusTest
@TestHTTPEndpoint(RecipeResource.class)
public class RecipeMarkdownTest {

    @InjectMock
    RecipeService service;

    @Test
    public void getRecipeAsMarkdown() {
        var recipe = RecipeFixture.LASAGNE.withId(ObjectId.get().toHexString());

        Mockito.when(service.get(recipe._id(), Optional.empty()))
                .thenReturn(Uni.createFrom().item(Optional.of(recipe)));

        //noinspection SpellCheckingInspection
        String expected = """
                # Lasagne              
                
                ## Ingredients for 1 servings
                                                        
                  * 500 g Hackfleisch
                  * 1 Zwiebel(n)
                  * 2 Knoblauchzehen
                  * 1 Bund Petersilie oder TK
                  * 1 EL Tomatenmark
                  * 1 Dose Tomaten, geschälte (800g)
                  * Etwas Rotwein                  
                
                ## Preparation
                                                        
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr, et dolore magna
                aliquyam erat, sed diam voluptua. At rebum.
                            
                Duis autem vel eum iriure dolor in hendrerit in vulputate Lorem ipsum dolor sit
                amet, consectetuer aliquam erat volutpat.
                            
                Ut wisi enim ad minim veniam, quis nostrud exerci tation qui blandit praesent
                luptatum zzril delenit augue facilisi.
                """;

        var actual = RestAssured.given()
                .accept("text/markdown; charset=UTF-8")
                .when()
                .get(recipe._id())
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().body().asString();

        // The following instruction can be used to display the difference
        // between expected and actual value in IntelliJ.
        Assertions.assertEquals(expected, actual);

        Mockito.verify(service, Mockito.times(1)).get(recipe._id(), Optional.empty());
    }

    @AfterEach
    public void verifyMocks() {
        Mockito.verifyNoMoreInteractions(service);
    }
}
