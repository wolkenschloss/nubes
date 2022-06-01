package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.test.junit.QuarkusTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.stream.Stream;

@QuarkusTest
@DisplayName("Recipe Repository")
public class RecipeRepositoryTest {

    @Inject
    RecipeRepository repository;

    Recipe theRecipe;

    @BeforeEach
    public void persistRecipe() {
        theRecipe = RecipeFixture.LASAGNE.withId();
        repository.deleteAll().await().indefinitely();
        repository.persist(theRecipe).await().indefinitely();
    }

    @TestFactory
    @DisplayName("should find recipe by id")
    public Stream<DynamicTest> findRecipe() {
        return Stream.of(
                new Recipe(
                        ObjectId.get().toHexString(),
                        "Recipe 1 with preparation",
                        "Preparation 1",
                        new ArrayList<>(),
                        new Servings(1),
                        0L),
                new Recipe(
                        ObjectId.get().toHexString(),
                        "Recipe 2 without preparation",
                        null,
                        new ArrayList<>(),
                        new Servings(4),
                        0L
                )
        ).map(recipe -> DynamicTest.dynamicTest(recipe.title(), () -> {
            var r1 = repository.persist(recipe).await().indefinitely();
            var r2 = repository.findById(new ObjectId(recipe._id())).await().indefinitely();
            Assertions.assertEquals(r1, r2);
        }));
    }

    @Test
    @DisplayName("should update recipe")
    public void updateRecipe() {
        repository.findById(new ObjectId(theRecipe._id()))
                .map(recipe -> new Recipe(recipe._id(), recipe.title(), "New preparation", new ArrayList<>(recipe.ingredients()), new Servings(recipe.servings().amount()), recipe.created()))
                .flatMap(recipe -> repository.update(recipe))
                .await()
                .indefinitely();

        Assertions.assertNotEquals(theRecipe, repository.findById(new ObjectId(theRecipe._id())).await().indefinitely());
    }

    @Test
    @DisplayName("should delete recipe")
    public void deleteRecipe() {
        repository.deleteById(new ObjectId(theRecipe._id())).await().indefinitely();
        Assertions.assertEquals(0, repository.findAll().count().await().indefinitely());
    }
}
