<#-- @ftlvariable name="" type="nz.co.trineo.salesforce.views.CompareView" -->
<#assign keys = diffMap?keys />
<ul class="nav nav-pills" id="CompareTabs">
	<li class="active"><a href="#all" aria-controls="all" data-toggle="pill">All</a></li>
	<#list keys as key>
		<li><a href="#${key}" aria-controls="${key}" data-toggle="pill">${key?cap_first}</a></li>
	</#list>
</ul>
<div class="tab-content">
	<div class="tab-pane active" id="all">
		<#list compare as diff>
		<div class="panel panel-default">
			<div class="panel-heading">
				<#assign pathA=diff.pathA!"" />
				<#if pathA == "" || pathA == "/dev/null">
					<h3 class="panel-title">${diff.pathB}</h3>
				<#else>
					<h3 class="panel-title">${diff.pathA}</h3>
				</#if>
			</div>
			<div class="diffContent">
				<table class="table table-condensed diff">
					<tbody>
						<tr class="header">
							<td class="num">...</td>
							<td class="num">...</td>
							<td class="line">@@ ${diff.headerA} ${diff.headerB} @@</td>
						</tr>
						<#list diff.lines as line>
							<#if line.removed>
								<tr class="removed">
									<td class="num">${(line.lineNumA + 1)?string["####"]}</td>
									<td class="num">&nbsp;&nbsp;&nbsp;</td>
									<td class="line">${line.line}</td>
								</tr>
							<#elseif line.added>
								<tr class="added">
									<td class="num">&nbsp;&nbsp;&nbsp;</td>
									<td class="num">${(line.lineNumB + 1)?string["####"]}</td>
									<td class="line">${line.line}</td>
								</tr>
							<#else>
								<tr>
									<td class="num">${(line.lineNumA + 1)?string["####"]}</td>
									<td class="num">${(line.lineNumB + 1)?string["####"]}</td>
									<td class="line">${line.line}</td>
								</tr>
							</#if>
						</#list>
					</tbody>
				</table>
			</div>
		</div>
		</#list>
	</div>
	<#list keys as key>
		<div class="tab-pane active" id="${key}">
			<#list diffMap[key] as diff>
			<div class="panel panel-default">
				<div class="panel-heading">
					<#assign pathA=diff.pathA!"" />
					<#if pathA == "" || pathA == "/dev/null">
						<h3 class="panel-title">${diff.pathB}</h3>
					<#else>
						<h3 class="panel-title">${diff.pathA}</h3>
					</#if>
				</div>
				<div class="diffContent">
					<table class="table table-condensed diff">
						<tbody>
							<tr class="header">
								<td class="num">...</td>
								<td class="num">...</td>
								<td class="line">@@ ${diff.headerA} ${diff.headerB} @@</td>
							</tr>
							<#list diff.lines as line>
								<#if line.removed>
									<tr class="removed">
										<td class="num">${(line.lineNumA + 1)?string["####"]}</td>
										<td class="num">&nbsp;&nbsp;&nbsp;</td>
										<td class="line">${line.line}</td>
									</tr>
								<#elseif line.added>
									<tr class="added">
										<td class="num">&nbsp;&nbsp;&nbsp;</td>
										<td class="num">${(line.lineNumB + 1)?string["####"]}</td>
										<td class="line">${line.line}</td>
									</tr>
								<#else>
									<tr>
										<td class="num">${(line.lineNumA + 1)?string["####"]}</td>
										<td class="num">${(line.lineNumB + 1)?string["####"]}</td>
										<td class="line">${line.line}</td>
									</tr>
								</#if>
							</#list>
						</tbody>
					</table>
				</div>
			</div>
			</#list>
		</div>
	</#list>
</div>
