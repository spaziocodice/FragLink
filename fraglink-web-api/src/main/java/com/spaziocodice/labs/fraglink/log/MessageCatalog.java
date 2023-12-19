package com.spaziocodice.labs.fraglink.log;

public interface MessageCatalog {
    String MODULE_NAME = "FRAGLINK";
    String PREFIX = "<" + MODULE_NAME + "-";
    String _00001_FRAGLINK_SNAPSHOT_ENABLED = PREFIX + "00001> : FragLink v{} has been enabled on this server.";
    String _00001_FRAGLINK_RELEASE_ENABLED = PREFIX + "00001> : FragLink v{} ({}) has been enabled on this server.";
    String _00002_SNAPSHOT_WARN = PREFIX + "00002> : Do not use a SNAPSHOT version in production!";
    String _00003_NO_PATTERN_RESOLVER = PREFIX + "00003> : Couldn't find any triple/quad pattern resolver: as a consequence of that, returned fragments will be always empty!";

}
