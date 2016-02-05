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
			<div class="row">
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
		</div>
<#include "/scripts.ftl">
		<script>
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
		</script>
	</body>
</html>
