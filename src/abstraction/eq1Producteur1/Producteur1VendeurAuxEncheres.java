
package abstraction.eq1Producteur1;


import java.util.List;

import abstraction.eqXRomu.encheres.IVendeurAuxEncheres;
import abstraction.eqXRomu.encheres.MiseAuxEncheres;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.encheres.Enchere;

class Producteur1VendeurAuxEncheres extends Producteur1VendeurBourse implements IVendeurAuxEncheres{


    public Producteur1VendeurAuxEncheres(){
        super();
    }

    public void next(){
        super.next();
        if(this.getStock(Feve.F_BQ)>=170){
            new MiseAuxEncheres(this, Feve.F_BQ , 170.0, true);
        };


        if(this.getStock(Feve.F_MQ)>=30){
            new MiseAuxEncheres(this, Feve.F_MQ , 30.0, true);
        }
    }


        /**
	 * @param propositions une liste non vide de propositions de prix pour une offre de vente emise par this
	 * @return retourne la proposition choisie parmi celles de propositions 
	 * (retourne null si aucune des propositions de propositions ne satisfait le vendeur this)
	 */
	public Enchere choisir(List<Enchere> propositions){
        for(int i=0; i<propositions.size(); i++){
            }
        return null;
    }
}