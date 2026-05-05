package abstraction.eq2Producteur2;

/** @author Simon */
import abstraction.eqXRomu.appelDOffre.AppelDOffre;
import abstraction.eqXRomu.appelDOffre.IVendeurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;

public class Producteur2VendeurAO extends Producteur2VendeurCC implements IVendeurAO {
    protected Journal journalAO;

    public Producteur2VendeurAO() {
        super();
        this.journalAO = new Journal("Journal AO Eq2", this);
    }

    @Override
    public java.util.List<Journal> getJournaux() {
        java.util.List<Journal> res = super.getJournaux();
        res.add(this.journalAO);
        return res;
    }

    @Override
    public OffreVente proposerVente(AppelDOffre offre) {
        if (!(offre.getProduit() instanceof Feve)) {
            return null;
        }
        Feve f = (Feve) offre.getProduit();

        // On vérifie si on a assez de stock disponible
        double quantiteDemandee = offre.getQuantiteT();
        double disponible = this.stockvar.get(f).getValeur(this.cryptogramme) - this.restantDu(f);

        if (disponible >= quantiteDemandee) {
            double coutProd = this.cout_unit_t.getOrDefault(f, 0.0);

            double marge = f.isEquitable() ? 1.20 : 1.05;
            double prixPropose = coutProd * marge;

            this.journalAO
                    .ajouter("Proposition Vente AO : " + quantiteDemandee + "T de " + f + " à " + prixPropose + "€/T");
            return new OffreVente(offre, this, f, prixPropose);
        }
        return null; // Pas assez de stock pour répondre à l'AO
    }

    @Override
    public void notifierVenteAO(OffreVente propositionRetenue) {
        Feve f = (Feve) propositionRetenue.getProduit();
        double quantite = propositionRetenue.getQuantiteT();

        // Retirer la quantité du stock
        this.retirerDuStock(f, quantite);

        if (this.stocks.containsKey(f) && this.stockvar.containsKey(f)) {
            this.stocks.get(f).setValeur(this, this.stockvar.get(f).getValeur());
        }

        this.journalAO.ajouter("Vente AO réussie : " + quantite + "T de " + f + " au prix de "
                + propositionRetenue.getPrixT() + "€/T");
    }

    @Override
    public void notifierPropositionNonRetenueAO(OffreVente propositionRefusee) {
        this.journalAO.ajouter("Vente AO échouée pour " + propositionRefusee.getQuantiteT() + "T de "
                + propositionRefusee.getProduit());
    }
}
