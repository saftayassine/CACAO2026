package abstraction.eq5Transformateur2;

import java.awt.Color;
import java.util.List;

import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.IVendeurAuxEncheres;
import abstraction.eqXRomu.encheres.SuperviseurVentesAuxEncheres;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.Chocolat; 
/**
 * @author Maxence
 */
public class Transformateur2VendeurAuxEncheres extends Transformateur2AcheteurBourse implements IVendeurAuxEncheres{
    private SuperviseurVentesAuxEncheres superviseur =(SuperviseurVentesAuxEncheres)(Filiere.LA_FILIERE.getActeur("Sup.Encheres"));

    public Transformateur2VendeurAuxEncheres() {
        super();
    }


    public void VendreEncheres(){
        Double c_bq=this.getStock_chocolat(Chocolat.C_BQ);
        Double c_bq_e=this.getStock_chocolat(Chocolat.C_BQ_E);
        Double c_mq=this.getStock_chocolat(Chocolat.C_MQ);
        Double c_mq_e=this.getStock_chocolat(Chocolat.C_MQ_E);
        Double c_hq=this.getStock_chocolat(Chocolat.C_HQ);
        Double c_hq_e=this.getStock_chocolat(Chocolat.C_HQ_E);

        superviseur.vendreAuxEncheres(this, cryptogramme, Chocolat.C_BQ, c_bq);
        superviseur.vendreAuxEncheres(this, cryptogramme, Chocolat.C_BQ_E, c_bq_e);
        superviseur.vendreAuxEncheres(this, cryptogramme, Chocolat.C_MQ, c_mq);
        superviseur.vendreAuxEncheres(this, cryptogramme, Chocolat.C_MQ_E, c_mq_e);
        superviseur.vendreAuxEncheres(this, cryptogramme, Chocolat.C_HQ, c_hq);
        superviseur.vendreAuxEncheres(this, cryptogramme, Chocolat.C_HQ, c_hq_e);
    }

    public Enchere choisir(List<Enchere> propositions) {
        Enchere choisie=propositions.get(0);
        for (Enchere enchere : propositions) {
            if(enchere.getPrixTonne()<choisie.getPrixTonne()){
                choisie=enchere;
            }}
            return choisie;
        }
    

    }
    