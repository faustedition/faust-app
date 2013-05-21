<#assign title>${document.CALLNUMBER!document.ID?html}<#if document.WA_ID?has_content> &ndash; ${document.WA_ID?html}</#if></#assign>
<@faust.page title=title layout="wide">
<script type="text/javascript">
    var document = {
        id: ${document.ID},
        metadata: ${document.METADATA!"{}"}
    };
</script>
</@faust.page>