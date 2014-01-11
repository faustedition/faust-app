[#ftl]
[@faust.page title="Text"]

<p>
    [#if prev??]<a href="${cp}/text/${prev}">&lt;&lt;</a>[/#if]
    [#if prev?? && next??]|[/#if]
    [#if next??]<a href="${cp}/text/${next}">&gt;&gt;</a>[/#if]
</p>

<div class="pure-g">
    <div class="pure-u-1-5"></div>
    <div class="pure-u-4-5"><div class="document-textual">[@transcriptMarkup text=text /]</div></div>
</div>

[/@faust.page]
