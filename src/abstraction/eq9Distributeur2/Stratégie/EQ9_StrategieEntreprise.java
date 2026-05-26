package abstraction.eq9Distributeur2.Stratégie;

import abstraction.eq9Distributeur2.Config.EQ9Config;

public class EQ9_StrategieEntreprise {

    public boolean doitPousserVolumes(double partMarcheActuelle) {
        return partMarcheActuelle < EQ9Config.PART_MARCHE_CIBLE;
    }

    public boolean mixMarquePropreInsuffisant(double mixMP) {
        return mixMP < EQ9Config.MIX_MARQUE_PROPRE_CIBLE;
    }

    public boolean margeTropFaible(double margeBrute) {
        return margeBrute < EQ9Config.MARGE_BRUTE_MIN * 100.0; // margeBrute en %
    }
}
