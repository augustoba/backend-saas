package com.saasweb.restaurante.service;

import com.saasweb.core.dto.TenantAdminDtos.TenantCreateRequest;
import com.saasweb.core.model.Sistema;
import com.saasweb.core.model.Tenant;
import com.saasweb.core.service.InicializadorDeSistema;
import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Siembra un restaurante recién creado para que funcione desde el primer
 * minuto: sucursal principal, sectores de cocina, salones con mesas, medios de
 * pago, categorías de gasto, clientes y proveedores de ejemplo, carta con
 * productos, cajas, modificadores, listas de precio y turnos de reserva.
 * Portado del DataSeeder del proyecto restaurante (sin roles ni usuarios: esos
 * los crea el núcleo con {@code PermisosRestaurante}). Todo get-or-create.
 */
@Service
@Transactional
public class InicializadorRestaurante implements InicializadorDeSistema {

    private final SucursalRepository sucursalRepository;
    private final SectorRepository sectorRepository;
    private final SalonRepository salonRepository;
    private final MesaRepository mesaRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final ClasificacionGastoRepository clasificacionGastoRepository;
    private final CategoriaGastoRepository categoriaGastoRepository;
    private final ClienteRepository clienteRepository;
    private final ProveedorRepository proveedorRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final CajaRepository cajaRepository;
    private final GrupoModificadorRepository grupoModificadorRepository;
    private final ModificadorRepository modificadorRepository;
    private final ListaPrecioRepository listaPrecioRepository;
    private final TurnoReservaRepository turnoReservaRepository;

    public InicializadorRestaurante(SucursalRepository sucursalRepository,
                                    SectorRepository sectorRepository,
                                    SalonRepository salonRepository,
                                    MesaRepository mesaRepository,
                                    MedioPagoRepository medioPagoRepository,
                                    ClasificacionGastoRepository clasificacionGastoRepository,
                                    CategoriaGastoRepository categoriaGastoRepository,
                                    ClienteRepository clienteRepository,
                                    ProveedorRepository proveedorRepository,
                                    CategoriaRepository categoriaRepository,
                                    ProductoRepository productoRepository,
                                    CajaRepository cajaRepository,
                                    GrupoModificadorRepository grupoModificadorRepository,
                                    ModificadorRepository modificadorRepository,
                                    ListaPrecioRepository listaPrecioRepository,
                                    TurnoReservaRepository turnoReservaRepository) {
        this.sucursalRepository = sucursalRepository;
        this.sectorRepository = sectorRepository;
        this.salonRepository = salonRepository;
        this.mesaRepository = mesaRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.clasificacionGastoRepository = clasificacionGastoRepository;
        this.categoriaGastoRepository = categoriaGastoRepository;
        this.clienteRepository = clienteRepository;
        this.proveedorRepository = proveedorRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.cajaRepository = cajaRepository;
        this.grupoModificadorRepository = grupoModificadorRepository;
        this.modificadorRepository = modificadorRepository;
        this.listaPrecioRepository = listaPrecioRepository;
        this.turnoReservaRepository = turnoReservaRepository;
    }

    @Override
    public boolean aplicaA(Sistema sistema) {
        return sistema == Sistema.RESTAURANTE;
    }

    @Override
    public void inicializar(Tenant tenant, TenantCreateRequest req) {
        sembrar(tenant.getId());
    }

    /** También lo usa el arranque para los restaurantes que todavía no tienen sucursal. */
    public void sembrar(String tenantId) {
        Sucursal sucursal = ensureSucursalPrincipal(tenantId);
        ensureSectores(tenantId, sucursal);
        ensureSalonesYMesas(tenantId, sucursal);
        ensureMediosDePago(tenantId);
        ensureClasificacionesYCategoriasGasto(tenantId);
        ensureClientes(tenantId);
        ensureProveedores(tenantId);
        ensureCategoriasYProductos(tenantId);
        ensureCajas(tenantId, sucursal);
        ensureModificadores(tenantId);
        ensureListasDePrecio(tenantId);
        ensureTurnosDeReserva(tenantId);
    }

