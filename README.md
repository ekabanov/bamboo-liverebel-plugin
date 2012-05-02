LiveRebel Deploy Plugin for Bamboo
==================================
LiveRebel Deploy Plugin helps to run updates to your JEE containers faster. LiveRebel is a tool for hot updates without downtime, lost sessions and OutOfMemoryErrors. You have to have a running LiveRebel installed to use this plugin.
More information on the official [website](http://liverebel.com)

Installation for development
----------------------------
1. Clone the repository
2. Install [Atlassian SDK](https://developer.atlassian.com/display/DOCS/Installing+the+Atlassian+Plugin+SDK)
3. Run `atlas-run` in plugin directory
4. Setup LiveRebel
    * Open `http://<bamboo>/`
    * Go to `Administration > LiveRebel Configuration`
    * Enter `URL` and `authentication token` for LiveRebel Command Center
5. Make changes to code
6. Refresh `http://<bamboo>/plugins/servlet/fastdev` to reload your changes