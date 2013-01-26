/**
 * Global datastructure containing key settings.
 */
var tocview = {
  // name of the class for the most recent url shown
  CURRENT_URL_CLASS: "currentURL",

  // enable or disable (expensive) structural invariant checking.
  INVARIANT_CHECKING: true,

  // url of the initial page to be shown in the content frame.
  CONTENT_START_URL: "content/A1.html",

  // ID for top list.
  TOP_UL_ID: "theTopUL"
}

/**
 * name of the most recent URL held at paginaVeld.
 */
var contentFrameURL = tocview.CONTENT_START_URL;

function cb(responseText, textStatus, XMLHttpRequest) {
 if (textStatus == "error") {
   $('#content').html(textStatus + " " + responseText);
 }
}

function nav(id) {
  $('#content').load('content/' + id + '.html');
}

$(document).ready(function()
{
	$("#theTopUL").treeview({
      collapsed: true,
      unique: true,
      prerendered: true
    });

    //$('#content').load(contentFrameURL, cb);

 $.listen('click','a.update',function(e){
           $('#content').load(this.href, cb);
           checkURL(this.href);
           e.stopPropagation();
           e.preventDefault();
	});

     //LEFT MENU
    $('a.remoteleft').remote('#content', function() {
      setTimeout('window.scrollTo(0, 0)',1); //IE scroll to top
      checkURL(this.holdhref);
     });
    
    $('#content').load('content/home.html');
    $('#home').addClass("top"); 
    
    $(".testing").click(function(){
       $('#content').load('content/' + this.id + '.html', cb);
    });

	  //TOP MENU
    $('a.remotetop').remote('#content', function() {
     $('a').removeClass("left top");
     $(this).addClass("top");
     if (window.console && window.console.info) {
         console.info('content loaded');
     }
    });

     $.ajaxHistory.initialize();
});


/**
 * Check continuously whether the paginaVeld content has changed.
 */
function checkURL(newUrl) {
  manageURLs(contentFrameURL, newUrl);
  contentFrameURL = newUrl;
  //invariant();
}

/**
 * The content frame has changed: update the menu accordingly.
 */
function manageURLs(oldUrl, newUrl) {
  // create the ID's that can be used to obtain the menu list items
  // corresponding to the url's.
  var oldId = getIdFromURL(oldUrl);
  var newId = getIdFromURL(newUrl);

  // start out by hiding the old list item.
  var oldli = document.getElementById(oldId);
  if (oldli) {
    $(oldli).removeClass(tocview.CURRENT_URL_CLASS);
    // jquery rocks.
    $(oldli).find(">.collapsable").each(function(e) { collapseCollapsable(e); });
    $(oldli).find(">ul").hide();
    collapsePathTo(oldli);
  } else {
    alert("Cannot find old li with id " + oldId + " from url " + oldUrl);
  }

  // then open up the entry for the new element.
  var newli = document.getElementById(getIdFromURL(newUrl));

  if (newli) {
    $(newli).addClass(tocview.CURRENT_URL_CLASS);
    if (newli.parentNode) {
      expandPathTo(newli.parentNode);
    }
  } else {
    alert("Cannot menu-element for url " + newUrl + ", id = " + newId );
  }
}

/**
 * Collapse a path to a given list element.
 */
function collapsePathTo(element) {
  if (element.id == tocview.TOP_UL_ID) {
    // we're done.
    return;
  }
  if ($(element).is(".collapsable")) {
    collapseCollapsable(element);
  }
  if (element.tagName == "UL") {
    $(element).hide();
  }
  // and move up!
  collapsePathTo(element.parentNode);
}


/**
 * Expand a path to a given list element.
 */
function expandPathTo(element) {
  if (element.id == tocview.TOP_UL_ID) {
    // we're done.
    return;
  }
  if ($(element).is(".collapsable")) {
    // we found a path to an element is open already.
    // perhaps from here we should close the other branch (if any) that is open?
    return;
  }
  if ($(element).is(".expandable")) {
    expandExpandable(element);
  }
  if (element.tagName == "UL") {
    $(element).show();
  }
  // and move up!
  expandPathTo(element.parentNode);
}

