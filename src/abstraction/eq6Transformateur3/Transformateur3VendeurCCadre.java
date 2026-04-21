package abstraction.eq6Transformateur3;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.IProduit;

public class Transformateur3VendeurCCadre extends Transformateur3AcheteurCCadre implements IVendeurContratCadre{


public boolean vend(IProduit produit) {
    return produit == Chocolat.C_MQ_E || produit == Chocolat.C_HQ_E;
}


public double totalEngagement(IProduit produit) {
    double total = 0.0;

    for (ExemplaireContratCadre c : contratsEnCours) {
        if (c.getProduit().equals(produit)) {
            total += c.getQuantiteRestantALivrer();
        }
    }

    return total;
}

    @Override
public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {

    Chocolat produit = (Chocolat) contrat.getProduit();

    if (stockChocolat.getQuantite(produit) - totalEngagement(produit) > contrat.getQuantiteTotale()) {
        return contrat.getEcheancier();
    }

    return null;
}

public double propositionPrix(ExemplaireContratCadre contrat) {

    Chocolat produit = (Chocolat) contrat.getProduit();

    if (produit == Chocolat.C_MQ_E) return 11000;
    if (produit == Chocolat.C_HQ_E) return 18000;

    return 10000;
}

    public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat) {
        if (contrat.getPrix()-contrat.getListePrix().get(0)<250) {
            return contrat.getPrix();
        } else {
            return contrat.getListePrix().get(0)-100;
        }
    }


public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {

    Chocolat choco = (Chocolat) produit;

    double dispo = stockChocolat.getQuantite(choco);

    double livrable = Math.min(dispo, quantite);

    stockChocolat.retirerQuantite(choco, (int) livrable);

    return livrable;
}
}
