package ru.region_stat.dataloader;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import javax.annotation.Resource;

@SpringBootApplication
@Profile("import")
public class ImportApplication implements CommandLineRunner {

    @Resource
    private ApplicationArguments applicationArguments;

    @Resource
    private SharepointTableLoader sharepointTableLoader;

    @Resource
    private DataLoader dataLoader;

    @Override
    public void run(String... args) throws Exception {
        if (applicationArguments.containsOption("loadTables")) {
            sharepointTableLoader.run();
        }

        if (applicationArguments.containsOption("init")) {
            dataLoader.run();
        }

        if (applicationArguments.containsOption("filesMO")) {
            dataLoader.loadFilesMOReference();
//            dataLoader.loadFilesMO();
        }
    }
}