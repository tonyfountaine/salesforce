package nz.co.trineo.salesforce.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_DEFAULT)
public class MetadataNode {

	public static class NodeState {
		private boolean checked;
		private boolean disabled;
		private boolean expanded;
		private boolean selected;

		@JsonProperty
		public boolean isChecked() {
			return checked;
		}

		public void setChecked(final boolean checked) {
			this.checked = checked;
		}

		@JsonProperty
		public boolean isDisabled() {
			return disabled;
		}

		public void setDisabled(final boolean disabled) {
			this.disabled = disabled;
		}

		@JsonProperty
		public boolean isExpanded() {
			return expanded;
		}

		public void setExpanded(final boolean expanded) {
			this.expanded = expanded;
		}

		@JsonProperty
		public boolean isSelected() {
			return selected;
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
	private List<MetadataNode> nodes = new ArrayList<>();

	@JsonProperty
	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	@JsonProperty
	public String getIcon() {
		return icon;
	}

	public void setIcon(final String icon) {
		this.icon = icon;
	}

	@JsonProperty
	public String getSelectedIcon() {
		return selectedIcon;
	}

	public void setSelectedIcon(final String selectedIcon) {
		this.selectedIcon = selectedIcon;
	}

	@JsonProperty
	public String getColor() {
		return color;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	@JsonProperty
	public String getBackColor() {
		return backColor;
	}

	public void setBackColor(final String backColor) {
		this.backColor = backColor;
	}

	@JsonProperty
	public String getHref() {
		return href;
	}

	public void setHref(final String href) {
		this.href = href;
	}

	@JsonProperty
	public boolean isSelectable() {
		return selectable;
	}

	public void setSelectable(final boolean selectable) {
		this.selectable = selectable;
	}

	@JsonProperty
	public NodeState getState() {
		return state;
	}

	public void setState(final NodeState state) {
		this.state = state;
	}

	@JsonProperty
	public Set<String> getTags() {
		return tags;
	}

	public void setTags(final Set<String> tags) {
		this.tags = tags;
	}

	public List<MetadataNode> getNodes() {
		return nodes;
	}

	public void setNodes(final List<MetadataNode> nodes) {
		this.nodes = nodes;
	}
}
