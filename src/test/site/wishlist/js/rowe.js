function nav(id) {
  $('#content').load('content/' + id + '.html');
}

$(document).ready(function()
{
	
    $('#content').load('content/home.html');
    $('#home').addClass("top"); 
    
    $(".testing").click(function(){
       $('#content').load('content/' + this.id + '.html');
    });

	  //TOP MENU
    $('a.remotetop').remote('#content', function() {
     $('a').removeClass("left top");
     $(this).addClass("top");
     if (window.console && window.console.info) {
         console.info('content loaded');
     }
    });

     //LEFT MENU
    $('a.remoteleft').remote('#content', function() {
			 setTimeout('window.scrollTo(0, 0)',1); //scroll to top
			 $('a').removeClass("left top");
			 $(this).addClass("left");
       if (window.console && window.console.info) {
         console.info('content loaded');
       }
     });
     
     $.ajaxHistory.initialize();

//END DOCUMENT FUNCTION   
});      