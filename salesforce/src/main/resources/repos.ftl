<#-- @ftlvariable name="" type="nz.co.trineo.common.views.AccountView" -->
<html lang="en">
<#assign title="GitHub Repos" />
<#include "/head.ftl">
	<body>
<#include "/nav.ftl">
		<div class="container-fluid">
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
                                    <label class="control-label">Account</label>
                                    <select class="form-control" id="acc" name="acc">
                                        <#list accounts as account>
                                            <option value="${account.id?string["####"]}">${account.name}</option>
                                        </#list>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label class="control-label">URL</label>
                                    <input type="text" class="form-control" id="url" name="url" />
                                </div>
                            </form>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                            <button type="submit" class="btn btn-primary" data-dismiss="modal" id="submit">Add</button>
                        </div>
                    </div>
                </div>
            </div>
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
				<table class="table table-striped table-hover">
					<thead>
						<tr>
							<th>Name</th>
							<th>&nbsp;</th>
						</tr>
					</thead>
					<tbody>
						<#list repos as repo>
							<tr>
								<td><a href="/github/repos/${repo.id?string["####"]}">${repo.name}</a></td>
								<td>
                                    <button type="button" class="btn btn-warning delete" data-id="${repo.id?string["####"]}"><i class="fa fa-remove" aria-hidden="true"></i> Delete</button>
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
    $('body').on('click', '#submit', function (e) {
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
});
		</script>
	</body>
</html>
