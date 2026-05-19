
package abstraction.eq6Transformateur3;
import abstraction.eq4Transformateur1.Transformateur1Stock;
import abstraction.eqXRomu.filiere.Filiere;


import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.acteurs.TransformateurXAcheteurBourse;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;

import abstraction.eqXRomu.appelDOffre.IAcheteurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.appelDOffre.SuperviseurVentesAO;


import abstraction.eq6Transformateur3.StockFeve;
import abstraction.eq6Transformateur3.StockChocolat;

/**
 * Classe simple de transformation des fèves en chocolat pour EQ6.
 * Version avec :
 * - qualité du chocolat calculée selon les règles du prof
 * - qualité perçue
 */
public class Transformateur3Transformation extends Transformateur3Couts{

    

    // Paramètres simples de production
    private int nbOuvriers;
    private int nbMachines;

    // Paramètres de capacité
    private static final double PRODUCTION_PAR_OUVRIER = 8.4;   // T / période / ouvrier
    private static final double PRODUCTION_PAR_MACHINE = 840.0; // T / période / machine

    // Coûts variables simples
    private static final double COUT_ENERGIE_PAR_T = 90.0;      // €/T
    private static final double COUT_ENTRETIEN_PAR_T = 40.0;    // €/T

    // Règles qualité perçue
    private static final double GAIN_QUALITE_EQUITABLE = 0.5;
    private static final double IMPACT_MARQUE_QUALITE_PERCUE = 0.3;
    private static final double IMPACT_CACAO_QUALITE_PERCUE = 0.3;

    public Transformateur3Transformation() {
        this.nbOuvriers = 100;
        this.nbMachines = 2;
    }

    public Transformateur3Transformation( int nbOuvriers, int nbMachines) {
        this.nbOuvriers = nbOuvriers;
        this.nbMachines = nbMachines;
    }

    public int getNbOuvriers() {
        return nbOuvriers;
    }

    public int getNbMachines() {
        return nbMachines;
    }

    public void setNbOuvriers(int nbOuvriers) {
        this.nbOuvriers = nbOuvriers;
    }

    public void setNbMachines(int nbMachines) {
        this.nbMachines = nbMachines;
    }

    /**
     * Capacité maximale de production sur une période.
     */
    public double capaciteProduction() {
        double capaciteOuvriers = nbOuvriers * PRODUCTION_PAR_OUVRIER;
        double capaciteMachines = nbMachines * PRODUCTION_PAR_MACHINE;
        return Math.min(capaciteOuvriers, capaciteMachines);
    }

    /**
     * Associe une valeur numérique à la qualité des fèves.
     * HQ = 1.0, MQ = 0.75, BQ = 0.45
     */
    public double valeurQualiteFeve(Feve feve) {
        if (feve.getGamme() == Gamme.HQ) {
            return 1.0;
        } else if (feve.getGamme() == Gamme.MQ) {
            return 0.75;
        } else {
            return 0.45;
        }
    }


    public double noteQualiteChocolat(Feve feve, double pourcentageCacao) {
        return pourcentageCacao + 3.0 * valeurQualiteFeve(feve);

    }

    /**
     * Détermine la gamme du chocolat à partir de la note.
     * BQ : [1.8 ; 2.58[
     * MQ : [2.58 ; 3.575[
     * HQ : [3.575 ; 4]
     */
    public Gamme gammeDepuisNote(double noteQualite) {
        if (noteQualite >= 3.575 && noteQualite <= 4.0) {
            return Gamme.HQ;
        } else if (noteQualite >= 2.58 && noteQualite < 3.575) {
            return Gamme.MQ;
        } else if (noteQualite >= 1.8 && noteQualite < 2.58) {
            return Gamme.BQ;
        } else {
            return null;
        }
    }

    /**
     * Vérifie si le pourcentage de cacao  dans le chocolat respecte le minimum requis
     * pour la gamme du chocolat.
     */
    public boolean pourcentageValidePourGamme(Gamme gamme, double pourcentageCacao) {
        if (gamme == Gamme.BQ) {
            return pourcentageCacao >= 0.45;
        } else if (gamme == Gamme.MQ) {
            return pourcentageCacao >= 0.60;
        } else if (gamme == Gamme.HQ) {
            return pourcentageCacao >= 0.80;
        }
        return false;
    }

    /**
     * Détermine le chocolat correspondant à partir :
     * - de la fève
     * - du pourcentage de cacao
     *
     * Le chocolat garde le label équitable seulement si la fève est équitable.
     */
    public Chocolat chocolatCorrespondant(Feve feve, double pourcentageCacao) {
        if (feve == null) {
            return null;
        }

        // règle générale : % cacao >= 0.45
        if (pourcentageCacao < 0.45) {
            return null;
        }

        double noteQualite = noteQualiteChocolat(feve, pourcentageCacao);
        Gamme gammeChocolat = gammeDepuisNote(noteQualite);

        if (gammeChocolat == null) {
            return null;
        }

        if (!pourcentageValidePourGamme(gammeChocolat, pourcentageCacao)) {
            return null;
        }

        boolean equitable = feve.isEquitable();
        return Chocolat.get(gammeChocolat, equitable);
    }

