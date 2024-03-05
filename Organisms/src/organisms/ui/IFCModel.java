//***********************************************************
//*
//* File:           IFCModel.java
//* Author:         Abhinav Kamra
//* Contact:        kamra-at-cs.columbia.edu
//* Update:         9.5.2003
//*
//* Description:    Model interface for all projects.
//*                 Rather than separate the model from
//*                 the view, the views are delegated
//*                 by the game models.
//*
//***********************************************************

package organisms.ui;

import java.io.Serializable;
import javax.swing.*;

public abstract class IFCModel implements Serializable {
    
    public abstract JPanel           exportViewPanel()               throws Exception;
    public abstract  JPanel           exportControlPanel()            throws Exception;
    protected  abstract IFCConfiguration exportConfiguration()           throws Exception;
    public  abstract JComponent[]        exportTools()                   throws Exception;   
    public  abstract JMenu            exportMenu()                    throws Exception;
    public  abstract void             register(IFCUI __UI)            throws Exception;
    public  abstract String           name()                          throws Exception;
    public  abstract void             run(IFCTournament __tournament) throws Exception;
}
