package family.haschka.wolkenschloss.cookbook.ingredient;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

public class IngredientServiceTest {

    @BeforeEach
    public void mockCollaborators() {
        this.repository = Mockito.mock(IngredientRepository.class);
        this.identityGenerator = Mockito.mock(IdentityGenerator.class);
    }

    IngredientRepository repository;
    IdentityGenerator identityGenerator;

    @Test
    public void createIngredient() {
        Ingredient ingredient = IngredientFixture.FLOUR.withId(UUID.randomUUID());

        Mockito.when(identityGenerator.generate())
                .thenReturn(ingredient.getId());

        Mockito.when(repository.persist(ingredient))
                .thenReturn(Uni.createFrom().item(ingredient));

        var service = new IngredientService(repository, identityGenerator);
        service.create(ingredient.getName())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(ingredient);

        Mockito.verify(identityGenerator, Mockito.times(1)).generate();
        Mockito.verify(repository, Mockito.times(1)).persist(ingredient);
    }

    enum ListIngredientsTestCase {
        COUNT_1("flour", 1L, 0, 0),
        COUNT_100("sugar", 100L, 50, 149);

        private final String search;
        private final long count;
        private final int first;
        private final int last;
        private final ArrayList<Ingredient> elements;

        ListIngredientsTestCase(String search, long count, int first, int last) {
            this.search = search;

            this.count = count;
            this.first = first;
            this.last = last;

            this.elements = new ArrayList<>();
            for (int i = first; i < last + 1; i++) {
                elements.add(new Ingredient(UUID.randomUUID(), "Ingredient #" + i));
            }
        }
    }

    public interface IngredientQuery extends ReactivePanacheQuery<Ingredient> {
    }

    @ParameterizedTest
    @EnumSource(ListIngredientsTestCase.class)
    @DisplayName("search for ingredient")
    public void searchIngredients(ListIngredientsTestCase testcase) {

        IngredientQuery query = Mockito.mock(IngredientQuery.class);
        IngredientQuery range = Mockito.mock(IngredientQuery.class);

        Mockito.when(repository.find(
                        eq("name like ?1"),
                        any(Sort.class),
                        eq(testcase.search)))
                .thenReturn(query);

        Mockito.when(query.range(testcase.first, testcase.last))
                .thenReturn(range);

        Mockito.when(range.list())
                .thenReturn(Uni.createFrom().item(() -> testcase.elements));

        Mockito.when(query.count())
                .thenReturn(Uni.createFrom().item(testcase.count));

        var expected = new TableOfContents(testcase.count, testcase.elements);
        var service = new IngredientService(repository, identityGenerator);

        service.list(testcase.first, testcase.last, testcase.search)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(expected);

        Mockito.verify(repository, Mockito.times(1))
                .find(
                        eq("name like ?1"),
                        ArgumentMatchers.any(Sort.class),
                        eq(testcase.search));

        Mockito.verify(query, Mockito.times(1)).count();
        Mockito.verify(query, Mockito.times(1)).range(testcase.first, testcase.last);
        Mockito.verify(range, Mockito.times(1)).list();

        Mockito.verifyNoMoreInteractions(query);
        Mockito.verifyNoMoreInteractions(range);
    }

    @ParameterizedTest
    @EnumSource(ListIngredientsTestCase.class)
    @DisplayName("list all ingredients")
    public void listAllIngredients(ListIngredientsTestCase testcase) {

        IngredientQuery query = Mockito.mock(IngredientQuery.class);
        IngredientQuery range = Mockito.mock(IngredientQuery.class);

        Mockito.when(repository.findAll(any(Sort.class)))
                .thenReturn(query);

        Mockito.when(query.range(testcase.first, testcase.last))
                .thenReturn(range);

        Mockito.when(range.list())
                .thenReturn(Uni.createFrom().item(() -> testcase.elements));

        Mockito.when(query.count())
                .thenReturn(Uni.createFrom().item(testcase.count));

        var expected = new TableOfContents(testcase.count, testcase.elements);

        var service = new IngredientService(repository, identityGenerator);

        service.list(testcase.first, testcase.last, null)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(expected);

        Mockito.verify(repository, Mockito.times(1))
                .findAll(argThat((Sort sort) -> sort.getColumns().stream()
                        .map(Sort.Column::getName).anyMatch(name -> name.equals("name"))
                        && sort.getColumns().size() == 1));

        Mockito.verify(query, Mockito.times(1)).count();
        Mockito.verify(query, Mockito.times(1)).range(testcase.first, testcase.last);
        Mockito.verify(range, Mockito.times(1)).list();

        Mockito.verifyNoMoreInteractions(repository);
        Mockito.verifyNoMoreInteractions(query);
        Mockito.verifyNoMoreInteractions(range);
    }

    @Test
    @DisplayName("should create ingredient if required ingredient is unknown")
    public void onIngredientRequired() {

        var recipeId = UUID.randomUUID();
        var ingredientId = UUID.randomUUID();
        var entity = IngredientFixture.FLOUR.withId(ingredientId);

        Mockito.when(identityGenerator.generate()).thenReturn(ingredientId);
        Mockito.when(repository.persist(entity)).thenReturn(Uni.createFrom().item(entity));

        var event = new IngredientRequiredEvent(recipeId, IngredientFixture.FLOUR.title);
        var service = new IngredientService(repository, identityGenerator);

        service.onIngredientRequired(Message.of(event))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .assertTerminated();

        Mockito.verify(identityGenerator, Mockito.times(1))
                .generate();

        Mockito.verify(repository, Mockito.times(1))
                .persist(entity);

        Mockito.verifyNoMoreInteractions(identityGenerator);
        Mockito.verifyNoMoreInteractions(repository);
    }

    @AfterEach
    public void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(repository);
        Mockito.verifyNoMoreInteractions(identityGenerator);
    }
}
