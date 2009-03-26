
/* ************************************************************************

#asset(dsp_management/*)

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


function PropertyUI()
{
	  PropertyUI.components = null;
	  PropertyUI.grid = null;
	  PropertyUI.offset = 0;
	  
	  PropertyUI.widgets = new Array();
	  
	  PropertyUI.processInstruction = function(inst)
	  {
		  if (inst.action == "CREATE")
			  PropertyUI.doCreate(inst);
		  if (inst.action == "CLEAR")
			  PropertyUI.doClear(inst);
		  if (inst.action == "SET_COMPONENTS")
			  PropertyUI.doSetComponents(inst);
	  }
	  
	  PropertyUI.doCreate = function(inst)
	  {
		  var r = inst.row + PropertyUI.offset;
		  var c = inst.column;
		  
		  var widget = null;
		  if (inst.type == "BUTTON") {
			  widget = new qx.ui.form.Button(inst.text);
			  widget.addListener("execute", function(e) {
				  PropertyUI.buttonPressed(e);
			  });
		  }
		  if (inst.type == "LABEL")
			  widget = new qx.ui.basic.Label(inst.text);
		  if (inst.type == "INPUT"){
			  widget = new qx.ui.form.TextField(inst.text);
		  }
		  var layout = PropertyUI.grid.getLayout();
		  var old_widget = null;
		  try {
			  old_widget = layout.getCellWidget(r, c);
		  } catch(ex) {
			  // Ignore. Qooxdoo throws exception if (r, c) does not exist
		  }
		  if (old_widget != null)
			  PropertyUI.grid.remove(old_widget);
		  PropertyUI.grid.add(widget, {row: r, column: c});
		  PropertyUI.widgets.push(widget);
	  }

	  PropertyUI.doClear = function(inst)
	  {
		  for (var w in PropertyUI.widgets)
			  PropertyUI.grid.remove(PropertyUI.widgets[w]);
		  PropertyUI.widgets = new Array();
	  }

	  PropertyUI.doSetComponents = function(inst)
	  {
		  PropertyUI.components.removeAll();
		  var comps = inst.components.split(":");
	      for (var i = 0; i < comps.length; i++) {
	          var tempItem = new qx.ui.form.ListItem(comps[i]);
	          PropertyUI.components.add(tempItem);
	      }
	  }

	  PropertyUI.buttonPressed = function(e)
	  {
		  var input = e.getTarget().getLabel();
		  for (var w in PropertyUI.widgets) {
			  var widget = PropertyUI.widgets[w];
			  if (widget instanceof qx.ui.form.TextField) {
				  input += ":" + widget.getValue();
			  }
		  }
		  PropertyUI.sendButtonEvent(input);
	  }

	  PropertyUI.queryButtonPressed = function(e)
	  {
		  var input = e.getTarget().getLabel();
		  input += ":" + PropertyUI.components.getValue();
		  PropertyUI.sendButtonEvent(input);
	  }

	  PropertyUI.sendButtonEvent = function(data)
	  {
	      var req = new qx.io.remote.Request("/property-ui/PUSH=" + data, "GET", "text/plain");
		  req.send();
	  }
	  
	  PropertyUI.callback = function(e)
	  {
	    var inst = eval("(" + e.getContent() + ")");
	    PropertyUI.processInstruction(inst);
	    PropertyUI.loop();
	  }
	  
	  PropertyUI.loop = function()
	  {
	    var req = new qx.io.remote.Request("/property-ui/PULL", "GET", "text/plain");
	    req.setTimeout(600000);
	    req.addListener("completed", PropertyUI.callback);
	    req.send();
	  }
}
PropertyUI();



qx.Class.define("dsp_management.Application",
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

      var label = new qx.ui.basic.Label("DSP Manager");
      var font = new qx.bom.Font(18, ["Arial"]);
      font.setBold(true);
      label.setFont(font);
      doc.add(label, {top: 20, left: 50});

      var rootPane = new qx.ui.splitpane.Pane("horizontal");
      doc.add(rootPane, {left:50, top:50, right: 50, bottom: 50});
      
      // Property Pane
      var layout = new qx.ui.layout.Grid(10, 10);
      var propPane = new qx.ui.container.Composite(layout);
      propPane.set({allowGrowX: true, allowGrowY: true, padding: 15});
      PropertyUI.grid = propPane;
      PropertyUI.offset = 2;
      label = new qx.ui.basic.Label("Properties");
      font = new qx.bom.Font(16, ["Arial"]);
      //font.setBold(true);
      label.setFont(font);
      propPane.add(label, {row: 0, column: 0, colSpan: 2});
      label = new qx.ui.basic.Label("Component:");
      propPane.add(label, {row:1, column: 0});
      var comboBox = new qx.ui.form.ComboBox();
      PropertyUI.components = comboBox;
      propPane.add(comboBox, {row:1, column: 1});
      var b = new qx.ui.form.Button("Query");
	  b.addListener("execute", function(e) {
		  PropertyUI.queryButtonPressed(e);
	  });
      propPane.add(b, {row: 1, column: 2});
      
      // Logger Pane
      layout = new qx.ui.layout.Grid(10, 10);
      layout.setColumnFlex(0, 1);
      layout.setRowFlex(1, 1);
      var loggerPane = new qx.ui.container.Composite(layout);
      loggerPane.set({allowGrowX: true, allowGrowY: true, padding: 15});
      label = new qx.ui.basic.Label("Data Logger");
      font = new qx.bom.Font(16, ["Arial"]);
      //font.setBold(true);
      label.setFont(font);
      loggerPane.add(label, {row: 0, column: 0});
      
      var list = new qx.ui.form.List();
      list.set({allowGrowX: true, allowGrowY: true});

      loggerPane.add(list, {row: 1, column: 0});
      
      var clear_button = new qx.ui.form.Button("Clear List");
      clear_button.set({allowGrowX: false});
      loggerPane.add(clear_button, {row: 2, column:0 });
      clear_button.addListener("execute", function(e) {
	  list.removeAll();
      });
      
      rootPane.add(propPane, 0);
      rootPane.add(loggerPane, 1);
      DSPDataFetcher.list = list;
      DSPDataFetcher.loop();

      PropertyUI.loop();
    }
  }
});
