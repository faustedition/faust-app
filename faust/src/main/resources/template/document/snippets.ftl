<#macro archiveData a complete=true>
<div id="archive_${a.LABEL}" class="archive">
	<#if complete><span class="big">${a.NAME?html}</span><br><br></#if>
	<#if a.INSTITUTION?has_content && !a.NAME?contains(a.INSTITUTION!'n/a')>${a.INSTITUTION!'n/a'?html}<br></#if>
	<#if a.DEPARTMENT?has_content && !a.NAME?contains(a.DEPARTMENT!'n/a')>${a.DEPARTMENT!'n/a'?html}<br></#if>
	${a.CITY?html}, ${a.COUNTRY?html}<br>
	<span class="hidden lat">${a.LOCATION_LAT?html}</span>
	<span class="hidden lng">${a.LOCATION_LNG?html}</span>
	<#if complete><br><a class="big" href="${a.URL}" title="${a.NAME?html}">${a.URL?html}</a></#if>
</div>
</#macro>