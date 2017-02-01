function formatGitHeader(header) {
	if (header != undefined) {
		switch (header.end) {
		case 0:
			return header.start - 1 + ",0";
		case 1:
			return header.start;
		default:
			return header.start + "," + header.end;
		}
	}
	return '';
}

function getTreeFilename(tree, selectedNodes) {
	var filename = "";
	if (selectedNodes.length > 0) {
		var n = selectedNodes[0];
		while (n.text != "/") {
			filename = "/" + n.text + filename;
			n = tree.treeview('getParent', n);
		}
	}
	return filename;
};

function extension(name) {
	var i = name.lastIndexOf('.');
	if (i > 0) {
		return name.substring(i + 1);
	}
	return name;
}
