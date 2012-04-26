[@ww.textfield labelKey="Artifact" name="warFilePath" required="true" cssClass="long-field"/]

[@ui.bambooSection titleKey="Fallback update strategy"]
[@ww.radio name="updateMode" list=modes listKey="value" listValue="title" value="updateMode"  theme="simple" /]
[/@ui.bambooSection]
<br />
[@ww.checkbox labelKey="Use fallback strategy if Hotpatching (online, instant) is <b>compatible with warnings</b>" name="fallback" /]

[@ui.bambooSection titleKey="Servers"]
[@ww.checkboxlist name="server" list=servers value="checked" listKey="id" listValue="title"  theme="simple" /]
[/@ui.bambooSection]

