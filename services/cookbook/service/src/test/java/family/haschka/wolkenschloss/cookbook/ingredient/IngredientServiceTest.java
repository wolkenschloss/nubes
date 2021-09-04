package family.haschka.wolkenschloss.cookbook.ingredient;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;

@QuarkusTest
public class IngredientServiceTest {

    @InjectMock
    IngredientRepository repository;

    @Inject
    IngredientService subjectUnderTest;

    @Test
    public void createIngredient() {
        Ingredient ingredient = IngredientFixture.FLOUR.withId(UUID.randomUUID());
        Mockito.when(repository.persist(ingredient))
                .thenReturn(Uni.createFrom().item(ingredient));

        subjectUnderTest.create(ingredient)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(ingredient);

        Mockito.verify(repository, Mockito.times(1)).persist(ingredient);
        Mockito.verifyNoMoreInteractions(repository);
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

    @ParameterizedTest
    @EnumSource(ListIngredientsTestCase.class)
    @DisplayName("search for ingredient")
    public void searchIngredients(ListIngredientsTestCase testcase) {

        ReactivePanacheQuery<Ingredient> query = Mockito.mock(ReactivePanacheQuery.class, Mockito.RETURNS_MOCKS);
        ReactivePanacheQuery<Ingredient> range = Mockito.mock(ReactivePanacheQuery.class, Mockito.RETURNS_MOCKS);
        Mockito.when(repository.find(any(String.class), any(Sort.class), argThat((Object[] s) -> s[0].equals(testcase.search))))
                .thenReturn(query);

        Mockito.when(query.range(testcase.first, testcase.last))
                .thenReturn(range);

        Mockito.when(range.list())
                .thenReturn(Uni.createFrom().item(() ->testcase.elements));

        Mockito.when(query.count())
                .thenReturn(Uni.createFrom().item(testcase.count));

        var expected = new TableOfContents(testcase.count, testcase.elements);

        subjectUnderTest.list(testcase.first, testcase.last, testcase.search)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(expected);

        Mockito.verify(repository, Mockito.times(1))
                .find(
                        ArgumentMatchers.eq("name like ?1"),
                        ArgumentMatchers.any(Sort.class),
                        argThat((Object[] s) -> s[0].equals(testcase.search)));

        Mockito.verify(query, Mockito.times(1)).count();
        Mockito.verify(query, Mockito.times(1)).range(testcase.first, testcase.last);
        Mockito.verify(range, Mockito.times(1)).list();

        Mockito.verifyNoMoreInteractions(repository);
        Mockito.verifyNoMoreInteractions(query);
    }

    @ParameterizedTest
    @EnumSource(ListIngredientsTestCase.class)
    @DisplayName("list all ingredients")
    public void listAllIngredients(ListIngredientsTestCase testcase) {
        ReactivePanacheQuery<Ingredient> query = Mockito.mock(ReactivePanacheQuery.class, Mockito.RETURNS_MOCKS);
        ReactivePanacheQuery<Ingredient> range = Mockito.mock(ReactivePanacheQuery.class, Mockito.RETURNS_MOCKS);

        Mockito.when(repository.findAll(any(Sort.class)))
                .thenReturn(query);

        Mockito.when(query.range(testcase.first, testcase.last))
                .thenReturn(range);

        Mockito.when(range.list())
                .thenReturn(Uni.createFrom().item(() ->testcase.elements));

        Mockito.when(query.count())
                .thenReturn(Uni.createFrom().item(testcase.count));

        var expected = new TableOfContents(testcase.count, testcase.elements);

        subjectUnderTest.list(testcase.first, testcase.last, null)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(expected);

        Mockito.verify(repository, Mockito.times(1))
                .findAll(argThat((Sort sort) -> {
                    return sort.getColumns().stream().map(c -> c.getName()).anyMatch(name -> name.equals("name")) &&
                    sort.getColumns().size() == 1;
                }));

        Mockito.verify(query, Mockito.times(1)).count();
        Mockito.verify(query, Mockito.times(1)).range(testcase.first, testcase.last);
        Mockito.verify(range, Mockito.times(1)).list();

        Mockito.verifyNoMoreInteractions(repository);
        Mockito.verifyNoMoreInteractions(query);
        Mockito.verifyNoMoreInteractions(range);
    }
}
