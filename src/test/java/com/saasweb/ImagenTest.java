package com.saasweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Etapa 1h: ticket de subida de imágenes, siempre dentro de la carpeta de la tienda. */
@SpringBootTest
@AutoConfigureMockMvc
class ImagenTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private String login(String dni, String pass) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"" + dni + "\",\"password\":\"" + pass + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(body).get("token").asText();
    }

    private org.springframework.test.web.servlet.ResultActions pedir(String auth, String json) throws Exception {
        return mvc.perform(post("/api/core/imagenes/subidas").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void elTicketQuedaDentroDeLaCarpetaDeLaTienda() throws Exception {
        String admin = login("11111111", "test-pass");
        pedir(admin, "{\"carpeta\":\"productos\",\"nombre\":\"remera-1\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proveedor").value("CLOUDINARY"))
                .andExpect(jsonPath("$.metodo").value("POST_FORM"))
                .andExpect(jsonPath("$.campos.folder").value("estilos-pequenos/productos"))
                .andExpect(jsonPath("$.campos.public_id").value("remera-1"))
                .andExpect(jsonPath("$.campoUrlFinal").value("secure_url"));

        // No se puede escapar de la carpeta de la tienda.
        pedir(admin, "{\"carpeta\":\"../otra-tienda\"}").andExpect(status().isBadRequest());
        // Ni subir a otra tienda sin ser superadmin.
        pedir(admin, "{\"carpeta\":\"logo\",\"slugTienda\":\"otra\"}").andExpect(status().isForbidden());
    }

    @Test
    void elSuperadminPuedeSubirElLogoDeUnaTiendaNueva() throws Exception {
        String superadmin = login("33756194", "augusto123");
        pedir(superadmin, "{\"carpeta\":\"logo\",\"nombre\":\"logo\",\"slugTienda\":\"tienda-nueva\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campos.folder").value("tienda-nueva/logo"));
    }
}
