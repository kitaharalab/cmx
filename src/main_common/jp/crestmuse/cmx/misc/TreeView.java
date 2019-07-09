package jp.crestmuse.cmx.misc;
import java.util.HashMap;
import java.util.Map;

public class TreeView<E extends Ordered> {

    /****************************************************************
     *<p>TreeViewは, 時刻の概念を持つオブジェクトを二分木で管理するための
     *クラスです. 
     *TreeViewに格納するオブジェクトは, Orderedインターフェイスを実装し, 
     *ordinalとsubordinalの2つのメソッドを持っていなければなりません. 
     *これらのメソッドを用いてそのオブジェクトの時刻を参照します. 
     *たとえば, 音符を扱うのであれば, ordinalはその音符の小節番号, 
     *subordinalは小節の頭からその音符が発音されるまでの時刻を返すように
     *設計します. 
     *この二分木は, 任意のノードに対して, そのノードと左の子とは時刻が同じで, 
     *右の子よりも時刻が早いという性質を満たします. </p>
     ***************************************************************/

//  private Node head;
////  private Stack<Node> branches;
//  private Node first;
//  private Node current;
  private boolean additionPermitted = true;
  private Map<E,Node> map;

//  private ArrayList<Node> lastnodes;

  private BranchHolder trunk;   // equal to branches.get(0)
  private Map<Byte,BranchHolder> branches;
  private BranchHolder currentBranch;
  private byte currentBranchID = (byte)0;

  private String id;

  public TreeView() {
    this("", null);
  }

  public TreeView(String id) {
    this(id, null);
//    this.id = id;
//    head = new Node();
//    first = head;
//    current = head;
//    map = new HashMap<E,Node>();
//    lastnodes = new ArrayList<Node>();
////    branches = new Stack<Node>();
  }

  private TreeView(Node head) {
    this("", head);
//    this.id = "";
//    this.head = head;
//    first = head;
//    current = head;
//    map = new HashMap<E,Node>();
//    lastnodes = new ArrayList<Node>();
  }

  private TreeView(String id, Node head) {
    this.id = id;
//    head = head == null ? new Node() : head;
//    branches = new ArrayList<BranchHolder>();
    branches = new HashMap<Byte,BranchHolder>();
    trunk = new BranchHolder((byte)0, head == null 
                             ? new NullNode(-1, -1, (byte)0) : head);
//    branches.add(trunk);
    branches.put((byte)0, trunk);
    currentBranch = trunk;
    map = new HashMap<E,Node>();
  }

  public String getTreeID() {
    return id;
  }

  public void add(E e, byte i, String label) {
    if (additionPermitted) {
      if (branches.containsKey(i)) {
        insertToSortedRightPath(e, label, branches.get(i));
      } else {
        Node node = insertNewBranchToSortedRightPath(e, label);
        BranchHolder b = new BranchHolder(i, node);
        branches.put(i, b);
      }
    }
    else
      throw new IllegalStateException("Addition is not permitted");
  }

  public void add(E e, String label) {
    if (additionPermitted) 
      insertToSortedRightPath(e, label, trunk);
    else
      throw new IllegalStateException("Addition is not permitted");
  }

  private Node insertNewBranchToSortedRightPath(E e, String label) {
    int ordinal = e.ordinal();
    int subordinal = e.subordinal();
    if (trunk.head.compare(ordinal, subordinal) < 0)
      throw new IllegalStateException();
    int cmp = trunk.getFirstNodeAtSameTime().compare(ordinal, subordinal);
    if (cmp < 0) {
      trunk.getFirstNodeAtPreviousTime();
      return insertNewBranchToSortedRightPath(e, label);
    } else if 
        (trunk.getFirstNodeAtSameTime().compareToChild(ordinal, subordinal)
         >= 0) {
      trunk.getFirstNodeAtNextTime();
      return insertNewBranchToSortedRightPath(e, label);
    } else if (cmp == 0) {
      trunk.appendToLeftLeaf(e, label);
      return trunk.current;
    } else {
      trunk.insertNullToChildR(ordinal, subordinal, label);
      trunk.appendToLeftLeaf(e, label);
      return trunk.current;
    }
  }

