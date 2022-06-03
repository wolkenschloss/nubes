package family.haschka.wolkenschloss.cookbook.job;

import javax.json.bind.adapter.JsonbAdapter;
import javax.swing.text.html.Option;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static family.haschka.wolkenschloss.cookbook.UuidExtensionsKt.nil;

public class ImportJobAdapter  implements JsonbAdapter<ImportJob, ImportJobAnnotation> {
    @Override
    public ImportJobAnnotation adaptToJson(ImportJob obj) {
        var annotated = new ImportJobAnnotation();
        annotated.jobId = obj.getJobId();
        annotated.order = obj.getOrder();
        annotated.state = obj.getState();
        annotated.location = obj.getLocation();
        annotated.error = obj.getError();

        return annotated;
    }

    @Override
    public ImportJob adaptFromJson(ImportJobAnnotation obj) {
        return new ImportJob(
                Optional.ofNullable(obj.jobId).orElse(nil),
                obj.order,
                Optional.ofNullable(obj.state).orElse(State.UNKNOWN),
                Optional.ofNullable(obj.location).orElse(ImportJob.getNoLocation()),
                Optional.ofNullable(obj.error).orElse(""));
    }
}
