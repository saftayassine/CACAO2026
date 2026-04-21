package abstraction.eq3Producteur3;

import java.util.LinkedList;
import java.util.List;
import abstraction.eqXRomu.acteurs.ProducteurXVendeurBourse;
import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;


/** @author Victor Vannier-Moreau */
public class Producteur3VendeurCC extends Producteur3VendeurBourse implements IVendeurContratCadre {
    
    
    protected Journal journalCC;

    public Producteur3VendeurCC() {
        super();
        
        this.journalCC = new Journal("Journal Ventes CC EQ3", this);
    }

    public List<Journal> getJournaux() {
		List<Journal> jx=super.getJournaux();
		jx.add(this.journalCC);
		return jx;
	}

    public void next() {
    super.next(); 
    SuperviseurVentesContratCadre supCC = (SuperviseurVentesContratCadre)Filiere.LA_FILIERE.getActeur("Sup.CCadre");
    List<Feve> mesFeves = new LinkedList<Feve>();
    mesFeves.add(Feve.F_MQ);
    mesFeves.add(Feve.F_HQ);

    for (Feve f : mesFeves) {
        //On identifie tous les acheteurs potentiels pour ce produit 
        List<IAcheteurContratCadre> acheteurs = supCC.getAcheteurs(f);
        
        // On calcule une quantité à proposer (ex: 30% du stock par acheteur)
        double quantiteTotaleVoulue = this.stock.getStock(f) * 0.3;
        double quantiteParStep = quantiteTotaleVoulue / 12; // Étallé sur 6 mois

        if (quantiteParStep >= 100.0) {
            Echeancier ech = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 12, quantiteParStep);

            // Boucle sur tous les acheteurs
            for (IAcheteurContratCadre acheteur : acheteurs) {
                double prixPropose = this.propositionPrix(null); 
                int dureeEcheancier = ech.getNbEcheances();

                // 2. On construit le message détaillé
                String msg = "S" + Filiere.LA_FILIERE.getEtape();
                msg = msg + " : Tentative de vente de " + f;
                msg = msg + " à " + acheteur.getNom();
                msg = msg + " | Prix: " + prixPropose + "€/t";
                msg = msg + " | Durée: " + dureeEcheancier + " steps";

                this.journalCC.ajouter(msg);
                supCC.demandeVendeur(acheteur, this, f, ech, cryptogramme, false);
            }
        }
    }
}

    // On ne vend que les gammes MQ et HQ par contrat cadre
    public boolean vend(IProduit produit) {
        if (produit instanceof Feve) {
            Feve f = (Feve) produit;
            if (f.getGamme() == Gamme.MQ) { 
                return true; 
            }
            if (f.getGamme() == Gamme.HQ) { 
                return true; 
            }
        }
        return false;
    }

    /**
     * Négociation de la quantité
     */
    public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {
        if (contrat.getProduit() instanceof Feve) {
            Feve f = (Feve) contrat.getProduit();
            double stockTotalReel = this.stock.getStock(f); 

            // On soustrait ce qui est déjà promis aux autres clients
            double totalDejaPromis = 0;
            for (ExemplaireContratCadre c : contratsEnCours) {
                if (c.getProduit().equals(f)) {
                    totalDejaPromis = totalDejaPromis + c.getQuantiteRestantALivrer();
                }
            }

            double disponible = stockTotalReel - totalDejaPromis;
            double demandeAcheteur = contrat.getEcheancier().getQuantiteTotale();

            if (disponible >= demandeAcheteur) {
                return contrat.getEcheancier();
            } else {
                if (disponible > 0) {
                    Echeancier e = contrat.getEcheancier();
                    e.set(e.getStepDebut(), disponible);
                    return e;
                } else {
                    return null;
                }
            }
        }
        return null; 
    }

    public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {
        Feve f = (Feve) produit;
        String nomAcheteur = contrat.getAcheteur().getNom();

        // On vérifie le stock total pour ne pas demander plus que possible
        double stockTotal = this.stock.getStock(f);
        double aLivre = quantite;
        if (stockTotal < quantite) {
            aLivre = stockTotal;
        }

        this.stock.retireStock(f, aLivre);
        this.mettreAJourIndicateurStock();
        this.journalCC.ajouter("période "+Filiere.LA_FILIERE.getEtape()+" : Livraison de " + aLivre + " tonnes de " + f + " à " + nomAcheteur);
        return aLivre;
    }

    public double propositionPrix(ExemplaireContratCadre contrat) {
    double coutTotalCacao = this.gestionCouts.getCoutTot(this);
    double productionTotale = this.plantationeq3.getProductionTotale();
    double coutParTonne = coutTotalCacao / productionTotale;

    //Fixer le prix avec 35% de marge 
    double prixVente = coutParTonne * 1.35;

    return prixVente;
}

    public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat) {
    double coutTotalCacao = this.gestionCouts.getCoutTot(this);
    double productionTotale = this.plantationeq3.getProductionTotale();

    // La marge minimum est de 10%, on ne vend pas en dessous.
    double prixPlancher = (coutTotalCacao / productionTotale) * 1.10;

    double prixAcheteur = contrat.getPrix();

    //Logique de négociation
    if (prixAcheteur >= prixPlancher) {
        // Si l'acheteur propose plus que notre minimum, on accepte
        return prixAcheteur;
    } else {
        // Sinon, on propose une contre-proposition à mi-chemin 
        // entre notre prix initial et notre prix plancher
        double prixInitial = this.propositionPrix(contrat);
        double contreProposition = (prixInitial + prixPlancher) / 2.0;

        return contreProposition;
    }
}
    
    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
    String client = contrat.getAcheteur().getNom();
    Echeancier ech = contrat.getEcheancier();
    
    String info = "Nouveau contrat avec " + client;
    info = info + " | Durée: " + ech.getNbEcheances() + " steps";
    info = info + " | Fin: " + ech.getStepFin();
    info = info + " | Total: " + ech.getQuantiteTotale() + "t";
    info = info + " | Prix: " + contrat.getPrix() + "€/t";

    this.journalCC.ajouter(info);
    this.contratsEnCours.add(contrat);
}
}