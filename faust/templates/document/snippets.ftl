<#ftl ns_prefixes={ "D":"http://www.faustedition.net/ns" }>
<#macro archiveData a complete=true>
<div id="archive_${a.@id}" class="archive">
	<#if complete><span class="big">${a.name?html}</span><br><br></#if>
	<#if !(a.name?contains((a.institution.@@text)!'n/a'))>${a.institution?html}<br></#if>
	<#if !(a.name?contains((a.department.@@text)!'n/a'))>${a.department?html}<br></#if>
	${a.city?html}, ${a.country?html}<br>
	<span class="hidden lat">${a.geolocation.@lat?html}</span>
	<span class="hidden lng">${a.geolocation.@lng?html}</span>
	<#if complete><br><a class="big" href="${a.url}" title="${a.name?html}">${a.url?html}</a></#if>
</div>
</#macro>