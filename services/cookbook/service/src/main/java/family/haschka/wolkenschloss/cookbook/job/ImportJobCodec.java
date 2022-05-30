package family.haschka.wolkenschloss.cookbook.job;

import org.bson.BsonBinary;
import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public class ImportJobCodec implements CollectibleCodec<ImportJob> {

    private final Codec<Document> documentCodec;

    public ImportJobCodec(CodecRegistry registry) {
        this.documentCodec = registry.get(Document.class);
    }

    @Override
    public ImportJob decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);

        return new ImportJob(
                document.get("_id", UUID.class),
                Optional.ofNullable(document.getString("order"))
                        .map(URI::create)
                        .orElse(null),
                Optional.ofNullable(document.getString("state"))
                        .map(State::valueOf)
                        .orElse(null),
                Optional.ofNullable(document.getString("location"))
                        .map(URI::create)
                        .orElse(null),
                document.getString("error"));
    }

    @Override
    public void encode(BsonWriter writer, ImportJob value, EncoderContext encoderContext) {
        Document doc = new Document();

        doc.put("_id", value.jobId());
        doc.put("order", value.order());
        doc.put("error", value.error());
        doc.put("location", value.location());
        doc.put("state", value.state().toString());

        documentCodec.encode(writer, doc, encoderContext);
    }

    @Override
    public Class<ImportJob> getEncoderClass() {
        return ImportJob.class;
    }

    /**
     * Wird vom Mongo Client aufgerufen, wenn die Entität geschrieben wird.
     * Bewirkt, dass encode immer mit einer Entität aufgerufen wird, die eine
     * id besitzt.
     *
     * @param document the document for which to generate a value for the jobId.
     * @return Ein ImportJob Objekt, dessen jobId Feld garantiert gesetzt ist.
     */
    @Override
    public ImportJob generateIdIfAbsentFromDocument(ImportJob document) {
        if (!documentHasId(document)) {
            return new ImportJob(UUID.randomUUID(), document.order(), document.state(), document.location(), document.error());
        }
        return document;
    }

    @Override
    public boolean documentHasId(ImportJob document) {
        return document.jobId() != null;
    }

    @Override
    public BsonValue getDocumentId(ImportJob document) {
        if (!documentHasId(document)) {
            throw new IllegalStateException("document does not contain an id");
        }

        return new BsonBinary(document.jobId());
    }
}
