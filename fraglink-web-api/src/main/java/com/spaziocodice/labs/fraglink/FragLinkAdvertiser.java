package com.spaziocodice.labs.fraglink;

import com.spaziocodice.labs.fraglink.log.MessageCatalog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class FragLinkAdvertiser implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        var version = version();
        log.info(version.endsWith("SNAPSHOT")
                        ? MessageCatalog._00001_FRAGLINK_SNAPSHOT_ENABLED
                        : MessageCatalog._00001_FRAGLINK_RELEASE_ENABLED, version);
    }

    public String version() {
        try (InputStream stream = getClass().getResourceAsStream("/version.properties")) {
            var properties = new Properties();
            properties.load(stream);

            return properties.getProperty("version", "N.A.");
        } catch(IOException exception) {
            return "N.A.";
        }
    }
}