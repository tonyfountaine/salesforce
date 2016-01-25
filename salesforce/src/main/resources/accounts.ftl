<#-- @ftlvariable name="" type="nz.co.trineo.common.views.AccountView" -->
<#assign title="Connected Accounts" />
<#include "/header.ftl">
		<div class="container">
			<div class="row">
				<div class="col-xs-11 col-xs-offset-1">
					<h1>${title}</h1>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-1 col-xs-offset-1">
					<button type="button" class="btn btn-primary" data-toggle="modal" data-target="#NewAccount"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> New</button>
				</div>
			</div>
			<div class="row">
				<table class="table table-striped">
					<thead>
						<tr>
							<th>Name</th>
							<th>Service</th>
							<th></th>
						</tr>
					</thead>
					<tbody>
						<#list accounts as account>
							<tr>
								<td>${account.name!""}</td>
								<td>${account.service}</td>
								<td>
									<button type="button" class="btn btn-default">Verify</button>
									<button type="button" class="btn btn-default">Rename</button>
									<button type="button" class="btn btn-default">Reconnect</button>
									<button type="button" class="btn btn-default">Disconnect</button>
									<button type="button" class="btn btn-warning">Delete</button>
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
<#include "/footer.ftl">
		<script>
$(function () {
    $('body').on('click', '#submit', function (e) {
        $('#NewAccountForm').submit();
        //$('#NewAccount').modal('hide');
    });
});		</script>
	</body>
</html>