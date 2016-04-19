<#-- @ftlvariable name="" type="nz.co.trineo.salesforce.views.CoverageView" -->
<#list coverage as c>
	<#assign num = "${c.numLocations - c.numLocationsNotCovered}" />
	<tr>
		<td>${c.namespace!""}</td>
		<td>${c.name}</td>
		<td>${c.percent?string["##0.00"]}</td>
		<td>${num} / ${c.numLocations}</td>
	</tr>
</#list>