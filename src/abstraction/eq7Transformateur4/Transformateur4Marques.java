package abstraction.eq7Transformateur4;

import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.filiere.IMarqueChocolat;

//Auteur : Matteo

public class Transformateur4Marques extends Transformateur4AcheteurBourse implements IMarqueChocolat {


    public Transformateur4Marques(){
        super();
    }

    @Override
    public List<String> getMarquesChocolat() {
        List<String> listeMarques = new ArrayList<>();
        listeMarques.add("CACAO+");
        return listeMarques;
    }

}
