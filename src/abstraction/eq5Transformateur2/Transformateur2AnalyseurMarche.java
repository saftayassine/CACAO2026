package abstraction.eq5Transformateur2;
import java.util.HashMap;
import java.util.List;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IFabricantChocolatDeMarque;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;

/**
 * @author Maxence
 */
public class Transformateur2AnalyseurMarche extends Transformateur2Production{
    public Transformateur2AnalyseurMarche(){
        super();
    }

    public HashMap<ChocolatDeMarque,Double> DemandeMarque(){
        HashMap<ChocolatDeMarque,Double> demandeMarque=new HashMap<>();
        int etapeActuelle=Filiere.LA_FILIERE.getEtape();
        for (ChocolatDeMarque choco : Filiere.LA_FILIERE.getChocolatsProduits()) {
            double ventes=0;
            for (int etape=-24; etape<etapeActuelle; etape++) {
                ventes += Filiere.LA_FILIERE.getVentes(choco,etape); 
            }
            demandeMarque.put(choco, ventes/(24+etapeActuelle));
        } 
        return demandeMarque;            
    }

    public HashMap<Chocolat,Double> DemandeChocolat(){
        HashMap<Chocolat,Double> demandeChocolat=new HashMap<>();
        demandeChocolat.put(Chocolat.C_BQ,0.0);
        demandeChocolat.put(Chocolat.C_BQ_E,0.0);
        demandeChocolat.put(Chocolat.C_MQ,0.0);
        demandeChocolat.put(Chocolat.C_MQ_E,0.0);
        demandeChocolat.put(Chocolat.C_HQ,0.0);
        demandeChocolat.put(Chocolat.C_HQ_E,0.0);
        for (ChocolatDeMarque choco : this.DemandeMarque().keySet()) {
            Double vente=this.DemandeMarque().get(choco);
            demandeChocolat.put(choco.getChocolat(),demandeChocolat.get(choco.getChocolat())+vente);
        }
        return demandeChocolat;
    }
}

