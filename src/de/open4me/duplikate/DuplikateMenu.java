package de.open4me.duplikate;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;

/**
 */
public class DuplikateMenu extends ContextMenu
{
	private TablePart tablePart;
	
	
	
	public DuplikateMenu(TablePart orderList, final DuplikateView view) {
		this.tablePart = orderList;
		addItem(new CheckedContextMenuItem("Anzeigen",new Action() {

			@Override
			public void handleAction(Object context)
					throws ApplicationException {
				try {
					List<Umsatz> um = Utils.getUmsatz(context);
					if (um.size() > 0) {
						UmsatzDetail ud = new UmsatzDetail();
						ud.handleAction(um.get(0));
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}));
		addItem(new CheckedContextMenuItem("Löschen",new Action() {

			@Override
			public void handleAction(Object context)
					throws ApplicationException {
				try {
					List<Umsatz> um = Utils.getUmsatz(context);
					for (Umsatz x : um) {
						x.delete();
					}
					view.aktualisiere();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}));
	}

}

