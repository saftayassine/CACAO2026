package abstraction.eq5Transformateur2;

import java.awt.Color;
import java.util.List;

import abstraction.eqXRomu.appelDOffre.AppelDOffre;
import abstraction.eqXRomu.appelDOffre.IVendeurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.bourseCacao.BourseCacao;

/**
 * @author Pierre GUTTIEREZ
 */
public class Transformateur2VendeurAppelOffre extends Transformateur2AchatAppelOffre implements IVendeurAO{

    public Transformateur2VendeurAppelOffre() {
        super();
    }

	public OffreVente proposerVente(AppelDOffre offre){
            IProduit p = offre.getProduit();
            
            // Si le produit n'est pas un chocolat de marque, on refuse
            if (!(p instanceof ChocolatDeMarque)) {
                return null;
            }
            
            ChocolatDeMarque cdm = (ChocolatDeMarque) p;
            
            // On s'assure qu'on ne vend que NOTRE marque
            if (!cdm.getNom().equals("Ferrara Rocher")) {
                return null;
            }

            OffreVente OV = new OffreVente(offre, this, offre.getProduit(),((BourseCacao) (Filiere.LA_FILIERE.getActeur("BourseCacao"))).getCours(Feve.F_MQ).getValeur()*1.18*offre.getQuantiteT());
			
			Chocolat c = cdm.getChocolat();
			Feve F;
			if (c == Chocolat.C_BQ){
				F = Feve.F_BQ;
			} else if (c == Chocolat.C_MQ) {
				F = Feve.F_MQ;
			} else {
				F = Feve.F_HQ;
			}
			this.ProductionChocolat(F, 0.65, offre.getQuantiteT());
            return OV;
    }

	public void notifierVenteAO(OffreVente propositionRetenue){
        this.getJournaux().get(6).ajouter(propositionRetenue.toString()+ "\n");
    }

	public void notifierPropositionNonRetenueAO(OffreVente propositionRefusee){
        this.getJournaux().get(6).ajouter(propositionRefusee.toString()+ "\n");
    }
}