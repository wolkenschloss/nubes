package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.test.junit.QuarkusTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.ArrayList;

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

    @Test
    @DisplayName("should find recipe by id")
    public void findRecipe() {
        var clone = repository.findById(new ObjectId(theRecipe._id())).await().indefinitely();
        Assertions.assertEquals(theRecipe, clone);
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
