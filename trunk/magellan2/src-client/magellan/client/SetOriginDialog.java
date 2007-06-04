/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.client;

import magellan.client.event.EventDispatcher;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 312 $
 */
public class SetOriginDialog extends magellan.client.swing.InternationalizedDataDialog {
	private static final Logger log = Logger.getInstance(SetOriginDialog.class);
	/**
	 * if true, SetOriginAction initiate client.setOrigin
	 */
	private boolean approved = false;
	/**
	 * new Origin, entered eventually by the user
	 */
	private CoordinateID newOrigin = new CoordinateID(0,0,0);


	/**
	 * Creates new form SetOriginDialog
	 *
	 * @param parent the <code>Frame</code> from which the dialog is displayed
	 * @param ed The event dispatcher that this dialog should use
	 * @param _data The corresponding GameData
	 * 
 	 */
	public SetOriginDialog(java.awt.Frame parent, EventDispatcher ed, GameData _data) {
		super(parent, true, ed, _data, new java.util.Properties());
		initComponents();
		pack();
		approved = false;
	}

	private void initComponents() {
		btnOK = new javax.swing.JButton();
		btnCancel = new javax.swing.JButton();
		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		editX = new javax.swing.JTextField();
		editY = new javax.swing.JTextField();
		editLevel = new javax.swing.JTextField();
		getContentPane().setLayout(new java.awt.GridBagLayout());
		setTitle(Resources.get("magellan.setorigindialog.window.title"));

		java.awt.GridBagConstraints gridBagConstraints1;
		addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent evt) {
					quit();
				}
			});

		btnOK.setText(Resources.get("magellan.setorigindialog.btn.ok.caption"));
		btnOK.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					btnOKActionPerformed(evt);
				}
			});

		gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 5);
		getContentPane().add(btnOK, gridBagConstraints1);

		btnCancel.setText(Resources.get("magellan.setorigindialog.btn.cancel.caption"));
		btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					btnCancelActionPerformed(evt);
				}
			});

		gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridx = 2;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 5);
		getContentPane().add(btnCancel, gridBagConstraints1);

		gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.weightx = 1.0;
		getContentPane().add(jPanel1, gridBagConstraints1);

		jPanel2.setLayout(new java.awt.GridBagLayout());

		java.awt.GridBagConstraints gridBagConstraints2;

		jLabel1.setText(Resources.get("magellan.setorigindialog.lbl.x.caption") + ":");

		gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.insets = new java.awt.Insets(5, 5, 5, 5);
		gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
		jPanel2.add(jLabel1, gridBagConstraints2);

		jLabel2.setText(Resources.get("magellan.setorigindialog.lbl.y.caption") + ":");

		gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 2;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 5);
		gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
		jPanel2.add(jLabel2, gridBagConstraints2);

		jLabel3.setText(Resources.get("magellan.setorigindialog.lbl.z.caption") + ":");

		gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 4;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 5);
		gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
		jPanel2.add(jLabel3, gridBagConstraints2);

		editX.setPreferredSize(new java.awt.Dimension(55, 20));
		editX.setText("0");
		editX.setMinimumSize(new java.awt.Dimension(50, 20));

		gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 5);
		jPanel2.add(editX, gridBagConstraints2);

		editY.setPreferredSize(new java.awt.Dimension(55, 20));
		editY.setText("0");
		editY.setMinimumSize(new java.awt.Dimension(50, 20));

		gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 3;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 5);
		jPanel2.add(editY, gridBagConstraints2);

		editLevel.setPreferredSize(new java.awt.Dimension(55, 20));
		editLevel.setText("0");
		editLevel.setMinimumSize(new java.awt.Dimension(50, 20));

		gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 5;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 5);
		jPanel2.add(editLevel, gridBagConstraints2);

		gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridwidth = 3;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.weighty = 1.0;
		getContentPane().add(jPanel2, gridBagConstraints1);
	}

	private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {
		int iX;
		int iY;
		int iLevel;
		
		iX = Integer.parseInt(editX.getText());
		iY = Integer.parseInt(editY.getText());
		iLevel = Integer.parseInt(editLevel.getText());
		
		// setOrigin only, if new Origin is wanted...
		if (iX!=0 || iY!=0){
			approved = true;
			newOrigin = new CoordinateID(iX, iY, iLevel);
		}
		
		setVisible(false);
		dispose();
	}

	private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
		quit();
		approved = false;
	}

	private javax.swing.JButton btnOK;
	private javax.swing.JButton btnCancel;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JTextField editX;
	private javax.swing.JTextField editY;
	private javax.swing.JTextField editLevel;

	/**
	 * Returns if user has pressed the OK-Button.
	 * 
	 * @return true if OK-Button has been pressed
	 */
	public boolean approved() {
		return approved;
	}

	/**
	 * Return the selected new origin.
	 * 
	 * @return The coordinates of the new origin
	 */
	public CoordinateID getNewOrigin() {
		return newOrigin;
	}
}
