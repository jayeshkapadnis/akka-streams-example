package com.hashmapinc.api;

import java.util.Properties;

public class SourceConfig {
    private final Properties props;

    public SourceConfig(Properties props) {
        this.props = props;
    }

    public String getProperty(String key){
        return props.getProperty(key);
    }
}
