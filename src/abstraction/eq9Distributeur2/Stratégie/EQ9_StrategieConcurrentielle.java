package abstraction.eq9Distributeur2.Stratégie;

public class EQ9_StrategieConcurrentielle {

    public double ajusterSelonConcurrence(double prixActuel, double prixConcurrent) {
        if (prixConcurrent <= 0) return prixActuel;

        double ratio = prixActuel / prixConcurrent;

        if (ratio > 1.05) {
            return prixActuel * 0.97;
        } else if (ratio > 1.02) {
            return prixActuel * 0.99;
        } else {
            return prixActuel;
        }
    }
}
