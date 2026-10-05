package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.MenuOnlineDtos.AnaliticaProducto;
import com.saasweb.restaurante.dto.MenuOnlineDtos.CartaPublica;
import com.saasweb.restaurante.dto.MenuOnlineDtos.CategoriaPublica;
import com.saasweb.restaurante.dto.MenuOnlineDtos.ComentarioRequest;
import com.saasweb.restaurante.dto.MenuOnlineDtos.ComentarioResponse;
import com.saasweb.restaurante.dto.MenuOnlineDtos.ConfigRequest;
import com.saasweb.restaurante.dto.MenuOnlineDtos.ProductoDetallePublico;
import com.saasweb.restaurante.dto.MenuOnlineDtos.ProductoPublico;
import com.saasweb.restaurante.dto.MenuOnlineDtos.RedSocialDto;
import com.saasweb.restaurante.dto.MenuOnlineDtos.ZonaEnvioPublica;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MenuOnlineService {

    private final MenuOnlineConfigRepository configRepository;
    private final SucursalRepository sucursalRepository;
    private final com.saasweb.core.repository.TenantRepository tenantRepository;
    private final ProductoRepository productoRepository;
    private final ComentarioProductoRepository comentarioRepository;
    private final ZonaEnvioRepository zonaEnvioRepository;

    public MenuOnlineService(MenuOnlineConfigRepository configRepository, SucursalRepository sucursalRepository,
                              com.saasweb.core.repository.TenantRepository tenantRepository, ProductoRepository productoRepository,
                              ComentarioProductoRepository comentarioRepository, ZonaEnvioRepository zonaEnvioRepository) {
        this.configRepository = configRepository;
        this.sucursalRepository = sucursalRepository;
        this.tenantRepository = tenantRepository;
        this.productoRepository = productoRepository;
        this.comentarioRepository = comentarioRepository;
        this.zonaEnvioRepository = zonaEnvioRepository;
    }

    public MenuOnlineConfig obtenerOCrear() {
        return configRepository.findByTenantId(tenantId()).orElseGet(() -> {
            MenuOnlineConfig config = new MenuOnlineConfig();
            config.setTenantId(tenantId());
            config.setSucursalId(sucursalPrincipal());
            config.setSlug("demo-" + UUID.randomUUID().toString().substring(0, 12));
            return configRepository.save(config);
        });
    }

    public MenuOnlineConfig actualizar(ConfigRequest request) {
        MenuOnlineConfig config = obtenerOCrear();
        if (request.formaLogo() != null) config.setFormaLogo(request.formaLogo());
        if (request.layout() != null) config.setLayout(request.layout());
        if (request.columnasMax() > 0) config.setColumnasMax(request.columnasMax());
        if (request.tipografia() != null) config.setTipografia(request.tipografia());
        config.setEncabezadoTexto(request.encabezadoTexto());
        config.setMostrarDescripciones(request.mostrarDescripciones());
        config.setMostrarDireccion(request.mostrarDireccion());

        config.getRedesSociales().clear();
        if (request.redesSociales() != null) {
            for (RedSocialDto r : request.redesSociales()) {
                MenuOnlineConfig.RedSocial red = new MenuOnlineConfig.RedSocial();
                red.setPlataforma(r.plataforma());
                red.setUrl(r.url());
                red.setNombreMostrar(r.nombreMostrar());
                config.getRedesSociales().add(red);
            }
        }
        return configRepository.save(config);
    }

    public CartaPublica cartaPublica(String slug) {
        MenuOnlineConfig config = configRepository.findByTenantIdAndSlugAndActivoTrue(TenantContext.getTenantId(), slug)
                .orElseThrow(() -> new ResourceNotFoundException("Menú no encontrado"));
        com.saasweb.core.model.Tenant tenant = tenantRepository.findById(config.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado"));
        Sucursal sucursal = sucursalRepository.findById(config.getSucursalId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada"));

        List<Producto> productos = productoRepository.findByTenantIdOrderByNombreAsc(config.getTenantId()).stream()
                .filter(Producto::isActivo)
                .filter(Producto::isDisponible)
                .filter(Producto::isDisponibleOnline)
                .filter(p -> p.getTipo() == Producto.Tipo.VENDIBLE || p.getTipo() == Producto.Tipo.COMBO)
                .toList();

        List<CategoriaPublica> categorias = productos.stream()
                .collect(Collectors.groupingBy(Producto::getCategoria))
                .entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().getOrden()))
                .map(e -> new CategoriaPublica(
                        e.getKey().getId(),
                        e.getKey().getNombre(),
                        e.getValue().stream()
                                .map(p -> new ProductoPublico(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getLikes()))
                                .toList()))
                .toList();

        List<RedSocialDto> redes = config.getRedesSociales().stream()
                .map(r -> new RedSocialDto(r.getPlataforma(), r.getUrl(), r.getNombreMostrar()))
                .toList();

        List<ZonaEnvioPublica> zonas = zonaEnvioRepository.findByTenantIdAndActivoTrueOrderByNombreAsc(config.getTenantId()).stream()
                .map(z -> new ZonaEnvioPublica(z.getId(), z.getNombre(), z.getCosto()))
                .toList();

        return new CartaPublica(tenant.getName(), sucursal.getDireccion(), sucursal.getTelefono(), config.getLogoUrl(), config.getFormaLogo(),
                config.getLayout(), config.getColumnasMax(), config.getEncabezadoTexto(), config.isMostrarDescripciones(),
                config.isMostrarDireccion(), redes, categorias, zonas);
    }

    public ProductoDetallePublico detalleProducto(String slug, Long productoId) {
        MenuOnlineConfig config = configRepository.findByTenantIdAndSlugAndActivoTrue(TenantContext.getTenantId(), slug)
                .orElseThrow(() -> new ResourceNotFoundException("Menú no encontrado"));
        Producto producto = productoDelMenu(config, productoId);

        producto.setVistas(producto.getVistas() + 1);
        productoRepository.save(producto);

        List<ComentarioResponse> comentarios = comentarioRepository
                .findByProductoIdAndActivoTrueOrderByFechaDesc(producto.getId()).stream()
                .map(ComentarioResponse::from).toList();
        return new ProductoDetallePublico(producto.getId(), producto.getNombre(), producto.getDescripcion(),
                producto.getPrecio(), producto.getLikes(), comentarios);
    }

    public int darLike(String slug, Long productoId) {
        MenuOnlineConfig config = configRepository.findByTenantIdAndSlugAndActivoTrue(TenantContext.getTenantId(), slug)
                .orElseThrow(() -> new ResourceNotFoundException("Menú no encontrado"));
        Producto producto = productoDelMenu(config, productoId);
        producto.setLikes(producto.getLikes() + 1);
        return productoRepository.save(producto).getLikes();
    }

    public ComentarioProducto comentar(String slug, Long productoId, ComentarioRequest request) {
        MenuOnlineConfig config = configRepository.findByTenantIdAndSlugAndActivoTrue(TenantContext.getTenantId(), slug)
                .orElseThrow(() -> new ResourceNotFoundException("Menú no encontrado"));
        Producto producto = productoDelMenu(config, productoId);

        ComentarioProducto comentario = new ComentarioProducto();
        comentario.setTenantId(config.getTenantId());
        comentario.setProducto(producto);
        comentario.setNombreCliente(request.nombreCliente());
        comentario.setTexto(request.texto());
        return comentarioRepository.save(comentario);
    }

    public List<ComentarioProducto> listarComentarios() {
        return comentarioRepository.findByTenantIdAndActivoTrueOrderByFechaDesc(tenantId());
    }

    public void eliminarComentario(Long id) {
        ComentarioProducto comentario = comentarioRepository.findById(id)
                .filter(c -> c.getTenantId().equals(tenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado"));
        comentario.setActivo(false);
        comentarioRepository.save(comentario);
    }

    public List<AnaliticaProducto> analiticas() {
        return productoRepository.findByTenantIdOrderByNombreAsc(tenantId()).stream()
                .sorted(Comparator.comparingInt(Producto::getVistas).reversed())
                .map(p -> new AnaliticaProducto(p.getId(), p.getNombre(), p.getVistas(), p.getLikes(),
                        comentarioRepository.countByProductoIdAndActivoTrue(p.getId())))
                .toList();
    }

    private Producto productoDelMenu(MenuOnlineConfig config, Long productoId) {
        Producto producto = productoRepository.findByIdAndTenantId(productoId, config.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        if (!producto.isDisponibleOnline() || !producto.isActivo()) {
            throw new BadRequestException("Producto no disponible en la carta online");
        }
        return producto;
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }

    private Long sucursalPrincipal() {
        return sucursalRepository.findByTenantIdAndActivoTrue(tenantId()).stream()
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("No hay sucursal activa")).getId();
    }
}
