package nz.co.trineo.salesforce.views;

import java.util.List;

import io.dropwizard.views.View;

public class CompareView extends View {

	private final List<String> compare;

	public CompareView(final List<String> compare) {
		super("/compare.ftl");
		this.compare = compare;
	}

	public List<String> getCompare() {
		return compare;
	}
}
