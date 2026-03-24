package abstraction.eq1Producteur1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.IVendeurBourse;
import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.MiseAuxEncheres;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;

/** 
 * @author Elise Dossal
 */
public class Producteur1VendeurBourse extends Producteur1AcheteurBourse implements IVendeurBourse{
///*
    protected List<Enchere> propositions;
    private int blacklist=0;

    public Producteur1VendeurBourse(){
        super();


    }



	/**
	 * Retourne la quantite en tonnes de feves de type f que le vendeur 
	 * souhaite vendre a cette etape sachant que le cours actuel de 
	 * la feve f est cours
	 * @param f le type de feve
	 * @param cours le cours actuel des feves de type f
	 * @return la quantite en tonnes de feves de type f que this souhaite vendre 
	 */
	public double offre(Feve f, double cours){
        if(f == Feve.F_MQ){
            return 120;
        }

        return 0;
    }


	/**
	 * Methode appelee par la bourse pour avertir le vendeur qu'il est parvenu
	 * a vendre quantiteEnT tonnes de feve f au prix de coursEnEuroParT euros par tonne.
	 * L'acteur this doit determiner la quantite qu'il livre reellement et destocker cette
	 * quantite. 
	 * La quantite quantiteEnT est inferieure ou egale a ce que le vendeur this a specifie
	 * vouloir vendre, et il doit donc normalement etre en mesure de retirer cette 
	 * quantite de ses sotcks afin de la livrer et de retourner quantiteEnT. 
	 * Mais il se peut qu'il retourne une quantite inferieure si ses stocks de feve f ne 
	 * sont pas suffisants pour livrer quantiteEnT tonnes.
	 * Remarque : le superviseur s'occupe des virements, vendeurs et acheteurs n'ont pas a
	 *  les gerer
	 * @return la quantite en tonnes de feves de type f rellement livree (retiree du stock) 
	 */
	public double notificationVente(Feve f, double quantiteEnT, double coursEnEuroParT){
        double vrai_quantite= Math.min(quantiteEnT,getStock(f));
        this.takeFeve(f, vrai_quantite);
        return vrai_quantite;
    }

	/**
	 * Methode appelee par la bourse pour avertir le vendeur qu'il vient 
	 * d'etre ajoute a la black list : l'acteur a precise une quantite qu'il desirait 
	 * mettre en vente qu'il n'a pas pu honorer (il n'a pu livrer qu'une quantite insuffisante)
	 * this ne pourra pas vendre en bourse pendant la duree precisee en 
	 * parametre 
	 */
	public void notificationBlackList(int dureeEnStep){
        this.blacklist = dureeEnStep;
    }

}

//*/