    /**
     * Qualité perçue du chocolat.
     *
     * Formule choisie :
     * qualité perçue = note qualité technique
     *                 + bonus équitable éventuel
     *                 + impact marque
     *                 + impact cacao
     *
     * noteMarque doit être donnée par votre stratégie (par exemple entre 0 et 1).
     */
    public double chocolatQualitePercue(Feve feve, double pourcentageCacao, double noteMarque) {
        if (feve == null || pourcentageCacao < 0.45) {
            return 0.0;
        }

        double noteQualite = noteQualiteChocolat(feve, pourcentageCacao);
        double bonusEquitable = feve.isEquitable() ? GAIN_QUALITE_EQUITABLE : 0.0;
        double impactMarque = IMPACT_MARQUE_QUALITE_PERCUE * noteMarque;
        double impactCacao = IMPACT_CACAO_QUALITE_PERCUE * pourcentageCacao;

        return noteQualite + bonusEquitable + impactMarque + impactCacao;
    }

    /**
     * Date de péremption en mois selon le pourcentage de cacao.
     */
    public int dureePeremption(double pourcentageCacao) {
        if (pourcentageCacao >= 0.45 && pourcentageCacao < 0.60) {
            return 9;
        } else if (pourcentageCacao >= 0.60 && pourcentageCacao <= 0.80) {
            return 12;
        } else {
            return 24;
        }
    }

    /**
     * Vérifie si la transformation est possible.
     */
    public boolean peutTransformer(Feve feve, double quantite, double pourcentageCacao) {
        if (feve == null || quantite <= 0.0) {
            return false;
        }

        if (this.stockFeve.getQuantite(feve) < quantite) {
            return false;
        }

        if (quantite > capaciteProduction()) {
            return false;
        }

        Chocolat choco = chocolatCorrespondant(feve, pourcentageCacao);
        return choco != null;
    }
    /*cout de production d'une quantité définie de chocolat sur une période */
    public double coutTransformation(double quantiteChocolatProduite) {
       if (quantiteChocolatProduite <= 0) {
           return 0.0;
    }

    


       double coutEnergie = quantiteChocolatProduite * 500.0 * 0.18;

    

       return coutEnergie;
}

    /**
     * Transforme une quantité de fèves en chocolat.
     *
     * Hypothèse simple :
     * 1 tonne de fèves -> 1 tonne de chocolat
     */
    public Chocolat transformer(Feve feve, double quantiteFevesUtilisee, double pourcentageCacao, double noteMarque) {
       if (!peutTransformer(feve, quantiteFevesUtilisee, pourcentageCacao)) {
           this.journal.ajouter("Transformation impossible : " + quantiteFevesUtilisee + " T de " + feve
                   + " avec pourcentage cacao = " + pourcentageCacao);
           return null;
       }

       Chocolat chocolatProduit = chocolatCorrespondant(feve, pourcentageCacao);
       if (chocolatProduit == null) {
           this.journal.ajouter("Aucun chocolat correspondant pour " + feve
                   + " avec pourcentage cacao = " + pourcentageCacao);
           return null;
       }

       double qualitePercue = chocolatQualitePercue(feve, pourcentageCacao, noteMarque);

    // Règle : pour 100 T de chocolat à p% cacao, il faut 100*p T de fèves
    // Donc : quantité de chocolat produite = quantité de fèves utilisée / p
       double quantiteChocolatProduite = quantiteFevesUtilisee / pourcentageCacao;

    // Retrait des fèves du stock
       this.stockFeve.retirerQuantite(feve, quantiteFevesUtilisee);

    // Ajout du chocolat au stock
       this.stockChocolat.ajouterQuantite(chocolatProduit, quantiteChocolatProduite);

    // Coût calculé à partir des fèves utilisées et du chocolat produit
       double cout = coutTransformation(quantiteChocolatProduite);

       this.journal.ajouter("Transformation de " + quantiteFevesUtilisee + " T de " + feve
               + " en " + quantiteChocolatProduite + " T de " + chocolatProduit
               + " | % cacao = " + pourcentageCacao
               + " | qualité perçue = " + qualitePercue
               + " | péremption = " + dureePeremption(pourcentageCacao) + " mois"
               + " | coût estimé = " + cout + " €");
        Filiere.LA_FILIERE.getBanque().payerCout(this, this.cryptogramme,"couts totaux", cout);

       return chocolatProduit;

}
    /**
     * Version simple appelée à chaque étape.
     *
     * Ici on prend un pourcentage fixe par défaut selon la gamme de la fève :
     * - BQ -> 0.45
     * - MQ -> 0.60
     * - HQ -> 0.80
     *
     * noteMarque par défaut = 1.0
     */
    public void next() {
        super.next();
        double capaciteRestante = capaciteProduction();
        double noteMarque = 1.0;

        for (Feve feve : this.stockFeve.getFeves()) {
            if (capaciteRestante <= 0.0) {
                break;
            }

            double stock = this.stockFeve.getQuantite(feve);
            if (stock <= 0.0) {
                continue;
            }

            double pourcentageCacao;
            if (feve.getGamme() == Gamme.BQ) {
                pourcentageCacao = 0.45;
            } else if (feve.getGamme() == Gamme.MQ) {
                pourcentageCacao = 0.60;
            } else {
                pourcentageCacao = 0.80;
            }

            double quantiteATransformer = Math.min(stock, capaciteRestante);

            Chocolat choco = transformer(feve, quantiteATransformer, pourcentageCacao, noteMarque);
            if (choco != null) {
                capaciteRestante -= quantiteATransformer;
            }
        }
        
    }
}