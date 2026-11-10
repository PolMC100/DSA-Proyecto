package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Granada;
import edu.upc.dsa.domain.models.Jugador;
import edu.upc.dsa.exceptions.DatosInvalidosException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TiendaManagerTest {
    private TiendaManager tm;

    @Before
    public void setUp() {
        tm = TiendaManagerImpl.getInstance();
    }

    @After
    public void tearDown() {
        tm.clear();
    }

    @Test
    public void testCargarCatalogoInicial() {
        Assert.assertEquals(5, tm.getArmas().size());
        Assert.assertEquals(3, tm.getGranadas().size());
        Assert.assertEquals(3, tm.getComplementos().size());
    }

    @Test
    public void testComprar() throws DatosInvalidosException {
        Jugador jugador1 = new Jugador("J1","Jugador 1",500,3);
        Jugador jugador2 = new Jugador("J2","Jugador 2",2000,0);
        Jugador jugador3 = new Jugador("J3","Jugador 3",5000,5);

        Assert.assertEquals(0, jugador1.getInventario().getArmas().size());
        Assert.assertEquals(0, tm.comprarArma(jugador1, "Pistola"));
        Assert.assertEquals(200, jugador1.getSaldo(), 0.01);
        Assert.assertEquals(1, jugador1.getInventario().getArmas().size());

        Assert.assertEquals(1, tm.comprarGranada(jugador2, "Flash", 2));
        jugador2.setNivel(3);
        Assert.assertEquals(0, tm.comprarGranada(jugador2, "Flash", 2));
        Granada granada = tm.getGranadas().get("Flash");
        Assert.assertEquals(Integer.valueOf(2),  jugador2.getInventario().checkGranadasQty(granada));


        Assert.assertThrows(DatosInvalidosException.class, () -> tm.comprarComplemento(jugador3, "nonExistentExample"));

        Assert.assertEquals(0, tm.comprarComplemento(jugador3, "Chaleco"));
        Assert.assertEquals(1, jugador3.getInventario().getComplementos().size());
        Assert.assertEquals(3, tm.comprarComplemento(jugador3, "Chaleco"));
    }

    @Test
    public void usarGranada() throws DatosInvalidosException {
        Jugador j1 = new Jugador("J1","Jugador 1",5000,5);
        tm.comprarGranada(j1, "Humo", 2);
        Granada granada = tm.getGranadas().get("Humo");

        j1.getInventario().useGranada(granada);
        j1.getInventario().useGranada(granada);
        Assert.assertEquals(-1, j1.getInventario().useGranada(granada));
    }
}
