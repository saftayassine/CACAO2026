package abstraction.eq6Transformateur3;

import java.util.List;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

//@author: Le Clezio Brevael

public class Transformateur3VendeurCCadre extends Transformateur3AcheteurCCadre implements IVendeurContratCadre{
    protected Journal journalVente;

public Transformateur3VendeurCCadre() {
    super(); // très important

    this.journalVente = new Journal("Journal Vente CC EQ6", this);
}

public boolean vend(IProduit produit) {
    return produit == Chocolat.C_MQ_E || produit == Chocolat.C_HQ_E || this.stockchocomarque.containsKey(produit);
}



public List<Journal> getJournaux() {
    List<Journal> res = super.getJournaux();
    res.add(this.journalVente);
    return res;
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

    IProduit produit = contrat.getProduit();
    double stockDisponible;

    if (produit instanceof ChocolatDeMarque) {
        stockDisponible = stockchocomarque.getOrDefault((ChocolatDeMarque) produit, 0.0);
    } else {
        stockDisponible = stockChocolat.getQuantite((Chocolat) produit);
    }

    double pourcentage = contrat.getQuantiteTotale() / (stockDisponible - totalEngagement(produit));

    if (produit == Chocolat.C_MQ_E) return 11000;
    if (produit == Chocolat.C_HQ_E) return 18000;

    if (produit instanceof ChocolatDeMarque) {
        ChocolatDeMarque chocoMarque = (ChocolatDeMarque) produit;
        double prixBase = 0.0;
        switch (chocoMarque.getChocolat()) {
            case C_MQ_E: prixBase = 11000; break;
            case C_HQ_E: prixBase = 18000; break;
        }
        return prixBase + (1 - pourcentage) * 2000;
    }

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

    if (produit instanceof Chocolat) {
        Chocolat choco = (Chocolat) produit;
        double dispo = stockChocolat.getQuantite(choco);
        double livrable = Math.min(dispo, quantite);
        stockChocolat.retirerQuantite(choco, (int) livrable);
        return livrable;
    } else if (produit instanceof ChocolatDeMarque) {
        ChocolatDeMarque chocoMarque = (ChocolatDeMarque) produit;
        double dispo = stockchocomarque.getOrDefault(chocoMarque, 0.0);
        double livrable = Math.min(dispo, quantite);
        stockchocomarque.put(chocoMarque, dispo - livrable);
        return livrable;
    } else {
        return 0.0;
    }
}



}
