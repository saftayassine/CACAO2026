/** @author Ewan Lefort */


package abstraction.eq4Transformateur1;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;

public class Transformateur1VendeurCC extends Transformateur1AcheteurBourse implements IVendeurContratCadre {
    
    
    public boolean vend(IProduit produit){
        if (this.getStocksProduit(produit)>200 && !produit.getType().equals("Feve")){
            return true;
        }
        else{
            return false;
        }
    }

    public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat){
        Echeancier e = contrat.getEcheancier();
        return e;
    }

    public double propositionPrix(ExemplaireContratCadre contrat){
        return 9000;
    }

    public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat){
        return contrat.getPrix();
    }

    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat){

    }


    public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat){
        if (this.getStocksProduit(produit)<0){
            return 0;
        }
        else if (this.getStocksProduit(produit)>=quantite){
        this.setStocksProduit(produit, this.getStocksProduit(produit)-quantite);
        return quantite;
        }
        else{
            double alivrer=this.getStocksProduit(produit);
            this.setStocksProduit(produit, 0);
            return alivrer;
        }
    }

    public void next(){
        super.next();
    SuperviseurVentesContratCadre sup =null;
    sup= (SuperviseurVentesContratCadre)(Filiere.LA_FILIERE.getActeur("Sup.CCadre"));
    List<IAcheteurContratCadre> acheteurs= sup.getAcheteurs(ProntellaM);
    if (this.getStocksProduit(ProntellaM)>200){
    Echeancier e= new Echeancier(Filiere.LA_FILIERE.getEtape()+1,2,this.getStocksProduit(ProntellaM)/2);
        if (!acheteurs.isEmpty()) {
        sup.demandeVendeur(acheteurs.get(0), this, ProntellaM, e, cryptogramme, false);
        }
    }
    }
}