package abstraction.eqXRomu.appelDOffre;

import abstraction.eqXRomu.produits.IProduit;

public class OffreVente implements Comparable<OffreVente>{
	private AppelDOffre offre;
	private IVendeurAO vendeur;
	private IProduit produit;
	private double prixT;
	
	public OffreVente(AppelDOffre offre, IVendeurAO vendeur, IProduit produit, double prixT) {
		this.offre = offre;
		this.vendeur = vendeur;
		this.produit = produit;
		this.prixT = prixT;
	}

	public AppelDOffre getOffre() {
		return offre;
	}

	public IVendeurAO getVendeur() {
		return vendeur;
	}
	
	public IProduit getProduit() {
		return this.produit;
	}

	public double getPrixT() {
		return prixT;
	}
	
	public double getQuantiteT() { // delegation
		return offre.getQuantiteT();
	}

	public String toString() {
		return "["+offre+" v="+vendeur+" choco="+produit+" px="+prixT+"]";
	}
	
	public int compareTo(OffreVente o) {
		return this.getPrixT()<o.getPrixT()? -1 : (this.getPrixT()==o.getPrixT()? 0 : 1);
	}
}
