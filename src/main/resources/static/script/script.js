/**
 * 
 */
function showAlert() {
    alert("The button was clicked!");
}

function showLogs() {
     $("#log").show();
      setTimeout(executeLogQuery, 2000);
}

function fetchStatus() {
    $("#fetchStatus").val("Refresh Status");
    getMigrationStatus();
}
