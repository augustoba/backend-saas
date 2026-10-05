package com.saasweb;

import com.saasweb.ecommerce.model.Product;
import com.saasweb.ecommerce.model.Variante;
import com.saasweb.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Etapa 2.2b: las variantes tienen identidad propia y editar el producto no las recrea. */
@SpringBootTest
class VarianteTest {

    @Autowired ProductRepository products;
    @Autowired TransactionTemplate tx;

    @Test
    void editarLasVariantesConservaIdYCodigoDeBarras() {
        String id = UUID.randomUUID().toString();
        String[] idM = new String[1];
        tx.executeWithoutResult(s -> {
            Product p = new Product();
            p.setId(id);
            p.setTenantId("t-" + id);
            p.setName("Remera");
            p.setDescription("Remera de algodón");
            p.setPrice(new BigDecimal("1000"));
            Variante m = new Variante("M", 3);
            m.setCodigoBarras("7790001112223");
            p.getSizeStocks().addAll(List.of(new Variante("S", 2), m));
            products.save(p);
            idM[0] = m.getId();
        });

        tx.executeWithoutResult(s -> {
            Product p = products.findById(id).orElseThrow();
            // Se va S, M cambia de stock y pasa primera, aparece XL.
            p.reemplazarVariantes(List.of(new Variante("M", 7), new Variante("XL", 1)));
            products.save(p);
        });

        tx.executeWithoutResult(s -> {
            List<Variante> v = products.findById(id).orElseThrow().getSizeStocks();
            assertThat(v).extracting(Variante::getNombre).containsExactly("M", "XL");
            assertThat(v.get(0).getId()).isEqualTo(idM[0]);
            assertThat(v.get(0).getCodigoBarras()).isEqualTo("7790001112223");
            assertThat(v.get(0).getStock()).isEqualTo(7);
        });
    }
}
