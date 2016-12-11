package nz.co.trineo.salesforce;

import java.util.HashMap;
import java.util.Map;

import com.uwyn.jhighlight.highlighter.ExplicitStateHighlighter;
import com.uwyn.jhighlight.renderer.XhtmlRenderer;

public class ApexXhtmlRenderer extends XhtmlRenderer {
	public final static Map<String, String> DEFAULT_CSS = new HashMap<String, String>() {
		private static final long serialVersionUID = -7350224508359224165L;

		{
			put("h1",
					"font-family: sans-serif; " + "font-size: 16pt; " + "font-weight: bold; " + "color: rgb(0,0,0); "
							+ "background: rgb(210,210,210); " + "border: solid 1px black; " + "padding: 5px; "
							+ "text-align: center;");

			put("code",
					"color: rgb(0,0,0); " + "font-family: monospace; " + "font-size: 12px; " + "white-space: nowrap;");

			put(".apex_plain", "color: rgb(0,0,0);");

			put(".apex_keyword", "color: rgb(0,0,0); " + "font-weight: bold;");

			put(".apex_type", "color: rgb(0,44,221);");

			put(".apex_operator", "color: rgb(0,124,31);");

			put(".apex_separator", "color: rgb(0,33,255);");

			put(".apex_literal", "color: rgb(188,0,0);");

			put(".apex_comment", "color: rgb(147,147,147); " + "background-color: rgb(247,247,247);");

			put(".apex_apexdoc_comment",
					"color: rgb(147,147,147); " + "background-color: rgb(247,247,247); " + "font-style: italic;");

			put(".apex_apexdoc_tag", "color: rgb(147,147,147); " + "background-color: rgb(247,247,247); "
					+ "font-style: italic; " + "font-weight: bold;");
		}
	};

	@Override
	protected String getCssClass(final int style) {
		switch (style) {
		case ApexHighlighter.PLAIN_STYLE:
			return "apex_plain";
		case ApexHighlighter.KEYWORD_STYLE:
			return "apex_keyword";
		case ApexHighlighter.TYPE_STYLE:
			return "apex_type";
		case ApexHighlighter.OPERATOR_STYLE:
			return "apex_operator";
		case ApexHighlighter.SEPARATOR_STYLE:
			return "apex_separator";
		case ApexHighlighter.LITERAL_STYLE:
			return "apex_literal";
		case ApexHighlighter.APEX_COMMENT_STYLE:
			return "apex_comment";
		case ApexHighlighter.APEXDOC_COMMENT_STYLE:
			return "apex_apexdoc_comment";
		case ApexHighlighter.APEXDOC_TAG_STYLE:
			return "apex_apexdoc_tag";
		}

		return null;
	}

	@Override
	protected Map<String, String> getDefaultCssStyles() {
		return DEFAULT_CSS;
	}

	@Override
	protected ExplicitStateHighlighter getHighlighter() {
		final ApexHighlighter highlighter = new ApexHighlighter();
		ApexHighlighter.ASSERT_IS_KEYWORD = true;

		return highlighter;
	}
}
