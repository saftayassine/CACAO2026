package abstraction.eq7Transformateur4;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.produits.IProduit;

//Auteur -> Aymeric
public class Transformateur4Vente extends Transformateur4Production implements IVendeurContratCadre {

    @Override
    public boolean vend(IProduit produit) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {
        // TODO Auto-generated method stub
        Echeancier echeance= new Echeancier(0);
        return echeance;
    }

    @Override
    public double propositionPrix(ExemplaireContratCadre contrat) {
        // TODO Auto-generated method stub
        return 0.;
    }

    @Override
    public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat) {
        // TODO Auto-generated method stub
        return 0.;
    }

    @Override
    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
        //TODO Auto-generated method stub
    
    }

    @Override
    public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {
        // TODO Auto-generated method stub
        return 0.;
    }
    
}