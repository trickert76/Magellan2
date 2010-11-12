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

package magellan.client.swing;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * A Dialog that asks the user for a string input (usually an eressea order). In Addition the user
 * is asked, if the order shall extend or replace existing orders.
 * 
 * @author Ulrich K�ster
 */
public class GiveOrderDialog extends InternationalizedDialog {
  private static final Logger log = Logger.getInstance(GiveOrderDialog.class);

  public static final String FIRST_POS = "first";
  public static final String LAST_POS = "last";

  private ButtonGroup position;
  private JCheckBox replaceOrders;
  private JCheckBox keepComments;
  private JTextArea order;
  private JButton ok;
  private JButton cancel;

  /**
   * Creates a new GiveOrderDialog object.
   */
  public GiveOrderDialog(Frame owner, String caption) {
    super(owner, true);
    setTitle(Resources.get("giveorderdialog.window.title"));

    Container cp = getContentPane();
    cp.setLayout(new GridBagLayout());

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
            new Insets(3, 3, 3, 3), 0, 0);

    c.gridwidth = 2;
    JLabel captionLabel = new JLabel(caption);
    captionLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
    captionLabel.setHorizontalAlignment(SwingConstants.CENTER);
    captionLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    cp.add(captionLabel, c);

    c.gridwidth = 1;
    c.gridy++;

    cp.add(new JLabel(Resources.get("giveorderdialog.window.message")), c);

    order = new JTextArea(3, 25);
    // order.setPreferredSize(new Dimension(200, 75));

    JScrollPane helperPane = new JScrollPane(order);

    c.gridx = 1;
    c.weightx = 0.5;
    cp.add(helperPane, c);

    JRadioButton firstButton = new JRadioButton(Resources.get("giveorderdialog.radio.first.title"));
    firstButton.setActionCommand(GiveOrderDialog.FIRST_POS);
    JRadioButton lastButton = new JRadioButton(Resources.get("giveorderdialog.radio.last.title"));
    lastButton.setActionCommand(GiveOrderDialog.LAST_POS);
    position = new ButtonGroup();
    position.add(firstButton);
    position.add(lastButton);
    position.setSelected(firstButton.getModel(), true);
    c.gridx = 0;
    c.gridy++;
    c.weightx = 0;
    c.anchor = GridBagConstraints.WEST;
    cp.add(firstButton, c);
    c.gridx = 1;
    c.weightx = 0;
    c.anchor = GridBagConstraints.EAST;
    cp.add(lastButton, c);

    replaceOrders = new JCheckBox(Resources.get("giveorderdialog.chkbox.replaceOrder.title"));
    c.gridx = 0;
    c.gridy++;
    c.weightx = 0;
    cp.add(replaceOrders, c);

    keepComments = new JCheckBox(Resources.get("giveorderdialog.chkbox.keepComments.title"));
    keepComments.setSelected(true);
    c.gridy++;
    cp.add(keepComments, c);

    ok = new JButton(Resources.get("giveorderdialog.btn.ok.caption"));
    ok.setMnemonic(Resources.get("giveorderdialog.btn.ok.mnemonic").charAt(0));

    // actionListener is added in the show() method
    c.gridy++;
    c.anchor = GridBagConstraints.EAST;
    cp.add(ok, c);

    cancel = new JButton(Resources.get("giveorderdialog.btn.cancel.caption"));
    cancel.setMnemonic(Resources.get("giveorderdialog.btn.cancel.mnemonic").charAt(0));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit();
      }
    });
    c.gridx = 1;
    c.anchor = GridBagConstraints.WEST;
    cp.add(cancel, c);
  }

  /**
   * Shows the dialog.
   * 
   * @return A string array with the following values: <br/>
   *         [0] : The order that was given <br/>
   *         [1] : A String representative of the boolean value for "Replace orders" <br/>
   *         [2] : A String representative of the boolean value for "Keep comments" <br/>
   *         [3] : One of {@link GiveOrderDialog#FIRST_POS}, {@link GiveOrderDialog#LAST_POS}
   */
  public String[] showGiveOrderDialog() {
    final String retVal[] = new String[4];
    ActionListener okButtonAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        retVal[0] = order.getText();
        retVal[1] = String.valueOf(replaceOrders.isSelected());
        retVal[2] = String.valueOf(keepComments.isSelected());
        retVal[3] = String.valueOf(position.getSelection().getActionCommand());
        GiveOrderDialog.log.debug(position.getSelection() + " " + retVal[3]);
        quit();
      }
    };

    ok.addActionListener(okButtonAction);
    // order.addActionListener(okButtonAction);
    pack();
    setLocationRelativeTo(getOwner());
    setVisible(true);

    return retVal;
  }
}
