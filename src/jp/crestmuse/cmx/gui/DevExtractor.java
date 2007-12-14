package jp.crestmuse.cmx.gui;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import jp.crestmuse.cmx.commands.DeviationInstanceExtractor;
import java.awt.Insets;
import javax.swing.JRadioButton;
import java.awt.GridBagConstraints;

/**
 * @author Mitsuyo Hashida @ CrestMuse Project
 * @since Sep. 25, 2007
 */
public class DevExtractor extends JFrame {

	private static final long serialVersionUID = 1L;
	private String path;
	private JPanel jContentPane = null;

	private JPanel jPanel = null;

	private JButton jButton = null;

	private JButton jButton1 = null;

	private JTextField xmlfilename = null;

	private JTextField smffilename = null;

	// private JProgressBar jProgressBar = null;

	private JTextArea log = null;

	private int isXMLselected;

	private int isSMFselected;

	private JButton extractButton = null;

	private JButton originalSMFbutton = null;
	protected File smfpath;
	protected File xmlpath;
	private JRadioButton gpoButton = null;
	private JRadioButton bosenButton = null;
	private ButtonGroup modelButtonGroup;  //  @jve:decl-index=0:

	/**
	 * @throws HeadlessException
	 */
	public DevExtractor() throws HeadlessException {
		super();
		initialize();
	}

	/**
	 * @param gc
	 */
	public DevExtractor(GraphicsConfiguration gc) {
		super(gc);
		initialize();
	}

	/**
	 * @param title
	 * @throws HeadlessException
	 */
	public DevExtractor(String title) throws HeadlessException {
		super(title);
		initialize();
	}

	/**
	 * @param title
	 * @param gc
	 */
	public DevExtractor(String title, GraphicsConfiguration gc) {
		super(title, gc);
		initialize();
	}

	/**
	 * This method initializes jPanel
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 1;
			gridBagConstraints22.anchor = GridBagConstraints.WEST;
			gridBagConstraints22.gridy = 3;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.anchor = GridBagConstraints.EAST;
			gridBagConstraints13.gridy = 3;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 3;
			gridBagConstraints12.fill = GridBagConstraints.NONE;
			gridBagConstraints12.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints12.anchor = GridBagConstraints.WEST;
			gridBagConstraints12.gridy = 3;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 2;
			gridBagConstraints4.gridwidth = 1;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.insets = new Insets(5, 20, 5, 10);
			gridBagConstraints4.gridy = 3;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.gridy = 5;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.weighty = 1.0;
			gridBagConstraints3.gridwidth = 4;
			gridBagConstraints3.insets = new Insets(10, 60, 10, 60);
			gridBagConstraints3.gridx = 0;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 0;
			gridBagConstraints21.gridwidth = 2;
			gridBagConstraints21.insets = new Insets(5, 60, 0, 60);
			gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints21.gridy = 3;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.BOTH;
			gridBagConstraints11.gridy = 1;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.insets = new Insets(15, 0, 5, 20);
			gridBagConstraints11.gridwidth = 3;
			gridBagConstraints11.gridx = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.gridy = 2;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.insets = new Insets(5, 0, 5, 20);
			gridBagConstraints2.gridwidth = 3;
			gridBagConstraints2.gridx = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.insets = new Insets(15, 20, 5, 10);
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridy = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.insets = new Insets(5, 20, 5, 10);
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridy = 2;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getJButton(), gridBagConstraints);
			jPanel.add(getJButton1(), gridBagConstraints1);
			jPanel.add(getXmlfilename(), gridBagConstraints2);
			jPanel.add(getSmffilename(), gridBagConstraints11);
			jPanel.add(getLog(), gridBagConstraints3);
			jPanel.add(getExtractButton(), gridBagConstraints4);
			jPanel.add(getOriginalSMFbutton(), gridBagConstraints12);
			jPanel.add(getGpoButton(), gridBagConstraints13);
			jPanel.add(getBosenButton(), gridBagConstraints22);
		}
		return jPanel;
	}

	/**
	 * This method initializes jButton
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("MusicXML");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					final JFileChooser fc = new JFileChooser(xmlpath);
					isXMLselected = fc.showOpenDialog((JButton) e.getSource());
					if (isXMLselected == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						// This is where a real application would open the file.
						log.append("Opening: " + file.getName() + ".\n");
						try {
							xmlfilename.setText(file.getCanonicalPath());
							xmlpath=file.getParentFile();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						if (isSMFselected == JFileChooser.APPROVE_OPTION) {
							extractButton.setEnabled(true);
						}
					} else {
						log.append("Open command cancelled by user.\n");
					}
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jButton1
	 * @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("SMF");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					final JFileChooser fc = new JFileChooser(smfpath);
					if(smfpath!=null)
						fc.setCurrentDirectory(smfpath);
					isSMFselected = fc.showOpenDialog((JButton) e.getSource());
					if (isSMFselected == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						log.append("Opening: " + file.getName() + ".\n");
						try {
							smffilename.setText(file.getCanonicalPath());
							xmlpath=smfpath=file.getParentFile().getParentFile();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						if (isXMLselected == JFileChooser.APPROVE_OPTION) {
							extractButton.setEnabled(true);
						}
					} else {
						log.append("Open command cancelled by user.\n");
					}
				}
			});
		}
		return jButton1;
	}

	/**
	 * This method initializes xmlfilename
	 * @return javax.swing.JTextField
	 */
	private JTextField getXmlfilename() {
		if (xmlfilename == null) {
			xmlfilename = new JTextField();
		}
		return xmlfilename;
	}

