package de.open4me.duplikate;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;

public class Utils {
	
	static List<Umsatz> getUmsatz(Object context) throws RemoteException {
		List<GenericObjectHashMap> list = new ArrayList<GenericObjectHashMap>();
		if (context instanceof GenericObjectHashMap) {
			list.add((GenericObjectHashMap) context);
		} else if (context instanceof GenericObjectHashMap[]) {
			GenericObjectHashMap[] array = (GenericObjectHashMap[]) context;
			list.addAll(Arrays.asList(array));
		}
		List<Umsatz> out = new ArrayList<Umsatz>();
		for (GenericObjectHashMap ghm : list) {
			DBIterator liste = DuplikatePlugin.getDBService().createList(Umsatz.class);
			liste.addFilter("id=" + ghm.getAttribute("Umsatz ID"));
			if (liste.hasNext()) {
				out.add((Umsatz) liste.next());
			}
		}
		return out;
	}
	
	static String getVerwendungszwecke(Umsatz x) throws RemoteException {
		return (x.getZweck() == null ? "" : x.getZweck() + "\n") +  
		(x.getZweck2() == null ? "" : x.getZweck2() + "\n") +  
		((x.getWeitereVerwendungszwecke() == null || x.getWeitereVerwendungszwecke().length == 0) ? "" : VerwendungszweckUtil.merge(x.getWeitereVerwendungszwecke()));  
	}

}
