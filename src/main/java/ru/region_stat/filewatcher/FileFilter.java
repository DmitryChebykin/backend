package ru.region_stat.filewatcher;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.region_stat.service.PublicationTypeService;
import javax.annotation.Resource;
import java.io.File;

@Component
@Profile("!import")
public class FileFilter  {
    @Resource
    PublicationTypeService publicationTypeService;

    public boolean isFileNameValid(File file) {
        String fileName = file.getName();
        String code = StringUtils.substring(fileName, 0, 6);
        String complexCode = "pub_" + code;

        if (fileName.startsWith("ПЗ")) {
            code = StringUtils.substringBetween(fileName, "ПЗ", "_");
            complexCode = "req_" + code;
        }

        return publicationTypeService.existByCode(complexCode);
    }
}