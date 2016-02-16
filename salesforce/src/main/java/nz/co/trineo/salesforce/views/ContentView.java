package nz.co.trineo.salesforce.views;

import java.util.List;

import io.dropwizard.views.View;

public class ContentView extends View {

	private final List<String> lines;

	public ContentView(final List<String> lines) {
		super("/content.ftl");
		this.lines = lines;
	}

	public List<String> getLines() {
		return lines;
	}
}
