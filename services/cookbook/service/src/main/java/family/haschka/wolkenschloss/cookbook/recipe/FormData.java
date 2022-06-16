package family.haschka.wolkenschloss.cookbook.recipe;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.ws.rs.core.MediaType;
import java.io.File;

public class FormData {

    @RestForm("recipe{}")
    @PartType(MediaType.APPLICATION_JSON)
    public Recipe recipe;

    @RestForm("preview")
//    public File upload;
    public FileUpload upload;
}

