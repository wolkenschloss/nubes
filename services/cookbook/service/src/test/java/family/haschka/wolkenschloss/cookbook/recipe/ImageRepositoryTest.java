package family.haschka.wolkenschloss.cookbook.recipe;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Objects;

@QuarkusTest
@DisplayName("Image Repository")
public class ImageRepositoryTest {

    @Inject
    ImageRepository repository;

    Image2 theImage = null;

    @BeforeEach
    public void persistImage() {
        theImage = new Image2(ObjectId.get().toHexString(), "image/jpeg", new byte[0]);
        repository.deleteAll().await().indefinitely();
        var tester = repository.persist(theImage)
                .log("persisted")
                .onItem().invoke( i -> Assertions.assertNotNull(i.getId()))
//                .log("asserted")
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        tester.awaitItem(Duration.ofSeconds(5000)).assertCompleted();
    }

    @Test
    @DisplayName("should find image by id")
     public void findById() {
        var imageId = Objects.requireNonNull(theImage.getId());

        var tester = repository.findById(imageId)
                .log("findById")
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        tester.awaitItem(Duration.ofSeconds(2000))
                .assertItem(theImage);
    }

    @Test
    @DisplayName("should not be empty")
    public void findAllTest() {
        var tester = repository.findAll()
                .list()
                .log("list")
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        tester.awaitItem(Duration.ofSeconds(10000))
                .assertCompleted();
//                .assertItem(List.of());
    }

    @Inject
    MongoClient mongoClient;

    @Test
    @DisplayName("find with mongodb driver")
    public void findWithDriver() {
        var collection = mongoClient.getDatabase("cookbook")
                .getCollection("Image2");

        MongoCursor<Document> cursor = collection.find().iterator();
        while(cursor.hasNext()) {
            Document document = cursor.next();
            var id = document.getString("_id");

            System.out.printf("id: %s%n", id);

            var tester = repository.findById(id)
                    .log("findById")
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create());

            tester.awaitItem(Duration.ofSeconds(2))
                    .assertCompleted();
        }
    }
}
