function NewAccountModel() {
	this.name = ko.observable();
	this.service = ko.observable();
	this.environment = ko.observable();
};

function AccountModel(account) {
	this.name = account.name;
	this.id = account.id;
};

function ClientModel(client) {
	this.name = client.name;
	this.id = client.id;
	this.organizations = ko.observableArray(client.organizations);
	this.repositories = ko.observableArray(client.repositories);
	this.boards = ko.observableArray(client.boards);
};

function OrgModel(org) {
	var self = this;
	self.name = ko.editable(org.nickName != null ? org.nickName : org.name);
	self.id = org.id;
	self.organizationType = org.organizationType;
	self.sandbox = org.sandbox;
	self.sourceBackup = ko.observable();
	self.targetBackup = ko.observable();
	self.diffTree = ko.observableArray([]);
	self.diffData = ko.observableArray([]);
	self.selectedDiff = ko.observable();
	self.clientOrgs = ko.observableArray([]);
	self.targetOrg = ko.observable();
	if (org.account != null) {
		self.accountName = org.account.name;
		self.accountId = org.account.id;
	}
	if (org.branch != null) {
		self.branchName = org.branch.name;
		self.branchId = ko.editable(org.branch.id);
		self.branchRepoName = org.branch.repo.name;
	} else {
		self.branchId = ko.editable();
	}
	if (org.client != null) {
		self.clientId = org.client.id;
		self.clientName = ko.editable(org.client.name);
	} else {
		self.clientName = ko.editable();
	}
	self.startOrgBackup = function(org) {
		alert('start backup: ' + org.id)
		$.post("/sf/orgs/" + org.id + '/backups');
	};
	self.startOrgTestRun = function(org) {
		alert('start tests: ' + org.id)
		$.post("/sf/orgs/" + org.id + '/tests');
	};
	self.deleteOrg = function(org) {
		$.ajax({
			method: "DELETE",
			url: "/sf/orgs/" + org.id
		}).done(function() {
        	mainModel.getOrgs();
        });
	};
	self.updateName = function() {
		$.ajax({
			data: JSON.stringify({id: self.id, nickName: self.name()}),
			method: "PUT",
			url: "/sf/orgs/",
			contentType: "application/json",
			dataType: "json"
		});
	};
	self.updateClientName = function() {
        var clients = ko.unwrap(mainModel.clients());
        clients.forEach(function(c) {
        	if (c.name == self.clientName()) {
        		self.clientId = c.id;
        	}
        });
        var data = JSON.stringify({id: self.id, client: {id: self.clientId}});
        $.ajax({
            data: data,
            method: "PUT",
            url: "/sf/orgs/",
            contentType: "application/json",
            dataType: "json"
        });
	};
	self.updateBranchName = function() {
        var data = JSON.stringify({id: self.id, branch: {id: self.branchId()}});
        $.ajax({
            data: data,
            method: "PUT",
            url: "/sf/orgs/",
            contentType: "application/json",
            dataType: "json"
        });
	};
	self.compareBackups = function() {
        $.getJSON("/sf/orgs/" + self.id + "/compare/" + self.sourceBackup() + "/" + self.targetBackup(), function(data) {
        	var temp = new Map();
        	data.forEach(function(d) {
        		var path = '/dev/null' == d.pathA ? d.pathB : d.pathA;
        		if (!temp.has(path)) {
        			temp.set(path, new Array());
        		}
        		temp.get(path).push(d);
        	});
        	var temp1 = [...temp.keys()];
        	var temp2 = [...temp];
        	self.diffTree(temp1);
        	self.diffData(temp2);
        });
	};
	self.compareBranch = function() {
        $.post("/sf/orgs/" + self.id + "/compareBranch", function(data) {
        	var temp = new Map();
        	data.forEach(function(d) {
        		var path = '/dev/null' == d.pathA ? d.pathB : d.pathA;
        		if (!temp.has(path)) {
        			temp.set(path, new Array());
        		}
        		temp.get(path).push(d);
        	});
        	var temp1 = [...temp.keys()];
        	var temp2 = [...temp];
        	self.diffTree(temp1);
        	self.diffData(temp2);
        });
	};
	self.compareOrgs = function() {
        $.getJSON("/sf/orgs/" + self.id + "/compare/" + self.targetOrg(), function(data) {
        	var temp = new Map();
        	data.forEach(function(d) {
        		var path = '/dev/null' == d.pathA ? d.pathB : d.pathA;
        		if (!temp.has(path)) {
        			temp.set(path, new Array());
        		}
        		temp.get(path).push(d);
        	});
        	var temp1 = [...temp.keys()];
        	var temp2 = [...temp];
        	self.diffTree(temp1);
        	self.diffData(temp2);
        });
	};
};

