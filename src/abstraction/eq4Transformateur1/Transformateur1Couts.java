/**@author Yassine Safta */

/**@author Yassine Safta */

package abstraction.eq4Transformateur1;
import abstraction.eqXRomu.filiere.Filiere;

public class Transformateur1Couts extends Transformateur1Stock {
    public void achatmachines() {
        if (this.ChocoProduit>this.getNbMachines()*840){
            int Nbprecedent=this.getNbMachines();
            this.setNbMachines((int) Math.ceil(this.ChocoProduit/840)) ;
            Filiere.LA_FILIERE.getBanque().payerCout(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme,"achat machines Suppl", (this.getNbMachines()-Nbprecedent)*150000 );
        };
    }
    public void next() {
		super.next();
        achatmachines();
        Filiere.LA_FILIERE.getBanque().payerCout(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme,"salaires",11250000 );
        
        if (this.ChocoProduit>0){
        Filiere.LA_FILIERE.getBanque().payerCout(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme,"energie",  this.ChocoProduit*90 );
        Filiere.LA_FILIERE.getBanque().payerCout(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme,"entretien machines",  this.ChocoProduit*40 );
        }
        ;
        Filiere.LA_FILIERE.getBanque().payerCout(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme,"couts fixes",  2000000);
        if (Filiere.LA_FILIERE.getEtape() ==0){
            Filiere.LA_FILIERE.getBanque().payerCout(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme,"achat machines init",  13500000);
        };
    

}}