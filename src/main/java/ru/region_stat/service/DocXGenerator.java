package ru.region_stat.service;

import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.region_stat.domain.entity.oneTimeRequest.OneTimeRequestEntity;
import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocXGenerator {
    private static final String TEMPLATE_NAME = "template.docx";
    private static final String pattern = "MM/dd/yyyy";
    private static final DateFormat dateFormat = new SimpleDateFormat(pattern);

    @Value("${sharepoint.path-for-onetimereq-docx}")
    private String pathForOutRequestInDocx;

    @Resource
    private OneTimeRequestService oneTimeRequestService;

    public byte[] generateDocxFileByRequestId(UUID id) throws Exception {

        Optional<OneTimeRequestEntity> optionalOneTimeRequestEntity = oneTimeRequestService.getOneTimeRequestEntityById(id);
        Map<String, String> variables = new HashMap<>();

        if (optionalOneTimeRequestEntity.isPresent()) {
            OneTimeRequestEntity oneTimeRequestEntity = optionalOneTimeRequestEntity.get();

            variables.put("Date", dateFormat.format(oneTimeRequestEntity.getCreatedAt()));
            variables.put("Number", oneTimeRequestEntity.getPetrostatNumber());
            variables.put("Text", oneTimeRequestEntity.getContent());

            InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream(TEMPLATE_NAME);

            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(templateInputStream);

            MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

            VariablePrepare.prepare(wordMLPackage);

            documentPart.variableReplace(variables);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            wordMLPackage.save(outputStream);

            String pathname = pathForOutRequestInDocx + "/Запрос " + oneTimeRequestEntity.getPetrostatNumber().replaceAll("/", "_") + ".docx";

            byte[] bytes = outputStream.toByteArray();

            Files.write(Paths.get(pathname), bytes, new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING});

            return bytes;
        }
        return new byte[0];
    }
}