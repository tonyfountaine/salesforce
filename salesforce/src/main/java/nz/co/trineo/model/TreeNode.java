package nz.co.trineo.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_DEFAULT)
public class TreeNode {

	public static class NodeState {
		private boolean checked;
		private boolean disabled;
		private boolean expanded;
		private boolean selected;

		@JsonProperty
		public boolean isChecked() {
			return checked;
		}

		@JsonProperty
		public boolean isDisabled() {
			return disabled;
		}

		@JsonProperty
		public boolean isExpanded() {
			return expanded;
		}

		@JsonProperty
		public boolean isSelected() {
			return selected;
		}

		public void setChecked(final boolean checked) {
			this.checked = checked;
		}

		public void setDisabled(final boolean disabled) {
			this.disabled = disabled;
		}

		public void setExpanded(final boolean expanded) {
			this.expanded = expanded;
		}

		public void setSelected(final boolean selected) {
			this.selected = selected;
		}
	}

	private String text;
	private String icon;
	private String selectedIcon;
	private String color;
	private String backColor;
	private String href;
	private boolean selectable;
	private NodeState state = new NodeState();
	private Set<String> tags = new HashSet<>();
	private List<TreeNode> nodes = new ArrayList<>();

	@JsonProperty
	public String getBackColor() {
		return backColor;
	}

	@JsonProperty
	public String getColor() {
		return color;
	}

	@JsonProperty
	public String getHref() {
		return href;
	}

	@JsonProperty
	public String getIcon() {
		return icon;
	}

	public List<TreeNode> getNodes() {
		return nodes;
	}

	@JsonProperty
	public String getSelectedIcon() {
		return selectedIcon;
	}

	@JsonProperty
	public NodeState getState() {
		return state;
	}

	@JsonProperty
	public Set<String> getTags() {
		return tags;
	}

	@JsonProperty
	public String getText() {
		return text;
	}

	@JsonProperty
	public boolean isSelectable() {
		return selectable;
	}

	public void setBackColor(final String backColor) {
		this.backColor = backColor;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public void setHref(final String href) {
		this.href = href;
	}

	public void setIcon(final String icon) {
		this.icon = icon;
	}

	public void setNodes(final List<TreeNode> nodes) {
		this.nodes = nodes;
	}

	public void setSelectable(final boolean selectable) {
		this.selectable = selectable;
	}

	public void setSelectedIcon(final String selectedIcon) {
		this.selectedIcon = selectedIcon;
	}

	public void setState(final NodeState state) {
		this.state = state;
	}

	public void setTags(final Set<String> tags) {
		this.tags = tags;
	}

	public void setText(final String text) {
		this.text = text;
	}
}
