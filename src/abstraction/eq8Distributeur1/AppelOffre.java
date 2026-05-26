package abstraction.eq8Distributeur1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import abstraction.eqXRomu.appelDOffre.AppelDOffre;
import abstraction.eqXRomu.appelDOffre.IAcheteurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.appelDOffre.SuperviseurVentesAO;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

public class AppelOffre extends ContratCadre2 implements IAcheteurAO {

    private static final int TAILLE_HISTORIQUE = 10;
    private static final int TOUR_DEBUT_NEGOCIATION = 4;
    private static final double MARGE_ACCEPTATION = 1.1; // +10% max

    // Fenêtre glissante des 10 derniers prix observés par produit
    private Map<IProduit, List<Double>> historiquePrix;

    public AppelOffre() {
        super();
        this.historiquePrix = new HashMap<>();
    }

    public void next() {
        super.next();

        for (ChocolatDeMarque cdm : Filiere.LA_FILIERE.getChocolatsProduits()) {
            double stockActuel = this.Stock.getOrDefault(cdm, 0.0);
            if (stockActuel < 10000) {
                double quantiteManquante = 10000 - stockActuel;
                this.journal3.ajouter(
                    "Besoin d'approvisionnement AO : "
                    + quantiteManquante + " T de " + cdm
                );
            }
        }
    }

    public void receptionner_AO(
            IProduit p,
            double quantiteEnTonnes,
            ExemplaireContratCadre contrat) {

        double stockActuel = this.Stock.getOrDefault(p, 0.0);
        this.Stock.put(p, stockActuel + quantiteEnTonnes);
        this.journal3.ajouter("Réception AO : " + quantiteEnTonnes + " T de " + p);
    }

    public OffreVente choisirOV(List<OffreVente> propositions) {

        if (propositions == null || propositions.isEmpty()) return null;

        int tour = Filiere.LA_FILIERE.getEtape();
        OffreVente meilleure = null;

        if (tour < TOUR_DEBUT_NEGOCIATION) {
            // Phase apprentissage : on prend le moins cher sans condition
            for (OffreVente ov : propositions) {
                if (meilleure == null || ov.getPrixT() < meilleure.getPrixT()) {
                    meilleure = ov;
                }
            }
            if (meilleure != null) {
                enregistrerPrix(meilleure);
                this.journal3.ajouter(
                    "AO (apprentissage) tour " + tour
                    + " : retenu " + meilleure.getPrixT()
                    + " €/T pour " + meilleure.getProduit()
                );
            }

        } else {
            // Phase négociation : on filtre par rapport à la moyenne des 10 derniers prix
            for (OffreVente ov : propositions) {
                double prixMoyen = getPrixMoyen(ov.getProduit());
                if (ov.getPrixT() <= prixMoyen * MARGE_ACCEPTATION) {
                    if (meilleure == null || ov.getPrixT() < meilleure.getPrixT()) {
                        meilleure = ov;
                    }
                }
            }
            if (meilleure != null) {
                enregistrerPrix(meilleure);
                this.journal3.ajouter(
                    "AO (négociation) tour " + tour
                    + " : retenu " + meilleure.getPrixT()
                    + " €/T pour " + meilleure.getProduit()
                    + " (moy. " + TAILLE_HISTORIQUE + " derniers : "
                    + String.format("%.2f", getPrixMoyen(meilleure.getProduit())) + " €/T)"
                );
            } else {
                this.journal3.ajouter(
                    "AO (négociation) tour " + tour
                    + " : aucune offre acceptable pour "
                    + propositions.get(0).getProduit()
                    + " (moy. ref : "
                    + String.format("%.2f", getPrixMoyen(propositions.get(0).getProduit()))
                    + " €/T, plafond : "
                    + String.format("%.2f", getPrixMoyen(propositions.get(0).getProduit()) * MARGE_ACCEPTATION)
                    + " €/T)"
                );
            }
        }

        return meilleure;
    }

    @Override
    protected void methodeIntermediaireAchatCC(
            ChocolatDeMarque cdm,
            double besoin,
            double prixCible,
            double prixMax,
            boolean TG) {

        if (besoin < AppelDOffre.AO_QUANTITE_MIN) return;

        SuperviseurVentesAO superviseur =
            (SuperviseurVentesAO) Filiere.LA_FILIERE.getActeur("Sup.AO");

        if (superviseur == null) {
            this.journal3.ajouter("ERREUR : SuperviseurVentesAO introuvable");
            return;
        }

        OffreVente retenue = superviseur.acheterParAO(
            (IAcheteurAO) this,
            this.cryptogramme,
            cdm,
            besoin,
            TG
        );

        if (retenue != null) {
            this.journal3.ajouter(
                "AO retenu : " + retenue.getQuantiteT() + " T de " + cdm
                + " à " + retenue.getPrixT() + " €/T"
                + (TG ? " [TG]" : "")
            );
        } else {
            this.journal3.ajouter("AO sans résultat pour " + cdm);
        }
    }

    // --- Fenêtre glissante ---

    private void enregistrerPrix(OffreVente ov) {
        IProduit p = ov.getProduit();
        this.historiquePrix.putIfAbsent(p, new ArrayList<>());
        List<Double> hist = this.historiquePrix.get(p);
        hist.add(ov.getPrixT());
        // On ne garde que les TAILLE_HISTORIQUE dernières valeurs
        if (hist.size() > TAILLE_HISTORIQUE) {
            hist.remove(0);
        }
    }

    private double getPrixMoyen(IProduit p) {
        List<Double> hist = this.historiquePrix.getOrDefault(p, new ArrayList<>());
        if (hist.isEmpty()) return Double.MAX_VALUE;
        double somme = 0;
        for (double prix : hist) somme += prix;
        return somme / hist.size();
    }
}