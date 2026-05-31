package com.cjconfecciones.back.util;

public class OrdenQuery {
    public static final String PEDIDOS_FINALIZADOS_PENDIENTES =
        "SELECT c.id, c.total, tp.nombre, tp.telefono, " +
        "c.estado, COALESCE(SUM(a.valor), 0) as totalAbonado, " +
        "(c.total - COALESCE(SUM(a.valor), 0)) as saldo " +
        "FROM cjconfecciones.tpedidocabecera c " +
        "JOIN cjconfecciones.tcliente cli ON c.ccliente = cli.id " +
        "JOIN cjconfecciones.tpersona tp ON cli.idpersona = tp.cedula " +
        "LEFT JOIN cjconfecciones.tabono a ON a.ccabecera = c.id " +
        "WHERE c.estadoconfeccion = 3 " +
        "AND c.estado IN ('A', 'AB') " +
        "GROUP BY c.id, c.total, tp.nombre, tp.telefono, c.estado " +
        "ORDER BY c.id DESC";
}
