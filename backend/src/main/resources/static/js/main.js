/* =========
   FlightOnTime — JS unificado (individual + lote)
   Ubicación: /static/js/main.js
   ========= */

// ===== CONFIG =====
const API_BASE = window.API_BASE || window.location.origin;
const ENDPOINT_INDIVIDUAL = `${API_BASE}/predict`;
const ENDPOINT_LOTE       = `${API_BASE}/predict/batch`;
const BATCH_USE_MULTIPART = true;

// ===== UTILS =====
const qs = (sel, root = document) => root.querySelector(sel);
const show = (el, on = true) => { if (el) el.style.display = on ? "" : "none"; };
const pretty = (obj) => (typeof obj === "string" ? obj : JSON.stringify(obj, null, 2));

// ===== CARGA INICIAL DE DATOS MAESTROS =====
document.addEventListener('DOMContentLoaded', function () {
    const aerolineaSelect = qs("#aerolinea");
    const origenSelect = qs("#origen");
    const destinoSelect = qs("#destino");
    const distanciaInput = qs("#distancia");

    // Hacer distancia no editable y con valor inicial
    if (distanciaInput) {
        distanciaInput.readOnly = true;
        distanciaInput.value = "1";
    }

    function llenarDatalist(datalistId, valores) {
        const datalist = document.getElementById(datalistId);
        datalist.innerHTML = ''; // limpiar
        valores.forEach(valor => {
            const option = document.createElement('option');
            option.value = valor;
            datalist.appendChild(option);
        });
    }

    // Cargar aerolíneas
    fetch('/api/aerolineas')
      .then(r => r.json())
      .then(data => llenarDatalist('aerolineas-list', data.sort()));

    // Cargar aeropuertos
    fetch('/api/aeropuertos')
      .then(r => r.json())
      .then(data => llenarDatalist('aeropuertos-list', data.sort()));

    // Función para cargar distancia
    function cargarDistancia() {
        const aerolinea = aerolineaSelect.value;
        const origen = origenSelect.value;
        const destino = destinoSelect.value;

        if (aerolinea && origen && destino) {
            fetch(`/api/ruta/distancia?aerolinea=${aerolinea}&origen=${origen}&destino=${destino}`)
                .then(response => response.json())
                .then(distancia => {
                    if (distanciaInput) distanciaInput.value = distancia;
                })
                .catch(err => {
                    console.warn("No se pudo cargar la distancia:", err);
                    if (distanciaInput) distanciaInput.value = "1";
                });
        }
    }

    // Escuchar cambios
    if (aerolineaSelect) aerolineaSelect.addEventListener('change', cargarDistancia);
    if (origenSelect) origenSelect.addEventListener('change', cargarDistancia);
    if (destinoSelect) destinoSelect.addEventListener('change', cargarDistancia);
});

// ===== TABS =====
function setupTabs() {
    const tabIndividual = qs("#tab-individual");
    const tabLote = qs("#tab-lote");
    const panelIndividual = qs("#panel-individual");
    const panelLote = qs("#panel-lote");
    if (!tabIndividual || !tabLote || !panelIndividual || !panelLote) return;

    function setTab(tab) {
        const isInd = tab === "individual";
        tabIndividual.setAttribute("aria-selected", String(isInd));
        tabLote.setAttribute("aria-selected", String(!isInd));
        panelIndividual.classList.toggle("active", isInd);
        panelLote.classList.toggle("active", !isInd);
        if (location.hash !== "#" + tab) history.replaceState(null, "", "#" + tab);
    }
    tabIndividual.addEventListener("click", () => setTab("individual"));
    tabLote.addEventListener("click", () => setTab("lote"));
    window.addEventListener("hashchange", () => {
        const h = location.hash.replace("#", "");
        setTab(h === "lote" ? "lote" : "individual");
    });

    const start = location.hash.replace("#", "") === "lote" ? "lote" : "individual";
    setTab(start);
}