  private void insertToSortedRightPath(E e, String label, 
                                       BranchHolder b) {
    int ordinal = e.ordinal();
    int subordinal = e.subordinal();
//    System.out.println(e);
    if (b.head.compare(ordinal, subordinal) < 0) {
      b.replaceBranchHeadTo(e, label);
    } else {
    int cmp = b.getFirstNodeAtSameTime().compare(ordinal, subordinal);
    if (cmp < 0) {
      b.getFirstNodeAtPreviousTime();
      insertToSortedRightPath(e, label, b);
    } else if (b.getFirstNodeAtSameTime().compareToChild(ordinal, subordinal)
               >= 0) {
      b.getFirstNodeAtNextTime();
      insertToSortedRightPath(e, label, b);
    } else if (cmp == 0){
      b.appendToLeftLeaf(e, label);
    } else {
      b.insertToChildR(e, label);
    }
    }
  }


/*
  private void insertToSortedRightPath(E e, String label) {
    int ordinal = e.ordinal();
    int subordinal = e.subordinal();
    int cmp = first().compare(ordinal, subordinal);
    if (cmp < 0) {
      getFirstNodeAtPreviousTime();
      insertToSortedRightPath(e, label);
    } else if (first().compareToChild(ordinal, subordinal) >= 0) {
      getFirstNodeAtNextTime();
      insertToSortedRightPath(e, label);
    } else if (cmp == 0) {
      current = current.appendToLeftLeaf(e, label);
    } else {
      first = current = first().insertToChildR(e, label);
    }
  }
*/

  private void insertNullToSortedRightPath(int ordinal, int subordinal, 
                                           String label, 
                                           BranchHolder b) {
    if (b.head.compare(ordinal, subordinal) < 0)
      throw new IllegalStateException(ordinal + " " + subordinal);
//      b.replaceBranchHeadToNull(ordinal, subordinal, label);
    else {
//      throw new IllegalStateException();
    int cmp = b.getFirstNodeAtSameTime().compare(ordinal, subordinal);
    if (cmp < 0) {
      b.getFirstNodeAtPreviousTime();
      insertNullToSortedRightPath(ordinal, subordinal, label, b);
    } else if (b.getFirstNodeAtSameTime().compareToChild(ordinal,subordinal)
               >= 0) {
      b.getFirstNodeAtNextTime();
      insertNullToSortedRightPath(ordinal, subordinal, label, b);
    } else if (cmp == 0) {
      b.appendNullToLeftLeaf(ordinal, subordinal, label);
    } else {
      b.insertNullToChildR(ordinal, subordinal, label);
    }
    }
  }

/*
  private void insertNullToSortedRightPath(int ordinal, int subordinal, 
                                           String label) {
    int cmp = first().compare(ordinal, subordinal);
    if (cmp < 0) {
      getFirstNodeAtPreviousTime();
      insertNullToSortedRightPath(ordinal, subordinal, label);
    } else if (first().compareToChild(ordinal, subordinal) >= 0) {
      getFirstNodeAtNextTime();
      insertNullToSortedRightPath(ordinal, subordinal, label);
    } else if (cmp == 0) {
      current = current.appendNullToLeftLeaf(ordinal, subordinal, label);
    } else {
      first = current = first().insertNullToChildR(ordinal, subordinal, label);
    }
  }
*/

//  public void forbidAddition() {
//    additionPermitted = false;
//  }

  public E getRoot() {
    currentBranch = trunk;
    currentBranchID = (byte)0;
    return trunk.getRoot().entry;
//    first = current = head;
//    return head.entry;
  }

  public E getBranchHead(byte i) {
    currentBranchID = i;
    currentBranch = branches.get(i);
    return currentBranch.getRoot().entry;
  }

  public E nextL() {
    return currentBranch.nextL().entry;
//    return trunk.nextL().entry;
//    current = current.childL;
//    return current.entry;
  }

  public boolean hasNextL() {
    return currentBranch.hasNextL();
//    return trunk.hasNextL();
//    return current.childL != null;
  }

  public E nextR() {
    return currentBranch.nextR().entry;
//    return trunk.nextR().entry;
//    first = null;
//    current = current.childR;
//    return current.entry;
  }

  public boolean hasNextR() {
    return currentBranch.hasNextR();
//    return current.childR != null;
  }

  public E parent() {
    return currentBranch.parent().entry;
//    first = null;
//    current = current.parent;
//    return current.entry;
  }

//  private Node first() {
//    if (first == null)
//      first = first(current);
//    return first;
//  }

//  private Node first(Node node) {
//    if (node.parent != null && node.parent.childL != null
//        && node.parent.childL == node)
//      return first(node.parent);
//    else
//      return node;
//  }

//  private Node getFirstNodeAtPreviousTime() {
//    return (first = current = first().parent);
//  }

