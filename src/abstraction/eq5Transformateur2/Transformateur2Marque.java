package abstraction.eq5Transformateur2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.filiere.IMarqueChocolat;
/** @author Maxence 
**/
public class Transformateur2Marque extends Transformateur2Stock implements IMarqueChocolat{

    @Override
    public List<String> getMarquesChocolat() {
        LinkedList<String> listeMarqueTransformateur=new LinkedList<String>();
        listeMarqueTransformateur.add("Ferrara Rocher");
        return listeMarqueTransformateur;

    }

}
