
/* ************************************************************************

#asset(dsp_web_logger/*)

************************************************************************ */

function DSPDataFetcher()
{
  DSPDataFetcher.list = null;
  

  DSPDataFetcher.callback = function(e)
  {
    var items = e.getContent().split("$");
    for (var i = 0; i < items.length; i++) {
      var item = new qx.ui.form.ListItem(items[i]);
      DSPDataFetcher.list.add(item);
      DSPDataFetcher.list.scrollChildIntoView(item);
    }
    DSPDataFetcher.loop();
  }
  
  DSPDataFetcher.loop = function()
  {
    var req = new qx.io.remote.Request("/get-data", "GET", "text/plain");
    req.setTimeout(600000);
    req.addListener("completed", DSPDataFetcher.callback);
    //req.addListener("receiving", function(e) { alert("receiving");});
    //req.addListener("failed", function(e) { alert("failed");});
    //req.addListener("timeout", function(e) { alert("timeout");});
    //req.addListener("aborted", function(e) { alert("aborted");});
    req.send();
  }
}
DSPDataFetcher();



/**
 * This is the main application class of your custom application "DSP Web Logger"
 */
qx.Class.define("dsp_web_logger.Application",
{
  extend : qx.application.Standalone,



  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    /**
     * This method contains the initial application code and gets called 
     * during startup of the application
     */
    main : function()
    {
      // Call super class
      this.base(arguments);

      // Enable logging in debug variant
      if (qx.core.Variant.isSet("qx.debug", "on"))
      {
        // support native logging capabilities, e.g. Firebug for Firefox
        qx.log.appender.Native;
        // support additional cross-browser console. Press F7 to toggle visibility
        qx.log.appender.Console;
      }

      var doc = this.getRoot();

      var label = new qx.ui.basic.Label("DSP Web Logger");
      var font = new qx.bom.Font(18, ["Arial"]);
      font.setBold(true);
      label.setFont(font);
      doc.add(label, {top: 30, left: 50});
      
      var list = new qx.ui.form.List();
      var item;

      doc.add(list, {left:50, top:60, right: 50, bottom: 100 });
      
      var clear_button = new qx.ui.form.Button("Clear List");
      doc.add(clear_button, {left: 50, bottom: 50});
      clear_button.addListener("execute", function(e) {
	  list.removeAll();
      });
      DSPDataFetcher.list = list;
      DSPDataFetcher.loop();
    }
  }
});
