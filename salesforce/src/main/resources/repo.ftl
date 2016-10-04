<#-- @ftlvariable name="" type="nz.co.trineo.github.views.RepoView" -->
<html lang="en">
<#assign title="${repo.name}" />
<#include "/head.ftl">
	<body>
<#include "/nav.ftl">
		<div class="container">
			<div class="row">
				<div class="col-xs-11 col-xs-offset-1">
					<h1>${title}</h1>
				</div>
			</div>
            <ul class="nav nav-pills" id="OrgTabs">
                <li class="active"><a href="#overview" aria-controls="overview" data-toggle="pill">Overview</a></li>
                <li><a href="#branches" aria-controls="branches" data-toggle="pill">Branches</a></li>
                <li><a href="#tags" aria-controls="tags" data-toggle="pill">Tags</a></li>
                <li><a href="#commits" aria-controls="commits" data-toggle="pill">Commits</a></li>
            </ul>
            <div class="tab-content">
                <div class="tab-pane active" id="overview">
                    <div class="row">
                        <div class="col-xs-11 col-xs-offset-1">
                            <div class="row">
                                <div id="clientName">
                                    <span>Client: </span>
                                    <span>
                                        <#if repo.client??>
                                        ${repo.client.name!""}
                                        </#if>
                                    </span>
                                    <a class="btn" id="editClientName">
                                        <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
                                    </a>
                                </div>
                                <div class="input-group" id="clientNameEdit">
                                    <#if repo.client??>
                                        <select class="form-control" id="newClientName" placeholder="Client Name" value="${repo.client.id}">
                                            <#list clients as client>
                                                <option value="${client.id?string["####"]}">${client.name}</option>
                                            </#list>
                                        </select>
                                    <#else />
                                        <select class="form-control" id="newClientName" placeholder="Client Name">
                                            <#list clients as client>
                                                <option value="${client.id?string["####"]}">${client.name}</option>
                                            </#list>
                                        </select>
                                    </#if>
                                    <span class="input-group-btn">
                                        <button type="button" class="btn btn-danger" id="clientNameCancel">
                                            <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
                                        </button>
                                        <button type="button" class="btn btn-success" data-id="${repo.id}" id="clientNameChange">
                                            <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>
                                        </button>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="tab-pane" id="branches">
                    <div class="row">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <h3 class="panel-title">Branches</h3>
                            </div>
            				<table class="table table-striped table-hover">
            					<thead>
            						<tr>
            							<th>Name</th>
            							<th>SHA</th>
            							<th>URL</th>
            							<th>&nbsp;</th>
            						</tr>
            					</thead>
            					<tbody>
            					   <#list branches as branch>
            					       <tr>
            					           <td>${branch.name}</td>
            					           <td>${branch.sha!""}</td>
            					           <td>${branch.url!""}</td>
            					           <td>
            					               <button type="button" class="btn btn-default checkout" data-id="${repo.id?string["####"]}" data-branch="${branch.name}">Checkout</button>
            					           </td>
            					       </tr>
            					   </#list>
            					</tbody>
            				</table>
            			</div>
            		</div>
            	</div>
                <div class="tab-pane" id="tags">
                    <div class="row">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <h3 class="panel-title">Tags</h3>
                            </div>
                            <table class="table table-striped table-hover">
                                <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>SHA</th>
                                        <th>URL</th>
                                        <th>&nbsp;</th>
                                    </tr>
                                </thead>
                                <tbody>
                                   <#list tags as tag>
                                       <tr>
                                           <td>${tag.name}</td>
                                           <td>${tag.sha!""}</td>
                                           <td>${tag.url!""}</td>
                                           <td>
                                               <button type="button" class="btn btn-default download" data-id="${tag.tarballUrl}"><i class="fa fa-cloud-download" aria-hidden="true"></i> Tar</button>
                                               <button type="button" class="btn btn-default download" data-id="${tag.zipballUrl}"><i class="fa fa-cloud-download" aria-hidden="true"></i> Zip</button>
                                           </td>
                                           <td>&nbsp;</td>
                                       </tr>
                                   </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div class="tab-pane" id="commits">
                    <div class="row">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <h3 class="panel-title">Commits</h3>
                            </div>
                            <table class="table table-striped table-hover">
                                <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>&nbsp;</th>
                                    </tr>
                                </thead>
                                <tbody>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
			</div>
		</div>
<#include "/scripts.ftl">
		<script>
$(function () {
    $("#clientName").show();
    $("#clientNameEdit").hide();
    $('body').on('click', '#editClientName', function (e) {
        $("#clientName").hide();
        $("#clientNameEdit").show();
    });
    $('body').on('click', '#clientNameCancel', function (e) {
        $("#clientName").show();
        $("#clientNameEdit").hide();
    });
    $('body').on('click', '#clientNameChange', function (e) {
        var newName = parseInt($('#newClientName').val());
        var data = JSON.stringify({id: ${repo.id?string["####"]}, client: {id: newName}});
        $.ajax({
            data: data,
            type: "PUT",
            url: "/github/repos/",
            contentType: "application/json",
            dataType: "json",
            success: function (data, textStatus, jqXHR) {
                window.location.reload();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert("failure");
            }
        });
    });

/*    $('body').on('click', '#submit', function (e) {
        var values = $('#NewAccountForm').serialize();
        $.ajax({
            type: "POST",
            url: "/github/repos/?" + values,
            success: function(data, textStatus, jqXHR) {
                location.reload(true);  
            },
            error: function(jqXHR, textStatus, errorThrown) {
                alert("failure");
            }
        });
    });

    $('body').on('click', '.delete', function (e) {
        var value = $(this).data("id");
        $.ajax({
            type: "DELETE",
            url: "/github/repos/" + value,
            done: function(data, textStatus, jqXHR) {
                location.reload(true);
            },
            fail: function(jqXHR, textStatus, errorThrown) {
                alert("failure");
            }
        });
    });
    */
});
		</script>
	</body>
</html>
