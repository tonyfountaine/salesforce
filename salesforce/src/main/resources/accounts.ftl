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
					<button type="button" class="btn btn-primary" data-toggle="modal" data-target="#newAccount"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> New</button>
				</div>
			</div>
			<div class="row">
				<table class="table table-striped">
					<thead>
						<tr>
							<th>Service</th>
							<th></th>
						</tr>
					</thead>
					<tbody>
						<#list accounts as account>
							<tr>
								<td>${account.service}</td>
								<td><button type="button" class="btn btn-default">Verify</button></td>
							</tr>
						</#list>
					</tbody>
				</table>
			</div>
			<div class="modal fade" id="newAccount" tabindex="-1" role="dialog" aria-labelledby="newAccountLabel">
			  <div class="modal-dialog" role="document">
			    <div class="modal-content">
			      <div class="modal-header">
			        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
			        <h4 class="modal-title" id="newAccountLabel">New Account</h4>
			      </div>
			      <div class="modal-body">
					<form class="form" name="newAccount" action="/accounts">
						<div class="form-group">
							<label class="control-label">Service</label>
							<input type="text" class="form-control" id="service" name="service />
						</div>
						<div class="form-group">
							<label class="control-label">Username</label>
							<input type="text" class="form-control" id="username" name="username" />
						</div>
						<div class="form-group">
							<label class="control-label">Password</label>
							<input type="password" class="form-control" id="password" name="password" />
						</div>
					</form>
			      </div>
			      <div class="modal-footer">
			        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			        <button type="button" class="btn btn-primary">Save changes</button>
			      </div>
			    </div>
			  </div>
			</div>
		</div>
<#include "/footer.ftl">