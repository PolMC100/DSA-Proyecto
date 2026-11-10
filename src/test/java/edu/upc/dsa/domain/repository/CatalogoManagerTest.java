package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CatalogoManagerTest {
    private CatalogoManager cm;

    @Before
    public void setUp() {
        cm = CatalogoManagerImpl.getInstance();
    }

    @After
    public void tearDown() {
        cm.clear();
    }

    @Test
    public void testCargarCatalogoInicial() {
        Assert.assertEquals(5, cm.getArmasCatalogo().size());
        Assert.assertEquals(3, cm.getGranadasCatalogo().size());
        Assert.assertEquals(3, cm.getComplementosCatalogo().size());
    }

    @Test
    public void testAdd() {
        Arma arma = new Arma("Example", 15, 3, 25, 300, 0);
        cm.addArmaCatalogo(arma);
        Assert.assertEquals(6, cm.getArmasCatalogo().size());
        Assert.assertTrue(cm.getArmasCatalogo().containsKey(arma.getNombre()));

        Granada granada = new Granada("Example", 0, 5.0, "", 15, 400, 0);
        cm.addGranadaCatalogo(granada);
        Assert.assertEquals(4, cm.getGranadasCatalogo().size());
        Assert.assertTrue(cm.getGranadasCatalogo().containsKey(granada.getNombre()));

        Complemento complemento = new Complemento("Example", "", "", 2, 0);
        cm.addComplementoCatalogo(complemento);
        Assert.assertEquals(4, cm.getComplementosCatalogo().size());
        Assert.assertTrue(cm.getComplementosCatalogo().containsKey(complemento.getNombre()));
    }
}
