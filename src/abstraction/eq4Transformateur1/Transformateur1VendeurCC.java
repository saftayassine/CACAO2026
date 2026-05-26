/** @author Ewan Lefort */


package abstraction.eq4Transformateur1;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;

public class Transformateur1VendeurCC extends Transformateur1AcheteurBourse implements IVendeurContratCadre {
    
    protected Journal journalVenteCC;

    public Transformateur1VendeurCC() {
        super();
        this.journalVenteCC = new Journal(this.getNom()+" journal Vente CC", this);
    }
	public List<Journal> getJournaux() {
		List<Journal> jx=super.getJournaux();
		jx.add(journalVenteCC);
		return jx;
	}
    public boolean vend(IProduit produit){
        if (this.getStocksPrevuProduit(produit)>200 && this.getStocksProduit(produit)>200 && !produit.getType().equals("Feve")){
            return true;
        }
        else{
            return false;
        }
    }

    public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat){
        Echeancier e = contrat.getEcheancier();
        double quantite=e.getQuantiteTotale();
        if (quantite<this.getStocksPrevuProduit(contrat.getProduit()) && quantite<this.getStocksProduit(contrat.getProduit())){
            return e;
        }
        else{
            double quantitetotale= Double.min(this.getStocksProduit(contrat.getProduit()), this.getStocksPrevuProduit(contrat.getProduit()));
            int stepdebut= e.getStepDebut();
            int nbstep= e.getNbEcheances();
            double quantiteparstep= quantitetotale/nbstep;
            Echeancier newEcheancier = new Echeancier(stepdebut,nbstep,quantiteparstep);
            return newEcheancier;
        }
    }

    public double propositionPrix(ExemplaireContratCadre contrat){
        IProduit produit = contrat.getProduit();
            if (produit == this.ProntellaM){
                return 11000;
            }
            else if (produit == this.ProntellaB){
                return 9000;
            }
            else if (produit == this.ProntellaH){
                return 13000;
            }
            else if (produit == this.ProntellaBE){
                return 10000;
            }
            else if (produit == this.ProntellaHE){
                return 14000;
            }
            else{
                return 0.0;
                
            }
    }

    public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat){
        IProduit produit = contrat.getProduit();
        double prix= contrat.getPrix();
            if (produit == this.ProntellaM && prix>=9500){
                return prix;
            }
            else if (produit == this.ProntellaB && prix>=7500){
                return prix;
            }
            else if (produit == this.ProntellaH && prix>=11000){
                return prix;
            }
            else if (produit == this.ProntellaBE && prix>=8500){
                return prix;
            }
            else if (produit == this.ProntellaHE && prix>=12000){
                return prix;
            }
            else{
                return 0.0;
            }
    }

    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat){
        if (contrat.getVendeur().equals(this)){
            journalVenteCC.ajouter("Nouveau contrat cadre : "+contrat.toString());
        this.setStocksPrevuProduit(contrat.getProduit(),this.getStocksPrevuProduit(contrat.getProduit())-contrat.getQuantiteTotale());
        }        
    }


    public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat){
       if (this.getStocksProduit(produit)>=quantite){
        this.setStocksProduit(produit, this.getStocksProduit(produit)-quantite);
        this.Vente(produit, quantite);
        return quantite;
       }
       else if (this.getStocksProduit(produit)<quantite && this.getStocksProduit(produit)>0){
        double quantiteLivree= this.getStocksProduit(produit);
        this.setStocksProduit(produit, 0);
        this.Vente(produit, quantiteLivree);
        return quantiteLivree;
       }
       else{
           return 0;
       }
    }

    public void next(){
        super.next();
    SuperviseurVentesContratCadre sup =null;
    sup= (SuperviseurVentesContratCadre)(Filiere.LA_FILIERE.getActeur("Sup.CCadre"));
    for ( ChocolatDeMarque cm:this.getChocolatsProduits() ){
        List<IAcheteurContratCadre> acheteurs= sup.getAcheteurs(cm);
        if (this.getStocksProduit(cm)>200 && this.getStocksPrevuProduit(cm)>200 && this.getStocksPrevuProduit(cm)<200000){
        Echeancier e= new Echeancier(Filiere.LA_FILIERE.getEtape()+1,2,(Double.min(this.getStocksProduit(cm),this.getStocksPrevuProduit(cm)))/2);
        if (!acheteurs.isEmpty()) {
        ExemplaireContratCadre contrat=sup.demandeVendeur(acheteurs.get(0), this, cm, e, cryptogramme, true);
        if (! (contrat==null)){
            this.setStocksPrevuProduit(contrat.getProduit(),this.getStocksPrevuProduit(contrat.getProduit())-contrat.getQuantiteTotale());
        }
        }
    }
    }
    
    }
}