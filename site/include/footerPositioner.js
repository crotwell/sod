function positionFooter(){
    var menu = document.getElementById('menu');
    var content = document.getElementById('content');
    if(menu.offsetHeight > content.offsetHeight){ 
        var footer = document.getElementById('footer'); 
        footer.style.position = 'absolute'; 
    } 
} 