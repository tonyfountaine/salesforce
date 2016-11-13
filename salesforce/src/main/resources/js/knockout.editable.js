(function () {
	function toEditable(observable, getRollbackValue) {
	    var rollbackValues = [];
	
	    getRollbackValue = getRollbackValue || function (observable) { return observable(); };
	
	    //a flag to indicate if the field is being edited
	    observable.isEditing = ko.observable(false);
	
	    //start an edit
	    observable.beginEdit = function () {
	        if (observable.isEditing()) { return; }
	
	        rollbackValues.push(getRollbackValue(observable));
	
	        observable.isEditing(true);
	    };
	
	    //end (commit) an edit
	    observable.endEdit = function () {
	        if (!observable.isEditing()) { return; }
	
	        observable.isEditing(false);
	    };
	
	    //cancel and roll-back an edit
	    observable.cancelEdit = function () {
	        if (!observable.isEditing() || !rollbackValues.length) { return; }
	
	        observable(rollbackValues.pop());
	
	        observable.isEditing(false);
	    };
	
	    //roll-back to historical committed values
	    observable.rollback = function () {
	        if (rollbackValues.length) {
	            observable(rollbackValues.pop());
	        }
	    };
	
	    return observable;
	}
	
	ko.editable = function (initial) {
	    return toEditable(ko.observable(initial));
	};
})();