function BackupModel(backup) {
	var self = this;
	self.id = backup.id;
	self.name = backup.name;
	self.retrieveId = backup.retrieveId;
	self.status = backup.status;
	self.downloadBackup = function(org) {
		alert('download backup: ' + org.id);
		window.location.href = "/sf/orgs/" + org.id + "/backups/" + self.id;
	};
	self.deleteBackup = function(org) {
		$.ajax({
			method: "DELETE",
			url: "/sf/orgs/" + org.id + "/backups/" + self.id 
		}).done(function() {
        	mainModel.getBackups(org.id);
        });
	};
};

function RepoModel(repo) {
	var self = this;
	self.name = repo.name;
	self.id = repo.id;
	if (repo.account != null) {
		self.accountName = repo.account.name;
		self.accountId = repo.account.id;
	}
	if (repo.client != null) {
		self.clientId = repo.client.id;
		self.clientName = ko.editable(repo.client.name);
	} else {
		self.clientName = ko.editable();
	}
	self.sourceBranch = ko.observable();
	self.targetBranch = ko.observable();
	self.diffTree = ko.observableArray([]);
	self.diffData = ko.observableArray([]);
	self.selectedDiff = ko.observable();
	self.deleteRepo = function(repo) {
		$.ajax({
			method: "DELETE",
			url: "/github/repos/" + repo.id
		}).done(function() {
        	mainModel.getRepos();
        });
	};
	self.compare = function() {
        $.getJSON("/github/branches/" + self.sourceBranch() + "/compare/" + self.targetBranch(), function(data) {
        	var temp = new Map();
        	data.forEach(function(d) {
        		var path = '/dev/null' == d.pathA ? d.pathB : d.pathA;
        		if (!temp.has(path)) {
        			temp.set(path, new Array());
        		}
        		temp.get(path).push(d);
        	});
        	var temp1 = [...temp.keys()];
        	var temp2 = [...temp];
        	self.diffTree(temp1);
        	self.diffData(temp2);
        });
	};
	self.updateClientName = function() {
        var clients = ko.unwrap(mainModel.clients());
        clients.forEach(function(c) {
        	if (c.name == self.clientName()) {
        		self.clientId = c.id;
        	}
        });
        var data = JSON.stringify({id: self.id, client: {id: self.clientId}});
        $.ajax({
            data: data,
            method: "PUT",
            url: "/github/repos/",
            contentType: "application/json",
            dataType: "json"
        });
	};
	self.pull = function() {
		$.ajax({
			method: "POST",
			url: "/github/repos/" + repo.id + "/pull"
		});
	};
	self.updateBranches = function() {
		$.ajax({
			method: "POST",
			url: "/github/repos/" + repo.id + "/branches"
		});
	};
	self.updateTags = function() {
		$.ajax({
			method: "POST",
			url: "/github/repos/" + repo.id + "/tags"
		});
	};
};

function BranchModel(branch) {
	var self = this;
	self.id = branch.id;
	self.name = branch.name;
	self.sha = branch.sha;
	self.url = branch.url;
	if (branch.org != null) {
		self.orgName = branch.org.name;
	} else {
		self.orgName = '';
	}
	self.checkout = function(branch) {
		
	};
}

function BoardModel(board) {
	var self = this;
	self.name = board.name;
	self.id = board.id;
	if (board.account != null) {
		self.accountName = board.account.name;
		self.accountId = board.account.id;
	}
	if (board.client != null) {
		self.clientId = board.client.id;
		self.clientName = ko.editable(board.client.name);
	} else {
		self.clientName = ko.editable();
	}
	self.deleteBoard = function(board) {
		$.ajax({
			method: "DELETE",
			url: "/trello/boards/" + board.id
		}).done(function() {
			mainModel.getBoards();
        });
	};
	self.updateClientName = function() {
        var clients = ko.unwrap(mainModel.clients());
        clients.forEach(function(c) {
        	if (c.name == self.clientName()) {
        		self.clientId = c.id;
        	}
        });
        var data = JSON.stringify({id: self.id, client: {id: self.clientId}});
        $.ajax({
            data: data,
            method: "PUT",
            url: "/trello/boards/",
            contentType: "application/json",
            dataType: "json"
        });
	};
};