	/**
	 * This method initializes smffilename
	 * @return javax.swing.JTextField
	 */
	private JTextField getSmffilename() {
		if (smffilename == null) {
			smffilename = new JTextField();
		}
		return smffilename;
	}

	/**
	 * This method initializes log
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getLog() {
		if (log == null) {
			log = new JTextArea(10, 50);
			log.setEditable(false);
			log.setWrapStyleWord(true);
		}
		return log;
	}

	/**
	 * This method initializes extractButton
	 * @return javax.swing.JButton
	 */
	private JButton getExtractButton() {
		if (extractButton == null) {
			extractButton = new JButton();
			extractButton.setText("Extract");
			extractButton.setEnabled(false);
			extractButton.addActionListener(new java.awt.event.ActionListener() {

				public void actionPerformed(java.awt.event.ActionEvent e) {
					log.append("Extracting...");
					String model="";
					if(getGpoButton().isSelected())model="gpo";
					else if(getBosenButton().isSelected())model="bosen";
					String[] obj = { "-target", xmlfilename.getText(), "-smf",
							smffilename.getText(), "-d", "%_" + File.separator + "dev-"+model, "-o",
							"%d" + File.separator + "deviation.xml", "-mkdir" };
					DeviationInstanceExtractor die = new DeviationInstanceExtractor();
					try {
						die.start(obj);
						originalSMFbutton.setEnabled(true);
						path = die.getDestDir();
						log.append("done. output files are created at\n" + path);
					} catch (Exception e1) {
						try {
							e1.printStackTrace(new PrintWriter(new File(smfpath+"-error.log")));
						} catch (FileNotFoundException e2) {
							e2.printStackTrace();
						}
						e1.printStackTrace();
					}
				}
			});
		}
		return extractButton;
	}

	/**
	 * This method initializes originalSMFbutton
	 * @return javax.swing.JButton
	 */
	private JButton getOriginalSMFbutton() {
		if (originalSMFbutton == null) {
			originalSMFbutton = new JButton();
			originalSMFbutton.setText("Open Viewer");
			originalSMFbutton.setEnabled(false);
			originalSMFbutton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					log.append(e.getActionCommand() + ": not implemented");
					String args[]= {getSmffilename().getText()};
					CMXMusicDataViewer.main(args);
				}
			});
		}
		return originalSMFbutton;
	}

	/**
	 * This method initializes gpoButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getGpoButton() {
		if (gpoButton == null) {
			gpoButton = new JRadioButton();
			gpoButton.setText("gpo");
			gpoButton.setSelected(true);
		}
		return gpoButton;
	}

	/**
	 * This method initializes bosenButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getBosenButton() {
		if (bosenButton == null) {
			bosenButton = new JRadioButton();
			bosenButton.setText("bosen");
		}
		return bosenButton;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DevExtractor thisClass = new DevExtractor();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	/**
	 * This method initializes this
	 * @return void
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		this.setTitle("DeviationExtractor - CrestmUs");
		this.setSize(new Dimension(480, 340));
	}

	/**
	 * This method initializes jContentPane
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJPanel(), BorderLayout.CENTER);
			modelButtonGroup = new ButtonGroup();
			modelButtonGroup.add(getGpoButton());
			modelButtonGroup.add(getBosenButton());
		}
		return jContentPane;
	}

} // @jve:decl-index=0:visual-constraint="23,14"
