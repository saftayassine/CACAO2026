package abstraction.eq5Transformateur2;
import java.util.ArrayList;
import java.util.HashMap;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;

/**
 * @author Maxence
 */
public class Transformateur2AnalyseurMarche extends Transformateur2Acteur{
    private ArrayList<Double> prixDernieresEncheres; /*0:FerraraHQ, 1:FerraraMQ, 2:FerraraBQ */

    public Transformateur2AnalyseurMarche(){
        super();
        this.prixDernieresEncheres = new ArrayList<>(3);
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

    public void updatePrixEnchere(Integer indice, Double prixTonne){
        /*indice 0:FerraraHQ, 1:FerraraMQ, 2:FerraraBQ  */
        this.prixDernieresEncheres.add(indice,prixTonne);
    }

    public Double getPrixEnchere(Integer indice){
        /*indice 0:FerraraHQ, 1:FerraraMQ, 2:FerraraBQ  */
        return this.prixDernieresEncheres.get(indice);
    }
    @Override
    public void next(){
        super.next();
    }
}

