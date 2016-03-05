package de.open4me.duplikate;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import de.willuhn.datasource.GenericObject;

public class GenericObjectHashMap implements GenericObject {

	protected Map<?, ?> map;

	public Map<?, ?> getMap() {
		return map;
	}
	
	public GenericObjectHashMap() {
		map = new HashMap<Object, Object>();
	}

	public GenericObjectHashMap(Map<?, ?> map) {
		this.map = map;
	}
	@Override
	public Object getAttribute(String name) throws RemoteException {
		return map.get(name);
	}

	@SuppressWarnings("unchecked")
	public void setAttribute(String key, Object value)  {
		((Map<Object, Object>) getMap()).put(key, value);
	}

	@Override
	public String[] getAttributeNames() throws RemoteException {
		HashMap hm = (HashMap) map;
		return map.keySet().toArray(new String[0]);
	}

	@Override
	public String getID() throws RemoteException {
		return null;
	}

	@Override
	public String getPrimaryAttribute() throws RemoteException {
		return null;
	}

	@Override
	public boolean equals(GenericObject other) throws RemoteException {
		if (!(other instanceof GenericObjectHashMap)) {
			return false;
		}
		return getMap().equals(((GenericObjectHashMap) other).getMap());
	}
	
	@Override
	public String toString() {
		return getMap().entrySet().toString();
	}

}
