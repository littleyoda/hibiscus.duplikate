package de.open4me.duplikate;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;

public class DuplikateView extends AbstractView
{
	private DuplikateControl control;
	
	private ArrayList<GenericObjectHashMap> list = new ArrayList<GenericObjectHashMap>(); 
	/**
	 * @see de.willuhn.jameica.gui.AbstractView#bind()
	 */
	public void bind() throws Exception {



		GUI.getView().setTitle("");
		aktualisiere();
		control = new DuplikateControl(this, list);

		control.getControl().paint(this.getParent());


	}

	public void aktualisiere() throws RemoteException {
		list.clear();
		DBIterator liste;
		try {
			liste = DuplikatePlugin.getDBService().createList(Konto.class);
			while (liste.hasNext()) {
				Konto k = (Konto) liste.next();
				check(k, list);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (control != null) {
			control.setList(list);
		}
	}
	
	private void check(Konto k, ArrayList<GenericObjectHashMap> list) throws RemoteException {
		DBIterator liste = DuplikatePlugin.getDBService().createList(Umsatz.class);
		liste.addFilter("konto_id=" + k.getID());
		liste.setOrder("order by datum,betrag,id desc");
		Umsatz lastUmsatz = null;
		while (liste.hasNext()) {
			Umsatz u = (Umsatz) liste.next();
			if (lastUmsatz != null) {
				if ((Math.abs(u.getBetrag()) > 0.009 
						&& (Math.abs(u.getBetrag()  - lastUmsatz.getBetrag()) < 0.01) 
						&&  (u.getDatum().toString().equals(lastUmsatz.getDatum().toString()))
						&& (u.getKonto().getID() == lastUmsatz.getKonto().getID())
						)) {
					int id = (list.size() / 2) + 1;
					String aehnlichkeit = getAehnlichkeit(u, lastUmsatz);
					for ( Umsatz x : new Umsatz[] {u, lastUmsatz } ) {
						GenericObjectHashMap g = new GenericObjectHashMap();
						g.setAttribute("AID", id);
						g.setAttribute("Konto", k.getName() + " (" + k.getBezeichnung() + ", " + k.getIban() + ") ");
						g.setAttribute("Umsatz ID", x.getID());
						g.setAttribute("Datum", x.getDatum());
						g.setAttribute("Betrag", x.getBetrag());
						g.setAttribute("Kategorie", x.getUmsatzTyp());
						g.setAttribute("Verwendungszweck", Utils.getVerwendungszwecke(x));
						g.setAttribute("Ähnlichkeit", aehnlichkeit);
						g.setAttribute("Notizen", x.getAttribute("kommentar"));
						list.add(g);
					}

				}
			}
			lastUmsatz = u;
		}
	}

	private String getAehnlichkeit(Umsatz u, Umsatz lastUmsatz) throws RemoteException {
		int id1 = Integer.parseInt(u.getID());
		int id2 = Integer.parseInt(lastUmsatz.getID());
		int abstand = Math.abs(id1 - id2);
		String bz1 = Utils.getVerwendungszwecke(u);
		String bz2 = Utils.getVerwendungszwecke(lastUmsatz);
		if (bz1.isEmpty() || bz2.isEmpty()) {
			return "Niedrig";
		}
		if (bz1.equals(bz2)) {
			if (abstand < 2) {
				return "Niedrig";
			}
			return "Sehr Hoch";
		} 
		bz1 = bz1.replace("\n", "").replace(" ", "");
		bz2 = bz2.replace("\n", "").replace(" ", "");
		if (bz1.equals(bz2)) {
			return "Hoch";
		}
		if (bz1.startsWith(bz2) || bz2.startsWith(bz1)) {
			return "Hoch";
		}
		return "Mittel";
	}

}