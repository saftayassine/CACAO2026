package abstraction.eq7Transformateur4;
import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IFabricantChocolatDeMarque;
//Matteo
public class Transformateur4Production extends Transformateur4AcheteurBourse implements IFabricantChocolatDeMarque{

    public Transformateur4Production(){
        super();
    }

    public void production(double quantity, Gamme gamme, double cacao_pourcentage){
        //Verification
        assert cacao_pourcentage>0.45;
        //Calcul qualité
        double quality=0.;
        if (gamme==Gamme.BQ){
            quality=cacao_pourcentage + 3*0.45;
            assert this.get_LQ().getValeur()>quantity*cacao_pourcentage;}
        else{
            if (gamme==Gamme.MQ){
                quality=cacao_pourcentage+ 3*0.75;
                assert this.get_MQ().getValeur()>quantity*cacao_pourcentage;
            }
            else{
                quality = cacao_pourcentage + 3*1;
                assert this.get_HQ().getValeur()>quantity*cacao_pourcentage;
            }
        
        }

        //Achat de matières premières
        double prix_MP=1000;
        double quantite_mp=(quantity*(1-cacao_pourcentage));
        double prix_total_mp=quantite_mp*prix_MP;
        Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Achat des matières premières pour la production de chocolat", prix_total_mp);
        this.get_Stock().remove(quantity*cacao_pourcentage, gamme);
        if (quality>3.575){
            this.get_StockChoco_HQ().ajouter(this,quantity);
        }
        else{
            if (quality>2.58){
                this.get_StockChoco_MQ().ajouter(this,quantity);
            }
            else {
                this.get_StockChoco_BQ().ajouter(this,quantity);
            }
        }
        

        
    }

    public List<ChocolatDeMarque> getChocolatsProduits() {
        //ChocolatDeMarque marque1 = new ChocolatDeMarque(Chocolat.C_BQ, "CACAO+", 45);
        List<ChocolatDeMarque> liste= new ArrayList<>();
        //liste.add(marque1);
        return liste;
    }

}
