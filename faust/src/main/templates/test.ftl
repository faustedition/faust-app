<@faust.page title="Test">

<div id="facsimile-view"></div>

<script type="text/javascript">
    YUI().use("facsimile", "dump", function (Y) {
        facsimileViewer = new Y.Faust.FacsimileViewer({
            srcNode: "#facsimile-view",
            view: { x: 0, y: 0, width: 900, height: 600 }
        });
        facsimileViewer.render();
    });
</script>
</@faust.page>