// ===== INDIVIDUAL =====
function setupIndividual() {
    const form = qs("#form-individual");
    if (!form) return;

    const btn = qs("#btn-individual");
    const out = qs("#resultado-individual");
    const state = qs("#estado-individual");

    const fechaInput = qs("#fechaPartida");
    if (fechaInput && !fechaInput.value) {
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
        fechaInput.value = now.toISOString().slice(0, 16);
    }

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        if (state) { show(out, false); show(state, true); state.textContent = "Procesando predicción…"; }
        else if (out) { show(out, true); out.textContent = "Procesando predicción…"; }

        const aerolinea = (qs("#aerolinea")?.value || "").trim().toUpperCase();
        const origen    = (qs("#origen")?.value || "").trim().toUpperCase();
        const destino   = (qs("#destino")?.value || "").trim().toUpperCase();
        const fechaPartida = qs("#fechaPartida")?.value || "";
        const distancia = Number(qs("#distancia")?.value || 0);

        if (!aerolinea || !origen || !destino || !fechaPartida || !distancia) {
            const msg = "⚠️ Todos los campos son obligatorios";
            if (state) state.textContent = msg; else if (out) out.textContent = msg;
            return;
        }
        if (aerolinea.length < 2) {
            const msg = "⚠️ Aerolínea debe tener 2-3 caracteres (IATA)";
            if (state) state.textContent = msg; else if (out) out.textContent = msg;
            return;
        }
        if (origen.length !== 3 || destino.length !== 3) {
            const msg = "⚠️ Origen y destino deben tener 3 letras (IATA)";
            if (state) state.textContent = msg; else if (out) out.textContent = msg;
            return;
        }

        if (btn) btn.disabled = true, btn.dataset.prevText = btn.textContent, (btn.textContent = "⏳ Prediciendo...");

        try {
            const res = await fetch(ENDPOINT_INDIVIDUAL, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ aerolinea, origen, destino, fecha_partida: fechaPartida, distancia })
            });

            if (state) show(state, false);
            if (out) show(out, true);

            if (!res.ok) {
                const text = await res.text();
                let errorData;
                try {
                    errorData = JSON.parse(text);
                } catch {
                    throw new Error("Error del servidor: respuesta no válida");
                }
                const mensajeAmigable = `${errorData.errorCode || 'ERROR'} - ${errorData.error || 'Error'} - ${errorData.message || 'Sin detalles.'}`;
                out.innerHTML = `<div class="alert alert-danger">${mensajeAmigable}</div>`;
                return;
            }

            const data = await res.json();

            // ✅ Ajuste de probabilidad → confianza en la predicción
            let probabilidadMostrar;
            if (data.prevision === "Puntual") {
                probabilidadMostrar = 1 - (data.probabilidad || 0);
            } else {
                probabilidadMostrar = data.probabilidad || 0;
            }

            out.innerHTML = `
                <h3>✅ Predicción</h3>
                <p><strong>Estado:</strong> <span style="color:${data.prevision === 'Puntual' ? 'green' : 'red'}">
                    ${data.prevision === 'Puntual' ? '🟢 Puntual' : '🔴 Retrasado'}
                </span></p>
                <p><strong>Confianza:</strong> ${(probabilidadMostrar * 100).toFixed(1)}%</p>
            `;

        } catch (err) {
            if (state) { show(state, false); }
            if (out) { show(out, true); out.textContent = "Error al consultar el backend: " + (err?.message || err); }
        } finally {
            if (btn) { btn.disabled = false; btn.textContent = btn.dataset.prevText || "🔍 Predecir"; }
        }
    });
}

