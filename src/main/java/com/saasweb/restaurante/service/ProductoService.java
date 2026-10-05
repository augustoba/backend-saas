package com.saasweb.restaurante.service;

import com.saasweb.restaurante.model.*;
import com.saasweb.restaurante.dto.*;
import com.saasweb.restaurante.repository.*;
import com.saasweb.core.common.BadRequestException;
import com.saasweb.core.common.ConflictException;
import com.saasweb.core.common.ResourceNotFoundException;
import com.saasweb.core.common.TenantContext;

import com.saasweb.restaurante.dto.ImportarDtos.ImportarResultado;
import com.saasweb.restaurante.dto.ProductoDtos.CategoriaRequest;
import com.saasweb.restaurante.dto.ProductoDtos.ProductoRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductoService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public ProductoService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findByTenantIdOrderByOrdenAsc(tenantId());
    }

    public Categoria crearCategoria(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.setTenantId(tenantId());
        categoria.setNombre(request.nombre());
        categoria.setOrden(request.orden());
        return categoriaRepository.save(categoria);
    }

    public List<Producto> listar() {
        return productoRepository.findByTenantIdOrderByNombreAsc(tenantId());
    }

    public Producto crear(ProductoRequest request) {
        Producto producto = new Producto();
        producto.setTenantId(tenantId());
        aplicar(producto, request);
        return productoRepository.save(producto);
    }

    public Producto actualizar(Long id, ProductoRequest request) {
        Producto producto = obtener(id);
        aplicar(producto, request);
        return productoRepository.save(producto);
    }

    public Producto cambiarDisponibilidad(Long id, boolean disponible) {
        Producto producto = obtener(id);
        producto.setDisponible(disponible);
        return productoRepository.save(producto);
    }

    public void archivar(Long id) {
        Producto producto = obtener(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private Producto obtener(Long id) {
        return productoRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    private void aplicar(Producto producto, ProductoRequest request) {
        Categoria categoria = categoriaRepository.findByIdAndTenantId(request.categoriaId(), tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        producto.setCategoria(categoria);
        producto.setCodigo(request.codigo());
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        if (request.tipo() != null) producto.setTipo(request.tipo());
        producto.setCosto(request.costo());
        producto.setPrecio(request.precio());
        producto.setUnidad(request.unidad());
        producto.setControlaStock(request.controlaStock());
        producto.setDisponibleOnline(request.disponibleOnline());
    }

    /**
     * Importa productos desde un CSV simple (sin comas dentro de campos):
     * cabecera {@code nombre,codigo,categoria,costo,precio,unidad}. Upsert
     * por código dentro del tenant; si no trae código, siempre crea nuevo.
     * La categoría debe existir de antes (por nombre, sin distinguir mayúsculas).
     */
    public ImportarResultado importarCsv(MultipartFile archivo) {
        Map<String, Categoria> categoriasPorNombre = new HashMap<>();
        for (Categoria c : listarCategorias()) {
            categoriasPorNombre.put(c.getNombre().trim().toLowerCase(), c);
        }

        int creados = 0;
        int actualizados = 0;
        List<String> errores = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                errores.add("El archivo está vacío");
                return new ImportarResultado(0, 0, errores);
            }

            String linea;
            int numeroFila = 1;
            while ((linea = reader.readLine()) != null) {
                numeroFila++;
                if (linea.isBlank()) continue;
                String[] campos = linea.split(",", -1);
                if (campos.length < 6) {
                    errores.add("Fila " + numeroFila + ": faltan columnas (esperado nombre,codigo,categoria,costo,precio,unidad)");
                    continue;
                }

                String nombre = campos[0].trim();
                String codigo = campos[1].trim();
                String categoriaNombre = campos[2].trim();
                String costoStr = campos[3].trim();
                String precioStr = campos[4].trim();
                String unidad = campos[5].trim();

                if (nombre.isEmpty()) {
                    errores.add("Fila " + numeroFila + ": el nombre es obligatorio");
                    continue;
                }
                Categoria categoria = categoriasPorNombre.get(categoriaNombre.toLowerCase());
                if (categoria == null) {
                    errores.add("Fila " + numeroFila + ": categoría \"" + categoriaNombre + "\" no existe");
                    continue;
                }

                Producto producto = null;
                if (!codigo.isEmpty()) {
                    producto = productoRepository.findByTenantIdOrderByNombreAsc(tenantId()).stream()
                            .filter(p -> codigo.equalsIgnoreCase(p.getCodigo()))
                            .findFirst().orElse(null);
                }

                boolean esNuevo = producto == null;
                if (esNuevo) {
                    producto = new Producto();
                    producto.setTenantId(tenantId());
                    producto.setTipo(Producto.Tipo.VENDIBLE);
                }

                producto.setNombre(nombre);
                if (!codigo.isEmpty()) producto.setCodigo(codigo);
                producto.setCategoria(categoria);
                producto.setUnidad(unidad.isEmpty() ? null : unidad);
                try {
                    if (!costoStr.isEmpty()) producto.setCosto(new BigDecimal(costoStr));
                    if (!precioStr.isEmpty()) producto.setPrecio(new BigDecimal(precioStr));
                } catch (NumberFormatException e) {
                    errores.add("Fila " + numeroFila + ": costo/precio inválido");
                    continue;
                }

                productoRepository.save(producto);
                if (esNuevo) creados++; else actualizados++;
            }
        } catch (IOException e) {
            errores.add("No se pudo leer el archivo: " + e.getMessage());
        }

        return new ImportarResultado(creados, actualizados, errores);
    }

    private String tenantId() {
        return TenantContext.getTenantId();
    }
}