/**
 * Collapse an element that was expanded.
 */
function collapseCollapsable(collapsable) {
  swapClass(collapsable, "collapsable", "expandable");
  var div = collapsable.childNodes.item(1);
  swapClass(div, "collapsable-hitarea", "expandable-hitarea");
  if ($(collapsable).is(".lastCollapsable")) {
    swapClass(collapsable, "lastCollapsable", "lastExpandable");
    swapClass(div, "lastCollapsable-hitarea", "lastExpandable-hitarea");
  }
}

/**
 * Expand element that was collapsed.
 */
function expandExpandable(expandable) {
  swapClass(expandable, "expandable", "collapsable");
  var div = expandable.childNodes.item(1);
  swapClass(div, "expandable-hitarea", "collapsable-hitarea");
  if ($(expandable).is(".lastExpandable")) {
    swapClass(expandable, "lastExpandable", "lastCollapsable");
    swapClass(div, "lastExpandable-hitarea", "lastCollapsable-hitarea");
  }
}

/**
 * Replace oldClass by newClass
 */
function swapClass(element, oldClass, newClass) {
  $(element).removeClass(oldClass);
  $(element).addClass(newClass);
}

/**
 * Turn a URL into an ID.
 * Note: depends on way this is done in menu2ul.pl as well!
 */
function getIdFromURL(url) {
  // strip off the prefix, to allow easy finding of the element.
  var result = url;
  if (url) {
    var splitter = "/content/";
    var pos = url.indexOf(splitter);
    if (pos < 0) {
      // we might be in IE...
      splitter = "\\content\\";
      pos = url.indexOf(splitter);
    }
    result = url.substring(pos + splitter.length);
  }
  // let's be a nice w3c id.
  result = result.replace(/\//g, "X");
  result = result.replace(/\\/g, "X");
  result = result.replace(/\%/g, "P");
  result = result.replace(/\,/g, "C");
  result = result.replace(/\./g, "D");
  result = "ID" + result;
	  //alert("idfromurl = " + result);
  return result;
}

/**
  * Return true iff the DOM adheres to certain sanity rules.
  */
function invariant() {
    if (!tocview.INVARIANT_CHECKING) {
        return;
    }
    var ul = document.getElementById(tocview.TOP_UL_ID);
    // alert("Let's check this tree, ul = " + ul + " doc = " + menuDoc);
    checkUL(ul);
}

function checkUL(ul) {
  // double check things are working:
  $(ul).find(">li.expandable").each(function(index, li) {
    // alert("Found exp: " + li.id + " : " + objectDetails(li));
  });

  // warn about collapsable divs within expandable items
  $(ul)
  .find("li.expandable")
  .find(">div.collapsable-hitarea")
  .each(function(index, div) {
    //alert("Found collapsable hitarea within expandable! " + div);
    var curText = $('#content').html();
    //$('#content').html("<BLINK>Found collapsable hitarea within expandable! " + div + " <br/> " + curText + "</BLINK>");
  });

  // warn about collapsable items within expandable items
  $(ul)
  .find("li.expandable")
  .find(">ul")
  .find(">li.collapsable")
  .each(function(index, li) {
    //alert("Found collapsable list item within expandable! " + li + " id = " + li.id);
    var curText = $('#content').html();
    //$('#content').html("<BLINK>Found collapsable list item within expandable! " + li + " id = " + li.id + " <br/> " + curText + "</BLINK>");
  });

  // todo: warn about visible within hidden content?
  // todo: warn about hidden current url.

  // additional checks, not currently violated:
  // todo: last li in ul should be of class "last"
  // todo: last li of expandable should be of class "lastExpandable"

  // recurse into sub-ul's.
  /*$(ul)
  .find(">li")
  .find(">ul")
  .each(function(index, ul) {
    checkUL(ul);
  });*/
}



// helper for debugging.
function objectDetails(object) {
  var result = "";
  for (var i in object) {
    result = result + i + " ";
  }
  return object + " type = " + typeof(object) + " fields= " + result;
}
