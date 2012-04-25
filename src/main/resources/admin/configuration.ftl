<html>
<head>
<title>LiveRebel Command Center configuration</title>
<meta name="decorator" content="atl.admin">
</head>
<body>

[@ww.form action='saveLiveRebel' submitLabelKey='Save'
titleKey='LiveRebel Command Center configuration'
descriptionKey='Configure an LiveRebel Command Center that will be available to project configurations for deployment of artifacts.']

[@ww.textfield labelKey='LiveRebel URL' name="URL" required="true" 
descriptionKey="Specify the root URL of your LiveRebel Command Center installation."/]

[@ww.textfield labelKey='Authentication token' name="token" required="true" 
descriptionKey="Authentication token for the user with sufficient permissions."/]

[@ww.param name='buttons']
[@ww.submit value="Test connection" name="method" theme='simple' /]
[/@ww.param]

[/@ww.form]

</body>
</html>

