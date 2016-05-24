package nz.co.trineo.salesforce;

import java.util.HashMap;
import java.util.Map;

import com.uwyn.jhighlight.highlighter.ExplicitStateHighlighter;
import com.uwyn.jhighlight.renderer.XhtmlRenderer;

public class ApexXhtmlRenderer extends XhtmlRenderer {
	@SuppressWarnings("serial")
	public final static Map<String, String> DEFAULT_CSS = new HashMap<String, String>() {
		{
			put("h1",
					"font-family: sans-serif; " + "font-size: 16pt; " + "font-weight: bold; " + "color: rgb(0,0,0); "
							+ "background: rgb(210,210,210); " + "border: solid 1px black; " + "padding: 5px; "
							+ "text-align: center;");

			put("code",
					"color: rgb(0,0,0); " + "font-family: monospace; " + "font-size: 12px; " + "white-space: nowrap;");

			put(".java_plain", "color: rgb(0,0,0);");

			put(".java_keyword", "color: rgb(0,0,0); " + "font-weight: bold;");

			put(".java_type", "color: rgb(0,44,221);");

			put(".java_operator", "color: rgb(0,124,31);");

			put(".java_separator", "color: rgb(0,33,255);");

			put(".java_literal", "color: rgb(188,0,0);");

			put(".java_comment", "color: rgb(147,147,147); " + "background-color: rgb(247,247,247);");

			put(".java_javadoc_comment",
					"color: rgb(147,147,147); " + "background-color: rgb(247,247,247); " + "font-style: italic;");

			put(".java_javadoc_tag", "color: rgb(147,147,147); " + "background-color: rgb(247,247,247); "
					+ "font-style: italic; " + "font-weight: bold;");
		}
	};

	protected Map<String,String> getDefaultCssStyles() {
		return DEFAULT_CSS;
	}

	protected String getCssClass(int style) {
		switch (style) {
		case ApexHighlighter.PLAIN_STYLE:
			return "java_plain";
		case ApexHighlighter.KEYWORD_STYLE:
			return "java_keyword";
		case ApexHighlighter.TYPE_STYLE:
			return "java_type";
		case ApexHighlighter.OPERATOR_STYLE:
			return "java_operator";
		case ApexHighlighter.SEPARATOR_STYLE:
			return "java_separator";
		case ApexHighlighter.LITERAL_STYLE:
			return "java_literal";
		case ApexHighlighter.APEX_COMMENT_STYLE:
			return "java_comment";
		case ApexHighlighter.APEXDOC_COMMENT_STYLE:
			return "java_javadoc_comment";
		case ApexHighlighter.APEXDOC_TAG_STYLE:
			return "java_javadoc_tag";
		}

		return null;
	}

	protected ExplicitStateHighlighter getHighlighter() {
		ApexHighlighter highlighter = new ApexHighlighter();
		ApexHighlighter.ASSERT_IS_KEYWORD = true;

		return highlighter;
	}
}
