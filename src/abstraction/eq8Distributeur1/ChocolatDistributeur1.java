package abstraction.eq8Distributeur1;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IMarqueChocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;


    /** @author Alexandre Cornet */

public class ChocolatDistributeur1 extends Distributeur1Acteur implements IMarqueChocolat {

    @Override
    public List<String> getMarquesChocolat() {
        LinkedList<String> liste=new LinkedList<String>();
        liste.add("Choco1");
        return liste;

    }
    
}
