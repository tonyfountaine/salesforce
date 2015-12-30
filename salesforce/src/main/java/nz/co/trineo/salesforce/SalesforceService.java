package nz.co.trineo.salesforce;

import nz.co.trineo.common.CredentalsDAO;
import nz.co.trineo.common.model.Credentals;

public class SalesforceService {
	private final CredentalsDAO dao;

	public SalesforceService(final CredentalsDAO dao) {
		this.dao = dao;
	}

	public Credentals currentCredentals() {
		return dao.get("salesforce");
	}

	public Credentals updateCredentals(final Credentals credentals) {
		credentals.setId("salesforce");
		dao.persist(credentals);
		return currentCredentals();
	}
}
