package abstraction.eq7Transformateur4;

import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
//Auteur -> Matteo
public class Transformateur4AcheteurBourse extends Transformateur4Acteur implements IAcheteurBourse{

    @Override
    public double demande(Feve f, double cours) {  
        if (f.getGamme()==Gamme.MQ){
            return 80;
        }
        else{
            return 0;
        }
    }

    @Override
    public void notificationAchat(Feve f, double quantiteEnT, double coursEnEuroParT) {
        if (f.isEquitable()){
            this.get_EqStock().add(quantiteEnT, f.getGamme());
        }
        else{
            this.get_Stock().add(quantiteEnT, f.getGamme());
        }
    }

    @Override
    public void notificationBlackList(int dureeEnStep) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notificationBlackList'");
    }
    

}
