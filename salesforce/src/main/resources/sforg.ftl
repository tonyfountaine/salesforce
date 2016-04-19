<#-- @ftlvariable name="" type="nz.co.trineo.salesforce.views.SfOrgView" -->
<html lang="en">
<#assign title="${org.name}" />
<#include "/head.ftl" />
	<body>
<#include "/nav.ftl" />
		<div class="container-fluid">
			<div class="row">
				<div class="col-xs-11 col-xs-offset-1">
					<h1>${title} <small>${org.id}</small></h1>
				</div>
			</div>
			<ul class="nav nav-pills" id="OrgTabs">
				<li class="active"><a href="#backups" aria-controls="backups" data-toggle="pill">Backups</a></li>
				<li><a href="#metadata" aria-controls="metadata" data-toggle="pill">Metadata</a></li>
				<li><a href="#tests" aria-controls="tests" data-toggle="pill">Tests</a></li>
				<li><a href="#compare" aria-controls="compare" data-toggle="pill">Compare</a></li>
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
							<div id="metaTree"></div>
						</div>
						<div class="col-xs-8">
							<div class="panel panel-default">
								<div class="panel-heading">
									<h3 class="panel-title" id="codeHead">Panel title</h3>
								</div>
								<div id="code">
								</div>
							</div>
						</div>
					</div>
				</div>
				<div class="tab-pane" id="tests">
					<div class="row">
						<div class="col-xs-6">
							<div id="testTree"></div>
						</div>
						<div class="col-xs-6">
							<div class="panel panel-default">
								<div class="panel-heading">
									<h3 class="panel-title" id="codeHead">Code Coverage</h3>
								</div>
								<table class="table table-striped table-responsive">
									<thead>
										<tr>
											<th>Namespace</th>
											<th>Name</th>
											<th>Percent</th>
											<th>Coverage</th>
										</tr>
									</thead>
									<tbody id="coverage">
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
				<div class="tab-pane" id="compare">
					<div class="row">
						<div class="col-xs-5">
							<div class="panel panel-default">
								<div class="panel-heading">
									<h3 class="panel-title">
										Source
										<select class="form-control" id="sourceSelect">
											<#list backups as backup>
												<option>${backup}</option>
											</#list>
										</select>
									</h3>
								</div>
							</div>
						</div>
						<div class="col-xs-5">
							<div class="panel panel-default">
								<div class="panel-heading">
									<h3 class="panel-title">
										Target
										<select class="form-control" id="targetSelect">
											<#list backups as backup>
												<option>${backup}</option>
											</#list>
										</select>
									</h3>
								</div>
							</div>
						</div>
						<div class="col-xs-2">
							<div class="panel panel-default">
								<div class="panel-heading">
									<h3 class="panel-title">
										<button class="btn btn-default" id="compareButton">
											Compare
										</button>
									</h3>
								</div>
							</div>
						</div>
					</div>
					<div class="row" id="compareData">
					</div>
				</div>
			</div>
		</div>
<#include "/scripts.ftl">
		<script>
$(function () {
    $('body').on('click', '.delete', function (e) {
        var value = $(this).data("id");
        $.ajax({
            type: "DELETE",
            url: "/sf/orgs/${org.id}/backups/" + value,
            success: function(data, textStatus, jqXHR) {
                location.reload(true);  
            },
            error: function(jqXHR, textStatus, errorThrown) {
                alert("failure");
            }
        });
    });

    $('body').on('click', '.download', function (e) {
        var value = $(this).data("id");
        window.open("/sf/orgs/${org.id}/backups/" + value);
    });

    $('body').on('click', '#compareButton', function (e) {
        var sourceValue = $('#sourceSelect').val();
        var targetValue = $('#targetSelect').val();
        $.ajax({
            type: "GET",
            url: "/sf/orgs/${org.id}/compare/" + sourceValue + "/" + targetValue,
            success: function(data, textStatus, jqXHR) {
                $('#compareData').html(data);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                alert("failure");
            }
        });
    });

	$.ajax({
		type: "GET",
		url: "/sf/orgs/${org.id}/coverage",
		accepts: "text/html",
		success: function (data, textStatus, jqXHR) {
			$('#coverage').html(data);
		},
		error: function (jqXHR, textStatus, errorThrown) {
			alert("failure");
		}
	});
});

$.getJSON("/sf/orgs/${org.id}/metadata", "", function (data) {
	var $metaTree = $('#metaTree').treeview({
		data: [data],
		collapseIcon: "fa fa-folder-open-o",
		expandIcon: "fa fa-folder-o",
		nodeIcon: "fa fa-file-o"
	});

	$('#metaTree').on('nodeSelected', function(event, node) {
		var path = "";
		var n = node;
		if (!n.text.contains(".")) {
			return;
		}
		while (n.text != '/') {
			path = "/" + n.text + path;
			n = $metaTree.treeview('getParent', n);
		}
		$.ajax({
			type: "GET",
			url: "/sf/orgs/${org.id}/metadata" + path,
			accepts: "text/html",
			success: function (data, textStatus, jqXHR) {
				$('#codeHead').text(node.text);
				$('#code').html(data);
			},
			error: function (jqXHR, textStatus, errorThrown) {
				alert("failure");
			}
		});
	});
});

$.getJSON("/sf/orgs/${org.id}/tests", "", function (data) {
	var $metaTree = $('#testTree').treeview({
		data: [data],
		collapseIcon: "fa fa-folder-open-o",
		expandIcon: "fa fa-folder-o",
		nodeIcon: "fa fa-file-o"
	});
/*
	$('#metaTree').on('nodeSelected', function(event, node) {
		var path = "";
		var n = node;
		if (!n.text.contains(".")) {
			return;
		}
		while (n.text != '/') {
			path = "/" + n.text + path;
			n = $metaTree.treeview('getParent', n);
		}
		$.ajax({
			type: "GET",
			url: "/sf/orgs/${org.id}/metadata" + path,
			accepts: "text/html",
			success: function (data, textStatus, jqXHR) {
				$('#codeHead').text(node.text);
				$('#code').html(data);
			},
			error: function (jqXHR, textStatus, errorThrown) {
				alert("failure");
			}
		});
	});*/
});
		</script>
	</body>
</html>
