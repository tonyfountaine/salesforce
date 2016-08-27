		<nav class="navbar navbar-default navbar-fixed-top">
		  <div class="container-fluid">
		    <!-- Brand and toggle get grouped for better mobile display -->
		    <div class="navbar-header">
		      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
		        <span class="sr-only">Toggle navigation</span>
		        <span class="icon-bar"></span>
		        <span class="icon-bar"></span>
		        <span class="icon-bar"></span>
		      </button>
		      <a class="navbar-brand" href="#">Trineo</a>
		    </div>
		
		    <!-- Collect the nav links, forms, and other content for toggling -->
		    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
              <ul class="nav navbar-nav" data-bind="foreach: sections">
                <li><a href="/sf/orgs"><i class="fa fa-cloud" aria-hidden="true"></i> Salesforce</a></li>
                <li><a href="/github/repos"><i class="fa fa-github" aria-hidden="true"></i> GitHub</a></li>
                <li><a href="/trello"><i class="fa fa-trello" aria-hidden="true"></i> Trello</a></li>
              </ul>
		      <ul class="nav navbar-nav navbar-right">
		        <li><a href="#">Link</a></li>
		        <li class="dropdown">
		          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"><span class="fa fa-user" aria-hidden="true"></span> <span class="caret"></span></a>
		          <ul class="dropdown-menu">
		            <li><span><strong>User Name</strong></span></li>
		            <li class="disabled"><span><small>email@address.com</small></span></li>
		            <li role="separator" class="divider"></li>
		            <li><a href="#"><span class="fa fa-cog" aria-hidden="true"></span> Settings</a></li>
		            <li><a href="/accounts"><span class="fa fa-flash" aria-hidden="true"></span> Connected Accounts</a></li>
		            <li><a href="#"><span class="fa fa-power-off" aria-hidden="true"></span> Log Out</a></li>
		          </ul>
		        </li>
		      </ul>
		    </div><!-- /.navbar-collapse -->
		  </div><!-- /.container-fluid -->
		</nav>
