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
            
            if (quantiteEnStock > 5 && Filiere.LA_FILIERE.getEtape() % 10 == 0) {
                superviseur.vendreAuxEncheres(this, cryptogramme, choco, quantiteEnStock);
            }
        }
    }

    public Enchere choisir(List<Enchere> propositions) {
        if (propositions.isEmpty()) return null;
        
        Enchere choisie=propositions.get(0);
        for (Enchere enchere : propositions) {
            if(enchere.getPrixTonne() > choisie.getPrixTonne()){
                choisie=enchere;
            }
        }
        this.getJournaux().get(6).ajouter(choisie.toString()+ "\n");
        Double quantite = choisie.getQuantiteT();
        ChocolatDeMarque choco = (ChocolatDeMarque)choisie.getProduit();
        this.remove_chocolatDeMarque(choco, quantite);
        
        Integer indice=2;
        Gamme gamme = choco.getGamme();
        if (gamme.equals(Gamme.HQ)){
            indice = 0;
        }
        else if (gamme.equals(Gamme.MQ)){
            indice = 1;
        }
        this.updatePrixEnchere(indice, choisie.getPrixTonne());
        
        return choisie;
    }

    @Override
    public void next(){
        super.next();
        this.VendreEncheres();
    }
}