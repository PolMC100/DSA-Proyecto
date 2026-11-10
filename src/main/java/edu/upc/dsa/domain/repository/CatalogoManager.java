package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Arma;
import edu.upc.dsa.domain.models.Complemento;
import edu.upc.dsa.domain.models.Granada;
import edu.upc.dsa.domain.models.Inventario;

import java.util.HashMap;

public interface CatalogoManager {

    // --- Inicialización y Consulta de Catálogo ---
    HashMap<String, Arma> getArmasCatalogo();
    HashMap<String, Granada> getGranadasCatalogo();
    HashMap<String, Complemento> getComplementosCatalogo();

    void addArmaCatalogo(Arma arma);
    void addGranadaCatalogo(Granada granada);
    void addComplementoCatalogo(Complemento complemento);

    void clear();
}
