package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.*;

import edu.upc.dsa.exceptions.DatosInvalidosException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TiendaManagerImpl implements TiendaManager{
    private static final Logger logger = LogManager.getLogger(TiendaManagerImpl.class);
    private static TiendaManager instance;
    private CatalogoManager cm;


    private TiendaManagerImpl() {
        cm = CatalogoManagerImpl.getInstance();
    }

    public static TiendaManager getInstance() {
        logger.info("getInstance(): Instancia de TiendaManager creada");
        if (instance == null) instance = new TiendaManagerImpl();
        return instance;
    }

    @Override
    public void clear() {
        cm.clear();

    }

    @Override
    public HashMap<String, Arma> getArmas() {
        HashMap<String, Arma> armas = cm.getArmasCatalogo();
        logger.info("getArmas(): Devuelta lista con "+armas.size()+" armas");
        return armas;
    }

    @Override
    public HashMap<String, Granada> getGranadas() {
        HashMap<String, Granada> granadas = cm.getGranadasCatalogo();
        logger.info("getGranadas(): Devuelta lista con "+granadas.size()+" granadas");
        return granadas;
    }

    @Override
    public HashMap<String, Complemento> getComplementos() {
        HashMap<String, Complemento> complementos = cm.getComplementosCatalogo();
        logger.info("getComplementos(): Devuelta lista con "+complementos.size()+" complementos");
        return complementos;
    }

    @Override
    public int comprarArma(Jugador jugador, String nombreArma) throws DatosInvalidosException {
        if (jugador == null) {
            logger.error("comprarArma(): Jugador inválido ");
            throw new DatosInvalidosException("Jugador invalido");
        }
        Inventario userInv = jugador.getInventario();
        Arma arma = cm.getArmasCatalogo().get(nombreArma);
        if (arma == null ) {
            logger.error("comprarArma(): No existe el arma " +nombreArma);
            throw new DatosInvalidosException("No existe el arma " + nombreArma);
        }
        else if (jugador.getNivel() < arma.getNivelRequerido()) {
            logger.error("comprarArma(): nivel insuficiente!");
            return 1;
        }
        else if (jugador.getSaldo() < arma.getPrecio()) {
            logger.error("comprarArma(): saldo insuficiente!");
            return 2;
        }
        else if (userInv.getArmas().contains(arma)) {
            logger.error("comprarArma(): el Jugador ya tiene esta arma!");
            return 3;
        }
        else {
            jugador.setSaldo(jugador.getSaldo() - arma.getPrecio());
            userInv.addArma(arma);
            logger.info("comprarArma(): "+arma.getNombre()+" comprada con éxito para jugador: "+jugador.getNombre());
            return 0;
        }
    }

    @Override
    public int comprarGranada(Jugador jugador, String nombreGranada, Integer qty) throws DatosInvalidosException{
        if (jugador == null) {
            logger.error("comprarGranada(): Jugador inválido ");
            throw new DatosInvalidosException("Jugador invalido");
        }
        Inventario userInv = jugador.getInventario();
        Granada granada = cm.getGranadasCatalogo().get(nombreGranada);
        if (granada == null ) {
            logger.error("comprarGranada(): No existe la granada " +nombreGranada);
            throw new DatosInvalidosException("No existe la granada " + nombreGranada);
        }
        else if (jugador.getNivel() < granada.getNivelRequerido()) {
            logger.error("comprarGranada(): nivel insuficiente!");
            return 1;
        }
        else if (jugador.getSaldo() < (granada.getPrecio()*qty)) {
            logger.error("comprarGranada(): saldo insuficiente!");
            return 2;
        }
        else {
            jugador.setSaldo(jugador.getSaldo() - (granada.getPrecio()*qty));
            userInv.addGranada(granada, qty);
            logger.info("comprarGranada(): "+qty+" granadas(" + granada.getNombre() + ") compradas con éxito para jugador: " + jugador.getNombre());
            return 0;
        }
    }

    @Override
    public int comprarComplemento(Jugador jugador, String nombreComplemento) throws DatosInvalidosException {
        if (jugador == null) {
            logger.error("comprarComplemento(): Jugador inválido ");
            throw new DatosInvalidosException("Jugador invalido");
        }
        Inventario userInv = jugador.getInventario();
        Complemento complemento = cm.getComplementosCatalogo().get(nombreComplemento);

        if (complemento == null ) {
            logger.error("comprarComplemento(): No existe el complemento " +nombreComplemento);
            throw new DatosInvalidosException("No existe el complemento " + nombreComplemento);
        }
        else if (jugador.getNivel() < complemento.getNivelRequerido()) {
            logger.error("comprarComplemento(): nivel insuficiente!");
            return 1;
        }
        else if (jugador.getSaldo() < complemento.getPrecio()) {
            logger.error("comprarComplemento(): saldo insuficiente!");
            return 2;
        }
        else if (userInv.getComplementos().contains(complemento)) {
            logger.error("comprarComplemento(): el Jugador ya tiene este complemento!");
            return 3;
        }
        else {
            jugador.setSaldo(jugador.getSaldo() - complemento.getPrecio());
            userInv.addComplemento(complemento);
            logger.info("comprarComplemento(): " + complemento.getNombre() + " comprado con éxito para jugador: " + jugador.getNombre());
            return 0;
        }
    }
}
