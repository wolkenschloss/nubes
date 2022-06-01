package family.haschka.wolkenschloss.cookbook.ingredient;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

@QuarkusTest
@DisplayName("Ingredient Repository")
public class IngredientRepositoryTest {

    @Inject
    IngredientRepository repository;
    private Ingredient theIngredient;

    @BeforeEach
    public void persistIngredient() {
        theIngredient = new Ingredient(UUID.randomUUID(), "Mondzucker");
        repository.deleteAll().await().indefinitely();
        repository.persist(theIngredient).await().indefinitely();
    }

    @Test
    @DisplayName("should find ingredient by id")
    public void findIngredient() {
        var clone = repository.findById(theIngredient.getId()).await().indefinitely();
        Assertions.assertNotNull(clone);
        Assertions.assertEquals(theIngredient, clone);
    }

    @Test
    @DisplayName("should delete ingredient by id")
    public void deleteIngredient() {
        repository.deleteById(theIngredient.getId()).await().indefinitely();
        Assertions.assertEquals(0, repository.findAll().count().await().indefinitely());
    }

    @Test
    @DisplayName("should update ingredient")
    public void updateIngredient() {
        repository.findById(theIngredient.getId())
                .map(ingredient -> new Ingredient(ingredient.getId(), "Schlammkrabbenchitin"))
                .flatMap(ingredient -> repository.update(ingredient))
                .await()
                .indefinitely();

        Assertions.assertNotEquals(theIngredient, repository.findById(theIngredient.getId()).await().indefinitely());
    }

    @Test
    @DisplayName("should exist")
    public void shouldExist() {
        Assertions.assertNotNull(repository);
    }
}
