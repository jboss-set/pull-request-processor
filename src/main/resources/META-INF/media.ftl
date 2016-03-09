<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <title>Set Feed</title>

    <!-- Bootstrap -->
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet">
	<
    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
	<style>
		.streams {
             padding: 5px;
             width: 280px;
        }
	</style>
  </head>
  <body>
  
    <div class="container">
		<div class="row">
		  <div class="col-md-12"><h1>Sustaining engineering feed</h1></div>
		</div>
		<div class="row">
		  <div class="col-md-12">
		  			<div class="media-list">
		  				<#list rows as row>
		  			      <div class="media">
							  <#assign data = row.data>
							  <div class="media-body">
							    <h4 class="media-heading"><a href="${data.pullRequest.link}">#${data.pullRequest.label}</a> Dummy title</h4>
							    <p>Component <a href="${data.repository.link}">${data.repository.label}</a> in branch ${data.branch} related to streams <#list data.streams as stream>	${stream} </#list></p>
							    <p>
							    	Patches related: <#list data.pullRequestsRelated as patch><a href="${patch.link}">#${patch.label}</a> </#list>
							    </p>
							    <p>
							    	<#list data.issuesRelated as issue>
			  							<a href="${issue.link}">#${issue.label} ${issue.stream} </a> 
			  							<#assign streams = data.labels>
                                        <span class="streams">
											<#list streams[issue.label] as label>
			  									<span class="label <#if label.isOk()>label-success<#else>label-danger</#if> ">${label.name}</span>
                                        	</#list>
                                        </span>
									</#list>
							    </p>
							  </div>
					  	   </div>
					  	</#list>
					</div>
		  
		    </div>
		</div>
	</div>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>

  </body>
</html>
