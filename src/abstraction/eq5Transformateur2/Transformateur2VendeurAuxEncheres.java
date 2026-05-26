package abstraction.eq5Transformateur2;

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
        List<ChocolatDeMarque> mesChocolats = this.getChocolatsProduits();

        for (ChocolatDeMarque choco : mesChocolats) {
            Double quantiteEnStock = this.getStock_chocolatDeMarque(choco);
            
            if (quantiteEnStock > 70000.0) {
                superviseur.vendreAuxEncheres(this, cryptogramme, choco, 15000.0);
            }
        }
    }

    public Enchere choisir(List<Enchere> propositions) {
        if (propositions.isEmpty()) return null;
        //On sélectionne la meilleure offre
        Enchere choisie = propositions.get(0);
        for (Enchere enchere : propositions) {
            if(enchere.getPrixTonne() > choisie.getPrixTonne()){
                choisie = enchere;
            }
        }
        
        // Prix Plancher
        ChocolatDeMarque choco = (ChocolatDeMarque)choisie.getProduit();
        double prixMinimum = 0.0;
        switch (choco.getChocolat()) {
            case C_HQ: prixMinimum = 12000.0; break; 
            case C_MQ: prixMinimum = 8000.0; break;
            case C_BQ: prixMinimum = 6000.0; break;
            default:   prixMinimum = 5000.0; break;
        }

        if (choisie.getPrixTonne() < prixMinimum) {
            return null; 
        }

        // Si le prix est acceptable, on valide
        this.getJournaux().get(6).ajouter(choisie.toString()+ "\n");
        Double quantite = choisie.getQuantiteT();
        this.remove_chocolatDeMarque(choco, quantite);
        return choisie;
    }

    @Override
    public void next(){
        super.next();
        this.VendreEncheres();
    }
}