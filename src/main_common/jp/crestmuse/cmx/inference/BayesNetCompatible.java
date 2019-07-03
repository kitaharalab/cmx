package jp.crestmuse.cmx.inference;

public interface BayesNetCompatible {
    void addNode(String sName, int nCardinality);
    void deleteNode(int nTargetNode);
    void deleteNode(String sName);
    void addArc(int nParent, int nChild);
    void addArc(String sParent, String sChild);
    double[][] getDistribution(int nTargetName);
    void setDistribution(int nTargetNode, double[][] P)
	throws Exception;
    void addNodeValue(int nTargetNode, String sNewValue);
    void delNodeValue(int nTargetNode, String sValue);
    void renameNodeValue(int nTargetNode, String sValue, String newValue);
    double[] getMargin(int iNode);
    void setMargin(int iNode, double[] fMarginP);
    String getValueName(int iNode, int index);
    int getNode(String sNodeName);
    int getHighestMarginIndex(int iNode);
    String getHighestMarginName(int iNode);
    String getRankedMarginName(int iNode, int rank);
    int getEvidence(int iNode);
    void setEvidence(int iNode, int iValue);
    void setEvidence(int iNode, String sValue);
    String[] getValues(String sNode);
    String[] getValues(int iNode);
    void update() throws Exception;
}