  public E getFirstElementAtPreviousTime() {
    return currentBranch.getFirstNodeAtPreviousTime().entry;
//    return getFirstNodeAtPreviousTime().entry;
  }

//  private Node getFirstNodeAtSameTime() {
//    return (current = first);
//  }

  public E getFirstElementAtSameTime() {
    return currentBranch.getFirstNodeAtSameTime().entry;
//    return getFirstNodeAtSameTime().entry;
  }

  public E getNextElementAtSameTime() {
    return currentBranch.nextL().entry;
//    return nextL();
  }

  public boolean hasMoreElementsAtSameTime() {
    return currentBranch.hasNextL();
//    return hasNextL();
  }

//  private Node getFirstNodeAtNextTime() {
//    return (current = first = first().childR);
//  }

  public E getFirstElementAtNextTime() {
    return currentBranch.getFirstNodeAtNextTime().entry;
//    return getFirstNodeAtNextTime().entry;
  }

  public boolean hasElementsAtNextTime() {
    return currentBranch.hasNodesAtNextTime();
//    return first().childR != null;
  }

  /** obsolete */
  public E get(int ordinal, int subordinal) {
    return search(ordinal, subordinal);
  }

  public E search(int ordinal, int subordinal) {
    Node node = currentBranch.search(ordinal, subordinal);
    return node == null ? null : node.entry;
  }

  public E search(int ordinal, int subordinal, NodeSearchFilter<E> filter) {
    Node node = currentBranch.search(ordinal, subordinal, filter);
    return node == null ? null : node.entry;
  }
  
/*
  public E get(int ordinal, int subordinal) {
    current = first().get(ordinal, subordinal);
    first = null;
    if (current != null)
      return current.entry;
    else
      return null;
  }
*/

  public E lookAhead(NodeSearchFilter<E> filter) {
    Node node = currentBranch.lookAhead(filter);
    return node == null ? null : node.entry;
  }

/*
  // 要チェック
  public E lookAhead(NodeSearchFilter<E> filter) {
    if (current.childL != null) {
      Node n2 = current.childL.searchL(filter);
      if (n2 != null) return n2.entry;
    } 
    Node n = first();
    while (n.childR != null) {
      n = n.childR;
      Node n2 = n.searchL(filter);
      if (n2 != null) return n2.entry;
    }
    return null;
  }
*/

  public void jumpTo(E e) {
    currentBranch = trunk;
    currentBranchID = (byte)0;
    trunk.jumpTo(e);
  }

/*
  public void jumpTo(E e) {
    Node node = map.get(e);
    current = node;
    first = null;
  }
*/

/*
  public void newBranch(int ordinal, int subordinal) {
    if (additionPermitted) {
      insertNullToSortedRightPath(ordinal, subordinal, "");
//      first().insertNullToSortedRightPath(ordinal, subordinal, "");
      current.appendNullToLeftLeaf(ordinal, subordinal, "NewBranch");
      branches.push(first = current);
    } else {
      throw new IllegalStateException("Addition is not permitted");
    }
  }
*/

/*
  public void endBranch() {
    current = branches.pop().parent;
    first = null;
//    while (base.parent.childR == null)
//      base = base.parent;
  }
*/

  public boolean isempty() {
    return (trunk.head.childL == null && trunk.head.childR == null);
  }

  public String toString() {
    return trunk.head.toString("");
  }

