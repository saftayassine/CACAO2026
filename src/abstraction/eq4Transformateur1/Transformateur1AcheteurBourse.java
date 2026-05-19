
/**@author Ewan Lefort */

package abstraction.eq4Transformateur1;

import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;

public class Transformateur1AcheteurBourse extends Transformateur1Couts implements IAcheteurBourse {
    public void notificationBlackList(int dureeEnStep){

    }
    public void notificationAchat(Feve f, double quantiteEnT, double coursEnEuroParT){
        this.setStocksProduit(f,this.getStocksProduit(f)+quantiteEnT);
    }
    /**@author Safta Yassine */ 
    public double demande(Feve f, double cours){
        if (this.getStocksPrevuProduit(this.getChoco(f)) < 20000){
			return 20000; 
		}
		else{
            return 0;
        }
    }
}
