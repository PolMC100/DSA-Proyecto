package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Arma;
import edu.upc.dsa.domain.models.Complemento;
import edu.upc.dsa.domain.models.Granada;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CatalogoManagerImpl implements CatalogoManager{
    private static final Logger logger = LogManager.getLogger(CatalogoManagerImpl.class);
    private static CatalogoManager instance;

    protected HashMap<String,Arma> armasCatalogo;
    protected HashMap<String,Granada> granadasCatalogo;
    protected HashMap<String,Complemento> complementosCatalogo;

    private CatalogoManagerImpl() {
        this.armasCatalogo = new HashMap<>();
        this.complementosCatalogo = new HashMap<>();
        this.granadasCatalogo = new HashMap<>();

        // Inicializar catálogo prefabricado (armas, granadas, complementos ya hechos)
        cargarCatalogoInicial();
    }

    public static CatalogoManager getInstance() {
        logger.info("getInstance(): Instancia de CatalogoManager creada");
        if (instance == null) instance = new CatalogoManagerImpl();
        return instance;
    }

    public void clear() {
        this.armasCatalogo.clear();
        this.granadasCatalogo.clear();
        this.complementosCatalogo.clear();
        logger.info("clear(): Borrados todos los valores");
    }

    @Override
    public HashMap<String, Arma> getArmasCatalogo() {
        logger.info("getArmasCatalogo(): Devuelto catalogo con "+armasCatalogo.size()+" armas"  );
        return this.armasCatalogo;
    }

    @Override
    public HashMap<String, Granada> getGranadasCatalogo() {
        logger.info("getGranadasCatalogo(): Devuelto catalogo con "+granadasCatalogo.size()+" granadas");
        return this.granadasCatalogo;
    }

    @Override
    public HashMap<String, Complemento> getComplementosCatalogo() {
        logger.info("getComplementosCatalogo(): Devuelto catalogo con "+complementosCatalogo.size()+" complementos");
        return this.complementosCatalogo;
    }

    @Override
    public void addArmaCatalogo(Arma arma) {
        logger.info("addArmaCatalogo(): Añadida arma ("+arma.getNombre()+") al catalogo");
        this.armasCatalogo.put(arma.getNombre(), arma);
    }

    @Override
    public void addGranadaCatalogo(Granada granada) {
        logger.info("addGranadaCatalogo(): Añadida granada ("+granada.getNombre()+") al catalogo");
        this.granadasCatalogo.put(granada.getNombre(), granada);
    }

    @Override
    public void addComplementoCatalogo(Complemento complemento) {
        logger.info("addComplementoCatalogo(): Añadido complemento ("+complemento.getNombre()+") al catalogo");
        this.complementosCatalogo.put(complemento.getNombre(), complemento);
    }

    public void cargarCatalogoInicial() {
        addArmaCatalogo(new Arma("Pistola", 15, 3, 25, 300, 0));
        addArmaCatalogo(new Arma("Escopeta", 8, 1, 80, 800, 2));
        addArmaCatalogo(new Arma("Rifle", 30, 5, 45, 1200, 1));
        addArmaCatalogo(new Arma("Ametralladora", 100, 10, 15, 2000, 3));
        addArmaCatalogo(new Arma("Francotirador", 5, 1, 100, 2500, 4));

        addGranadaCatalogo(new Granada("Fragmentación", 100, 5.0, "Explosión", 0, 500, 1));
        addGranadaCatalogo(new Granada("Humo", 0, 8.0, "Reduce la visibilidad", 15, 400, 0));
        addGranadaCatalogo(new Granada("Flash", 0, 6.0, "Ceguera temporal", 5, 600, 1));

        addComplementoCatalogo(new Complemento("Chaleco", "Defensa", "Reduce el daño recibido", 700, 2));
        addComplementoCatalogo(new Complemento("Casco", "Protección", "Reduce el daño recibido en la cabeza", 500, 1));
        addComplementoCatalogo(new Complemento("Jetpack", "Movilidad", "Permite realizar saltos más altos y prolongados", 2000, 5));
        logger.info("cargarCatalogoInicial(): cargados catalogos de armas, granadas y complementos");
    }
}
