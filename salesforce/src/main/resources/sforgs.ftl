<#-- @ftlvariable name="" type="nz.co.trineo.salesforce.views.SfOrgsView" -->
<html lang="en">
<#assign title="Salesforce Orgs" />
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
					<button type="button" class="btn btn-primary" data-toggle="modal" data-target="#NewOrg"><span class="fa fa-plus" aria-hidden="true"></span> New</button>
				</div>
			</div>
			<div class="row">
				<table class="table table-striped">
					<thead>
						<tr>
							<th>Name</th>
							<th>Type</th>
							<th>Sandbox</th>
							<th>Account</th>
							<th>&nbsp;</th>
						</tr>
					</thead>
					<tbody>
						<#list orgs as org>
							<tr>
								<td><a href="/sf/orgs/${org.id}">${org.name!""}</a></td>
								<td><a href="/sf/orgs/${org.id}">${org.organizationType}</a></td>
								<td><a href="/sf/orgs/${org.id}">${org.sandbox?string('yes', 'no')}</a></td>
								<td><a href="/accounts/${org.account.id}">${org.account.name}</a></td>
								<td>
									<button type="button" class="btn btn-default backup" data-id="${org.id}"><i class="fa fa-cloud-download" aria-hidden="true"></i> Backup</button>
									<button type="button" class="btn btn-default tests" data-id="${org.id}"><i class="fa fa-tasks" aria-hidden="true"></i> Tests</button>
								</td>
							</tr>
						</#list>
					</tbody>
				</table>
			</div>
			<div class="modal fade" id="NewOrg" tabindex="-1" role="dialog" aria-labelledby="NewOrgLabel">
			  <div class="modal-dialog" role="document">
			    <div class="modal-content">
			      <div class="modal-header">
			        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
			        <h4 class="modal-title" id="NewOrgLabel">New Org</h4>
			      </div>
			      <div class="modal-body">
					<form class="form" name="NewOrgForm" id="NewOrgForm" action="oauth" method="get" target="_blank">
						<div class="form-group">
							<label class="control-label">Account</label>
							<select class="form-control" id="acc" name="acc">
								<#list accounts as account>
									<option value="${account.id}">${account.name}</option>
								</#list>
							</select>
						</div>
					</form>
			      </div>
			      <div class="modal-footer">
			        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			        <button type="submit" class="btn btn-primary" data-dismiss="modal" id="submit">
			        	Add
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
        var values = $('#NewOrgForm').serialize();
        $.ajax({
            type: "POST",
            url: "/sf/orgs/?" + values,
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
    $('body').on('click', '.backup', function (e) {
        var value = $(this).data("id");
        $.ajax({
            type: "POST",
            url: "/sf/orgs/" + value + "/backups",
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
    $('body').on('click', '.tests', function (e) {
        var value = $(this).data("id");
        $.ajax({
            type: "POST",
            url: "/sf/orgs/" + value + "/tests",
            success: function() {
                location.reload(true);  
            },
            error: function() {
                alert("failure");
            }
        });
    });
});
		</script>
	</body>
</html>
