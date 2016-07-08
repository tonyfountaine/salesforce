<#-- @ftlvariable name="" type="nz.co.trineo.common.views.AccountView" -->
<html lang="en">
<#assign title="Account - ${account.id}" />
<#include "/head.ftl">
	<body>
<#include "/nav.ftl">
		<div class="container">
			<div class="row">
				<div class="col-xs-11 col-xs-offset-1">
					<h1>Connected Account <small>${account.id}</small></h1>
				</div>
			</div>
			<div class="row">
				<form class="form-horizontal">
					<div class="form-group">
						<label class="col-xs-2 control-label">Service</label>
						<div class="col-xs-9">
							<input type="text" class="form-control" value="${account.service}" />
						</div>
					</div>
					<div class="form-group">
						<label class="col-xs-2 control-label">Username</label>
						<div class="col-xs-9">
							<input type="text" class="form-control" value="${account.credentals.username}" />
						</div>
					</div>
					<div class="form-group">
						<label class="col-xs-2 control-label">Password</label>
						<div class="col-xs-9">
							<input type="password" class="form-control" value="${account.credentals.password}" />
						</div>
					</div>
				</form>
			</div>
		</div>
<#include "/scripts.ftl">
	</body>
</html>
