package abstraction.eq1Producteur1;

import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.IVendeurAuxEncheres;
import abstraction.eqXRomu.encheres.MiseAuxEncheres;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;

/** 
 * @author Elise Dossal
 */
public class Producteur1VendeurBourse extends Producteur1AcheteurBourse implements IVendeurAuxEncheres{
///*
    protected List<Enchere> propositions;
        private double enchere_BQ = 0;
        private double enchere_BQ_E = 0;
        private double enchere_MQ = 0;
        private double enchere_MQ_E = 0;
        private double enchere_HQ = 0;
        private double enchere_HQ_E = 0;

    public Producteur1VendeurBourse(){
        super();

    }

    public void next(){
        super.next();
        if(this.getBQStock()-this.enchere_BQ>=170){
            Feve feve = Feve.F_BQ  ;
            this.enchere_BQ += 170;
            MiseAuxEncheres mise = new MiseAuxEncheres(this, feve , 170.0, true);
        
        };
        


    }

    public Enchere choisir(List<Enchere> propositions){
        return this.propositions.get(0);

    }
//*/
}