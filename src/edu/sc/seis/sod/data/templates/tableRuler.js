/*
    tableruler()
    written by Chris Heilmann for alistapart.
    enables a rollover of rows for each table with the classname "ruler"
*/

function tableruler() {
    if (document.getElementById && document.createTextNode) {
        var tables=document.getElementsByTagName('table');
        for (var i=0;i<tables.length;i++) {
            if(tables[i].className=='ruler') {
                var trs=tables[i].getElementsByTagName('tr');
                for(var j=0;j<trs.length;j++){
                   if(trs[j].className!='title'){
                     trs[j].onmouseover=function(){this.className='ruled';return false}
                     trs[j].onmouseout=function(){this.className='';return false}
                   }
                }
            }
        }
    }
}
