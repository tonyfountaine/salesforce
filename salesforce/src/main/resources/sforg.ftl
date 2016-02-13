<#-- @ftlvariable name="" type="nz.co.trineo.common.views.AccountView" -->
<html lang="en">
<#assign title="${org.name}" />
<#include "/head.ftl">
	<body>
<#include "/nav.ftl">
		<div class="container">
			<div class="row">
				<div class="col-xs-11 col-xs-offset-1">
					<h1>${title} <small>${org.id}</small></h1>
				</div>
			</div>
			<ul class="nav nav-pills" id="OrgTabs">
				<li class="active"><a href="#backups" aria-controls="backups" data-toggle="pill">Backups</a></li>
				<li><a href="#metadata" aria-controls="metadata" data-toggle="pill">Metadata</a></li>
				<li><a href="#tests" aria-controls="tests" data-toggle="pill">Tests</a></li>
			</ul>
			<div class="tab-content">
				<div class="tab-pane active" id="backups">
					<div class="panel panel-default">
						<div class="panel-heading">
							<h3 class="panel-title">Backups</h3>
						</div>
						<table class="table table-striped">
							<thead>
								<tr>
									<th>Date</th>
									<th>&nbsp;</th>
								</tr>
							</thead>
							<tbody>
								<#list backups as backup>
									<tr>
										<td>${backup}</td>
										<td>
											<button type="button" class="btn btn-default download" data-id="${backup}"><i class="fa fa-download" aria-hidden="true"></i> Download</button>
											<button type="button" class="btn btn-warning delete" data-id="${backup}"><i class="fa fa-remove" aria-hidden="true"></i> Delete</button>
										</td>
									</tr>
								</#list>
							</tbody>
						</table>
					</div>
				</div>
				<div class="tab-pane" id="metadata">
					<div class="row">
						<div class="col-xs-4">
							<div class="panel panel-default">
								<div id="tree"></div>
							</div>
						</div>
						<div class="col-xs-8">
							<div class="panel panel-default">
								<div class="panel-heading">
									<h3 class="panel-title" id="codeHead">Panel title</h3>
								</div>
								<pre id="code" class="prettyprint linenums lang-xml">
								</pre>
							</div>
						</div>
					</div>
				</div>
				<div class="tab-pane" id="tests">
					<div class="panel panel-default">
						<div class="panel-heading">
							<h3 class="panel-title">Test Runs</h3>
						</div>
						<table class="table table-striped">
							<thead>
								<tr>
									<th>Date</th>
									<th>Results</th>
									<th>&nbsp;</th>
								</tr>
							</thead>
							<tbody>
								<#list backups as backup>
									<tr>
										<td>${backup}</td>
										<td>&nbsp;</td>
										<td>
											<button type="button" class="btn btn-default download" data-id="${backup}"><i class="fa fa-download" aria-hidden="true"></i> Download</button>
											<button type="button" class="btn btn-warning delete" data-id="${backup}"><i class="fa fa-remove" aria-hidden="true"></i> Delete</button>
										</td>
									</tr>
								</#list>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
<#include "/scripts.ftl">
		<script>
//hljs.initHighlightingOnLoad();
$(function () {
    $('body').on('click', '.delete', function (e) {
        var value = $(this).data("id");
        $.ajax({
            type: "DELETE",
            url: "/sf/orgs/${org.id}/backups/" + value,
            success: function() {
                location.reload(true);  
            },
            error: function() {
                alert("failure");
            }
        });
    });
});
$(function () {
    $('body').on('click', '.download', function (e) {
        var value = $(this).data("id");
        window.open("/sf/orgs/${org.id}/backups/" + value);
    });
});
//$('#tree').treeview({data: getTree()});
$.getJSON("/sf/orgs/${org.id}/metadata", "", function (data) {
	var $tree = $('#tree').treeview({
		data: [data],
		collapseIcon: "fa fa-folder-open-o",
		expandIcon: "fa fa-folder-o",
		nodeIcon: "fa fa-file-o",
		showBorder: false
	});
	$('#tree').on('nodeSelected', function(event, node) {
		var path = "";
		var n = node;
		while (n.text != '/') {
			path = "/" + n.text + path;
			n = $tree.treeview('getParent', n);
		}
		$.ajax({
			type: "GET",
			url: "/sf/orgs/${org.id}/metadata" + path,
			success: function (data) {
				$('#codeHead').text(node.text);
				$('#code').text(data);
				//hljs.highlightBlock($('#code').get());
				$('#code').removeClass('prettyprinted').removeClass('lang-xml').removeClass('lang-java');
				if (node.text.contains('.cls')) {
					$('#code').addClass('lang-java');
				} else {
					$('#code').addClass('lang-xml');
				}
				PR.prettyPrint();
			},
			error: function () {
				alert("failure");
			}
		});
	});
});
		</script>
	</body>
</html>
