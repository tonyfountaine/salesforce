<#-- @ftlvariable name="" type="nz.co.trineo.salesforce.views.ContentView" -->
<table class="table table-condensed">
	<#assign count=0 />
	<#list lines as line>
		<tr>
			<td class="num">${line?counter}</td>
			<td class="code">${line}</td>
		</tr>
	</#list>
</table>