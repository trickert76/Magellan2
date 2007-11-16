// class magellan.client.Help
// created on 15.11.2007
//
// Copyright 2003-2007 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;

import javax.swing.JOptionPane;

import magellan.library.utils.ResourcePathClassLoader;

/**
 * This class is a help tool from outside Magellan. It will be startet via
 * a shortcut in the windows start menu, that was created by the installer.
 *
 * @author Thoralf Rickert
 * @version 1.0, 15.11.2007
 */
public class Help {
  public static void open(String[] args) {
    
    try {
      ClassLoader loader = new ResourcePathClassLoader(System.getProperties());
      URL hsURL = loader.getResource("help/magellan.hs");
      
      if (hsURL == null) hsURL = loader.getResource("magellan.hs");
      if (hsURL == null) {
        JOptionPane.showMessageDialog(null, "Could not find the magellan-help.jar");
        return;
      }
      
      Class helpSetClass = null;
      Class helpBrokerClass = null;
      
      try {
        helpSetClass = Class.forName("javax.help.HelpSet", true, ClassLoader.getSystemClassLoader());
        Class.forName("javax.help.CSH$DisplayHelpFromSource", true, ClassLoader.getSystemClassLoader());
        helpBrokerClass = Class.forName("javax.help.HelpBroker", true, ClassLoader.getSystemClassLoader());
      } catch (ClassNotFoundException ex) {
        JOptionPane.showMessageDialog(null, "Could nor find the Java Help environment.");
  
        return;
      }
      
      Class helpSetConstructorSignature[] = { Class.forName("java.lang.ClassLoader"), hsURL.getClass() };
      Constructor helpSetConstructor = helpSetClass.getConstructor(helpSetConstructorSignature);
      Object helpSetConstructorArgs[] = { loader, hsURL };
  
      // this calls new javax.help.Helpset(ClassLoader, URL)
      Object helpSet = helpSetConstructor.newInstance(helpSetConstructorArgs);
  
      Method helpSetCreateHelpBrokerMethod = helpSetClass.getMethod("createHelpBroker", (Class[])null);
  
      // this calls new javax.help.Helpset.createHelpBroker()
      Object helpBroker = helpSetCreateHelpBrokerMethod.invoke(helpSet, (Object[])null);
  
      Method initPresentationMethod = helpBrokerClass.getMethod("initPresentation", (Class[])null);
      // this calls new javax.help.HelpBroker.initPresentation()
      initPresentationMethod.invoke(helpBroker, (Object[])null);
  
      Class setDisplayedMethodSignature[] = { boolean.class };
      Method setDisplayedMethod = helpBroker.getClass().getMethod("setDisplayed", setDisplayedMethodSignature);
      Object setDisplayedMethodArgs[] = { Boolean.TRUE };
  
      // this calls new javax.help.HelpBroker.setDisplayed(true)
      setDisplayedMethod.invoke(helpBroker, setDisplayedMethodArgs);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Could nor initialize the Java Help environment.");
    }
  }
}
