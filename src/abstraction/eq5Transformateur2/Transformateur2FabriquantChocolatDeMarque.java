package abstraction.eq5Transformateur2;

import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.filiere.IFabricantChocolatDeMarque;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
/**@author Maxence */
public class Transformateur2FabriquantChocolatDeMarque extends Transformateur2Marque implements IFabricantChocolatDeMarque{

    public List<ChocolatDeMarque> getChocolatsProduits() {
        ArrayList<ChocolatDeMarque> liste = new ArrayList<ChocolatDeMarque>(3);
        ChocolatDeMarque FerraraHQ = new ChocolatDeMarque(Chocolat.C_HQ,"Ferrara Rocher", 100);
        ChocolatDeMarque FerraraMQ = new ChocolatDeMarque(Chocolat.C_MQ,"Ferrara Rocher", 100);
        ChocolatDeMarque FerraraBQ = new ChocolatDeMarque(Chocolat.C_BQ,"Ferrara Rocher", 100);
        liste.add(FerraraHQ);
        liste.add(FerraraMQ);
        liste.add(FerraraBQ);
        return liste;
    }

}
