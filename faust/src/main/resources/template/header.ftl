<#import "faust.ftl" as faust>
<#assign cp = config["ctx.path"]>
<#if cp?ends_with("/")><#assign cp = cp?substring(0, cp?last_index_of("/"))></#if>
