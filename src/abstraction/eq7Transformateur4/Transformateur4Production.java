package abstraction.eq7Transformateur4;
import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IFabricantChocolatDeMarque;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
//Auteur : Matteo
public class Transformateur4Production extends Transformateur4Marques implements IFabricantChocolatDeMarque{
    public Transformateur4Production(){
        super();
    }

    public void production(double quantity, Gamme gamme, double cacao_pourcentage){
        //quantity est en tonne
        //Verification
        assert cacao_pourcentage>0.45;
        //Calcul qualité
        double quality=0.;
        if (gamme==Gamme.BQ){
            quality=cacao_pourcentage/100. + 3*0.45;
            //assert this.get_LQ().getValeur()>quantity*cacao_pourcentage/100.;}*
        }
        else{
            if (gamme==Gamme.MQ){
                quality=cacao_pourcentage+ 3*0.75;
                //assert this.get_MQ().getValeur()>quantity*cacao_pourcentage/100.;
            }
            else{
                quality = cacao_pourcentage + 3*1;
                //assert this.get_HQ().getValeur()>quantity*cacao_pourcentage/100.;
            }
        
        }

        //Achat de matières premières
        double prix_MP=1000;
        double quantite_mp=(quantity*(1-cacao_pourcentage/100.));
        double prix_total_mp=quantite_mp*prix_MP;
        if(prix_total_mp>0){
        Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Achat des matières premières pour la production de chocolat", prix_total_mp);
         }
          this.get_Stock().remove(quantity*cacao_pourcentage/100., gamme);
        if (quality>3.575){
            this.get_StockChoco_HQ().ajouter(this,quantity);
            this.journal_production.ajouter("Production de " + String.valueOf(quantity) + " tonnes de chocolat de qualité " + String.valueOf(quality));
        }
        else{
            if (quality>2.58){
                this.get_StockChoco_MQ().ajouter(this,quantity);
                this.journal_production.ajouter("Production de " + String.valueOf(quantity) + " tonnes de chocolat de qualité " + String.valueOf(quality));
            }
            else {
                this.get_StockChoco_BQ().ajouter(this,quantity);
                this.journal_production.ajouter("Production de " + String.valueOf(quantity) + " tonnes de chocolat de qualité " + String.valueOf(quality));
            }
        }
    }

    public void next(){
        double quantity = 0.;
        if (Filiere.LA_FILIERE.getEtape()>0 && this.StockChoco_BQ.getValeur()<1000000){
            quantity = get_LQ().getValeur()*2.2222;
        }
        else if (this.StockChoco_BQ.getValeur()>1000000){
            quantity = 0.;
        }
        else{
            quantity = 50000.;
        }
        this.production(quantity, Gamme.BQ,45);
        
        super.next();
        BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
        this.cours_feves_bq.setValeur(this, bourse.getCours(Feve.F_BQ).getValeur());
        this.cout_prod.setValeur(this, 0.45*this.cours_feves_bq.getValeur()+0.55*1000);

        
        // Auteur : Aymeric
        // Coûts de stockage du chocolat : 25 euros par tonne
        double totalChoco = this.get_StockChoco_BQ().getValeur() + this.get_StockChoco_MQ().getValeur() + this.get_StockChoco_HQ().getValeur();
        double coutStockageU = Filiere.LA_FILIERE.getParametre("cout moyen stockage producteur").getValeur()*4;
        double coutStockage = totalChoco * coutStockageU ;
        if(coutStockage > 0){
            Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Coûts de stockage du chocolat", coutStockage);
        }

        //Auteur  : Paul
        //Coûts de main d'oeuvre
         if (quantity > 0){
            double nb_ouvriers_nécessaires = quantity/8.4;
            if (this.nb_ouvriers.getValeur() > nb_ouvriers_nécessaires*1.3){
                Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Coûts de licenciement pour la production de chocolat", 3500*(this.nb_ouvriers.getValeur()-nb_ouvriers_nécessaires));
                this.journal_RH.ajouter("Licenciement de " + String.valueOf(this.nb_ouvriers.getValeur()-nb_ouvriers_nécessaires) + " ouvriers pour la production de chocolat");
                this.nb_ouvriers.setValeur(this,nb_ouvriers_nécessaires);
            }
            if (this.nb_ouvriers.getValeur() < nb_ouvriers_nécessaires){
                    Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Coûts de recrutement pour la production de chocolat", 5000*(nb_ouvriers_nécessaires-this.nb_ouvriers.getValeur()));
                    this.journal_RH.ajouter("Recrutement de " + String.valueOf(nb_ouvriers_nécessaires-this.nb_ouvriers.getValeur()) + " ouvriers pour la production de chocolat");
                    this.nb_ouvriers.setValeur(this,nb_ouvriers_nécessaires);
                }
            }

        double totalHR = this.nb_ouvriers.getValeur() * 1250;
        if(totalHR > 0){
            Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Coûts de main d'oeuvre pour la production de chocolat", totalHR);
        }
    }
        
    // Coûts de production 

    

    

    public List<ChocolatDeMarque> getChocolatsProduits() {
        ChocolatDeMarque cacao_plus = new ChocolatDeMarque(Chocolat.C_BQ, "CACAO+", 45);
        List<ChocolatDeMarque> liste= new ArrayList<>();
        liste.add(cacao_plus);
        return liste;
    }

}
