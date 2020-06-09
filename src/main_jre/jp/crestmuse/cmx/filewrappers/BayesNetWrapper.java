package jp.crestmuse.cmx.filewrappers;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;

import jp.crestmuse.cmx.inference.BayesNetCompatible;
import weka.classifiers.bayes.net.BIFReader;
import weka.classifiers.bayes.net.EditableBayesNet;
import weka.classifiers.bayes.net.MarginCalculator;
import weka.core.Instances;
import weka.estimators.Estimator;

public class BayesNetWrapper implements FileWrapperCompatible, BayesNetCompatible {

  public BayesNetWrapper(String fileName) {
    this.fileName = fileName;
    try {
      if (isArff(fileName)) {
        Instances instances = new Instances(new FileReader(fileName));
        bayesNet = new EditableBayesNet(instances);
        bayesNet.buildClassifier(instances);
      } else {
        BIFReader reader = new BIFReader();
        reader.processFile(fileName);
        bayesNet = new EditableBayesNet(reader);
      }
      update();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String getFileName() {
    return fileName;
  }

  public void addNode(String sName, int nCardinality) {
    try {
      bayesNet.addNode(sName, nCardinality);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void deleteNode(int nTargetNode) {
    try {
      bayesNet.deleteNode(nTargetNode);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void deleteNode(String sName) {
    try {
      bayesNet.deleteNode(sName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void addArc(int nParent, int nChild) {
    try {
      bayesNet.addArc(nParent, nChild);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void addArc(String sParent, String sChild) {
    try {
      bayesNet.addArc(sParent, sChild);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public double[][] getDistribution(int nTargetNode) {
    return bayesNet.getDistribution(nTargetNode);
  }

  public Estimator[][] getDistribution(){
    return bayesNet.m_Distributions;
  }

  public void setDistribution(int nTargetNode, double[][] P) throws Exception{
    bayesNet.setDistribution(nTargetNode, P);
  }

  public void addNodeValue(int nTargetNode, String sNewValue) {
    bayesNet.addNodeValue(nTargetNode, sNewValue);
  }

  public void delNodeValue(int nTargetNode, String sValue) {
    try {
      bayesNet.delNodeValue(nTargetNode, sValue);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void renameNodeValue(int nTargetNode, String sValue, String sNewValue) {
    bayesNet.renameNodeValue(nTargetNode, sValue, sNewValue);
  }
  
  public double[] getMargin(int iNode){
    return bayesNet.getMargin(iNode);
  }
  
  public void setMargin(int iNode, double[] fMarginP){
    bayesNet.setMargin(iNode, fMarginP);
  }

  public String getValueName(int iNode, int index){
    return bayesNet.getValueName(iNode, index);
  }
  
  public int getNode(String sNodeName) {
    return bayesNet.getNode2(sNodeName);
  }

  public int getHighestMarginIndex(int iNode){
    double max = 0;
    int index = 0;
    double[] margins = bayesNet.getMargin(iNode);
    for(int i=0; i<margins.length; i++)
      if(margins[i] > max){
        max = margins[i];
        index = i;
      }
    return index;
  }

  public String getHighestMarginName(int iNode){
    return bayesNet.getValueName(iNode, getHighestMarginIndex(iNode));
  }
  /**
   * <pre>iNodeのrank番目に確率の高い要素を返します
   *  (Ex.) rank = 2 で2番目に大きい要素
   * 同確率の場合、Margin配列中のインデックスが小さい方が優先されます</pre>
   * @param iNode
   * @param rank
   * @return
   */
  public String getRankedMarginName(int iNode, int rank){
    double[] margins = bayesNet.getMargin(iNode);
    int index = 0;
    double[] copymargin = new double[margins.length];
    double[] sortedmargin = new double[margins.length];
    copymargin = margins.clone();
    Arrays.sort(copymargin);
    
    for(int i=0; i<margins.length; i++){
      sortedmargin[i] = copymargin[margins.length-i-1];
    }
    
    for(int i=0; i<margins.length; i++){
      if(margins[i] == sortedmargin[rank-1]){
        index = i;
        break;
      }
    }
    return bayesNet.getValueName(iNode, index);
  }
  
  public int getEvidence(int iNode){
    return bayesNet.getEvidence(iNode);
  }
  
  public void setEvidence(int iNode, int iValue){
    bayesNet.setEvidence(iNode, iValue);
  }
  
  public void setEvidence(int iNode, String sValue){
    String[] values = bayesNet.getValues(iNode);
    for(int i=0; i<values.length; i++)
      if(values[i].equals(sValue)){
        bayesNet.setEvidence(iNode, i);
        break;
      }
  }

    public String[] getValues(String sNode) {
	return bayesNet.getValues(sNode);
    }

    public String[] getValues(int iNode) {
	return bayesNet.getValues(iNode);
    }
  
  public void update(){
    try {
      MarginCalculator mc = new MarginCalculator();
      mc.calcMargins(bayesNet);
      //  SerializedObject so = new SerializedObject(mc);
      //  MarginCalculator mcWithEvidence = (MarginCalculator) so.getObject();
      for (int iNode = 0; iNode < bayesNet.getNrOfNodes(); iNode++) {
	  if (bayesNet.getEvidence(iNode) >= 0) {
	      mc.setEvidence(iNode, bayesNet.getEvidence(iNode));
	  }
      }
      for (int iNode = 0; iNode < bayesNet.getNrOfNodes(); iNode++) {
	  bayesNet.setMargin(iNode, mc.getMargin(iNode));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void write(OutputStream out) throws IOException, SAXException {
    out.write(bayesNet.toXMLBIF03().getBytes());
    out.close();
  }

  public void write(Writer writer) throws IOException, SAXException {
    writer.write(bayesNet.toXMLBIF03());
    writer.close();
  }

  public void writeGZippedFile(File file) throws IOException, SAXException {
    writefile(file);
  }

  public void writefile(File file) throws IOException, SAXException {
    FileOutputStream fos = new FileOutputStream(file);
    write(fos);
    fos.close();
  }

  private boolean isArff(String fileName) {
    return fileName.substring(fileName.lastIndexOf(".") + 1).equals("arff");
  }

  private EditableBayesNet bayesNet;
  private String fileName;

/*
  public static void main(String[] args) {
    BayesNetWrapper w = new BayesNetWrapper("hoge.xml");
    // 0番目のノード(currentNote)を0番目の属性(0)に固定する
    w.setEvidence(0, 0);
    // 1番目のノード(currentChord)をAに固定する
    w.setEvidence(1, "G#");
    // 2番目のノード(nextNote)を0番目の属性(1)に固定する
    w.setEvidence(2, 0);
    w.update();
    // 予測する！
    System.out.println(w.getHighestMarginName(3));
    }
*/

}
