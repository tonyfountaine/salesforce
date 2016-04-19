<#-- @ftlvariable name="" type="nz.co.trineo.salesforce.views.ContentView" -->
<#if result??>
	${result.name!""} - ${result.numLocationsNotCovered!""}/${result.numLocations!""}
</#if>
<table class="table table-condensed">
	<#list lines as line>
		<#assign count=line?counter />
		<#if result??>
			<#assign notCovered=coverage?seq_contains(count) />
		<#else />
			<#assign notCovered=false />
		</#if>
		<tr class="${notCovered?string('notcovered', 'covered')}">
			<td class="num">${count?string["####"]}</td>
			<td class="code">${line}</td>
		</tr>
	</#list>
</table>