    private void ensureTurnosDeReserva(String tenantId) {
        if (!turnoReservaRepository.findByTenantIdOrderByHoraInicioAsc(tenantId).isEmpty()) {
            return;
        }
        Object[][] datos = {
                {"Almuerzo", java.time.LocalTime.of(12, 0), java.time.LocalTime.of(15, 30), 40},
                {"Cena", java.time.LocalTime.of(20, 0), java.time.LocalTime.of(23, 30), 60},
        };
        for (Object[] d : datos) {
            TurnoReserva turno = new TurnoReserva();
            turno.setTenantId(tenantId);
            turno.setNombre((String) d[0]);
            turno.setHoraInicio((java.time.LocalTime) d[1]);
            turno.setHoraFin((java.time.LocalTime) d[2]);
            turno.setCapacidadMaxima((int) d[3]);
            turnoReservaRepository.save(turno);
        }
    }

    private void ensureModificadores(String tenantId) {
        if (!grupoModificadorRepository.findByTenantIdOrderByNombreAsc(tenantId).isEmpty()) {
            return;
        }

        GrupoModificador puntoCoccion = new GrupoModificador();
        puntoCoccion.setTenantId(tenantId);
        puntoCoccion.setNombre("Punto de cocción");
        puntoCoccion.setObligatorio(true);
        puntoCoccion.setMultiple(false);
        puntoCoccion.setMin(1);
        puntoCoccion.setMax(1);
        puntoCoccion = grupoModificadorRepository.save(puntoCoccion);
        crearModificador(puntoCoccion, "Jugoso", BigDecimal.ZERO);
        crearModificador(puntoCoccion, "A punto", BigDecimal.ZERO);
        crearModificador(puntoCoccion, "Bien cocido", BigDecimal.ZERO);

        GrupoModificador extras = new GrupoModificador();
        extras.setTenantId(tenantId);
        extras.setNombre("Extras");
        extras.setObligatorio(false);
        extras.setMultiple(true);
        extras.setMin(0);
        extras.setMax(3);
        extras = grupoModificadorRepository.save(extras);
        crearModificador(extras, "Queso extra", new BigDecimal("1500"));
        crearModificador(extras, "Panceta", new BigDecimal("2000"));
        crearModificador(extras, "Huevo frito", new BigDecimal("1200"));
    }

    private void crearModificador(GrupoModificador grupo, String nombre, BigDecimal precioAdicional) {
        Modificador modificador = new Modificador();
        modificador.setGrupoModificador(grupo);
        modificador.setNombre(nombre);
        modificador.setPrecioAdicional(precioAdicional);
        modificadorRepository.save(modificador);
    }

    private void ensureListasDePrecio(String tenantId) {
        if (!listaPrecioRepository.findByTenantIdOrderByNombreAsc(tenantId).isEmpty()) {
            return;
        }
        String[][] datos = {
                {"Salón", "Precios para consumo en el local"},
                {"Delivery", "Precios para pedidos a domicilio"},
                {"Take Away", "Precios para retiro en local"},
        };
        for (String[] d : datos) {
            ListaPrecio lista = new ListaPrecio();
            lista.setTenantId(tenantId);
            lista.setNombre(d[0]);
            lista.setDescripcion(d[1]);
            listaPrecioRepository.save(lista);
        }
    }

    private void ensureCajas(String tenantId, Sucursal sucursal) {
        if (!cajaRepository.findByTenantIdOrderByNombreAsc(tenantId).isEmpty()) {
            return;
        }
        for (String nombre : new String[] {"Caja Principal", "Caja Secundaria", "Caja Terraza"}) {
            Caja caja = new Caja();
            caja.setTenantId(tenantId);
            caja.setSucursalId(sucursal.getId());
            caja.setNombre(nombre);
            cajaRepository.save(caja);
        }
    }

