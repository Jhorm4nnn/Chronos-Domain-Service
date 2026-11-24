/*
 * Interfaz web de Chronos: otro adaptador de entrada sobre la misma API REST (RNF-01).
 * El token se obtiene en AuthCore y viaja en la cabecera Authorization; nunca en cookies.
 */
'use strict';

const DIAS = { MONDAY: 'Lunes', TUESDAY: 'Martes', WEDNESDAY: 'Miércoles', THURSDAY: 'Jueves',
    FRIDAY: 'Viernes', SATURDAY: 'Sábado', SUNDAY: 'Domingo' };
const ORDEN_DIAS = Object.keys(DIAS);
const COLORES = ['#2e75b6', '#7a4fb5', '#1e7e45', '#c05621', '#b42375', '#0f7c8a', '#5b6b1e', '#8a5a00'];
const HORA_INICIO = 6, HORA_FIN = 21, PX_POR_HORA = 44;

const estado = {
    token: sessionStorage.getItem('chronos.token'),
    authUrl: localStorage.getItem('chronos.authUrl') || `${location.protocol}//${location.hostname}:8081`,
    clientes: [],
    profesionales: [],
    modo: 'lista',
    lunes: lunesDe(new Date()),
};

const $ = (id) => document.getElementById(id);

/* ---------- utilidades ---------- */

function el(tag, attrs = {}, ...hijos) {
    const nodo = document.createElement(tag);
    for (const [k, v] of Object.entries(attrs)) {
        if (k === 'class') nodo.className = v;
        else if (k.startsWith('on')) nodo.addEventListener(k.slice(2), v);
        else if (v !== undefined && v !== null && v !== false) nodo.setAttribute(k, v === true ? '' : v);
    }
    for (const h of hijos.flat()) {
        if (h === null || h === undefined || h === false) continue;
        nodo.append(h instanceof Node ? h : document.createTextNode(String(h)));
    }
    return nodo;
}

function iso(fecha) {
    const p = (n) => String(n).padStart(2, '0');
    return `${fecha.getFullYear()}-${p(fecha.getMonth() + 1)}-${p(fecha.getDate())}`;
}

function lunesDe(fecha) {
    const d = new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate());
    const dia = (d.getDay() + 6) % 7;           // 0 = lunes
    d.setDate(d.getDate() - dia);
    return d;
}

function sumarDias(fecha, dias) {
    const d = new Date(fecha);
    d.setDate(d.getDate() + dias);
    return d;
}

function hhmm(hora) {
    return hora ? hora.slice(0, 5) : '';
}

