function TrineoViewModel() {
	var self = this;
	self.sections = ["Salesforce", "GitHub", "Git", "Trello"];
	self.chosenSection = ko.observable();
	self.services = ko.observableArray([]);
	self.chosenService = ko.observable();
	self.environments = ko.observableArray([]);
	self.chosenEnvironment = ko.observable();
	self.accounts = ko.observableArray([]);
	
	self.gotoSection = function(section) {
		location.hash = section;
	}
	self.getAccounts = function() {
		$.getJSON("/accounts", function(data) {
			self.accounts(data);
		});
	}

	self.verifyAccount = function(id) {
		alert('verify: ' + id);
	}
	self.renameAccount = function(id) {
		alert('rename: ' + id);
	}
	self.deleteAccount = function(id) {
		alert('delete: ' + id);
	}
	
	Sammy(function() {
		this.get("#:section", function() {
			var section = this.params.section;
			self.chosenSection(section);
			if (section == 'Accounts') {
				self.getAccounts();
			}
		});
	}).run();
};

ko.applyBindings(new TrineoViewModel());