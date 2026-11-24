package mr.supnum.bib;

public class LivreIndisponibleInfo {
    public String isbn;
    public String retourPrevu;

    // Constructeur vide obligatoire pour JAXB
    public LivreIndisponibleInfo() { }

    public LivreIndisponibleInfo(String isbn, String retourPrevu) {
        this.isbn = isbn;
        this.retourPrevu = retourPrevu;
    }
}