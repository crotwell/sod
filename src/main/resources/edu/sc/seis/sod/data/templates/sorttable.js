// from http://www.kryogenix.org/code/browser/sorttable/

var SORT_COLUMN_INDEX;
var VALUE_EXTRACTOR;
var img_dir = "";

function set_img_dir(new_img_dir){
    img_dir = new_img_dir;
    sortables_init();
}

function sortables_init() {
    // Find all tables with class sortable and make them sortable
    if (!document.getElementsByTagName) return;
    tbls = document.getElementsByTagName("table");
    for (ti=0;ti<tbls.length;ti++) {
        thisTbl = tbls[ti];
        if (((' '+thisTbl.className+' ').indexOf("sortable") != -1) && (thisTbl.id)) {
            ts_makeSortable(thisTbl);
        }
    }
}

function ts_makeSortable(table) {
    if (table.rows && table.rows.length > 0) {
        var firstRow = table.rows[0];
    }
    if (!firstRow) return;

    // We have a first row: assume it's the header, and make its contents clickable links
    var sortedColumn = firstRow.cells[0];
    for (var i=0;i<firstRow.cells.length;i++) {
        var cell = firstRow.cells[i];
        var name = cell.className;
        var order = "";//sortdir='up' ";
        if(name.length > 0 && name.indexOf('initiallySorted') != -1) {
            sortedColumn = cell;
            if(name.indexOf('descending')){ order = "sortdir='down' "; }
        }
        var txt = ts_getInnerText(cell);
        cell.innerHTML = '<a href="#" class="sortheader" onclick="ts_resortTable(this);return false;">'+txt+'<span class="sortarrow" ' + order + '><img src="'+img_dir+'none.gif"/></span></a>';
    }

    var sortedKids = sortedColumn.childNodes;
    for (var ci=0; ci < sortedKids.length; ci++){
        if (sortedKids[ci].tagName && sortedKids[ci].tagName.toLowerCase() == 'a'){
          ts_resortTable(sortedKids[ci]);
          break;
        }
    }
}

function ts_getInnerText(el) {
    if (typeof el == "string") return el;
    if (typeof el == "undefined") { return el };
    if (el.innerText) return el.innerText;  //Not needed but it is faster
    var str = "";

    var cs = el.childNodes;
    var l = cs.length;
    for (var i = 0; i < l; i++) {
        switch (cs[i].nodeType) {
            case 1: //ELEMENT_NODE
                str += ts_getInnerText(cs[i]);
                break;
            case 3: //TEXT_NODE
                str += cs[i].nodeValue;
                break;
        }
    }
    return str;
}

function ts_resortTable(lnk) {
    // get the span
    var span;
    for (var ci=0;ci<lnk.childNodes.length;ci++) {
        if (lnk.childNodes[ci].tagName && lnk.childNodes[ci].tagName.toLowerCase() == 'span') span = lnk.childNodes[ci];
    }
    var spantext = ts_getInnerText(span);
    var td = lnk.parentNode;
    var table = getParent(td,'TABLE');
    var tr = getParent(td, 'TR');
    if (table.rows.length <= 1) return;
    /* Safari 1.2 seems to be broken and doesn't understand cellIndex
    so we're traversing the cells to find the right one.  If you run across this
    in the future you should check http://www.quirksmode.org/dom/w3c_html.html
    to see if Safari has fixed its support for cellIndex to simplify this */
    var column;// = td.cellIndex;
    var kids = tr.cells;
    for (i=0; i<kids.length; i++){
        if(kids[i] == td){ column = i; }
    }   
    SORT_COLUMN_INDEX = column;    
    
    var itm = ts_getInnerText(table.rows[1].cells[column]);
    VALUE_EXTRACTOR = defaultExtract;
    if(itm.match(/^\d\d\d\d/)) VALUE_EXTRACTOR = defaultExtract;
    else if (itm.match(/^[\d\.]+/)) VALUE_EXTRACTOR = numericExtract;
    
    var newRows = new Array();
    for (j=1;j<table.rows.length;j++) { newRows[j-1] = table.rows[j]; }

    if (span.getAttribute("sortdir") == 'down') {
        ARROW = '<img src="'+img_dir+'down.gif"/>';
        newRows.sort(ts_sort_reverse);
        span.setAttribute('sortdir','up');
    } else {
        ARROW = '<img src="'+img_dir+'up.gif"/>';
        newRows.sort(ts_sort_default);
        span.setAttribute('sortdir','down');
    }
 for (i=0;i<newRows.length;i++) {table.tBodies[0].appendChild(newRows[i]);}
    // Delete any other arrows there may be showing
    var allspans = document.getElementsByTagName("span");
    for (var ci=0;ci<allspans.length;ci++) {
        if (allspans[ci].className == 'sortarrow') {
            if (getParent(allspans[ci],"table") == getParent(lnk,"table")) { // in the same table as us?
                allspans[ci].innerHTML = '<img src="'+img_dir+'none.gif"/>';
            }
        }
    }
    span.innerHTML = ARROW;
}

function getParent(el, pTagName) {
    if (el == null) return null;
    else if (el.nodeType == 1 && el.tagName.toLowerCase() == pTagName.toLowerCase())    // Gecko bug, supposed to be uppercase
        return el;
    else
        return getParent(el.parentNode, pTagName);
}

function ts_sort_default(a,b) {
    aa = VALUE_EXTRACTOR(ts_getInnerText(a.cells[SORT_COLUMN_INDEX]));
    bb = VALUE_EXTRACTOR(ts_getInnerText(b.cells[SORT_COLUMN_INDEX]));
    if (aa==bb) return 0;
    if (aa<bb) return -1;
    return 1;
}

function ts_sort_reverse(a, b){
    var defResult = ts_sort_default(a, b);
    if(defResult == 0){ return 0; }
    else if(defResult > 0){ return -1; }
    else{ return 1; }
}

function defaultExtract(thing){
    return thing;
}

function numericExtract(thing){
    return parseFloat(thing);
}
