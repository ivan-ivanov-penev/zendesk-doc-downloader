package co.citizenlab.service;

import co.citizenlab.service.zendesk.api.resources.Call;
import co.citizenlab.service.zendesk.api.resources.Document;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileResolver {

    private final String baseDocsDir;

    @Autowired
    public FileResolver(@Value("${docs.dir.base}") String baseDocsDir) {

        this.baseDocsDir = baseDocsDir;
    }


    public String resolveDocName(Document document, String resourceType, String resourceName) throws Exception {

        String docParentDir = createParentDirsOnAbsence(resourceType, resourceName);

        String documentName = docParentDir + "/" + document.name;

        if (Files.exists(Paths.get(documentName))) {

            String nameWithoutExtension = FilenameUtils.getBaseName(document.name);
            String extension = FilenameUtils.getExtension(document.name);

            documentName = docParentDir + "/" + nameWithoutExtension + "_" + document.createdAt + "." + extension;
        }

        return documentName;
    }

    private String createParentDirsOnAbsence(String resourceType, String resourceName) throws Exception {

        String resourceTypeDir = baseDocsDir + "/" + resourceType;

        createDirOnAbsence(resourceTypeDir);

        String parentDir = resourceTypeDir + "/" + resourceName;

        createDirOnAbsence(parentDir);

        return parentDir;
    }

    private void createDirOnAbsence(String docParentDir) throws Exception {

        Path parentDirPath = Paths.get(docParentDir);

        if (!Files.exists(parentDirPath)) {

            Files.createDirectory(parentDirPath);
        }
    }

    public String resolveCallName(Call call, String resourceName) throws Exception {

        String parentDir = createParentDirsOnAbsence("calls", resourceName);

        return parentDir + "/" + call.madeAt;
    }
}
