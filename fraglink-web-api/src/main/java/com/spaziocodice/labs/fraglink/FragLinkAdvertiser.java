package com.spaziocodice.labs.fraglink;

import com.spaziocodice.labs.fraglink.log.MessageCatalog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

@Slf4j
public class FragLinkAdvertiser implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    BuildProperties build;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        var version = build.getVersion();
        if (version.endsWith("SNAPSHOT")) {
            log.info(MessageCatalog._00001_FRAGLINK_SNAPSHOT_ENABLED, build.getVersion(), build.getTime());
            log.info(MessageCatalog._00002_SNAPSHOT_WARN, build.getVersion(), build.getTime());
        } else {
            log.info(MessageCatalog._00001_FRAGLINK_RELEASE_ENABLED, build.getVersion(), build.getTime());
        }
    }
}