package ru.region_stat.filewatcher;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.File;

@Component
@Profile("!import")
public class FileWatcher extends RouteBuilder {
    @Value("${sharepoint.inboxFolder}")
    String inboxFolder;

    @Value("${sharepoint.savedFolder}")
    String savedFolder;

    @Resource
    private FileFilter fileFilter;

    Predicate fileIsValid = new Predicate() {

        @Override
        public boolean matches(Exchange exchange) {
            File file = exchange.getIn().getBody(File.class);
            return fileFilter.isFileNameValid(file);
        }
    };

    @Override
    public void configure() {

        String pathInbox = "file://" + inboxFolder + "?" +
                "idempotent=true" +
                "&noop=true" +
                "&idempotentKey=${file:name}-${file:size}";

        from(pathInbox)
                .routeId("save-publication")
                .choice()
                .when(fileIsValid)
                .to("file://" + savedFolder)
                .endChoice()
                .end();
    }
}