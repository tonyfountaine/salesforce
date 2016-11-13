(function () {
	ko.treeview = {
		viewmodel: function(configuration) {
			this.data = configuration.data || ko.observableArray([]);
			this.selectedNodes = ko.observableArray([]);
			this.showBorder = configuration.showBorder;
			this.onhoverColor = configuration.onhoverColor;
			this.selectedBackColor = configuration.selectedBackColor;
			this.updateFunction = configuration.updateFunction;
			this.collapseIcon = configuration.collapseIcon;
			this.expandIcon = configuration.expandIcon;
			this.nodeIcon = configuration.nodeIcon;
		}
	};

	ko.bindingHandlers.treeview = {
		init: function(element, valueAccessor, allBindings, viewModel, bindingContext) {
			var value = valueAccessor();
			$(element).treeview({
				data: ko.unwrap(value.data),
				showBorder: ko.unwrap(value.showBorder),
				onhoverColor: ko.unwrap(value.onhoverColor),
				selectedBackColor: ko.unwrap(value.selectedBackColor),
				collapseIcon: ko.unwrap(value.collapseIcon),
				expandIcon: ko.unwrap(value.expandIcon),
				nodeIcon: ko.unwrap(value.nodeIcon)
			});
		},
		update: function(element, valueAccessor, allBindings, viewModel, bindingContext) {
			var value = valueAccessor();
			$(element).treeview({
				data: ko.unwrap(value.data),
				showBorder: ko.unwrap(value.showBorder),
				onhoverColor: ko.unwrap(value.onhoverColor),
				selectedBackColor: ko.unwrap(value.selectedBackColor),
				collapseIcon: ko.unwrap(value.collapseIcon),
				expandIcon: ko.unwrap(value.expandIcon),
				nodeIcon: ko.unwrap(value.nodeIcon),
				onNodeSelected: function(event, node) {
					var selected = $(element).treeview("getSelected");
					value.selectedNodes(selected);
					if (value.updateFunction) {
						value.updateFunction($(element),value.selectedNodes());
					}
				},
				onNodeUnselected: function(event, node) {
					var selected = $(element).treeview("getSelected");
					value.selectedNodes(selected);
					if (value.updateFunction) {
						value.updateFunction($(element),value.selectedNodes());
					}
				}
			});
		}
	};
})();