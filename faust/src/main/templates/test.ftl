<@faust.page title="Test">
<div class="yui3-g">
    <div class="yui3-u-2-3"><div id="facsimile-view"></div></div>
    <div class="yui3-u">Test</div>
</div>

<script type="text/javascript">
    YUI().use("facsimile", "dump", function (Y) {
        facsimileViewer = new Y.Faust.FacsimileViewer({
            srcNode: "#facsimile-view",
            view: { x: 0, y: 0, width: 600, height: 400 }
        });
        facsimileViewer.render();
    });
</script>
</@faust.page>