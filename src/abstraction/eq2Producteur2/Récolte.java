package abstraction.eq2Producteur2;

/** @author Paul */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;

public class Récolte extends Producteur2Acteur {

    protected List<Plantation> plantations;
    protected HashMap<Feve, Double> feve_recolte;
    protected HashMap<Feve, Double> cout_recolte;
    protected HashMap<Feve, Double> cout_equitable_accumule; // Coûts équitables accumulés par fève
    private Journal Journalterrains;
    private Journal JournalRecolte;

    public Récolte() {
        super();
        this.plantations = new ArrayList<>();
        this.feve_recolte = new HashMap<Feve, Double>();
        this.cout_recolte = new HashMap<Feve, Double>();
        this.cout_equitable_accumule = new HashMap<Feve, Double>();
        this.JournalRecolte = new Journal("Journal Recolte Eq2", this);
        this.Journalterrains = new Journal("Journal Terrains Eq2", this);

        for (Feve f : Feve.values()) {
            this.feve_recolte.put(f, 0.0);
            this.cout_recolte.put(f, 0.0);
            this.cout_equitable_accumule.put(f, 0.0);
        }
    }

    /**
     * Ajoute une plantation à la liste.
     */
    public void ajouterPlantation(Plantation p) {
        plantations.add(p);
    }

    /**
     * Produit des fèves à partir de toutes les plantations.
     */
    public void recolteParStep() {
        double Prod_BQ = 0;
        double Prod_MQ = 0;
        double Prod_HQ = 0;
        double Prod_HQ_E = 0;
        double cout_BQ = 0;
        double cout_MQ = 0;
        double cout_HQ = 0;
        double cout_HQ_E = 0;
        for (Plantation p : plantations) {
            switch (p.getTypeFeve()) {
                case F_BQ:
                    Prod_BQ += p.prodPlantation();
                    cout_BQ += p.getcout();
                    break;
                case F_MQ:
                    Prod_MQ += p.prodPlantation();
                    cout_MQ += p.getcout();
                    break;
                case F_HQ:
                    Prod_HQ += p.prodPlantation();
                    cout_HQ += p.getcout();
                    break;
                case F_HQ_E:
                    Prod_HQ_E += p.prodPlantation();
                    cout_HQ_E += p.getcout();
                    break;
                default:
                    throw new IllegalArgumentException("Type de fève non reconnu !");
            }

        }
        this.feve_recolte.put(Feve.F_BQ, Prod_BQ);
        this.feve_recolte.put(Feve.F_MQ, Prod_MQ);
        this.feve_recolte.put(Feve.F_HQ, Prod_HQ);
        this.feve_recolte.put(Feve.F_HQ_E, Prod_HQ_E);
        this.cout_recolte.put(Feve.F_BQ, cout_BQ);
        this.cout_recolte.put(Feve.F_MQ, cout_MQ);
        this.cout_recolte.put(Feve.F_HQ, cout_HQ);
        this.cout_recolte.put(Feve.F_HQ_E, cout_HQ_E);
        JournalRecolte.ajouter(Filiere.LA_FILIERE.getEtape() + " : Recolte de " + Prod_BQ + " t de BQ, " + Prod_MQ
                + " t de MQ, " + Prod_HQ + " t de HQ et " + Prod_HQ_E + " t de HQ_E");
    }

