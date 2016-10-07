<#-- @ftlvariable name="" type="nz.co.trineo.salesforce.views.CompareView" -->
<#assign keys = diffMap?keys />
<#macro treeNode node>
{
    "text": "${node.text}",
    <#if node.nodes??>
        <#list node.nodes>
            "nodes": [
            <#items as child>
                <@treeNode node=child />
            </#items>
            ]
        </#list>
    </#if>
},
</#macro>
<div class="col-xs-4">
    <div id="metaTree"></div>
</div>
<script>
var data = [
<@treeNode node=rootNode />
];
$(function () {
    var $metaTree = $('#metaTree').treeview({
        data: data,
        collapseIcon: "fa fa-folder-open-o",
        expandIcon: "fa fa-folder-o",
        nodeIcon: "fa fa-file-o"
    });

    $('#metaTree').on('nodeSelected', function(event, node) {
        var path = "";
        var n = node;
        if (n.text.indexOf(".") == -1) {
            return;
        }
        while (n.text != '/') {
            if (path != "") {
                path = "_" + path;
            }
            path = n.text + path;
            n = $metaTree.treeview('getParent', n);
        }
        path = path.replace(/\./g, '_');
        $("#compareData .tab-content .tab-pane").hide();
        $("#" + path).show();
    });
});
</script>
<div class="col-xs-8">
	<div class="tab-content">
		<#list keys as key>
			<div class="tab-pane" id="${key?replace('.', '_')}">
				<#list diffMap[key] as diff>
    				<div class="panel panel-default">
    					<div class="panel-heading">
    						<#assign pathA=diff.pathA!"" />
    						<#if pathA == "" || pathA == "/dev/null">
    							<h3 class="panel-title">${diff.pathB}</h3>
    						<#else>
    							<h3 class="panel-title">${diff.pathA}</h3>
    						</#if>
    					</div>
    					<div class="diffContent">
    						<table class="table table-condensed diff">
    							<tbody>
    								<tr class="header">
    									<td class="num">...</td>
    									<td class="num">...</td>
    									<td class="line">@@ ${diff.headerA} ${diff.headerB} @@</td>
    								</tr>
    								<#list diff.lines as line>
    									<#if line.removed>
    										<tr class="removed">
    											<td class="num">${(line.lineNumA + 1)?string["####"]}</td>
    											<td class="num">&nbsp;&nbsp;&nbsp;</td>
    											<td class="line">${line.line}</td>
    										</tr>
    									<#elseif line.added>
    										<tr class="added">
    											<td class="num">&nbsp;&nbsp;&nbsp;</td>
    											<td class="num">${(line.lineNumB + 1)?string["####"]}</td>
    											<td class="line">${line.line}</td>
    										</tr>
    									<#else>
    										<tr>
    											<td class="num">${(line.lineNumA + 1)?string["####"]}</td>
    											<td class="num">${(line.lineNumB + 1)?string["####"]}</td>
    											<td class="line">${line.line}</td>
    										</tr>
    									</#if>
    								</#list>
    							</tbody>
    						</table>
    					</div>
    				</div>
				</#list>
			</div>
		</#list>
	</div>
</div>
