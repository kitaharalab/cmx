package jp.crestmuse.cmx.xml.processors;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

public class SimplifiedXPointerProcessor {
  private static final String XLINK_NS = "http://www.w3.org/1999/xlink";

  private static final Pattern FULL_XPOINTER_PATTERN = 
			Pattern.compile("xpointer\\((.+?)\\)");
  private static final Matcher FULL_XPOINTER_MATCHER = 
			FULL_XPOINTER_PATTERN.matcher("");
  private static final Pattern CHILD_SEQUENCE_PATTERN = 
			Pattern.compile("\\/(\\d+)\\/");
  private static final Matcher CHILD_SEQUENCE_MATCHER = 
			CHILD_SEQUENCE_PATTERN.matcher("");
  private static final Pattern BARE_NAME_PATTERN = 
			Pattern.compile("^([^\\/]+)");
  private static final Matcher BARE_NAME_MATCHER = 
			BARE_NAME_PATTERN.matcher("");


  public static NodeList getRemoteResource(Node node, Document targetdoc) 
					throws TransformerException {
    NamedNodeMap nodemap = node.getAttributes();
    if (nodemap != null) {
      Node hrefattr = nodemap.getNamedItemNS(XLINK_NS, "href");
      if (hrefattr != null) {
        String href = hrefattr.getNodeValue();
        String fragid = href.substring(href.indexOf(href) + 1);
        return selectNodeList(targetdoc, fragid);
      }
    }
    return null;
  }

  public static NodeList selectNodeList(Document doc, String fragid) 
					throws TransformerException {
    FULL_XPOINTER_MATCHER.reset(fragid);
    while (FULL_XPOINTER_MATCHER.find()) {
      String xpath = FULL_XPOINTER_MATCHER.group(1);
      NodeList nl = XPathAPI.selectNodeList(doc, xpath);
      if (nl != null) return nl;
    }
    CHILD_SEQUENCE_MATCHER.reset(fragid);
    while (CHILD_SEQUENCE_MATCHER.find()) {
      fragid = CHILD_SEQUENCE_MATCHER.replaceFirst("/*[" + 
				CHILD_SEQUENCE_MATCHER.group(1) + "]/");
      CHILD_SEQUENCE_MATCHER.reset(fragid);
    }
    BARE_NAME_MATCHER.reset(fragid);
    if (BARE_NAME_MATCHER.find())
      fragid = BARE_NAME_MATCHER.replaceFirst("id(" + 
				BARE_NAME_MATCHER.group(1) + ")");
    return XPathAPI.selectNodeList(doc, fragid);
  }
      
}