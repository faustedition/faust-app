[#ftl]
[@faust.page title=id]

<h2>Text</h2>

<p><a href="${cp}/transcript/${id?c}/source" title="XML Source">XML</a></p>

<pre>[#list text as t][#if !(t?is_hash)]${t?html}[/#if][/#list]</pre>

<script type="text/javascript">
    var model = [@json id=id text=text/];
    YUI().use("text-annotation", "datasource", function(Y) {
        var dataSource = new Y.DataSource.Local({ source: model });
        //var dataSource = new Y.DataSource.IO({ source: "data" });
        dataSource.plug({ fn: Y.Faust.TextSchema }).sendRequest({
            on: {
                success: function(e) {
                    Y.log(e.response);
                },
                failure: function(e) {
                    alert(e.error.message);
                }
            }
        });
    })
</script>
[/@faust.page]
