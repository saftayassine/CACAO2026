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
        mesFeves.add(Feve.F_MQ_E);
        mesFeves.add(Feve.F_HQ_E);

        for (Feve f : mesFeves) {
            //On identifie tous les acheteurs potentiels pour ce produit 
            List<IAcheteurContratCadre> acheteurs = supCC.getAcheteurs(f);
            double stockReel = this.stock.getStock(f);
                
                // Calculer tout ce qu'on a déjà promis de livrer dans le futur pour cette fève
                double resteALivrerTotal = 0;
                for (ExemplaireContratCadre c : contratsEnCours) {
                    if (c.getProduit().equals(f)) {
                        resteALivrerTotal += c.getQuantiteRestantALivrer();
                    }
                }

                // On ne propose un contrat que si le stock dépasse nos engagements totaux
                double surplusReel = stockReel - resteALivrerTotal;
                
                if (surplusReel > 200.0) { // Seuil de sécurité de 200 tonnes
                    double quantiteTotaleVoulue = surplusReel * 0.3; // On ne propose que 30% du surplus
                    double quantiteParStep = quantiteTotaleVoulue / 12;

                    if (quantiteParStep >= 100.0) {
                        Echeancier ech = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 12, quantiteParStep);

                        // Boucle sur tous les acheteurs
                        for (IAcheteurContratCadre acheteur : acheteurs) {
                            double prixPropose = this.calculerPrixVente(f);
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


    public double calculerPrixVente(Feve f) {
        double coutFeve = this.gestionCouts.getCoutFeve(f, this); 
        double productionFeve = this.plantationeq3.getProductionFeve(f); 

        if (productionFeve <= 0) {
            return 2000.0; 
        }

        double coutParTonne = coutFeve / productionFeve;
        return coutParTonne * 1.35;
}

    public double propositionPrix(ExemplaireContratCadre contrat) {
        return calculerPrixVente((Feve) contrat.getProduit());
    }

    public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat) {
        Feve f = (Feve) contrat.getProduit();
        
        //Calcul du prix plancher spécifique (Coût de production fève + 10%)
        double coutFeve = this.gestionCouts.getCoutFeve(f,this);
        double productionFeve = this.plantationeq3.getProductionFeve(f);
        
        if (productionFeve <= 0) {
            return contrat.getPrix(); // On accepte si on n'a pas de données de prod
        }

        double prixPlancher = (coutFeve / productionFeve) * 1.10;
        double prixAcheteur = contrat.getPrix();

        if (prixAcheteur >= prixPlancher) {
            // Si l'acheteur propose plus que notre minimum spécifique, on accepte
            return prixAcheteur;
        } else {
            // Sinon, contre-proposition entre notre prix initial (35%) et notre plancher (10%)
            double prixInitial = this.propositionPrix(contrat);
            double contreProposition = (prixInitial + prixPlancher) / 2.0;

            // On ne propose jamais en dessous du plancher
            return Math.max(contreProposition, prixPlancher);
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