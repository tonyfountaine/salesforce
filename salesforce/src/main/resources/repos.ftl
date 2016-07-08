<#-- @ftlvariable name="" type="nz.co.trineo.common.views.AccountView" -->
<html lang="en">
<#assign title="GitHub Repos" />
<#include "/head.ftl">
	<body>
<#include "/nav.ftl">
		<div class="container-fluid">
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
						<#list repos as repo>
							<tr>
								<td>${repo.name}</td>
								<td>
									<button type="button" class="btn btn-default clone" data-user="${repo.owner.login}" data-name="${repo.name}"><i class="fa fa-clone" aria-hidden="true"></i> Clone</button>
								</td>
							</tr>
						</#list>
					</tbody>
				</table>
			</div>
		</div>
<#include "/scripts.ftl">
		<script>
$(function () {
    $('body').on('click', '.clone', function (e) {
        var user = $(this).data("user");
        var name = $(this).data("name");
        $.ajax({
            type: "POST",
            url: "/github/users/" + user + "/repos/" + name,
         	success: function(data, textStatus, jqXHR) {
                location.reload(true);  
            },
            error: function(jqXHR, textStatus, errorThrown) {
                alert("failure");
            }
        });
    });
});
		</script>
	</body>
</html>
