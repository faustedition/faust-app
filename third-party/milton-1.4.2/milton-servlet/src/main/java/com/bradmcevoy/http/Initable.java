package com.bradmcevoy.http;

public interface Initable {
    void init(ApplicationConfig config, HttpManager manager);
    void destroy(HttpManager manager);
}
