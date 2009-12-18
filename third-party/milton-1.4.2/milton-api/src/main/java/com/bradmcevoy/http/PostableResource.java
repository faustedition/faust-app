package com.bradmcevoy.http;

import java.util.Map;

public interface PostableResource extends  GetableResource {
    String processForm(Map<String,String> parameters, Map<String,FileItem> files);    
}
