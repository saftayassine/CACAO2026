package abstraction.eq5Transformateur2;

import java.util.List;

import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.IVendeurAuxEncheres;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Gamme;
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
            
            if (quantiteEnStock > 100000.0) {
                superviseur.vendreAuxEncheres(this, cryptogramme, choco, 25000.0);
            }
        }
    }

    public Enchere choisir(List<Enchere> propositions) {
        if (propositions.isEmpty()) return null;
        
        Enchere choisie = propositions.get(0);
        for (Enchere enchere : propositions) {
            if(enchere.getPrixTonne() > choisie.getPrixTonne()){
                choisie = enchere;
            }
        }
        
        // CORRECTIF 2 : Le Bouclier Anti-Arnaque (Prix Plancher)
        ChocolatDeMarque choco = (ChocolatDeMarque)choisie.getProduit();
        double prixMinimum = 0.0;
        switch (choco.getChocolat()) {
            case C_HQ: prixMinimum = 15000.0; break; // On accepte de brader un peu (7000 au lieu de 8000)
            case C_MQ: prixMinimum = 10000.0; break;
            case C_BQ: prixMinimum = 7000.0; break;
            default:   prixMinimum = 5000.0; break;
        }

        // Si la "meilleure" offre est en dessous de notre prix de fabrication, on refuse la vente !
        if (choisie.getPrixTonne() < prixMinimum) {
            return null; 
        }

        // Si le prix est acceptable, on valide la vente et on retire du stock
        this.getJournaux().get(6).ajouter(choisie.toString()+ "\n");
        Double quantite = choisie.getQuantiteT();
        this.remove_chocolatDeMarque(choco, quantite);
        
        // Vous aviez des variables inutilisées ici (indice, gamme) dans votre code d'origine
        // Je les ai retirées pour nettoyer, à moins que vous ne vouliez les utiliser pour le journal
        
        return choisie;
    }

    @Override
    public void next(){
        super.next();
        this.VendreEncheres();
    }
}