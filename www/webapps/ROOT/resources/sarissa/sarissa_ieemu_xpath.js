/**
 * ====================================================================
 * About
 * ====================================================================
 * Sarissa cross browser XML library - IE XPath Emulation 
 * @version 0.9.6
 * @author: Manos Batsis, mailto: mbatsis at users full stop sourceforge full stop net
 *
 * This script emulates Internet Explorer's selectNodes and selectSingleNode
 * for Mozilla. Associating namespace prefixes with URIs for your XPath queries
 * is easy with IE's setProperty. 
 * USers may also map a namespace prefix to a default (unprefixed) namespace in the
 * source document with Sarissa.setXpathNamespaces
 *
 *
 * ====================================================================
 * Licence
 * ====================================================================
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 or
 * the GNU Lesser General Public License version 2.1 as published by
 * the Free Software Foundation (your choice of the two).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License or GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * or GNU Lesser General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * or visit http://www.gnu.org
 *
 */
if(_SARISSA_HAS_DOM_FEATURE && document.implementation.hasFeature("XPath", "3.0")){
    /**
    * <p>SarissaNodeList behaves as a NodeList but is only used as a result to <code>selectNodes</code>,
    * so it also has some properties IEs proprietery object features.</p>
    * @private
    * @constructor
    * @argument i the (initial) list size
    */
    function SarissaNodeList(i){
        this.length = i;
    };
    /** <p>Set an Array as the prototype object</p> */
    SarissaNodeList.prototype = new Array(0);
    /** <p>Inherit the Array constructor </p> */
    SarissaNodeList.prototype.constructor = Array;
    /**
    * <p>Returns the node at the specified index or null if the given index
    * is greater than the list size or less than zero </p>
    * <p><b>Note</b> that in ECMAScript you can also use the square-bracket
    * array notation instead of calling <code>item</code>
    * @argument i the index of the member to return
    * @returns the member corresponding to the given index
    */
    SarissaNodeList.prototype.item = function(i) {
        return (i < 0 || i >= this.length)?null:this[i];
    };
    /**
    * <p>Emulate IE's expr property
    * (Here the SarissaNodeList object is given as the result of selectNodes).</p>
    * @returns the XPath expression passed to selectNodes that resulted in
    *          this SarissaNodeList
    */
    SarissaNodeList.prototype.expr = "";
    /** dummy, used to accept IE's stuff without throwing errors */
    XMLDocument.prototype.setProperty  = function(x,y){};
    /**
    * <p>Programmatically control namespace URI/prefix mappings for XPath
    * queries.</p>
    * <p>This method comes especially handy when used to apply XPath queries
    * on XML documents with a default namespace, as there is no other way
    * of mapping that to a prefix.</p>
    * <p>Using no namespace prefix in DOM Level 3 XPath queries, implies you
    * are looking for elements in the null namespace. If you need to look
    * for nodes in the default namespace, you need to map a prefix to it
    * first like:</p>
    * <pre>Sarissa.setXpathNamespaces(oDoc, &quot;xmlns:myprefix=&amp;aposhttp://mynsURI&amp;apos&quot;);</pre>
    * <p><b>Note 1 </b>: Use this method only if the source document features
    * a default namespace (without a prefix), otherwise just use IE's setProperty
    * (moz will rezolve non-default namespaces by itself). You will need to map that
    * namespace to a prefix for queries to work.</p>
    * <p><b>Note 2 </b>: This method calls IE's setProperty method to set the
    * appropriate namespace-prefix mappings, so you dont have to do that.</p>
    * @param oDoc The target XMLDocument to set the namespace mappings for.
    * @param sNsSet A whilespace-seperated list of namespace declarations as
    *            those would appear in an XML document. E.g.:
    *            <code>&quot;xmlns:xhtml=&apos;http://www.w3.org/1999/xhtml&apos;
    * xmlns:&apos;http://www.w3.org/1999/XSL/Transform&apos;&quot;</code>
    * @throws An error if the format of the given namespace declarations is bad.
    */
    Sarissa.setXpathNamespaces = function(oDoc, sNsSet) {
        //oDoc._sarissa_setXpathNamespaces(sNsSet);
        oDoc._sarissa_useCustomResolver = true;
        var namespaces = sNsSet.indexOf(" ")>-1?sNsSet.split(" "):new Array(sNsSet);
        oDoc._sarissa_xpathNamespaces = new Array(namespaces.length);
        for(var i=0;i < namespaces.length;i++){
            var ns = namespaces[i];
            var colonPos = ns.indexOf(":");
            var assignPos = ns.indexOf("=");
            if(colonPos == 5 && assignPos > colonPos+2){
                var prefix = ns.substring(colonPos+1, assignPos);
                var uri = ns.substring(assignPos+2, ns.length-1);
                oDoc._sarissa_xpathNamespaces[prefix] = uri;
            }else{
                throw "Bad format on namespace declaration(s) given";
            };
        };
    };
    /**
    * @private Flag to control whether a custom namespace resolver should
    *          be used, set to true by Sarissa.setXpathNamespaces
    */
    XMLDocument.prototype._sarissa_useCustomResolver = false;
    /** @private */
    XMLDocument.prototype._sarissa_xpathNamespaces = new Array();
    /**
    * <p>Extends the XMLDocument to emulate IE's selectNodes.</p>
    * @argument sExpr the XPath expression to use
    * @argument contextNode this is for internal use only by the same
    *           method when called on Elements
    * @returns the result of the XPath search as a SarissaNodeList
    * @throws An error if no namespace URI is found for the given prefix.
    */
    XMLDocument.prototype.selectNodes = function(sExpr, contextNode){
        var nsDoc = this;
        var nsresolver = this._sarissa_useCustomResolver
        ? function(prefix){
            var s = nsDoc._sarissa_xpathNamespaces[prefix];
            if(s)return s;
            else throw "No namespace URI found for prefix: '" + prefix+"'";
            }
        : this.createNSResolver(this.documentElement);
            var oResult = this.evaluate(sExpr,
                    (contextNode?contextNode:this),
                    nsresolver,
                    XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
        var nodeList = new SarissaNodeList(oResult.snapshotLength);
        nodeList.expr = sExpr;
        for(var i=0;i<nodeList.length;i++)
            nodeList[i] = oResult.snapshotItem(i);
        return nodeList;
    };
    /**
    * <p>Extends the Element to emulate IE's selectNodes</p>
    * @argument sExpr the XPath expression to use
    * @returns the result of the XPath search as an (Sarissa)NodeList
    * @throws An
    *             error if invoked on an HTML Element as this is only be
    *             available to XML Elements.
    */
    Element.prototype.selectNodes = function(sExpr){
        var doc = this.ownerDocument;
        if(doc.selectNodes)
            return doc.selectNodes(sExpr, this);
        else
            throw "Method selectNodes is only supported by XML Elements";
    };
    /**
    * <p>Extends the XMLDocument to emulate IE's selectSingleNodes.</p>
    * @argument sExpr the XPath expression to use
    * @argument contextNode this is for internal use only by the same
    *           method when called on Elements
    * @returns the result of the XPath search as an (Sarissa)NodeList
    */
    XMLDocument.prototype.selectSingleNode = function(sExpr, contextNode){
        var ctx = contextNode?contextNode:null;
        sExpr = "("+sExpr+")[1]";
        var nodeList = this.selectNodes(sExpr, ctx);
        if(nodeList.length > 0)
            return nodeList.item(0);
        else
            return null;
    };
    /**
    * <p>Extends the Element to emulate IE's selectNodes.</p>
    * @argument sExpr the XPath expression to use
    * @returns the result of the XPath search as an (Sarissa)NodeList
    * @throws An error if invoked on an HTML Element as this is only be
    *             available to XML Elements.
    */
    Element.prototype.selectSingleNode = function(sExpr){
        var doc = this.ownerDocument;
        if(doc.selectSingleNode)
            return doc.selectSingleNode(sExpr, this);
        else
            throw "Method selectNodes is only supported by XML Elements";
    };
    Sarissa.IS_ENABLED_SELECT_NODES = true;
};