  private class BranchHolder {
    private Node head;
    private Node timewisetop;
    private Node current;
    private byte branchID;
    private BranchHolder(byte branchID, Node head) {
      this.branchID = branchID;
      current = timewisetop = this.head = head;
    }
    private Node getRoot() {
      return timewisetop = current = head;
    }
    private Node nextL() {
      return current = current.childL;
    }
    private boolean hasNextL() {
      return current.childL != null;
    }
    private Node nextR() {
      timewisetop = null;
      return current = current.childR;
    }
    private boolean hasNextR() {
      return current.childR != null;
    }
    private Node getFirstNodeAtPreviousTime() {
      return (timewisetop = current = timewisetop().parent);
    }
    private Node getFirstNodeAtSameTime() {
      return (current = timewisetop());
    }
    private Node getFirstNodeAtNextTime() {
      return (current = timewisetop = timewisetop().childR);
    }
    private boolean hasNodesAtNextTime() {
      return timewisetop().childR != null;
    }
    private boolean hasNodesAtPreviousTime() {
      return timewisetop().parent != null;
    }
    private Node timewisetop() {
      if (timewisetop == null)
        timewisetop = timewisetop(current);
      return timewisetop;
    }
    private Node timewisetop(Node node) {
      if (node.parent != null && node.parent.childL != null
          && node.parent.childL == node)
        return timewisetop(node.parent);
      else
        return node;
    }
    private Node parent() {
      timewisetop = null;
      return current = current.parent;
    }
    private void appendToLeftLeaf(E e, String label) {
      current = current.appendToLeftLeaf(e, label);
    }
    private void appendNullToLeftLeaf(int ordinal, int subordinal, 
                                      String label) {
      current = current.appendNullToLeftLeaf(ordinal, subordinal, label);
    }
    private void insertToChildR(E e, String label) {
      timewisetop = current = 
        timewisetop().insertToChildR(e, label);
    }
    private void insertNullToChildR(int ordinal, int subordinal, 
                                    String label) {
      timewisetop = current = 
        timewisetop().insertNullToChildR(ordinal, subordinal, label);
    }
/*
    private Node get(int ordinal, int subordinal) {
      current = timewisetop().get(ordinal, subordinal);
      timewisetop = null;
      return current;
    }
*/
    private Node lookAhead(NodeSearchFilter<E> filter) {
      if (current.childL != null) {
        Node n2 = current.childL.searchL(filter);
        if (n2 != null) return n2;
      }
      Node n = timewisetop();
      while (n.childR != null) {
        n = n.childR;
        Node n2 = n.searchL(filter);
        if (n2 != null) return n2;
      }
      return null;
    }
    private Node search(int ordinal, int subordinal, 
                        NodeSearchFilter<E> filter) {
      Node node = search(ordinal, subordinal);
      while (hasNodesAtPreviousTime()) {
        node = node.searchL(filter);
        if (node != null)
          return node;
        else
          node = getFirstNodeAtPreviousTime();
      }
      return null;
    }
    private Node search(int ordinal, int subordinal) {
      Node node = timewisetop();
      while (true) {
        if (node.compare(ordinal, subordinal) < 0) {
          if (hasNodesAtPreviousTime())
            node = getFirstNodeAtPreviousTime();
          else
            return null;
        } else if (node.compareToChild(ordinal, subordinal) >= 0) {
          if (hasNodesAtNextTime())
            node = getFirstNodeAtNextTime();
          else
            return null;
        } else {
          return node;
        }
      }
    }
    private void jumpTo(E e) {
      Node node = map.get(e);
      current = node;
      timewisetop = null;
    }
    private void replaceBranchHeadTo(E e, String label) {
      insertNullToSortedRightPath(e.ordinal(), e.subordinal(), "", trunk);
      Node newnode = trunk.current.appendToLeftLeaf(e, label);
      Node pastparent = head.parent;
      newnode.setChildR(head, pastparent.labelL);
      if (head.childL != null)        // kari
        pastparent.setChildL(head.childL, head.labelL);
      head.childL = null;
      head = timewisetop = current = newnode;
    }
//    private void replaceBranchHeadToNull(int ordinal, int subordinal, 
//                                         String label) {
      
  }

  private class NullNode extends Node {
    private int ordinal;
    private int subordinal;
    
    private NullNode(int ordinal, int subordinal, byte branchID) {
      super(branchID);
      this.ordinal = ordinal;
      this.subordinal = subordinal;
    }
    int ordinal() {
      return ordinal;
    }
    int subordinal() {
      return subordinal;
    }
  }

  private class Node {
    private E entry = null;
    private Node childL = null, childR = null;
    private Node parent = null;
    private String labelL = "", labelR = "";
    private byte branchID;

//    Node() {
//      super();
//      this.branchID = 0;
//    }

    Node(byte branchID) {
      super();
      this.branchID = branchID;
    }

    private Node(E e, byte branchID) {
      super();
      entry = e;
      this.branchID = branchID;
      map.put(e, this);
    }

    void setChildL(Node n, String label) {
      childL = n;
      n.parent = this;
      labelL = label;
    }

