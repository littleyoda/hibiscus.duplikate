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

	String[] einstufung =  new String[] {"sehr niedrig", "niedrig", "mittel", "hoch", "sehr hoch"};
	Integer[][] einstunfengrenzen = new Integer[][] { { Integer.MIN_VALUE,  10} , {11, 30} , {31, 70 }, { 71, 90}, {91, Integer.MAX_VALUE} };
	private void check(Konto k, ArrayList<GenericObjectHashMap> list) throws RemoteException {
		System.out.println("Konto " + k.getName());
		DBIterator liste = DuplikatePlugin.getDBService().createList(Umsatz.class);
		liste.addFilter("konto_id=" + k.getID());
		liste.setOrder("order by datum,betrag,id desc");
		Umsatz lastUmsatz = null;
		while (liste.hasNext()) {
			Umsatz u = (Umsatz) liste.next();
			if (lastUmsatz != null) {
				int aehnlichkeit = getAehnlichkeit(u, lastUmsatz);
				if (aehnlichkeit <= einstunfengrenzen[0][1]) {
					lastUmsatz = u;
					continue;
				}
				int id = (list.size() / 2) + 1;
				for ( Umsatz x : new Umsatz[] {u, lastUmsatz } ) {
					GenericObjectHashMap g = new GenericObjectHashMap();
					g.setAttribute("AID", id);
					g.setAttribute("Konto", k.getName() + " (" + k.getBezeichnung() + ", " + k.getIban() + ") ");
					g.setAttribute("Umsatz ID", x.getID());
					g.setAttribute("Datum", x.getDatum());
					g.setAttribute("Betrag", x.getBetrag());
					g.setAttribute("Kategorie", x.getUmsatzTyp());
					g.setAttribute("Verwendungszweck", Utils.getVerwendungszwecke(x));
					g.setAttribute("Ähnlichkeit", getEinstufung(aehnlichkeit));
					g.setAttribute("Notizen", x.getAttribute("kommentar"));
					list.add(g);
				}

			}
			lastUmsatz = u;
		}
	}

	private String getEinstufung(int a) {
		for (int i = 0; i < einstufung.length; i++) {
			Integer[] grenzen = einstunfengrenzen[i];
			if (grenzen[0] >= a && a <= grenzen[1]) {
				return einstufung[i] + "(" + a + ")";
			}
		}
		return null;
	}
	private int getAehnlichkeit(Umsatz u, Umsatz lastUmsatz) throws RemoteException {
		// Die Grundlagen testen. 
		// Betrag, Datum, Gegenkonto und Konto muss gleich sein. Und der Betrag darf nicht gleich 0 sein.
		String gk1 = u.getGegenkontoNummer() == null ? "1" : u.getGegenkontoNummer(); 
		String gk2 = lastUmsatz.getGegenkontoNummer() == null ? "1" : lastUmsatz.getGegenkontoNummer(); 
		boolean anschauen = ((Math.abs(u.getBetrag()) > 0.009 
				&& (Math.abs(u.getBetrag()  - lastUmsatz.getBetrag()) < 0.01) 
				&&  (u.getDatum().toString().equals(lastUmsatz.getDatum().toString()))
				&& (u.getKonto().getID() == lastUmsatz.getKonto().getID())
				&& (gk1.equals(gk2))
				));
		if (!anschauen) {
			return Integer.MIN_VALUE;
		}
		int stufe = 20;
		int counter = 50;
		int id1 = Integer.parseInt(u.getID());
		int id2 = Integer.parseInt(lastUmsatz.getID());
		int abstand = Math.abs(id1 - id2);
		String bz1 = Utils.getVerwendungszwecke(u);
		String bz2 = Utils.getVerwendungszwecke(lastUmsatz);
		if (bz1.isEmpty() || bz2.isEmpty()) {
			return einstunfengrenzen [0][0];
		}
		// Verwendungszweck auf echte Gleichheit testen
		if (bz1.equals(bz2)) {
			if (abstand < 2) {
				// Wenn die beiden Buchungen direkt hintereinander eingetragen sind, ist es unwahrschienlich, dass es ein Duplikat ist 
				counter -= stufe;
			} else {
				counter += stufe;
			}
		} else {
			// Verwendungszweck auf Gleichheit testen, wenn alle Leerzeichen und Zeilenumbrüche entfernt wurden.
			bz1 = bz1.replace("\n", "").replace(" ", "");
			bz2 = bz2.replace("\n", "").replace(" ", "");
			if (bz1.equals(bz2)) {
				counter += stufe;
			} else if (bz1.startsWith(bz2) || bz2.startsWith(bz1)) {
				// Test auf abgekürzte Verwendungszwecke
				counter += stufe;
			} else {
				counter -= stufe; // Abwertung für keine Ähnlichkeit
			}
			
		}
		// Wenn der Saldo unterschiedlich ist, abwerten, da Duplikate unwahrscheinlicher
		if ((Math.abs(u.getSaldo() - lastUmsatz.getSaldo()) > 0.001)) {
			counter -= stufe;
		}
		return counter;
	}

}