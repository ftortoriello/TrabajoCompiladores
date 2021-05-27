package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.Division;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.Multiplicacion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.Resta;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.Suma;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.EnteroAFlotante;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.FlotanteAEntero;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Variable;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Asignacion;


public class ValidadorTipos extends Transformer{

    private Alcance alcance_actual;

    public Programa procesar(Programa programa) throws ExcepcionDeTipos {
        this.alcance_actual = programa.getCuerpo().getAlcance();
        return programa.accept_transfomer(this);
    }

    private static Tipo tipo_comun(Tipo tipo_1, Tipo tipo_2) throws ExcepcionDeTipos{
        if (tipo_1 == tipo_2){
            return tipo_1;
        }
        if(tipo_1 == Tipo.INTEGER && tipo_2 == Tipo.FLOAT){
            return tipo_2;
        }
        if(tipo_1 == Tipo.FLOAT && tipo_2 == Tipo.INTEGER){
            return tipo_1;
        }
        throw new ExcepcionDeTipos(
                String.format("No existe un tipo común entre %1$s y %2$s\n", tipo_1, tipo_2 ));
    }

    private static Expresion convertir_a_tipo(Expresion expresion, Tipo tipo_destino) throws ExcepcionDeTipos{
        Tipo tipo_origen = expresion.getTipo();
        if(tipo_origen == tipo_destino){
            return expresion;
        }
        if(tipo_origen == Tipo.INTEGER && tipo_destino == Tipo.FLOAT){
            return new EnteroAFlotante(expresion);
        }
        if(tipo_origen == Tipo.FLOAT && tipo_destino == Tipo.INTEGER){
            return new FlotanteAEntero(expresion);
        }
        throw new ExcepcionDeTipos(
                String.format("No existe un tipo común entre %1$s y %2$s\n", tipo_origen, tipo_destino ));
    }

    @Override
    public Asignacion transform(Asignacion a) throws ExcepcionDeTipos{
        Asignacion asignacion = super.transform(a);
        asignacion.setExpresion(convertir_a_tipo(asignacion.getExpresion(), asignacion.getIdent().getTipo()));
        return asignacion;
    }

    private OperacionUnaria transformarOperacionUnaria(OperacionUnaria ou) throws ExcepcionDeTipos{
        if(ou.getTipo() == Tipo.UNKNOWN){
            ou.setTipo(ou.getExpresion().getTipo());
        }else{
            ou.setExpresion(convertir_a_tipo(ou.getExpresion(), ou.getTipo()));
        }
        return ou;
    }

    private OperacionBinaria transformarOperacionBinaria(OperacionBinaria ob) throws ExcepcionDeTipos{
        Tipo tipo_en_comun = tipo_comun(ob.getIzquierda().getTipo(), ob.getDerecha().getTipo());
        ob.setIzquierda(convertir_a_tipo(ob.getIzquierda(),tipo_en_comun));
        ob.setDerecha(convertir_a_tipo(ob.getDerecha(), tipo_en_comun));
        ob.setTipo(tipo_en_comun);
        return ob;
    }

    @Override
    public Division transform(Division d) throws ExcepcionDeTipos {
        Division nueva_op = super.transform(d);
        nueva_op = (Division) transformarOperacionBinaria(nueva_op);
        return nueva_op;
    }

    @Override
    public Multiplicacion transform(Multiplicacion m) throws ExcepcionDeTipos {
        Multiplicacion nueva_op = super.transform(m);
        nueva_op = (Multiplicacion) transformarOperacionBinaria(nueva_op);
        return nueva_op;
    }

    @Override
    public Resta transform(Resta r) throws ExcepcionDeTipos {
        Resta nueva_op = super.transform(r);
        nueva_op = (Resta) transformarOperacionBinaria(nueva_op);
        return nueva_op;
    }

    @Override
    public Suma transform(Suma s) throws ExcepcionDeTipos {
        Suma nueva_op = super.transform(s);
        nueva_op = (Suma) transformarOperacionBinaria(nueva_op);
        return nueva_op;
    }

    @Override
    public FlotanteAEntero transform(FlotanteAEntero fae) throws ExcepcionDeTipos {
        FlotanteAEntero nueva_op = super.transform(fae);
        nueva_op = (FlotanteAEntero) transformarOperacionUnaria(nueva_op);
        return nueva_op;
    }

    @Override
    public EnteroAFlotante transform(EnteroAFlotante eaf) throws ExcepcionDeTipos {
        EnteroAFlotante nueva_op = super.transform(eaf);
        nueva_op = (EnteroAFlotante) transformarOperacionUnaria(nueva_op);
        return nueva_op;
    }

    @Override
    public Identificador transform(Identificador identificador) throws ExcepcionDeTipos{
        Object elemento = alcance_actual.resolver(identificador.getNombre());
        Tipo tipo = Tipo.UNKNOWN;
        if(elemento instanceof Variable){
            tipo = ((Variable) elemento).getTipo();
        }
        if (tipo != Tipo.UNKNOWN){
            identificador.setTipo(tipo);
            return identificador;
        }
        throw new ExcepcionDeTipos(String.format("No se declaró el nombre %1$s\n", identificador.getNombre()));
    }

}
