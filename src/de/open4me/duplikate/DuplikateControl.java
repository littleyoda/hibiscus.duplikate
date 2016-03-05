package de.open4me.duplikate;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;

public class DuplikateControl extends AbstractControl
{

	private TablePart table;
	private List list;

	public void setList(List newList) throws RemoteException {
		System.out.println("Update");
		table.removeAll();
		for (Object x : newList) {
			table.addItem(x);
		}
	}
	
	public DuplikateControl(DuplikateView view, List list) {
		super(view);
		this.list = list;
	}


	public Part getControl() throws RemoteException
	{
		if (table != null) {
			return table;
		}

		table = new TablePart(list,new Action() {

			@Override
			public void handleAction(Object context)
					throws ApplicationException {
				List<Umsatz> um;
				try {
					um = Utils.getUmsatz(context);
					if (um.size() > 0) {
						UmsatzDetail ud = new UmsatzDetail();
						ud.handleAction(um.get(0));
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} });
		table.setMulti(true);
		if (list.size() > 0) {
			GenericObjectHashMap obj = (GenericObjectHashMap) list.get(0);
			ArrayList<String> entries = new ArrayList<String>(Arrays.asList(obj.getAttributeNames()));
			Collections.sort(entries);
			for (String o : entries) {
				table.addColumn(o, o);
			}
			table.setContextMenu(new DuplikateMenu(table, (DuplikateView) view));
		}
		return table;
	}
}