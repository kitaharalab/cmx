/**
 * mitsuyo 2008/05/31
 */
package jp.crestmuse.cmx.commands.test;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import jp.crestmuse.cmx.commands.Dev2CSV;
import jp.crestmuse.cmx.filewrappers.*;

/**
 * DeviationInstanceXMLから数種類のCSVデータを出力するスクリプトです．
 * @author Mitsuyo Hashida @ CrestMuse Project, JST
 *         <address>http://www.m-use.net/</address>
 *         <address>hashida@kwansei.ac.jp</address>
 * @since 2008/05/31
 */
public class Dev2CSVAllTest extends Dev2CSV {

	@Override
	protected CSVWrapper run(DeviationInstanceWrapper f) throws IOException,
			ParserConfigurationException, SAXException, TransformerException,
			InvalidFileTypeException {
		f.toCSV(1, 1).writefile(new File(getOutFileName() + "1-1.csv"));
		f.toCSV(2, 2).writefile(new File(getOutFileName() + "2-2.csv"));
		f.toCSV(4, 4).writefile(new File(getOutFileName() + "4-4.csv"));
		f.toCSV(8, 8).writefile(new File(getOutFileName() + "8-8.csv"));
		f.toCSV(16, 8).writefile(new File(getOutFileName() + "16-8.csv"));
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Dev2CSV d2c = new Dev2CSVAllTest();
		try {
			d2c.start(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
