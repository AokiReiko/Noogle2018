function autocomplete(queryString) {
	queryString = jQuery.trim(queryString);
	if (queryString.length == 0) {
		$('#autocomplete').hide(); 
	} else {
		$.ajax({
			url: "servlet/AutoComplete",
			type: "Get",
			dataType: 'jsonp',
            async: true,
            crossDomain: true,
            jsonpCallback: "autocomp",
			success: function(result) {
				console.log(result);
				var str = '';
				str += '<div class="panel-body" style="padding-bottom:2px;padding-top:2px;padding-left:1px;padding-right: 1px">';
                str += '<div class="list-group" style="line-height: 1px; margin-bottom: 1px">';
                str += 'eeee</div></div>';
                $('#autocomplete').html(str);
			}
		});
		$('#autocomplete').show(); 
	}
	//console.log(queryString);
}
function clickUrl(url) {
	console.log(url)
	$.ajax({
		url: "servlet/clickUrl",
		type: "Get",
		dataType: 'text',
        async: true,
        crossDomain: true,
        jsonpCallback: "clickurl",
		success: function(result) {
			console.log(url+" click get");
		}
	});
}
    	