var timeoutVar;
var message = "Migration completed successfully.";
function logAdd(logMessage) {
	$("<div />").html(logMessage).appendTo("#log");
	logScroll();
}

function logScroll() {
	var height = $("#log").get(0).scrollHeight;
	$("#log").animate({
		scrollTop : height
	}, 500);
}

function executeLogQuery() {
	$.ajax({
		url : '/getProgressInformation',
		success : function(data) {
		    var response = data;
            if (response.indexOf(message) >=0) {
                console.log("clearing timeout");
                clearTimeout(timeoutVar);
            }
			logAdd(data);
		}
	});
    timeoutVar = setTimeout(executeLogQuery, 2000);
}

function executeProgressBarQuery() {
    var progress = $(".loading-progress").progressTimer({
        timeLimit: 100,
        onFinish: function () {
            $("<div />").html("Migration completed successfully.").appendTo("#migrationComplete");
        }
    });
    $.ajax({
        url:"/getProgressInformation"
    }).error(function(){
        progress.progressTimer('error', {
            errorText:'ERROR!',
            onFinish:function(){
                $("<div />").html("Error occurred while migrating the data.").appendTo("#migrationComplete");
            }
        });
    }).done(function(data){

        var response = data;
        if (response.indexOf(message) >=0) {
            progress.progressTimer('complete');
            console.log("clearing timeout");
            clearTimeout(timeoutVar);
        }
    });
    timeoutVar = setTimeout(executeProgressBarQuery, 2000);
}

function displayTable (data){
    let tableHeadData = ['Project Id', 'Status', 'Status Steps'];

    let table = '<table><thead><tr>'
    let headers = ''
    tableHeadData.map(head =>{
        headers += '<th>' + head + '</th>'
    });

    headers = table + headers + '</tr></thead>';

    $("#table-data").html(table);
    let tableRows = '<tbody>';
    if(data && Object.keys(data).length > 0){
        (Object.keys(data)|| []).map(projectkey =>{
            if(data[projectkey]){
                let tableRow = '<tr><td>'+ projectkey + '</td>';
                tableRow += '<td>' + data[projectkey].status + '</td>';
                let statusSteps = '';
                data[projectkey].statusSteps.forEach((statusStep,index) => {
                    if(index  !== data[projectkey].statusSteps.length - 1){
                        statusSteps +=  statusStep + ',' + '<br>';
                    }else{
                        statusSteps +=  statusStep;
                    }
                });
                tableRow +=  '<td>' + statusSteps + '</td></tr>';
                tableRows += tableRow;
            }
        })
        table = headers + tableRows + '</tbody>';
        $("#table-data").html(table);
    }
}

function getMigrationStatus() {
	$.ajax({
		url : '/migration/status',
		success : function(data) {
            displayTable(data);
		}
	});
}
