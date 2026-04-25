package abstraction.eq5Transformateur2;

import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.filiere.Banque;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;

/** @author Pierre
 */
public class Transformateur2ProductionChocolat extends Transformateur2Production {
    
    public Transformateur2ProductionChocolat(){
        super();

    }

    @Override
    public void next() {
        super.next();
        ProductionChocolat(Chocolat.C_BQ, 2000.0);
        ProductionChocolat(Chocolat.C_MQ, 1500.0);
        ProductionChocolat(Chocolat.C_HQ, 500.0);
    }

    public void ProductionChocolat(Chocolat c,Double n){
        if(c==Chocolat.C_HQ){
            ProductionFerraraHQ(n);
        }
        else if(c==Chocolat.C_MQ){
            ProductionFerraraMQ(n);
        }
        else if(c==Chocolat.C_BQ){
            ProductionFerraraBQ(n);
        }
    }


    /**@author Maxence 
     * Notre Chocolat HQ a 100% de cacao, dont 49% de fèves HQ et 51% de fèves MQ
    */
    public void ProductionFerraraHQ(Double quantite){
        Double quantiteFeveHQ=quantite*0.49;
        Double quantiteFeveMQ=quantite*0.51;
        if((quantiteFeveHQ<=this.getStock_feve(Feve.F_HQ)) && (quantiteFeveMQ<=this.getStock_feve(Feve.F_MQ))){
            this.remove_feve(quantiteFeveHQ,Feve.F_HQ);
            this.remove_feve(quantiteFeveMQ,Feve.F_MQ);
            ChocolatDeMarque chocoHQ = new ChocolatDeMarque(Chocolat.C_HQ, "Ferrara Rocher", 100);
            this.add_chocolatDeMarque(chocoHQ, quantite);
        }
    }
/** @author Maxence
* notre chocolat MQ a 100% de cacao, dont 26% de fèves MQ et 74% de fèves BQ
 */
    public void ProductionFerraraMQ(Double quantite){
        Double quantiteFeveMQ=quantite*0.26;
        Double quantiteFeveBQ=quantite*0.74;
        if((quantiteFeveMQ<=this.getStock_feve(Feve.F_MQ)) && (quantiteFeveBQ<=this.getStock_feve(Feve.F_BQ))){
            this.remove_feve(quantiteFeveMQ,Feve.F_MQ);
            this.remove_feve(quantiteFeveBQ,Feve.F_BQ);
            ChocolatDeMarque chocoMQ = new ChocolatDeMarque(Chocolat.C_MQ, "Ferrara Rocher", 100);
            this.add_chocolatDeMarque(chocoMQ, quantite);       
        }
    }
/** @author Maxence
* notre chocolat BQ a 45% de cacao
 */
    public void ProductionFerraraBQ(Double quantite){
        Double quantiteFeveBQ=quantite*0.45;
        Double quantiteMP=quantite*0.65;
        if(quantiteFeveBQ<=this.getStock_feve(Feve.F_BQ)){
            ChocolatDeMarque chocoBQ = new ChocolatDeMarque(Chocolat.C_BQ, "Ferrara Rocher", 100);
            this.add_chocolatDeMarque(chocoBQ, quantite);
        }
        // J'ai ajouté une petite sécurité ici pour éviter un crash si prix_MP n'est pas initialisé
        if (prix_MP != null) {
            Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Achat de MP pour production de chocolat FerraraBQ", quantiteMP*prix_MP);
        }
    }
}