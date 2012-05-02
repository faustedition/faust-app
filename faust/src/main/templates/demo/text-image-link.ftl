<@faust.page title="Text/Bild-Verlinkung">

<div class="yui3-g">
    <div class="yui3-u-2-3"><div id="facsimile-view"></div></div>
    <div class="yui3-u-1-3">
        <div style="padding: 1em">
            <h2>Transkript</h2>

            <div id="lines" style="font-size: 116%; padding: 1em; border: 1px solid #ccc">
                <p id="line-1">Faust.</p>
                <p id="line-2">Zweyter Theil</p>
                <p id="line-3">1831.</p>
            </div>

            <h2>Legende</h2>

            <ul>
                <li>Bewegen im Faksimile mit <strong>Pfeiltasten</strong></li>
                <li>Zoom-In/-Out mit <strong>+/-</strong></li>
                <li>Zentrieren des Faksimiles mit <strong>c</strong></li>
                <li>Reset mit <strong>0 (null)</strong></li>
                <li><strong>Klicken</strong> auf eine Zeile des Transkripts zur Hervorhebung derselben im Faksimile</li>
            </ul>
        </div>
    </div>
</div>
<script type="text/javascript">
    YUI().use("facsimile", "event", "dump", function (Y) {
        facsimileViewer = new Y.Faust.FacsimileViewer({
            srcNode: "#facsimile-view",
            view: { x: 0, y: 0, width: 600, height: 600 }
        });
        facsimileViewer.render();

        var coords = {
            "line-1": { x: 1700, y: 2300, width: 700, height: 400 },
            "line-2": { x: 1600, y: 2600, width: 1000, height: 400 },
            "line-3": { x: 1950, y: 3100, width: 500, height: 400 }
        };

        Y.one("#lines").delegate("click", function(e) {
            facsimileViewer.highlight(coords[e.target.get("id")]);
        }, "p");
    });
</script>
</@faust.page>