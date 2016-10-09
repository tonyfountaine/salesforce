<#-- @ftlvariable name="" type="nz.co.trineo.common.views.SuccessView" -->
<html lang="en">
<#assign title="OAuth Success" />
<#include "/head.ftl">
	<body>
		<div class="container">
		</div>
<#include "/scripts.ftl">
		<script>
$(document).ready(function () {
		window.opener.location.reload(true);
    	window.close();
    });
		</script>
	</body>
</html>