function fechaLarga(isoFecha) {
    const [a, m, d] = isoFecha.split('-').map(Number);
    return new Date(a, m - 1, d).toLocaleDateString('es-CO', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
}

function colorDe(profesionalId) {
    return COLORES[(Number(profesionalId) - 1) % COLORES.length];
}

function aviso(texto, tipo = 'ok') {
    const caja = el('div', { class: `aviso aviso-${tipo}` }, texto);
    $('avisos').append(caja);
    setTimeout(() => caja.remove(), tipo === 'ok' ? 3500 : 7000);
}

function mensajeDe(error) {
    if (!error) return 'Error desconocido';
    const detalles = Array.isArray(error.detalles) && error.detalles.length ? ` (${error.detalles.join('; ')})` : '';
    return (error.mensaje || error.error || error.message || 'Error') + detalles;
}

function leerToken(token) {
    try {
        const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        const json = decodeURIComponent(atob(base64).split('').map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join(''));
        return JSON.parse(json);
    } catch (e) {
        return null;
    }
}

/* ---------- comunicación con la API ---------- */

async function api(metodo, ruta, cuerpo) {
    const opciones = { method: metodo, headers: { Authorization: `Bearer ${estado.token}` } };
    if (cuerpo !== undefined) {
        opciones.headers['Content-Type'] = 'application/json';
        opciones.body = JSON.stringify(cuerpo);
    }
    const r = await fetch(ruta, opciones);
    const texto = await r.text();
    const datos = texto ? JSON.parse(texto) : null;
    if (r.status === 401) {
        cerrarSesion('Su sesión expiró o el token no es válido. Inicie sesión de nuevo.');
        throw { mensaje: 'No autenticado' };
    }
    if (!r.ok) throw datos || { mensaje: `HTTP ${r.status}` };
    return datos;
}

/* ---------- sesión ---------- */

async function iniciarSesion(evento) {
    evento.preventDefault();
    const error = $('loginError');
    error.classList.add('oculto');
    estado.authUrl = $('loginAuthUrl').value.trim().replace(/\/$/, '');
    localStorage.setItem('chronos.authUrl', estado.authUrl);
    try {
        const r = await fetch(`${estado.authUrl}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: $('loginUsuario').value.trim(), password: $('loginClave').value }),
        });
        const datos = await r.json().catch(() => ({}));
        if (!r.ok) throw { mensaje: datos.error || `No se pudo iniciar sesión (HTTP ${r.status})` };
        estado.token = datos.token;
        sessionStorage.setItem('chronos.token', estado.token);
        $('loginClave').value = '';
        await mostrarApp();
    } catch (e) {
        error.textContent = e instanceof TypeError
            ? `No se pudo conectar con AuthCore en ${estado.authUrl}. ¿Está en ejecución?`
            : mensajeDe(e);
        error.classList.remove('oculto');
    }
}

function cerrarSesion(motivo) {
    estado.token = null;
    sessionStorage.removeItem('chronos.token');
    $('pantallaApp').classList.add('oculto');
    $('sesion').classList.add('oculto');
    $('pantallaLogin').classList.remove('oculto');
    if (motivo) {
        $('loginError').textContent = motivo;
        $('loginError').classList.remove('oculto');
    }
}

async function mostrarApp() {
    const datos = leerToken(estado.token);
    if (!datos || (datos.exp && datos.exp * 1000 < Date.now())) {
        cerrarSesion(datos ? 'Su sesión expiró. Inicie sesión de nuevo.' : null);
        return;
    }
    const roles = Array.isArray(datos.roles) ? datos.roles : [];
    $('usuarioActual').textContent = `${datos.sub} · ${roles.join(', ') || 'sin roles'}`;
    $('avisoRol').classList.toggle('oculto', roles.includes('ADMIN'));
    $('pantallaLogin').classList.add('oculto');
    $('loginError').classList.add('oculto');
    $('pantallaApp').classList.remove('oculto');
    $('sesion').classList.remove('oculto');
    await recargarCatalogos();
    await buscarTurnos();
}

/* ---------- catálogos (clientes y profesionales) ---------- */

async function recargarCatalogos() {
    try {
        [estado.clientes, estado.profesionales] = await Promise.all([api('GET', '/api/clientes'), api('GET', '/api/profesionales')]);
    } catch (e) {
        estado.clientes = [];
        estado.profesionales = [];
        aviso(mensajeDe(e), 'error');
    }
    llenarSelect($('fCliente'), estado.clientes, 'Todos', (c) => `${c.nombre} · ${c.telefono}`);
    llenarSelect($('fProfesional'), estado.profesionales, 'Todos', (p) => p.nombre);
    llenarSelect($('tCliente'), estado.clientes, 'Seleccione un cliente', (c) => `${c.nombre} · ${c.telefono}`);
    llenarSelect($('tProfesional'), estado.profesionales, 'Seleccione un profesional', (p) => p.nombre);
    mostrarDisponibilidad();
    pintarClientes();
    pintarProfesionales();
}

function llenarSelect(select, items, vacio, texto) {
    const actual = select.value;
    select.replaceChildren(el('option', { value: '' }, vacio), ...items.map((i) => el('option', { value: i.id }, texto(i))));
    if ([...select.options].some((o) => o.value === actual)) select.value = actual;
}

function disponibilidadTexto(p) {
    return [...p.disponibilidad]
        .sort((a, b) => ORDEN_DIAS.indexOf(a.dia) - ORDEN_DIAS.indexOf(b.dia) || a.desde.localeCompare(b.desde))
        .map((f) => `${DIAS[f.dia]} ${hhmm(f.desde)}–${hhmm(f.hasta)}`)
        .join(' · ');
}

function mostrarDisponibilidad() {
    const p = estado.profesionales.find((x) => String(x.id) === $('tProfesional').value);
    $('tDisponibilidad').textContent = p ? `Atiende: ${disponibilidadTexto(p)}` : '';
}

/* ---------- agenda ---------- */

function filtrosActuales() {
    const params = new URLSearchParams();
    if ($('fProfesional').value) params.set('profesionalId', $('fProfesional').value);
    if ($('fCliente').value) params.set('clienteId', $('fCliente').value);
    if (estado.modo === 'semana') {
        params.set('desde', iso(estado.lunes));
        params.set('hasta', iso(sumarDias(estado.lunes, 6)));
    } else {
        if ($('fDesde').value) params.set('desde', $('fDesde').value);
        if ($('fHasta').value) params.set('hasta', $('fHasta').value);
    }
    if ($('fCancelados').checked) params.set('incluirCancelados', 'true');
    return params;
}

async function buscarTurnos() {
    try {
        const turnos = await api('GET', `/api/turnos?${filtrosActuales()}`);
        if (estado.modo === 'semana') pintarSemana(turnos);
        else pintarLista(turnos);
    } catch (e) {
        aviso(mensajeDe(e), 'error');
    }
}

function etiquetaEstado(t) {
    return el('span', { class: `etiqueta etiqueta-${t.estado.toLowerCase()}` }, t.estado === 'AGENDADO' ? 'Agendado' : 'Cancelado');
}

function botonesTurno(t) {
    if (t.estado !== 'AGENDADO') return [];
    return [
        el('button', { class: 'btn btn-chico', onclick: () => reprogramar(t) }, 'Reprogramar'),
        el('button', { class: 'btn btn-chico btn-peligro', onclick: () => cancelar(t) }, 'Cancelar'),
    ];
}

function pintarLista(turnos) {
    const caja = $('agendaLista');
    if (!turnos.length) {
        caja.replaceChildren(el('p', { class: 'vacio' }, 'No hay turnos con esos filtros.'));
        return;
    }
    const filas = turnos.map((t) => el('tr', { class: t.estado === 'CANCELADO' ? 'cancelado' : '' },
        el('td', {}, `#${t.id}`),
        el('td', {}, fechaLarga(t.fecha)),
        el('td', {}, `${hhmm(t.horaInicio)} – ${hhmm(t.horaFin)}`),
        el('td', {}, el('span', { style: `color:${colorDe(t.profesionalId)};font-weight:700` }, '● '), t.profesional),
        el('td', {}, t.cliente),
        el('td', { class: 'estado' }, etiquetaEstado(t)),
        el('td', { class: 'botones' }, botonesTurno(t))));
    caja.replaceChildren(el('table', {},
        el('thead', {}, el('tr', {}, ['Turno', 'Fecha', 'Hora', 'Profesional', 'Cliente', 'Estado', ''].map((h) => el('th', {}, h)))),
        el('tbody', {}, filas)),
        el('p', { class: 'ayuda' }, `${turnos.length} turno(s), ordenados por fecha y hora.`));
}

function pintarSemana(turnos) {
    const alto = (HORA_FIN - HORA_INICIO) * PX_POR_HORA;
    const hoy = iso(new Date());
    const grilla = el('div', { class: 'semana' });
    grilla.append(el('div', { class: 'semana-cabecera' }, ''));
    for (let i = 0; i < 7; i++) {
        const d = sumarDias(estado.lunes, i);
        grilla.append(el('div', { class: `semana-cabecera${iso(d) === hoy ? ' hoy' : ''}` },
            d.toLocaleDateString('es-CO', { weekday: 'short', day: 'numeric', month: 'short' })));
    }
    const horas = el('div', { class: 'semana-horas', style: `height:${alto}px` });
    for (let h = HORA_INICIO + 1; h < HORA_FIN; h++) {
        horas.append(el('span', { class: 'semana-hora', style: `top:${(h - HORA_INICIO) * PX_POR_HORA}px` }, `${String(h).padStart(2, '0')}:00`));
    }
    grilla.append(horas);
    for (let i = 0; i < 7; i++) {
        const fecha = iso(sumarDias(estado.lunes, i));
        const columna = el('div', { class: 'semana-dia', style: `height:${alto}px;background-size:100% ${PX_POR_HORA}px` });
        for (const t of turnos.filter((x) => x.fecha === fecha)) {
            const [hi, mi] = t.horaInicio.split(':').map(Number);
            const [hf, mf] = t.horaFin.split(':').map(Number);
            const top = (hi + mi / 60 - HORA_INICIO) * PX_POR_HORA;
            const altoBloque = Math.max(((hf + mf / 60) - (hi + mi / 60)) * PX_POR_HORA - 2, 18);
            columna.append(el('div', {
                class: `bloque${t.estado === 'CANCELADO' ? ' cancelado' : ''}`,
                style: `top:${top}px;height:${altoBloque}px;background:${colorDe(t.profesionalId)}`,
                title: `#${t.id} · ${hhmm(t.horaInicio)}–${hhmm(t.horaFin)} · ${t.profesional} · ${t.cliente} · ${t.estado}`,
                onclick: () => t.estado === 'AGENDADO' && reprogramar(t),
            }, el('strong', {}, `${hhmm(t.horaInicio)} ${t.cliente}`), t.profesional));
        }
        grilla.append(columna);
    }
    $('agendaSemana').replaceChildren(grilla,
        el('p', { class: 'ayuda', style: 'margin-top:10px' }, `${turnos.length} turno(s) en la semana. Clic en un turno agendado para reprogramarlo.`));
}

function cambiarModo(modo) {
    estado.modo = modo;
    document.querySelectorAll('[data-modo]').forEach((b) => b.classList.toggle('activo', b.dataset.modo === modo));
    $('navSemana').classList.toggle('oculto', modo !== 'semana');
    $('agendaLista').classList.toggle('oculto', modo !== 'lista');
    $('agendaSemana').classList.toggle('oculto', modo !== 'semana');
    $('fDesde').disabled = $('fHasta').disabled = modo === 'semana';
    actualizarTituloSemana();
    buscarTurnos();
}

function actualizarTituloSemana() {
    const fin = sumarDias(estado.lunes, 6);
    const f = (d) => d.toLocaleDateString('es-CO', { day: 'numeric', month: 'short' });
    $('tituloSemana').textContent = `${f(estado.lunes)} – ${f(fin)} ${fin.getFullYear()}`;
}

function moverSemana(dias) {
    estado.lunes = dias === 0 ? lunesDe(new Date()) : sumarDias(estado.lunes, dias);
    actualizarTituloSemana();
    buscarTurnos();
}

/* ---------- acciones sobre turnos ---------- */

function abrirModal(titulo, cuerpo, aceptar, textoAceptar = 'Aceptar') {
    $('modalTitulo').textContent = titulo;
    $('modalCuerpo').replaceChildren(...[cuerpo].flat());
    $('modalAceptar').textContent = textoAceptar;
    $('modal').classList.remove('oculto');
    $('modalAceptar').onclick = async () => {
        if (await aceptar() !== false) cerrarModal();
    };
}

function cerrarModal() {
    $('modal').classList.add('oculto');
}

function reprogramar(t) {
    const fecha = el('input', { type: 'date', value: t.fecha });
    const hora = el('input', { type: 'time', step: '300', value: hhmm(t.horaInicio) });
    const duracion = el('input', { type: 'number', min: '5', max: '480', step: '5', value: t.duracionMinutos });
    const error = el('p', { class: 'alerta alerta-error oculto' });
    abrirModal(`Reprogramar turno #${t.id}`, [
        el('p', { class: 'ayuda' }, `${t.cliente} con ${t.profesional}. Se revalida la disponibilidad y que no haya cruces.`),
        el('div', { class: 'fila' }, el('label', {}, 'Fecha', fecha), el('label', {}, 'Hora', hora), el('label', {}, 'Duración', duracion)),
        error,
    ], async () => {
        try {
            await api('PUT', `/api/turnos/${t.id}/horario`, { fecha: fecha.value, hora: hora.value, duracionMinutos: Number(duracion.value) });
            aviso(`Turno #${t.id} reprogramado`);
            buscarTurnos();
        } catch (e) {
            error.textContent = mensajeDe(e);
            error.classList.remove('oculto');
            return false;
        }
    }, 'Reprogramar');
}

function cancelar(t) {
    abrirModal(`Cancelar turno #${t.id}`,
        el('p', {}, `${fechaLarga(t.fecha)}, ${hhmm(t.horaInicio)}–${hhmm(t.horaFin)} · ${t.cliente} con ${t.profesional}. El turno queda como CANCELADO y libera el horario.`),
        async () => {
            try {
                await api('POST', `/api/turnos/${t.id}/cancelacion`);
                aviso(`Turno #${t.id} cancelado`);
                buscarTurnos();
            } catch (e) {
                aviso(mensajeDe(e), 'error');
            }
        }, 'Cancelar turno');
}

async function agendar(evento) {
    evento.preventDefault();
    const cuerpo = {
        clienteId: Number($('tCliente').value),
        profesionalId: Number($('tProfesional').value),
        fecha: $('tFecha').value,
        hora: $('tHora').value,
        duracionMinutos: $('tDuracion').value ? Number($('tDuracion').value) : null,
    };
    try {
        const t = await api('POST', '/api/turnos', cuerpo);
        aviso(`Turno #${t.id} agendado: ${fechaLarga(t.fecha)} ${hhmm(t.horaInicio)}–${hhmm(t.horaFin)}`);
        $('tHora').value = '';
    } catch (e) {
        aviso(mensajeDe(e), 'error');
    }
}

/* ---------- clientes y profesionales ---------- */

function pintarClientes() {
    const caja = $('tablaClientes');
    if (!estado.clientes.length) {
        caja.replaceChildren(el('p', { class: 'vacio' }, 'Todavía no hay clientes registrados.'));
        return;
    }
    caja.replaceChildren(el('h2', {}, `Clientes (${estado.clientes.length})`), el('table', {},
        el('thead', {}, el('tr', {}, ['Id', 'Nombre', 'Teléfono', 'Correo'].map((h) => el('th', {}, h)))),
        el('tbody', {}, estado.clientes.map((c) => el('tr', {},
            el('td', {}, `#${c.id}`), el('td', {}, c.nombre), el('td', {}, c.telefono), el('td', {}, c.correo || '—'))))));
}

function pintarProfesionales() {
    const caja = $('tablaProfesionales');
    if (!estado.profesionales.length) {
        caja.replaceChildren(el('p', { class: 'vacio' }, 'Todavía no hay profesionales registrados.'));
        return;
    }
    caja.replaceChildren(el('h2', {}, `Profesionales (${estado.profesionales.length})`), el('table', {},
        el('thead', {}, el('tr', {}, ['Id', 'Nombre', 'Disponibilidad'].map((h) => el('th', {}, h)))),
        el('tbody', {}, estado.profesionales.map((p) => el('tr', {},
            el('td', {}, `#${p.id}`),
            el('td', {}, el('span', { style: `color:${colorDe(p.id)};font-weight:700` }, '● '), p.nombre),
            el('td', {}, disponibilidadTexto(p)))))));
}

async function registrarCliente(evento) {
    evento.preventDefault();
    try {
        const c = await api('POST', '/api/clientes', {
            nombre: $('cNombre').value.trim(), telefono: $('cTelefono').value.trim(), correo: $('cCorreo').value.trim() || null,
        });
        aviso(`Cliente #${c.id} registrado: ${c.nombre}`);
        evento.target.reset();
        await recargarCatalogos();
    } catch (e) {
        aviso(mensajeDe(e), 'error');
    }
}

function agregarFranja(dia = 'MONDAY', desde = '08:00', hasta = '12:00') {
    const fila = el('div', { class: 'franja' },
        el('label', {}, 'Día', el('select', { class: 'fDia' }, ORDEN_DIAS.map((d) => el('option', { value: d, selected: d === dia }, DIAS[d])))),
        el('label', {}, 'Desde', el('input', { class: 'fDesde', type: 'time', value: desde, required: true })),
        el('label', {}, 'Hasta', el('input', { class: 'fHasta', type: 'time', value: hasta, required: true })),
        el('button', { type: 'button', class: 'btn btn-chico btn-peligro', title: 'Quitar franja', onclick: () => fila.remove() }, '✕'));
    $('pFranjas').append(fila);
}

async function registrarProfesional(evento) {
    evento.preventDefault();
    const disponibilidad = [...document.querySelectorAll('#pFranjas .franja')].map((f) => ({
        dia: f.querySelector('.fDia').value, desde: f.querySelector('.fDesde').value, hasta: f.querySelector('.fHasta').value,
    }));
    try {
        const p = await api('POST', '/api/profesionales', { nombre: $('pNombre').value.trim(), disponibilidad });
        aviso(`Profesional #${p.id} registrado: ${p.nombre}`);
        $('pNombre').value = '';
        $('pFranjas').replaceChildren();
        agregarFranja();
        await recargarCatalogos();
    } catch (e) {
        aviso(mensajeDe(e), 'error');
    }
}

/* ---------- navegación ---------- */

function cambiarVista(vista) {
    document.querySelectorAll('.pestana').forEach((b) => b.classList.toggle('activa', b.dataset.vista === vista));
    document.querySelectorAll('.vista').forEach((v) => v.classList.toggle('oculto', v.id !== `vista-${vista}`));
    if (vista === 'agenda') buscarTurnos();
}

function iniciar() {
    $('loginAuthUrl').value = estado.authUrl;
    $('formLogin').addEventListener('submit', iniciarSesion);
    $('btnSalir').addEventListener('click', () => cerrarSesion());
    document.querySelectorAll('.pestana').forEach((b) => b.addEventListener('click', () => cambiarVista(b.dataset.vista)));
    document.querySelectorAll('[data-modo]').forEach((b) => b.addEventListener('click', () => cambiarModo(b.dataset.modo)));
    $('btnBuscar').addEventListener('click', buscarTurnos);
    $('btnLimpiar').addEventListener('click', () => {
        ['fProfesional', 'fCliente', 'fDesde', 'fHasta'].forEach((id) => { $(id).value = ''; });
        $('fCancelados').checked = false;
        buscarTurnos();
    });
    $('semAnterior').addEventListener('click', () => moverSemana(-7));
    $('semSiguiente').addEventListener('click', () => moverSemana(7));
    $('semHoy').addEventListener('click', () => moverSemana(0));
    $('formTurno').addEventListener('submit', agendar);
    $('tProfesional').addEventListener('change', mostrarDisponibilidad);
    $('formCliente').addEventListener('submit', registrarCliente);
    $('formProfesional').addEventListener('submit', registrarProfesional);
    $('btnAgregarFranja').addEventListener('click', () => agregarFranja());
    $('modalCancelar').addEventListener('click', cerrarModal);
    $('tFecha').value = iso(sumarDias(new Date(), 1));
    agregarFranja();
    actualizarTituloSemana();
    if (estado.token) mostrarApp();
}

iniciar();