    private void ensureMediosDePago(String tenantId) {
        if (!medioPagoRepository.findByTenantIdOrderByOrdenAsc(tenantId).isEmpty()) {
            return;
        }
        Object[][] datos = {
                {"Cuenta Corriente", false, true},
                {"Efectivo", true, false},
                {"MercadoPago", false, false},
                {"Tarjeta de Crédito", false, false},
                {"Tarjeta de Débito", false, false},
                {"Transferencia", false, false},
        };
        int orden = 0;
        for (Object[] d : datos) {
            MedioPago medioPago = new MedioPago();
            medioPago.setTenantId(tenantId);
            medioPago.setNombre((String) d[0]);
            medioPago.setEsEfectivo((boolean) d[1]);
            medioPago.setEsCuentaCorriente((boolean) d[2]);
            medioPago.setOrden(orden++);
            medioPagoRepository.save(medioPago);
        }
    }

    private void ensureClasificacionesYCategoriasGasto(String tenantId) {
        if (!clasificacionGastoRepository.findByTenantIdOrderByNombreAsc(tenantId).isEmpty()) {
            return;
        }
        String[] clasificaciones = {"Compra de Mercadería", "Gastos Administrativos", "Gastos Operacionales"};
        ClasificacionGasto[] guardadas = new ClasificacionGasto[clasificaciones.length];
        for (int i = 0; i < clasificaciones.length; i++) {
            ClasificacionGasto c = new ClasificacionGasto();
            c.setTenantId(tenantId);
            c.setNombre(clasificaciones[i]);
            guardadas[i] = clasificacionGastoRepository.save(c);
        }

        crearCategoriaGasto(tenantId, guardadas[1], "Alquileres");
        crearCategoriaGasto(tenantId, guardadas[0], "Insumos");
        crearCategoriaGasto(tenantId, guardadas[2], "Servicios");
    }

    private void crearCategoriaGasto(String tenantId, ClasificacionGasto clasificacion, String nombre) {
        CategoriaGasto categoria = new CategoriaGasto();
        categoria.setTenantId(tenantId);
        categoria.setClasificacion(clasificacion);
        categoria.setNombre(nombre);
        categoriaGastoRepository.save(categoria);
    }

    private void ensureClientes(String tenantId) {
        if (clienteRepository.countByTenantId(tenantId) > 0) {
            return;
        }
        String[][] datos = {
                {"Ana Martínez", "ana.martinez@email.com", null},
                {"Carolina Gómez", "carolina.gomez@email.com", "11-5555-0003"},
        };
        for (String[] d : datos) {
            Cliente cliente = new Cliente();
            cliente.setTenantId(tenantId);
            cliente.setNombre(d[0]);
            cliente.setEmail(d[1]);
            cliente.setTelefono(d[2]);
            clienteRepository.save(cliente);
        }
    }

    private void ensureProveedores(String tenantId) {
        if (!proveedorRepository.findByTenantIdOrderByNombreAsc(tenantId).isEmpty()) {
            return;
        }
        String[][] datos = {
                {"Verdulería Central", "+54 11 5678-1234"},
                {"Carnicería Don Pedro", "+54 11 4567-8901"},
                {"Distribuidora Norte", "+54 11 3456-7890"},
        };
        for (String[] d : datos) {
            Proveedor proveedor = new Proveedor();
            proveedor.setTenantId(tenantId);
            proveedor.setNombre(d[0]);
            proveedor.setTelefono(d[1]);
            proveedor.setSaldo(BigDecimal.ZERO);
            proveedorRepository.save(proveedor);
        }
    }

