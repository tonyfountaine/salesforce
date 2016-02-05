<#-- @ftlvariable name="" type="nz.co.trineo.common.views.AccountView" -->
<html lang="en">
<#assign title="Connected Accounts" />
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
					<button type="button" class="btn btn-primary" data-toggle="modal" data-target="#NewAccount"><span class="fa fa-plus" aria-hidden="true"></span> New</button>
				</div>
			</div>
			<div class="row">
				<table class="table table-striped">
					<thead>
						<tr>
							<th>Name</th>
							<th>&nbsp;</th>
						</tr>
					</thead>
					<tbody>
						<#list accounts as account>
							<tr>
								<td><i class="fa fa-${account.service} fa-2x"></i> ${account.name!""}</td>
								<td>
									<button type="button" class="btn btn-default"><i class="fa fa-check" aria-hidden="true"></i> Verify</button>
									<button type="button" class="btn btn-default"><i class="fa fa-edit" aria-hidden="true"></i> Rename</button>
									<button type="button" class="btn btn-warning delete" data-id="${account.id}"><i class="fa fa-remove" aria-hidden="true"></i> Delete</button>
								</td>
							</tr>
						</#list>
					</tbody>
				</table>
			</div>
			<div class="modal fade" id="NewAccount" tabindex="-1" role="dialog" aria-labelledby="NewAccountLabel">
			  <div class="modal-dialog" role="document">
			    <div class="modal-content">
			      <div class="modal-header">
			        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
			        <h4 class="modal-title" id="NewAccountLabel">New Account</h4>
			      </div>
			      <div class="modal-body">
					<form class="form" name="NewAccountForm" id="NewAccountForm" action="oauth" method="get" target="_blank">
						<div class="form-group">
							<label class="control-label">Service</label>
							<select class="form-control" id="service" name="service">
								<#list services as service>
									<option>${service}</option>
								</#list>
							</select>
						</div>
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
        var values = $('#NewAccountForm').serialize();
        e.preventDefault();
    	window.open("accounts/oauth?" + values, "oauth", "width=600,height=600,scrollbars=yes");
    });
});
$(function () {
    $('body').on('click', '.delete', function (e) {
        var value = $(this).data("id");
        $.ajax({
            type: "DELETE",
            url: "accounts/" + value,
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
