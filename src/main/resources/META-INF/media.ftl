<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Set Feed</title>
  </head>
  <body>
    <h1>Sustaining engineering feed</h1>
    <#list rows as row>
      <#assign data = row.data>
      <div>
        <h4><a href="${data.pullRequest.link}">#${data.pullRequest.label}</a></h4>
        <p>Component <a href="${data.repository.link}">${data.repository.label}</a> in branch ${data.branch} related to streams <#list data.streams as stream> ${stream}</#list></p>
        <p>Patches related: <#list data.pullRequestsRelated as patch><a href="${patch.link}">#${patch.label}</a> </#list></p>
        <p>
          <#list data.issuesRelated as issue>
            <a href="${issue.link}">#${issue.label} ${issue.stream}</a>
            <#assign streams = data.labels>
            <#list streams[issue.label] as label>
              <#if label.isOk()>[OK]<#else>[MISSING]</#if> ${label.name}
            </#list>
          </#list>
        </p>
      </div>
      <hr>
    </#list>
  </body>
</html>