    private void ensureCategoriasYProductos(String tenantId) {
        if (!categoriaRepository.findByTenantIdOrderByOrdenAsc(tenantId).isEmpty()) {
            return;
        }
        String[] nombresCategorias = {"Insumos", "Bebidas", "Minutas y Entradas", "Carnes y Parrilla"};
        Categoria[] categorias = new Categoria[nombresCategorias.length];
        for (int i = 0; i < nombresCategorias.length; i++) {
            Categoria categoria = new Categoria();
            categoria.setTenantId(tenantId);
            categoria.setNombre(nombresCategorias[i]);
            categoria.setOrden(i);
            categorias[i] = categoriaRepository.save(categoria);
        }

        crearProducto(tenantId, categorias[1], "Agua Mineral 500ml", "BEB-005", new BigDecimal("1800"), true, 48);
        crearProducto(tenantId, categorias[2], "Alitas de Pollo BBQ", "ALIT-001", new BigDecimal("10500"), false, null);
        crearProducto(tenantId, categorias[3], "Bife de Chorizo 250g", "BIF-001", new BigDecimal("30000"), true, 20);
    }

    private void crearProducto(String tenantId, Categoria categoria, String nombre, String codigo, BigDecimal precio,
                                boolean controlaStock, Integer stockInicial) {
        Producto producto = new Producto();
        producto.setTenantId(tenantId);
        producto.setCategoria(categoria);
        producto.setNombre(nombre);
        producto.setCodigo(codigo);
        producto.setPrecio(precio);
        producto.setTipo(Producto.Tipo.VENDIBLE);
        producto.setControlaStock(controlaStock);
        producto.setStockActual(stockInicial);
        productoRepository.save(producto);
    }

    private Sucursal ensureSucursalPrincipal(String tenantId) {
        return sucursalRepository.findByTenantIdAndActivoTrue(tenantId).stream().findFirst()
                .orElseGet(() -> {
                    Sucursal sucursal = new Sucursal();
                    sucursal.setTenantId(tenantId);
                    sucursal.setNombre("Sucursal Principal");
                    return sucursalRepository.save(sucursal);
                });
    }

    private void ensureSectores(String tenantId, Sucursal sucursal) {
        if (!sectorRepository.findByTenantIdOrderByNombreAsc(tenantId).isEmpty()) {
            return;
        }
        for (String nombre : new String[] {"Barra", "Cocina Principal", "Pastelería"}) {
            Sector sector = new Sector();
            sector.setTenantId(tenantId);
            sector.setSucursalId(sucursal.getId());
            sector.setNombre(nombre);
            sector.setTipo(Sector.Tipo.NINGUNA);
            sectorRepository.save(sector);
        }
    }

    private void ensureSalonesYMesas(String tenantId, Sucursal sucursal) {
        if (!salonRepository.findByTenantIdOrderByOrdenAsc(tenantId).isEmpty()) {
            return;
        }

        String[] nombres = {"Salón Principal", "Terraza", "Salón Privado"};
        for (int i = 0; i < nombres.length; i++) {
            Salon salon = new Salon();
            salon.setTenantId(tenantId);
            salon.setSucursalId(sucursal.getId());
            salon.setNombre(nombres[i]);
            salon.setOrden(i);
            salon = salonRepository.save(salon);

            if (i == 0) {
                crearMesasDeEjemplo(salon);
            }
        }
    }

    private void crearMesasDeEjemplo(Salon salon) {
        int[][] posiciones = {{20, 20}, {140, 20}, {260, 20}, {380, 20}, {20, 140}, {140, 140}, {260, 140}};
        for (int i = 0; i < posiciones.length; i++) {
            Mesa mesa = new Mesa();
            mesa.setSalon(salon);
            mesa.setNombre(String.valueOf(i + 1));
            mesa.setForma(i < 4 ? Mesa.Forma.CUADRADA : Mesa.Forma.CIRCULAR);
            mesa.setPosX(posiciones[i][0]);
            mesa.setPosY(posiciones[i][1]);
            mesa.setCapacidad(4);
            mesaRepository.save(mesa);
        }
    }



}
