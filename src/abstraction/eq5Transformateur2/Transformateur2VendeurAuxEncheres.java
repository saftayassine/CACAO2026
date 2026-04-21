package abstraction.eq5Transformateur2;

import java.awt.Color;
import java.util.List;

import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.IVendeurAuxEncheres;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
/**
 * @author Maxence
 */
public class Transformateur2VendeurAuxEncheres extends Transformateur2AchatEncheres implements IVendeurAuxEncheres{

    public Transformateur2VendeurAuxEncheres() {
        super();
    }

    public void VendreEncheres(){
        // On récupère la liste de nos produits (Ferrara Rocher HQ, MQ, BQ)
        List<ChocolatDeMarque> mesChocolats = this.getChocolatsProduits();

        for (ChocolatDeMarque choco : mesChocolats) {
            Double quantiteEnStock = this.getStock_chocolatDeMarque(choco);
            
            // On ne lance une enchère que si on a du stock
            if (quantiteEnStock > 0) {
                superviseur.vendreAuxEncheres(this, cryptogramme, choco, quantiteEnStock);
            }
        }
    }

    public Enchere choisir(List<Enchere> propositions) {
        if (propositions.isEmpty()) return null;
        
        Enchere choisie=propositions.get(0);
        for (Enchere enchere : propositions) {
            if(enchere.getPrixTonne() > choisie.getPrixTonne()){ // Modifié : On veut le prix le PLUS HAUT en tant que vendeur !
                choisie=enchere;
            }
        }
        return choisie;
    }
}