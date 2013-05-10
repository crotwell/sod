function positionFooter(){
    var leftColumn = document.getElementById('LeftColumn');
    var content = document.getElementById('Content');
    if(leftColumn.offsetHeight > content.offsetHeight){ 
        var footer = document.getElementById('footer'); 
        footer.style.position = 'absolute';
        footer.style.bottom ='10px'; 
    } 
} 