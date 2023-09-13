function clearFilter(){
	window.location = moduleURL;
}

function showDeletionConfirmModal(link, entityName){
	entityId = link.attr("entityId");
	
	$("#yesButton").attr("href",link.attr("href"));
	$("#confirmText").text("Are you sure about to delete this "+entityName+" ID "+entityId+ "?");
	$("#confirmModal").modal("show");
}