package family.haschka.wolkenschloss.cookbook.job;

import javax.json.bind.adapter.JsonbAdapter;

public class ImportJobAdapter  implements JsonbAdapter<ImportJob, ImportJobAnnotation> {
    @Override
    public ImportJobAnnotation adaptToJson(ImportJob obj) {
        var annotated = new ImportJobAnnotation();
        annotated.jobId = obj.jobId();
        annotated.order = obj.order();
        annotated.state = obj.state();
        annotated.location = obj.location();
        annotated.error = obj.error();

        return annotated;
    }

    @Override
    public ImportJob adaptFromJson(ImportJobAnnotation obj) {
        return new ImportJob(obj.jobId, obj.order, obj.state, obj.location, obj.error);
    }
}
