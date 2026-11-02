package com.sistematurnos.agenda.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.sistematurnos.agenda.domain.exception.ValorInvalidoException;
import com.sistematurnos.agenda.domain.vo.Franja;
import com.sistematurnos.agenda.domain.vo.RangoTiempo;
import com.sistematurnos.agenda.domain.vo.Telefono;
import com.sistematurnos.agenda.testsupport.Datos;
import java.time.DayOfWeek;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RangoTiempoYFranjaTest {

    @Test
    @DisplayName("Sin duración se asumen 30 minutos")
    void duracionPorDefecto() {
        RangoTiempo r = RangoTiempo.de(Datos.en(Datos.MANANA, "09:00"), null);
        assertEquals(Datos.en(Datos.MANANA, "09:30"), r.fin());
        assertEquals(30, r.duracionMinutos());
    }

    @Test
    void validaLaDuracion() {
        assertThrows(ValorInvalidoException.class, () -> RangoTiempo.de(Datos.en(Datos.MANANA, "09:00"), 4));
        assertThrows(ValorInvalidoException.class, () -> RangoTiempo.de(Datos.en(Datos.MANANA, "09:00"), 481));
        assertThrows(ValorInvalidoException.class, () -> RangoTiempo.de(null, 30));
        RangoTiempo.de(Datos.en(Datos.MANANA, "09:00"), 5);
        RangoTiempo.de(Datos.en(Datos.MANANA, "09:00"), 480);
    }

    @Test
    @DisplayName("Un turno no puede cruzar la medianoche ni terminar antes de empezar")
    void mismoDia() {
        assertThrows(ValorInvalidoException.class, () -> RangoTiempo.de(Datos.en(Datos.MANANA, "23:50"), 30));
        assertThrows(ValorInvalidoException.class,
                () -> new RangoTiempo(Datos.en(Datos.MANANA, "10:00"), Datos.en(Datos.MANANA, "09:00")));
    }

    @Test
    @DisplayName("Cruce: se solapan si inicio1 < fin2 y inicio2 < fin1")
    void cruces() {
        RangoTiempo base = Datos.rango(Datos.MANANA, "10:00", 30);                    // 10:00-10:30
        assertTrue(base.seCruzaCon(Datos.rango(Datos.MANANA, "10:00", 30)));          // idéntico
        assertTrue(base.seCruzaCon(Datos.rango(Datos.MANANA, "10:15", 30)));          // por detrás
        assertTrue(base.seCruzaCon(Datos.rango(Datos.MANANA, "09:45", 30)));          // por delante
        assertTrue(base.seCruzaCon(Datos.rango(Datos.MANANA, "09:00", 120)));         // lo envuelve
        assertTrue(base.seCruzaCon(Datos.rango(Datos.MANANA, "10:10", 5)));           // contenido
        assertFalse(base.seCruzaCon(Datos.rango(Datos.MANANA, "10:30", 30)));         // empieza cuando termina
        assertFalse(base.seCruzaCon(Datos.rango(Datos.MANANA, "09:30", 30)));         // termina cuando empieza
        assertFalse(base.seCruzaCon(Datos.rango(Datos.MANANA.plusDays(1), "10:00", 30)));
    }

    @Test
    void franjaContieneElRangoCompleto() {
        Franja f = new Franja(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        assertTrue(f.contiene(Datos.rango(Datos.MANANA, "08:00", 30)));
        assertTrue(f.contiene(Datos.rango(Datos.MANANA, "11:30", 30)));    // termina justo a las 12:00
        assertFalse(f.contiene(Datos.rango(Datos.MANANA, "11:45", 30)));   // se pasa 15 minutos
        assertFalse(f.contiene(Datos.rango(Datos.MANANA, "07:45", 30)));
        assertFalse(f.contiene(Datos.rango(Datos.HOY, "09:00", 30)));      // martes: otro día
    }

    @Test
    void franjaInvalida() {
        assertThrows(ValorInvalidoException.class, () -> new Franja(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(8, 0)));
        assertThrows(ValorInvalidoException.class, () -> new Franja(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(8, 0)));
        assertThrows(ValorInvalidoException.class, () -> new Franja(null, LocalTime.of(8, 0), LocalTime.of(9, 0)));
    }

    @Test
    void traslapeDeFranjas() {
        Franja a = new Franja(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        assertTrue(a.seTraslapaCon(new Franja(DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(13, 0))));
        assertFalse(a.seTraslapaCon(new Franja(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(13, 0))));
        assertFalse(a.seTraslapaCon(new Franja(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
    }

    @Test
    void telefonoSeNormaliza() {
        assertEquals("3001234567", Telefono.de("300 123-4567").valor());
        assertEquals("573001234567", Telefono.de("+57 300 1234567").valor());
        assertThrows(ValorInvalidoException.class, () -> Telefono.de("12345"));
        assertThrows(ValorInvalidoException.class, () -> Telefono.de("abc1234567"));
        assertThrows(ValorInvalidoException.class, () -> Telefono.de(" "));
    }
}
