<#-- @ftlvariable name="" type="nz.co.trineo.github.views.RepoView" -->
<html lang="en">
<#assign title="${repo.name}" />
<#include "/head.ftl">
	<body>
<#include "/nav.ftl">
		<div class="container-fluid">
			<div class="row">
				<div class="col-xs-11 col-xs-offset-1">
					<h1>${title}</h1>
				</div>
			</div>
            <ul class="nav nav-pills" id="OrgTabs">
                <li class="active"><a href="#branches" aria-controls="branches" data-toggle="pill">Branches</a></li>
                <li><a href="#tags" aria-controls="tags" data-toggle="pill">Tags</a></li>
                <li><a href="#commits" aria-controls="commits" data-toggle="pill">Commits</a></li>
            </ul>
            <div class="tab-content">
                <div class="tab-pane active" id="branches">
                    <div class="row">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <h3 class="panel-title">Branches</h3>
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
                                        <th>&nbsp;</th>
                                    </tr>
                                </thead>
                                <tbody>
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
