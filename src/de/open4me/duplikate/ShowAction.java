package de.open4me.duplikate;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class ShowAction implements Action
{

	public void handleAction(Object context) throws ApplicationException
	{

	  	GUI.startView(DuplikateView.class, null);
	}


}
