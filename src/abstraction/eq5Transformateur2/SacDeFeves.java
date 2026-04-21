package abstraction.eq5Transformateur2;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
/**
 * @author Maxence
 */
public class SacDeFeves {
    
    private int datePeremption;
    private Feve feve;
    private Double quantite;

    public SacDeFeves(Feve feve, Double quantite,int datePeremption){
        /*Pour des fèves que l'on vient d'acheter, les temps avant péremption sont de 6, 12 et 24 tours selon la qualité
        entrer Filiere.LA_FILIERE.getEtape()+ (6,12 ou 24) pour datePereption
         */
        this.datePeremption=datePeremption;
        this.feve=feve;
        this.quantite=quantite;
    }
    public SacDeFeves(Feve feve, Double quantite){
        /*Ne pas utiliser ce constructeur quand un sac vient d'être reçu
        Un sac qui arrive possède des fèves qui ont déjà passé la moitié du temps avant de perdre en qualité
        Ce constructeur servira quand un sac va baisser en qualité pour le recréer en qualité inférieure
        */
        this.feve=feve;
        this.quantite=quantite;
        Gamme gamme=this.feve.getGamme();
        if (gamme==Gamme.HQ){
            this.datePeremption=Filiere.LA_FILIERE.getEtape()+12;
        }
        else if (gamme==Gamme.MQ){
            this.datePeremption=Filiere.LA_FILIERE.getEtape()+24;
        }
        else {
            this.datePeremption=Filiere.LA_FILIERE.getEtape()+48;
        }

        }
    
    
    public Feve getFeve(){
        return this.feve;
    }

    public Double getQuantite(){
        return this.quantite;
    }

    public int getDatePeremption(){
        return this.datePeremption;
    }

    public Double remove_feve(Double q){
        /*Enlève q fèves du sac
        ne retire pas plus de fèves que la quantité dans le sac
        return la quantité de fèves qui n'a pas été enlevée (éventuellement 0.0)
        */
        if(this.quantite>=q){
            this.quantite=this.quantite-q;
            return 0.0;
        }
        else{
            Double reste= q-this.quantite;
            this.quantite=0.0;
            return reste;
        }
    }
}