    void setChildR(Node n, String label) {
      childR = n;
      n.parent = this;
      labelR = label;
    }

    Node insertToChildR(E e, String label) {
      Node newnode = new Node(e, branchID);
      if (childR != null) 
        newnode.setChildR(childR, labelR);
      setChildR(newnode, label);
      return newnode;
//	    first = newnode;
//	    current = newnode;
    }

    Node insertNullToChildR(int ordinal, int subordinal, 
                            String label) {
      Node newnode = new NullNode(ordinal, subordinal, branchID);
      if (childR != null)
        newnode.setChildR(childR, labelR);
      setChildR(newnode, label);
      return newnode;
    }

    Node appendToLeftLeaf(E e, String label) {
      if (childL != null) {
        return childL.appendToLeftLeaf(e, label);
      } else {
        Node newnode = new Node(e, branchID);
        setChildL(newnode, label);
        return newnode;
      }
    }

    Node appendNullToLeftLeaf(int ordinal, int subordinal, 
                              String label) {
      if (childL != null) {
        return childL.appendNullToLeftLeaf(ordinal, subordinal, label);
      } else {
        NullNode newnode = new NullNode(ordinal, subordinal, branchID);
        setChildL(newnode, label);
        return newnode;
      }
    }

    int compare(int ordinal, int subordinal) {
//      if (entry == null)
//        return 1;
//      else
        if (ordinal == ordinal())
          return subordinal - subordinal();
        else 
          return ordinal - ordinal();
    }

    int compareToChild(int ordinal, int subordinal) {
      if (childR == null)
        return -1;
        else 
          if (ordinal == childR.ordinal())
            return subordinal - childR.subordinal();
          else
            return ordinal - childR.ordinal();
    }    



/*
    int compareToChild(int ordinal, int subordinal) {
      if (childR == null)
        return -1;
      else if (childR.entry == null)
        return 1;
      else 
        if (ordinal == childR.entry.ordinal())
          return subordinal - childR.subordinal();
        else
          return ordinal - childR.ordinal();
    }
*/
  
/*
    Node get(int ordinal, int subordinal) {
      if (compare(ordinal, subordinal) < 0)
        return parent.get(ordinal, subordinal);
      else if (compareToChild(ordinal, subordinal) >= 0)
        return childR.get(ordinal, subordinal);
      else
        return childR;
    }
*/
	    
    Node searchL(NodeSearchFilter<E> filter) {
      if (filter.accept(entry)) 
        return this;
      else if (childL != null)
        return childL.searchL(filter);
      else
        return null;
    }

    int ordinal() {
      return entry.ordinal();
    }
    
    int subordinal() {
      return entry.subordinal();
    }
      
    public String toString(String indent) {
      return entry + "\n" 
        + indent + "+-" 
        + (childL != null ? childL.toString(indent + "  ") : "null") + "\n" 
        + indent + "+-" 
        + (childR != null ? childR.toString(indent + "  ") : "null");
    }

//	public String toString() {
//	    return "(" + entry + " " + childL + " " + childR + ")";
//        }

    }

/*
  static class TreeTest implements Ordered {
    private int n1, n2, n3;
	private TreeTest(int n1, int n2, int n3) {
          this.n1 = n1;
          this.n2 = n2;
          this.n3 = n3;
	}
    public final int ordinal() {
      return n1;
    }
    public final int subordinal() {
      return n2;
    }
    public String toString() {
      return "[" + n1 + "-" + n2 + "-" + n3 + "]";
    }
  }

  public static void main(String[] args) {
    TreeView<TreeTest> t = 
      new TreeView<TreeTest>();
    t.add(new TreeTest(1, 2 ,3), "");
    System.out.println(t);
    t.add(new TreeTest(2, 2, 3), "");
    System.out.println(t);
    t.add(new TreeTest(3, 3 ,2), "");
    System.out.println(t);
    t.add(new TreeTest(2, 2, 4), "");
    System.out.println(t);
    t.add(new TreeTest(1, 3, 5), "");
    System.out.println(t);
    t.newBranch(2,6);
    System.out.println(t);
    t.add(new TreeTest(2, 6, 1), "");
    System.out.println(t);
    t.add(new TreeTest(3, 3, 1), "");
    System.out.println(t);
    t.endBranch();
    t.add(new TreeTest(3, 4, 1), "");
    System.out.println(t);
  }
*/

}