// ===== LOTE =====
function setupBatch() {
    const fileInput = qs("#csvFile");
    const fileName  = qs("#fileName");
    const btnLote   = qs("#btn-lote");
    const sumLote   = qs("#summary-lote");
    const detLote   = qs("#detalles-lote");
    const stateLote = qs("#estado-lote");

    if (!fileInput || !btnLote) return;

    fileInput.addEventListener("change", function () {
        const f = this.files?.[0];
        if (!f) {
            if (fileName) fileName.textContent = "Ningún archivo seleccionado";
            btnLote.disabled = true;
            return;
        }
        if (!f.name.endsWith(".csv")) {
            alert("⚠️ Solo se permiten archivos .csv");
            this.value = "";
            if (fileName) fileName.textContent = "Ningún archivo seleccionado";
            btnLote.disabled = true;
            return;
        }
        if (fileName) fileName.textContent = `✅ ${f.name} (${Math.round(f.size / 1024)} KB)`;
        btnLote.disabled = false;
    });

    btnLote.addEventListener("click", async () => {
        const f = fileInput.files?.[0];
        if (!f) { alert("Por favor selecciona un archivo CSV."); return; }

        show(sumLote, false); show(detLote, false); show(stateLote, true);
        if (stateLote) stateLote.textContent = "Subiendo y procesando CSV…";
        btnLote.disabled = true;
        const prevText = btnLote.textContent; btnLote.textContent = "⏳ Procesando...";

        try {
            let res;
            if (BATCH_USE_MULTIPART) {
                const formData = new FormData();
                formData.append("file", f);
                res = await fetch(ENDPOINT_LOTE, { method: "POST", body: formData });
            } else {
                const csvText = await f.text();
                res = await fetch(ENDPOINT_LOTE, { method: "POST", headers: { "Content-Type": "text/csv" }, body: csvText });
            }

            if (!res.ok) {
                const text = await res.text();
                let errorData;
                try {
                    errorData = JSON.parse(text);
                } catch {
                    throw new Error("Error del servidor al procesar lote");
                }
                const mensajeAmigable = `${errorData.errorCode || 'ERROR'} - ${errorData.error || 'Error'} - ${errorData.message || 'Sin detalles.'}`;
                if (sumLote) {
                    sumLote.innerHTML = `<div class="summary error">❌ ${mensajeAmigable}</div>`;
                    show(sumLote, true);
                }
                show(stateLote, false);
                return;
            }

            const data = await res.json();
            const respuestas = Array.isArray(data) ? data : (data?.items || []);

            // Resumen
            if (respuestas.length) {
                const total = respuestas.length;
                const exitos = respuestas.filter(r => r.estado === "OK").length;
                const fallos = total - exitos;
                const summaryClass = exitos === 0 ? "error" : (fallos > 0 ? "warning" : "success");
                if (sumLote) {
                    sumLote.innerHTML = `
                        <div class="summary ${summaryClass}">
                            <h3>📊 Resultado del lote (${total} vuelos)</h3>
                            <p>✅ Éxitos: <strong>${exitos}</strong> | ❌ Errores: <strong>${fallos}</strong></p>
                        </div>
                    `;
                    show(sumLote, true);
                }
            }

            // Detalles
            if (detLote) {
                if (respuestas.length) {
                    const rows = respuestas.map(r => {
                        const statusClass = r.estado === "OK" ? "status-ok" : "status-err";
                        const statusText = r.estado === "OK" ? "✅ OK" : `⚠️ ${r.estado}`;
                        // ✅ Ajuste de probabilidad en lote también
                        let resultadoTexto = r.mensajeError || "Error no especificado";
                        if (r.estado === "OK") {
                            let probAjustada = r.prevision === "Puntual" ? (1 - r.probabilidad) : r.probabilidad;
                            resultadoTexto = `${r.prevision} (${(probAjustada * 100).toFixed(1)}%)`;
                        }
                        const partida = r.fechaPartida ? String(r.fechaPartida).replace("T"," ") : (r.fecha_partida || "");
                        return `
                            <tr>
                                <td>${r.fila ?? ""}</td>
                                <td>${r.aerolinea ?? ""}</td>
                                <td>${r.origen ?? ""} → ${r.destino ?? ""}</td>
                                <td>${partida}</td>
                                <td class="${statusClass}">${statusText}</td>
                                <td>${resultadoTexto}</td>
                            </tr>
                        `;
                    }).join("");

                    detLote.innerHTML = `
                        <h4>📋 Detalle por vuelo</h4>
                        <table>
                            <thead>
                                <tr>
                                    <th>Fila</th><th>Aerol.</th><th>Ruta</th><th>Partida</th><th>Estado</th><th>Resultado / Error</th>
                                </tr>
                            </thead>
                            <tbody>${rows}</tbody>
                        </table>
                    `;
                }
                show(detLote, true);
            }
            show(stateLote, false);

        } catch (error) {
            console.error("Error:", error);
            const msg = error?.message || "Error desconocido en el servidor";
            if (sumLote) {
                sumLote.innerHTML = `<div class="summary error">❌ Error: ${msg}</div>`;
                show(sumLote, true);
            }
            show(stateLote, false);
        } finally {
            btnLote.disabled = false;
            btnLote.textContent = prevText || "📤 Cargar y predecir";
        }
    });
}

// ===== INIT =====
document.addEventListener("DOMContentLoaded", () => {
    console.log("✅ main.js cargado. API_BASE:", API_BASE);
    setupTabs();
    setupIndividual();
    setupBatch();
});