function ListModel(list) {
	var self = this;
	self.name = list.name;
	self.id = list.id;
};

function CardModel(org) {
	var self = this;
	self.id = org.id;
	self.desc = org.desc;
	self.labels = org.labels;
	var labelEx = /\{(.+)\}/g;
	var pointsEx = /\((\d+)\)/;
    while (m = labelEx.exec(org.name)) {
        self.labels.push({ name: m[1], color: 'blue' });
    }
    if (m = pointsEx.exec(org.name)) {
        self.points = m[1];
    } else {
    		self.points = null;
    }
	self.name = org.name.replace(labelEx, '').replace(pointsEx, '');
	if (org.branch != null) {
		self.branchName = org.branch.name;
		self.branchId = ko.editable(org.branch.id);
		self.branchRepoName = org.branch.repo.name;
	} else {
		self.branchId = ko.editable();
	}
	self.updateBranchName = function() {
/*        var data = JSON.stringify({id: self.id, branch: {id: self.branchId()}});
        $.ajax({
            data: data,
            method: "PUT",
            url: "/sf/orgs/",
            contentType: "application/json",
            dataType: "json"
        });*/
	};
};

function TrineoViewModel() {
	var self = this;
	self.sections = ["Clients", "Salesforce", "GitHub", "Git", "Trello"];
	self.chosenSection = ko.observable();
	self.services = ko.observableArray([]);
	self.environments = ko.observableArray([]);
	self.subSections = ko.observableArray([]);
	self.chosenSubsection = ko.observable();

	self.accounts = ko.observableArray([]);
	self.newAccount = new NewAccountModel();
	self.newAccountModalVisible = ko.observable(false);

	self.clients = ko.observableArray([]);
	self.newClientName = ko.observable();
	self.newClientModalVisible = ko.observable(false);

	self.orgs = ko.observableArray([]);
	self.newOrgModalVisible = ko.observable(false);
	self.newOrgAccount = ko.observable();
	self.compareSourceOrg = ko.observable();
	self.compareTargetOrg = ko.observable();
	self.org = ko.observable();
	self.branches = ko.observableArray([]);
	self.backups = ko.observableArray([]);
	self.metadata = new ko.treeview.viewmodel({
		collapseIcon: "fa fa-folder-open-o",
		expandIcon: "fa fa-folder-o",
		nodeIcon: "fa fa-file-o",
		showBorder: false
	});
	self.testTree = new ko.treeview.viewmodel({
		collapseIcon: "fa fa-folder-open-o",
		expandIcon: "fa fa-folder-o",
		nodeIcon: "fa fa-file-o",
		showBorder: false
	});
	self.codeCoverage = ko.observableArray([]);
	self.content = ko.observableArray([]);
	self.schema = ko.observableArray([]);
	self.sobjectFields = ko.observableArray([]);
	self.sobjectData = ko.observableArray([]);

	self.repos = ko.observableArray([]);
	self.repo = ko.observable();
	self.newRepoModalVisible = ko.observable(false);
	self.newRepoAccount = ko.observable();
	self.cloneURL = ko.observable();
	self.branches = ko.observableArray([]);
	self.tags = ko.observableArray([]);
	self.commits = ko.observableArray([]);

	self.boards = ko.observableArray([]);
	self.board = ko.observable();
	self.newBoardModalVisible = ko.observable(false);
	self.newBoardAccount = ko.observable();
	self.newBoardName = ko.observable();
	self.cards = ko.observableArray([]);
	self.card = ko.observable();
	self.cardComments = ko.observableArray([]);
	self.cardAttachments = ko.observableArray([]);

	self.gotoSection = function(section) {
		location.hash = section;
	};
	self.getAccounts = function() {
		$.getJSON("/accounts", function(data) {
			self.accounts(data);
		});
	};
	self.getServices = function() {
		$.getJSON("/services", function(data) {
			self.services(data);
		});
	};
	self.getEnvironments = function() {
		$.getJSON("/sf/environments", function(data) {
			self.environments(data);
		});
	};

	self.verifyAccount = function(account, event) {
		alert('verify: ' + account.id, event);
        $.ajax({
            method: "POST",
            url: "accounts/" + account.id + '/verify'
        }).done(function() {
        		self.getAccounts();
        });
	};
	self.renameAccount = function(account, event) {
		alert('rename: ' + account.id);
	};
	self.deleteAccount = function(account, event) {
        $.ajax({
            method: "DELETE",
            url: "accounts/" + account.id
        }).done(function() {
        		self.getAccounts();
        });
	};
	self.addAccount = function() {
		self.hideNewAccountModal()
		var model = self.newAccount;
		var values;
		if (model.service() == 'salesforce') {
			var values = "name=" + model.name() + "&service=" + model.service() + "&environment=" + model.environment();
		} else {
			var values = "name=" + model.name() + "&service=" + model.service();
		}
		window.open("/accounts/oauth?" + values, "oauth", "width=600,height=600,scrollbars=yes")
	};
	self.showNewAccountModal = function() {
		self.newAccountModalVisible(true);
	};
	self.hideNewAccountModal = function() {
		self.newAccountModalVisible(false);
	};

	self.getClients = function() {
		$.getJSON("/clients", function(data) {
			var mapped = $.map(data, function(item) {
				return new ClientModel(item)
			});
			self.clients(mapped);
			self.clients().forEach(function(c) {
				$.getJSON("/clients/" + c.id + "/organizations", function(data) {
					var mapped = $.map(data, function(item) {
						return new OrgModel(item)
					});
					c.organizations(mapped);
				});
				$.getJSON("/clients/" + c.id + "/repos", function(data) {
					c.repositories(data);
				});
				$.getJSON("/clients/" + c.id + "/boards", function(data) {
					c.boards(data);
				});
			});
		});
	};
	self.addClient = function() {
		self.hideNewClientModal()
		alert(self.newClientName());
		$.post("/clients/?name=" + self.newClientName(), function() {
			self.getClients();
		});
	};
	self.showNewClientModal = function() {
		self.newClientModalVisible(true);
	};
	self.hideNewClientModal = function() {
		self.newClientModalVisible(false);
	};
	self.deleteClient = function(client, event) {
		alert('delete: ' + client.id);
        $.ajax({
            method: "DELETE",
            url: "/clients/" + client.id
        }).done(function() {
        		self.getClients();
        });
	};

	self.getOrgs = function() {
		$.getJSON("/sf/orgs", function(data) {
			var mapped = $.map(data, function(item) {
				return new OrgModel(item)
			});
			self.orgs(mapped);
		});
	};
	self.addOrg = function() {
		self.hideNewOrgModal();
        alert(self.newOrgAccount());
        $.post("/sf/orgs/?acc=" + self.newOrgAccount(), function() {
        		self.getOrgs();
        });
	};
	self.showNewOrgModal = function() {
		self.newOrgModalVisible(true);
	};
	self.hideNewOrgModal = function() {
		self.newOrgModalVisible(false);
	};
	self.deleteOrg = function(org, event) {
		alert('delete: ' + org.id);
        $.ajax({
            method: "DELETE",
            url: "/sf/orgs/" + org.id
        }).done(function() {
        		self.getOrgs();
        });
	};
	self.getServiceAccounts = function(service) {
		$.getJSON("/services/" + service + "/accounts", function(data) {
			self.accounts(data);
		});
	};
	self.getOrg = function(id) {
		$.getJSON("/sf/orgs/" + id, function(org) {
			self.org(new OrgModel(org));
			self.getClientBranches(self.org().clientId);
			self.getClientOrgs(self.org().clientId);
		});
	};
	self.getClientBranches = function(id) {
		self.branches([]);
		if (id != null) {
			$.getJSON("/clients/" + id + "/repos", function(repos) {
				repos.forEach(function(r) {
					$.getJSON("/github/repos/" + r.id + "/branches", function(data) {
						var temp = self.branches();
						temp = temp.concat(data);
						self.branches(temp);
					});
				});
			});
		}
	};
	self.getClientOrgs = function(id) {
		self.org().clientOrgs([]);
		$.getJSON("/clients/" + id + "/organizations", function(data) {
			var mapped = $.map(data, function(item) {
				return new OrgModel(item)
			});
			self.org().clientOrgs(mapped);
		});
	};
	self.getBackups = function(id) {
		$.getJSON("/sf/orgs/" + id + "/backups", function(data) {
			var mapped = $.map(data, function(item) {
				return new BackupModel(item)
			});
			self.backups(mapped);
		});
	};
	self.getMetadataTree = function(id) {
		$.getJSON("/sf/orgs/" + id + "/metadata", function(data) {
			self.metadata.data([data]);
		});
	};
	self.showMetadata = function(tree, selectedNodes) {
		var filename = getTreeFilename(tree, selectedNodes);
		if (filename.length > 0) {
			$.getJSON("/sf/orgs/" + self.org().id + "/metadata" + filename, function (data, textStatus, jqXHR) {
				$('#codeHead').text(selectedNodes[0].text);
				self.content(data);
			});
		} else {
			self.content(null);
		}
	};
	self.metadata.updateFunction = self.showMetadata;
	self.getTestTree = function(id) {
		$.getJSON("/sf/orgs/" + id + "/tests", function (data) {
			self.testTree.data([data]);
		});
	};
	self.getCodeCoverage = function(id) {
		$.getJSON("/sf/orgs/" + id + "/coverage", function (data) {
			self.codeCoverage(data);
		});
	};
	self.getSchema = function(id) {
		$.getJSON("/sf/orgs/" + id + "/describeGlobal", function (data) {
			self.schema(data.sobjects);
		});
	};
	self.describeSObject = function(data) {
		var type = data.name;
		$.getJSON("/sf/orgs/" + self.org().id + "/" + type + "/describe", function (data) {
			self.sobjectFields(data.fields);
			self.getData(type, data.fields);
		});
	};
	self.getData = function(type, fields) {
		var query = "SELECT ";
		var first = true;
		fields.forEach(function(f) {
			if (!first) {
				query += ", ";
			} else {
				first = false;
			}
			query += f.name;
		});
		query += " FROM " + type;
		query += " LIMIT 10000";
        $.ajax({
            method: "POST",
            url: "/sf/orgs/" + self.org().id + "/query?query=" + query
        }).done(function(data) {
        		self.sobjectData(data);
        });
	}

	self.getRepos = function() {
		$.getJSON("/github/repos/", function (data) {
			var mapped = $.map(data, function(item) {
				return new RepoModel(item)
			});
			self.repos(mapped);
		});
	};
	self.showNewRepoModal = function() {
		self.newRepoModalVisible(true);
	};
	self.hideNewRepoModal = function() {
		self.newRepoModalVisible(false);
	};
	self.addRepo = function() {
		self.hideNewRepoModal();
        alert(self.newRepoAccount());
        $.post("/github/repos/?acc=" + self.newRepoAccount() + "&url=" + self.cloneURL(), function() {
        		self.getRepos();
        });
	};
	self.getRepo = function(id) {
		$.getJSON("/github/repos/" + id, function (data) {
			self.repo(new RepoModel(data));
		});
	};
	self.getBranches = function(id) {
		$.getJSON("/github/repos/" + id + "/branches", function (data) {
			var mapped = $.map(data, function(item) {
				return new BranchModel(item)
			});
			self.branches(mapped);
		});
	};
	self.getTags = function(id) {
		$.getJSON("/github/repos/" + id + "/tags", function (data) {
			self.tags(data);
		});
	};
	self.getCommits = function(id) {
		$.getJSON("/github/repos/" + id + "/commits", function (data) {
			self.commits(data);
		});
	};

	self.getBoards = function() {
		$.getJSON("/trello/boards/", function (data) {
			var mapped = $.map(data, function(item) {
				return new BoardModel(item)
			});
			self.boards(mapped);
		});
	};
	self.showNewBoardModal = function() {
		self.newBoardModalVisible(true);
	};
	self.hideNewBoardModal = function() {
		self.newBoardModalVisible(false);
	};
	self.addBoard = function() {
		self.hideNewBoardModal();
        alert(self.newBoardAccount());
        $.post("/trello/boards/?acc=" + self.newBoardAccount(), function() {
        	self.getBoards();
        });
	};
	self.getBoard = function(id) {
		$.getJSON("/trello/boards/" + id, function (data) {
			self.board(new BoardModel(data));
			self.getClientBranches(self.board().clientId);
		});
	};
	self.getLists = function(id) {
		$.getJSON("/trello/boards/" + id + '/lists', function (data) {
			var mapped = $.map(data, function(item) {
				return new ListModel(item);
			});
			self.subSections(mapped);
		});
	};
	self.getCards = function(id, list) {
		$.getJSON("/trello/boards/" + id + '/cards?list=' + list, function (data) {
			var mapped = $.map(data, function(item) {
				return new CardModel(item)
			});
			self.cards(mapped);
		});
	};
	self.chooseCard = function(data) {
		self.card(data);
		$.getJSON("/trello/boards/" + self.board().id + '/cards/' + data.id + '/comments', function (data) {
			self.cardComments(data);
		});
		$.getJSON("/trello/boards/" + self.board().id + '/cards/' + data.id + '/attachments', function (data) {
			self.cardAttachments(data);
		});
	}

	Sammy(function() {
		this.get("#:section", function() {
			var section = this.params.section;
			self.chosenSection(section);
			if (section == 'Accounts') {
				self.getAccounts();
				self.getServices();
				self.getEnvironments();
			} else if (section == 'Clients') {
				self.getClients();
			} else if (section == 'Salesforce') {
				self.getOrgs();
				self.getServiceAccounts("salesforce");
				self.org(null);
			} else if (section == 'GitHub') {
				self.getRepos();
				self.getServiceAccounts("github");
				self.repo(null);
			} else if (section == 'Trello') {
				self.getBoards();
				self.getServiceAccounts("trello");
				self.board(null);
			} else if (section == 'github') {
				this.app.runRoute('get', '#GitHub');
			}
		});
		this.get("#:section/:id", function() {
			var section = this.params.section;
			var id = this.params.id;
			if (section == "Salesforce" || section == "GitHub" || section == "Trello") {
				this.app.runRoute('get', '#' + section + '/' + id + '/Overview');
			} else if (section == 'github') {
				this.app.runRoute('get', '#GitHub/' + id);
			}
		});
		this.get("#:section/:id/:subsection", function() {
			var section = this.params.section;
			var subsection = this.params.subsection;
			var id = this.params.id;
			self.chosenSection(section);
			self.orgs([]);
			self.repos([]);
			self.boards([]);
			if (section == "Salesforce") {
				self.getOrg(id);
				self.subSections(["Overview", "Backups", "Metadata", "Tests", "Compare", "Compare Branch", "Compare Orgs", "Data"])
				self.chosenSubsection(subsection);
				if (subsection == "Overview") {
					self.getClients();
				} else if (subsection == "Backups") {
					self.getBackups(id);
				} else if (subsection == "Metadata") {
					self.getMetadataTree(id);
					self.content(null);
				} else if (subsection == "Tests") {
					self.getTestTree(id);
					self.getCodeCoverage(id);
				} else if (subsection == "Compare") {
					self.getBackups(id);
				} else if (subsection == "Compare Branch") {
					self.getBackups(id);
				} else if (subsection == "Compare Orgs") {
				} else if (subsection == "Data") {
					self.getSchema(id);
				}
			} else if (section == "GitHub") {
				self.getRepo(id);
				self.subSections(["Overview", "Branches", "Tags", "Commits", "Compare"])
				self.chosenSubsection(subsection);
				if (subsection == "Overview") {
					self.getClients();
				} else if (subsection == "Branches") {
					self.getBranches(id);
				} else if (subsection == "Tags") {
					self.getTags(id);
				} else if (subsection == "Commits") {
					self.getCommits(id);
				} else if (subsection == "Compare") {
					self.getBranches(id);
				}
			} else if (section == "Trello") {
				self.chosenSubsection(subsection);
				self.getBoard(id);
				self.getLists(id);
				self.getClients();
				if (subsection != "Overview") {
					self.getCards(id, subsection);
				}
				self.card(null);
				self.cardComments([]);
			}
		});

		this.get('', function() {
			this.app.runRoute('get', '#Clients');
		});
	}).run();
};

var mainModel = new TrineoViewModel();
ko.applyBindings(mainModel);