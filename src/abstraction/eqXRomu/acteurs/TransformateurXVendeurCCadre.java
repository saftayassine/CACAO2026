package abstraction.eqXRomu.acteurs;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

public class TransformateurXVendeurCCadre extends TransformateurXAcheteurCCadre implements IVendeurContratCadre{

    public boolean vend(IProduit produit) {
       return (this.chocolatsVillors.contains(produit));
    }

    public double totalEngagement(IProduit produit) {
        double total=0.0;
        for (ExemplaireContratCadre c : this.contratsEnCours) {
            if (c.getProduit().equals(produit)) {
                total=total+c.getQuantiteRestantALivrer();
            }
        }
        return total;
    }
    @Override
    public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {
       if (stockChocoMarque.get((ChocolatDeMarque)(contrat.getProduit()))-totalEngagement(contrat.getProduit())>contrat.getQuantiteTotale()) {
        return contrat.getEcheancier();
       } else {
        return null; 
       }
    }

    public double propositionPrix(ExemplaireContratCadre contrat) {
        double pourcentage = contrat.getQuantiteTotale()/ stockChocoMarque.get((ChocolatDeMarque)(contrat.getProduit()))-totalEngagement(contrat.getProduit());
        ChocolatDeMarque cm = (ChocolatDeMarque)(contrat.getProduit());
        double prixBase=34000;
        switch (cm.getChocolat()) {
            case C_BQ: prixBase=6500;break;
            case C_BQ_E: prixBase=7500;break;
            case C_MQ  : prixBase=9000;break;
            case C_MQ_E: prixBase=11000;break;
            case C_HQ: prixBase=16000;break;
            case C_HQ_E: prixBase=18000;break;
        }
        return prixBase+(1-pourcentage)*2000;
     }

    public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat) {
        if (contrat.getPrix()-contrat.getListePrix().get(0)<250) {
            return contrat.getPrix();
        } else {
            return contrat.getListePrix().get(0)-100;
        }
    }

    public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {
        double livrable = (stockChocoMarque.get(produit)>=quantite) ? quantite : stockChocoMarque.get(produit);
        this.stockChocoMarque.put((ChocolatDeMarque)produit,stockChocoMarque.get(produit)-livrable );
        return livrable;
    }

}