    public void cout_plantations() {
        double cout = 0;
        HashMap<Feve, Double> coutParFeve = new HashMap<Feve, Double>();
        for (Feve f : Feve.values()) {
            coutParFeve.put(f, 0.0);
        }
        for (Plantation p : plantations) {
            double coutPlantation = p.getcout();

            // Ajuster le coût si la plantation est équitable
            if (p.estEquitable()) {
                coutPlantation = p.getCoutOuvriersEquitable();
            }

            cout += coutPlantation;
            Feve feve = p.getTypeFeve();
            coutParFeve.put(feve, coutParFeve.getOrDefault(feve, 0.0) + coutPlantation);
        }
        Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme,
                "Cout lié aux plantations (main d'oeuvre, achat, replantation) ", cout);
        JournalBanque.ajouter(
                Filiere.LA_FILIERE.getEtape() + " : Cout plantations BQ=" + coutParFeve.getOrDefault(Feve.F_BQ, 0.0)
                        + " | MQ=" + coutParFeve.getOrDefault(Feve.F_MQ, 0.0)
                        + " | HQ=" + coutParFeve.getOrDefault(Feve.F_HQ, 0.0)
                        + " | HQ_E=" + coutParFeve.getOrDefault(Feve.F_HQ_E, 0.0)
                        + " | Total=" + cout);
    }

    /**
     * Gère les coûts supplémentaires pour les certifications équitables (label)
     */
    public void gererCoutsEquitables() {
        double coutLabelTotal = 0.0;
        int etape = Filiere.LA_FILIERE.getEtape();

        for (Plantation p : plantations) {
            if (p.estEquitable() && p.verifierConditionsEquitables()) {
                double coutLabel = p.getCoutLabelEquitable(etape);
                if (coutLabel > 0) {
                    coutLabelTotal += coutLabel;
                    this.cout_equitable_accumule.put(Feve.F_HQ_E,
                            this.cout_equitable_accumule.get(Feve.F_HQ_E) + coutLabel);
                }
            }
        }

        if (coutLabelTotal > 0) {
            Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Coût label équitable", coutLabelTotal);
            JournalBanque.ajouter(Filiere.LA_FILIERE.getEtape() + " : Coût label équitable = " + coutLabelTotal + " €");
        }
    }

    public boolean seuil_replante(Feve f) {
        double stock_f = stockvar.get(f).getValeur();
        double prod_f = fevesSeches.get(f);
        if (stock_f <= 2 * prod_f) {
            return true;
        } else {
            return false;
        }
    }

    public void action_replante() {
        for (Plantation p : plantations) {
            // Récupérer le stock actuel de la fève de cette gamme
            double stockFeve = stockvar.get(p.getTypeFeve()).getValeur();

            if (p.Replante(stockFeve)) {
                // Replantation réussie (stock <= stock_max et arbre mort)
                Journalterrains.ajouter(
                        Filiere.LA_FILIERE.getEtape() + " : Replantation de " + p.getParcelles() + " parcelles de "
                                + p.getTypeFeve() + " (stock: " + stockFeve + " t, max: " + p.getStock_max() + " t)");
            } else if (p.estMorte() && stockFeve > p.getStock_max()) {
                // Arbre mort mais stock trop élevé : pas de replantation
                Journalterrains.ajouter(Filiere.LA_FILIERE.getEtape() + " : Pas de replantation de " + p.getTypeFeve()
                        + " car le stock (" + stockFeve + " t) dépasse le seuil maximum (" + p.getStock_max() + " t)");
            }
        }
    }

    /**
     * Retourne le nombre de parcelles par type de feve.
     */
    public void get_nb_plantations() {
        double nb_BQ = 0;
        double nb_MQ = 0;
        double nb_HQ = 0;
        double nb_HQ_E = 0;
        for (Plantation p : plantations) {
            if (p.estMorte() == false) {
                switch (p.getTypeFeve()) {
                    case F_BQ:
                        nb_BQ += p.getParcelles();
                        break;
                    case F_MQ:
                        nb_MQ += p.getParcelles();
                        break;
                    case F_HQ:
                        nb_HQ += p.getParcelles();
                        break;
                    case F_HQ_E:
                        nb_HQ_E += p.getParcelles();
                        break;
                    default:
                        throw new IllegalArgumentException("Type de fève non reconnu !");
                }
            }
        }
        Journalterrains.ajouter(Filiere.LA_FILIERE.getEtape() + " Nombre de parcelles avec arbres : " + nb_BQ + " BQ, "
                + nb_MQ + " MQ, " + nb_HQ + " HQ et " + nb_HQ_E + " HQ_E");
    }

    public void certifierPlantationsEquitable(double pourcentageACertifier, int nombreOuvriers, double salaireMini,
            double coutLabel) {
        if (pourcentageACertifier < 0 || pourcentageACertifier > 100) {
            JournalBanque.ajouter(Filiere.LA_FILIERE.getEtape() + " : Erreur - pourcentage doit être entre 0 et 100");
            return;
        }

        // Compter les plantations HQ
        List<Plantation> plantationsHQATraiter = new ArrayList<>();
        for (Plantation p : plantations) {
            if (p.getTypeFeve() == Feve.F_HQ && !p.estEquitable()) {
                plantationsHQATraiter.add(p);
            }
        }

        if (plantationsHQATraiter.isEmpty()) {
            JournalBanque
                    .ajouter(Filiere.LA_FILIERE.getEtape() + " : Aucune plantation HQ disponible pour certification");
            return;
        }

        // Certifier un pourcentage de chaque plantation HQ
        int totalParcellesCertifiees = 0;
        int totalParcellesNonCertifiees = 0;

        for (Plantation plantationHQ : plantationsHQATraiter) {
            // Diviser la plantation
            Plantation plantationNonCertifiee = plantationHQ.diviserPlantation(pourcentageACertifier);

            if (plantationNonCertifiee != null) {
                plantations.add(plantationNonCertifiee);
                totalParcellesNonCertifiees += plantationNonCertifiee.getParcelles();

                plantationHQ.activerCertificationEquitable(nombreOuvriers, salaireMini, coutLabel, 100.0);
                totalParcellesCertifiees += plantationHQ.getParcelles();

                Journalterrains
                        .ajouter(Filiere.LA_FILIERE.getEtape() + " : Plantation divisée - " + totalParcellesCertifiees
                                + " certifiées (équitable), " + totalParcellesNonCertifiees + " non-certifiées");
            }
        }

        JournalBanque.ajouter(Filiere.LA_FILIERE.getEtape() + " : Certification équitable - " + pourcentageACertifier
                + "% des parcelles HQ | Certifiées: " + totalParcellesCertifiees + " | Non-certifiées: "
                + totalParcellesNonCertifiees);
    }

    public void initialiser() {
        super.initialiser();
        int age_init = 72;
        ajouterPlantation(new Plantation(Feve.F_BQ, 200000, age_init));
        ajouterPlantation(new Plantation(Feve.F_MQ, 500000, age_init));
        ajouterPlantation(new Plantation(Feve.F_HQ, 300000, age_init));
        ajouterPlantation(new Plantation(Feve.F_HQ_E, 0, age_init));

        // Certifier automatiquement 10% des parcelles HQ en équitable
        // Paramètres : 10% certifiés, 30 ouvriers, 4€/jour, 1000€ tous les 2 steps
        this.certifierPlantationsEquitable(10.0, 30, 4.0, 1000.0);
        Journalterrains.ajouter("Initialisation - 10% des parcelles HQ certifiées équitables");
    }

    public void next() {
        action_replante();
        recolteParStep();
        cout_plantations();
        gererCoutsEquitables();
        get_nb_plantations();
        for (Plantation p : plantations) {
            p.add_age();
        }
        super.next();
    }

    public List<Journal> getJournaux() {
        List<Journal> res = super.getJournaux();
        res.add(JournalRecolte);
        res.add(Journalterrains);
        return res;
    }

    /**
     * Retourne les coûts équitables accumulés pour une fève donnée
     */
    public double getCoutEquitableAccumule(Feve f) {
        return this.cout_equitable_accumule.getOrDefault(f, 0.0);
    }
}
