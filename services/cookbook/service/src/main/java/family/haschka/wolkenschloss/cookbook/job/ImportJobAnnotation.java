package family.haschka.wolkenschloss.cookbook.job;

import javax.json.bind.annotation.JsonbProperty;
import java.net.URI;
import java.util.UUID;


// Dies ist eine Blaupause für die JSON Serialisierung.
//
// Diese Klasse darf nicht ein Java Record umgewandelt werden. Optionale Felder
// werden bei Records nicht unterstützt. Allerdings sind für das ImportJob Record
// alle Felder außer order optional.
public class ImportJobAnnotation {
    @JsonbProperty(nillable = true) public UUID jobId;
    public URI order;
    @JsonbProperty(nillable = true) State state;

    @JsonbProperty(nillable = true) URI location;
    @JsonbProperty(nillable = true) String error;
}