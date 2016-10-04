<#-- @ftlvariable name="" type="nz.co.trineo.common.views.ClientsView" -->
<html lang="en">
<#assign title="Clients" />
<#include "/head.ftl">
	<body>
<#include "/nav.ftl">
		<div class="container">
			<div class="row">
				<div class="col-xs-11 col-xs-offset-1">
					<h1>${title}</h1>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-1 col-xs-offset-1">
					<button type="button" class="btn btn-primary" data-toggle="modal" data-target="#NewClient"><span class="fa fa-plus" aria-hidden="true"></span> New</button>
				</div>
			</div>
			<div class="row">
                <#list clients as client>
                    <div class="col-sm-12">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <button type="button" class="close" aria-label="Delete" title="Delete"><i class="fa fa-remove" aria-hidden="true"></i></button>
                                <h3 class="panel-title">${client.name}</h3>
                            </div>
                            <div class="panel-body">
                                <div class="row">
                                    <div class="col-sm-6">
                                        <h4>
                                            <button type="button" class="close" aria-label="Add Org" title="Add Org"><i class="fa fa-plus" aria-hidden="true"></i></button>
                                            Salesforce Orgs
                                        </h4>
                                        <table class="table table-striped table-hover">
                                            <thead>
                                                <tr>
                                                    <th>Name</th>
                                                    <th>&nbsp;</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <#if client.organizations??>
                                                    <#list client.organizations as org>
                                                        <tr>
                                                            <td><a href="/sf/orgs/${org.id}">${org.nickName!org.name!""}</a></td>
                                                            <td>
                                                                <button type="button" class="btn btn-default backup" data-id="${org.id}"><i class="fa fa-cloud-download" aria-hidden="true"></i></button>
                                                                <button type="button" class="btn btn-default tests" data-id="${org.id}"><i class="fa fa-tasks" aria-hidden="true"></i></button>
                                                            </td>
                                                        </tr>
                                                    </#list>
                                                </#if>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="col-sm-6">
                                        <h4>
                                            <button type="button" class="close" aria-label="Add Repo" title="Add Repo"><i class="fa fa-plus" aria-hidden="true"></i></button>
                                            GitHub Repos
                                        </h4>
                                        <table class="table table-striped table-hover">
                                            <thead>
                                                <tr>
                                                    <th>Name</th>
                                                    <th>&nbsp;</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <#if client.repositories??>
                                                    <#list client.repositories as repo>
                                                        <tr>
                                                            <td><a href="/github/repos/${repo.id?string["####"]}">${repo.name}</a></td>
                                                            <td>
                                                                <button type="button" class="btn btn-warning delete" data-id="${repo.id?string["####"]}"><i class="fa fa-remove" aria-hidden="true"></i></button>
                                                            </td>
                                                        </tr>
                                                    </#list>
                                                </#if>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </#list>
			</div>
			<div class="modal fade" id="NewClient" tabindex="-1" role="dialog" aria-labelledby="NewClientLabel">
			  <div class="modal-dialog" role="document">
			    <div class="modal-content">
			      <div class="modal-header">
			        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
			        <h4 class="modal-title" id="NewClientLabel">New Client</h4>
			      </div>
			      <div class="modal-body">
					<form class="form" name="NewClientForm" id="NewClientForm" action="oauth" method="get" target="_blank">
						<div class="form-group">
							<label class="control-label">Name</label>
							<input type="text" class="form-control" id="name" name="name" />
						</div>
					</form>
			      </div>
			      <div class="modal-footer">
			        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			        <button type="submit" class="btn btn-primary" data-dismiss="modal" id="submit">
			        	Save changes
			        </button
			      </div>
			    </div>
			  </div>
			</div>
		</div>
<#include "/scripts.ftl">
		<script>
$(function () {
    $('body').on('click', '#submit', function (e) {
        var values = $('#NewClientForm').serialize();
        $.ajax({
            type: "POST",
            url: "/clients/?" + values,
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
            url: "clients/" + value,
            done: function(data, textStatus, jqXHR) {
                location.reload(true);
            },
            fail: function(jqXHR, textStatus, errorThrown) {
                alert("failure");
            }
        });
    });
});
		</script>
	</body>
</html>
