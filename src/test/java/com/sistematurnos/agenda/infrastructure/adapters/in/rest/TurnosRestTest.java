package com.sistematurnos.agenda.infrastructure.adapters.in.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Prueba de punta a punta (HTTP -> casos de uso -> dominio -> H2) con seguridad JWT real.
 * El secreto de abajo es SOLO de pruebas (48 bytes aleatorios en Base64); no es el de AuthCore.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "seguridad.jwt.secret-base64=Lu7e1SCfczRWZpilqgAQVZqNiJopaxOzxiu9UaLENKGTeGeBtIyfpBegmsIKQS3D6pmEiahNszQXjzxmhjabaw==")
class TurnosRestTest {

    private static final String SECRETO = "Lu7e1SCfczRWZpilqgAQVZqNiJopaxOzxiu9UaLENKGTeGeBtIyfpBegmsIKQS3D6pmEiahNszQXjzxmhjabaw==";
    private static final AtomicInteger SECUENCIA = new AtomicInteger(1000);

    @Autowired
    private MockMvc mvc;

    private String token(String... roles) {
        Instant ahora = Instant.now();
        return Jwts.builder().subject("tester").claim("uid", 99).claim("roles", List.of(roles))
                .issuedAt(Date.from(ahora)).expiration(Date.from(ahora.plus(Duration.ofHours(1))))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRETO)), Jwts.SIG.HS384).compact();
    }

    private String admin() { return token("ADMIN"); }

    private MockHttpServletRequestBuilder con(MockHttpServletRequestBuilder peticion, String token, String json) {
        MockHttpServletRequestBuilder b = peticion.header("Authorization", "Bearer " + token);
        return json == null ? b : b.contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private long id(String cuerpo) {
        return ((Number) JsonPath.read(cuerpo, "$.id")).longValue();
    }

    /** Un día de la semana futuro (al menos 2 días adelante) para que "no anterior a hoy" nunca estorbe. */
    private static LocalDate proximo(DayOfWeek dia) {
        return LocalDate.now(ZoneId.of("America/Bogota")).plusDays(2).with(TemporalAdjusters.nextOrSame(dia));
    }

    private long crearCliente() throws Exception {
        String telefono = "300" + (1000000 + SECUENCIA.incrementAndGet());
        return id(mvc.perform(con(post("/api/clientes"), admin(),
                "{\"nombre\":\"Cliente " + telefono + "\",\"telefono\":\"" + telefono + "\",\"correo\":\"c@correo.com\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    /** Lunes 08:00-12:00 y miércoles 14:00-18:00. */
    private long crearProfesional() throws Exception {
        String json = "{\"nombre\":\"Profesional " + SECUENCIA.incrementAndGet() + "\",\"disponibilidad\":["
                + "{\"dia\":\"MONDAY\",\"desde\":\"08:00\",\"hasta\":\"12:00\"},"
                + "{\"dia\":\"WEDNESDAY\",\"desde\":\"14:00\",\"hasta\":\"18:00\"}]}";
        return id(mvc.perform(con(post("/api/profesionales"), admin(), json))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private String turnoJson(long cliente, long profesional, LocalDate fecha, String hora, Integer minutos) {
        return "{\"clienteId\":" + cliente + ",\"profesionalId\":" + profesional + ",\"fecha\":\"" + fecha + "\",\"hora\":\"" + hora + "\""
                + (minutos == null ? "" : ",\"duracionMinutos\":" + minutos) + "}";
    }

    private long agendar(long cliente, long profesional, LocalDate fecha, String hora, Integer minutos) throws Exception {
        return id(mvc.perform(con(post("/api/turnos"), admin(), turnoJson(cliente, profesional, fecha, hora, minutos)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Sin token o con token falso: 401")
    void sinCredencialesEs401() throws Exception {
        mvc.perform(get("/api/turnos")).andExpect(status().isUnauthorized());
        mvc.perform(con(get("/api/turnos"), "token.falso.aqui", null)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Solo el ADMIN opera; un USER recibe 403 y lo que no es /api está denegado")
    void permisos() throws Exception {
        mvc.perform(con(get("/api/turnos"), token("USER"), null)).andExpect(status().isForbidden());
        mvc.perform(con(post("/api/clientes"), token("USER"), "{\"nombre\":\"X\",\"telefono\":\"3001111111\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(con(get("/otra/cosa"), admin(), null)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HU-8: registrar y consultar clientes y profesionales; faltantes, duplicados y 404")
    void registros() throws Exception {
        long cliente = crearCliente();
        mvc.perform(con(get("/api/clientes/" + cliente), admin(), null)).andExpect(status().isOk());
        mvc.perform(con(get("/api/clientes"), admin(), null)).andExpect(status().isOk());
        mvc.perform(con(get("/api/clientes/999999"), admin(), null)).andExpect(status().isNotFound());
        mvc.perform(con(post("/api/clientes"), admin(), "{}")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("nombre")))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("teléfono")));
        mvc.perform(con(post("/api/clientes"), admin(), "{\"nombre\":\"Repetido\",\"telefono\":\"3001234999\"}")).andExpect(status().isCreated());
        mvc.perform(con(post("/api/clientes"), admin(), "{\"nombre\":\"Repetido 2\",\"telefono\":\"300-123-4999\"}")).andExpect(status().isConflict());

        long profesional = crearProfesional();
        mvc.perform(con(get("/api/profesionales/" + profesional), admin(), null)).andExpect(status().isOk())
                .andExpect(jsonPath("$.disponibilidad.length()").value(2));
        mvc.perform(con(post("/api/profesionales"), admin(), "{\"nombre\":\"Sin horario\"}")).andExpect(status().isBadRequest());
        mvc.perform(con(post("/api/profesionales"), admin(),
                "{\"nombre\":\"Franja mala\",\"disponibilidad\":[{\"dia\":\"MONDAY\",\"desde\":\"12:00\",\"hasta\":\"08:00\"}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("HU-9: crear un turno devuelve los datos completos; valida cliente, profesional, pasado y disponibilidad")
    void creaTurnos() throws Exception {
        long c = crearCliente(), p = crearProfesional();
        LocalDate lunes = proximo(DayOfWeek.MONDAY);
        mvc.perform(con(post("/api/turnos"), admin(), turnoJson(c, p, lunes, "09:00", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("AGENDADO"))
                .andExpect(jsonPath("$.horaFin").value("09:30:00"))
                .andExpect(jsonPath("$.duracionMinutos").value(30))
                .andExpect(jsonPath("$.profesionalId").value(p));
        mvc.perform(con(post("/api/turnos"), admin(), turnoJson(999999, p, lunes, "10:00", null))).andExpect(status().isNotFound());
        mvc.perform(con(post("/api/turnos"), admin(), turnoJson(c, 999999, lunes, "10:00", null))).andExpect(status().isNotFound());
        mvc.perform(con(post("/api/turnos"), admin(), turnoJson(c, p, LocalDate.now().minusDays(3), "10:00", null)))
                .andExpect(status().isBadRequest());
        mvc.perform(con(post("/api/turnos"), admin(), turnoJson(c, p, proximo(DayOfWeek.TUESDAY), "10:00", null)))
                .andExpect(status().isConflict());   // el martes no atiende
        mvc.perform(con(post("/api/turnos"), admin(), turnoJson(c, p, lunes, "11:45", 30))).andExpect(status().isConflict());
        mvc.perform(con(post("/api/turnos"), admin(), "{\"clienteId\":" + c + "}")).andExpect(status().isBadRequest());
        mvc.perform(con(post("/api/turnos"), admin(), "{\"clienteId\":" + c + ",\"profesionalId\":" + p + ",\"fecha\":\"no\",\"hora\":\"09:00\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("HU-10: un cruce da 409 y explica con qué turno; turnos consecutivos y cancelados no estorban")
    void noSolapamiento() throws Exception {
        long c1 = crearCliente(), c2 = crearCliente(), p = crearProfesional();
        LocalDate lunes = proximo(DayOfWeek.MONDAY);
        long primero = agendar(c1, p, lunes, "10:00", 30);
        mvc.perform(con(post("/api/turnos"), admin(), turnoJson(c2, p, lunes, "10:15", 30)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("se cruza con el turno " + primero)))
                .andExpect(jsonPath("$.detalles[0]").value("turno en conflicto: " + primero));
        mvc.perform(con(post("/api/turnos"), admin(), turnoJson(c1, p, lunes, "10:10", 30))).andExpect(status().isConflict());
        agendar(c2, p, lunes, "10:30", 30);   // empieza cuando el otro termina: permitido
        mvc.perform(con(post("/api/turnos/" + primero + "/cancelacion"), admin(), null)).andExpect(status().isOk());
        agendar(c2, p, lunes, "10:00", 30);   // el cancelado liberó el horario
    }

    @Test
    @DisplayName("HU-11: cancelar y reprogramar, con el cruce validado sin contar el propio turno")
    void cancelarYReprogramar() throws Exception {
        long c = crearCliente(), p = crearProfesional();
        LocalDate lunes = proximo(DayOfWeek.MONDAY);
        long a = agendar(c, p, lunes, "09:00", 30);
        long b = agendar(c, p, lunes, "11:00", 30);

        mvc.perform(con(put("/api/turnos/" + a + "/horario"), admin(), "{\"hora\":\"11:15\"}")).andExpect(status().isConflict());
        mvc.perform(con(get("/api/turnos/" + a), admin(), null)).andExpect(jsonPath("$.horaInicio").value("09:00:00"));
        mvc.perform(con(put("/api/turnos/" + a + "/horario"), admin(), "{\"hora\":\"09:15\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.horaInicio").value("09:15:00"));
        mvc.perform(con(put("/api/turnos/" + a + "/horario"), admin(), "{}")).andExpect(status().isBadRequest());

        mvc.perform(con(post("/api/turnos/" + b + "/cancelacion"), admin(), null)).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
        mvc.perform(con(post("/api/turnos/" + b + "/cancelacion"), admin(), null)).andExpect(status().isConflict());
        mvc.perform(con(put("/api/turnos/" + b + "/horario"), admin(), "{\"hora\":\"10:00\"}")).andExpect(status().isConflict());
        mvc.perform(con(post("/api/turnos/999999/cancelacion"), admin(), null)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("HU-12: consultar por profesional, cliente y fechas; combinar filtros; cancelados solo si se piden")
    void consultas() throws Exception {
        long c1 = crearCliente(), c2 = crearCliente(), p1 = crearProfesional(), p2 = crearProfesional();
        LocalDate lunes = proximo(DayOfWeek.MONDAY);
        LocalDate miercoles = lunes.plusDays(2);   // el miércoles de esa misma semana: siempre después del lunes
        agendar(c1, p1, miercoles, "14:00", 30);
        agendar(c2, p1, lunes, "10:00", 30);
        agendar(c1, p2, lunes, "08:00", 30);
        long cancelado = agendar(c2, p2, lunes, "09:00", 30);
        mvc.perform(con(post("/api/turnos/" + cancelado + "/cancelacion"), admin(), null)).andExpect(status().isOk());

        mvc.perform(con(get("/api/turnos?profesionalId=" + p1), admin(), null)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fecha").value(lunes.toString()));      // ordenado por inicio
        mvc.perform(con(get("/api/turnos?clienteId=" + c1), admin(), null)).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(con(get("/api/turnos?profesionalId=" + p1 + "&clienteId=" + c1), admin(), null))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(con(get("/api/turnos?profesionalId=" + p2 + "&desde=" + lunes + "&hasta=" + lunes), admin(), null))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(con(get("/api/turnos?profesionalId=" + p2 + "&incluirCancelados=true"), admin(), null))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(con(get("/api/turnos?profesionalId=" + p2 + "&desde=" + miercoles), admin(), null))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(con(get("/api/turnos?desde=" + miercoles + "&hasta=" + lunes), admin(), null)).andExpect(status().isBadRequest());
        mvc.perform(con(get("/api/turnos?profesionalId=999999"), admin(), null)).andExpect(status().isNotFound());
        mvc.perform(con(get("/api/turnos?desde=no-es-fecha"), admin(), null)).andExpect(status().isBadRequest());
    }
}
