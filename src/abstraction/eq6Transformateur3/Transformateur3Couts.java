package abstraction.eq6Transformateur3;

import abstraction.eqXRomu.filiere.Filiere;

public class Transformateur3Couts extends Transformateur3Acteur {
    
    public void next() {
        super.next();
        Filiere.LA_FILIERE.getBanque().payerCout(this, this.cryptogramme,"salaires",11250000 );
        Filiere.LA_FILIERE.getBanque().payerCout(this, this.cryptogramme,"couts fixes",  2000000);
        if (Filiere.LA_FILIERE.getEtape() ==0){
            Filiere.LA_FILIERE.getBanque().payerCout(this, this.cryptogramme,"achat machines",  2000000);
        }
    }
}

