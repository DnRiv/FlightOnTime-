/* =========
   FlightOnTime — JS unificado (individual + lote)
   Ubicación: /static/js/main.js
   ========= */

// ===== CONFIG =====
const API_BASE = window.API_BASE || window.location.origin;
const ENDPOINT_INDIVIDUAL = `${API_BASE}/predict`;
const ENDPOINT_LOTE       = `${API_BASE}/predict/batch`;

// Usa multipart/form-data como en tu lote.js original:
const BATCH_USE_MULTIPART = true;

// ===== UTILS =====
const qs = (sel, root = document) => root.querySelector(sel);
const show = (el, on = true) => { if (el) el.style.display = on ? "" : "none"; };
const pretty = (obj) => (typeof obj === "string" ? obj : JSON.stringify(obj, null, 2));

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

// ===== INDIVIDUAL (compat con IDs viejos) =====
function setupIndividual() {
  const form = qs("#form-individual") || qs("#formulario");
  if (!form) return;

  const btn = qs("#btn-individual") || form.querySelector('button[type="submit"]');
  const out = qs("#resultado-individual") || qs("#resultado");
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

      // 👇 OCULTAMOS el estado de "procesando" ANTES de mostrar cualquier resultado
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

      if (data && typeof data === "object" && ("prevision" in data || "probabilidad" in data)) {
        const puntual = String(data.prevision || "").toLowerCase().includes("puntual");
        out.innerHTML = `
          <h3>✅ Predicción</h3>
          <p><strong>Estado:</strong> <span style="color:${puntual ? 'green' : 'red'}">
            ${puntual ? '🟢 Puntual' : '🔴 Retrasado'}
          </span></p>
          <p><strong>Probabilidad:</strong> ${data.probabilidad != null ? (data.probabilidad * 100).toFixed(1) : 'N/A'}%</p>
        `;
      } else {
        out.textContent = pretty(data);
      }

    } catch (err) {
      // 👇 También ocultamos "procesando" en errores de red o parsing
      if (state) { show(state, false); }
      if (out) { show(out, true); out.textContent = "Error al consultar el backend: " + (err?.message || err); }
    } finally {
      if (btn) { btn.disabled = false; btn.textContent = btn.dataset.prevText || "🔍 Predecir"; }
    }
  });
}

// ===== LOTE (compat con tus IDs de lote.js Y los del unificado) =====
function setupBatch() {
  // IDs del unificado:
  let fileInput = qs("#csvFile");
  let fileName  = qs("#fileName");
  let btnLote   = qs("#btn-lote");
  let sumLote   = qs("#summary-lote");
  let detLote   = qs("#detalles-lote");
  let stateLote = qs("#estado-lote");
  let resultadosWrap = null; // en unificado no hay 'resultados' wrapper

  // Compat con tu HTML antiguo de lote.js:
  if (!btnLote) {
    btnLote = qs("#uploadBtn");
    resultadosWrap = qs("#resultados");
    sumLote = sumLote || qs("#summary");
    detLote = detLote || qs("#details");
  }

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

    if (resultadosWrap) show(resultadosWrap, false);
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

      // Intentamos JSON; si no, texto plano
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

      // Tu lote.js espera un ARRAY llamado 'respuestas'
      // También soportamos forma objeto { items: [...] }
      const respuestas = Array.isArray(data) ? data : (data?.items || []);

      // Render resumen
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
      } else {
        if (sumLote) {
          sumLote.className = "summary warning";
          sumLote.textContent = "Respuesta procesada. Revisa detalles abajo.";
          show(sumLote, true);
        }
      }

      // Render detalles (tabla) similar a tu lote.js
      if (detLote) {
        if (respuestas.length) {
          const rows = respuestas.map(r => {
            const statusClass = r.estado === "OK" ? "status-ok" : "status-err";
            const statusText = r.estado === "OK" ? "✅ OK" : `⚠️ ${r.estado}`;
            const resultado = r.estado === "OK"
              ? `${r.prevision} (${(r.probabilidad * 100).toFixed(1)}%)`
              : (r.mensajeError || "Error no especificado");
            const partida = r.fechaPartida ? String(r.fechaPartida).replace("T"," ") : (r.fecha_partida || "");
            return `
              <tr>
                <td>${r.fila ?? ""}</td>
                <td>${r.aerolinea ?? ""}</td>
                <td>${r.origen ?? ""} → ${r.destino ?? ""}</td>
                <td>${partida}</td>
                <td class="${statusClass}">${statusText}</td>
                <td>${resultado}</td>
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
        } else {
          detLote.textContent = pretty(data);
        }
        show(detLote, true);
      }

      if (resultadosWrap) show(resultadosWrap, true);
      show(stateLote, false);

    } catch (error) {
      console.error("Error:", error);
      const msg = error?.message || "Error desconocido en el servidor";
      if (sumLote) {
        sumLote.innerHTML = `<div class="summary error">❌ Error: ${msg}</div>`;
        show(sumLote, true);
      }
      show(stateLote, false);
      if (resultadosWrap) show(resultadosWrap, true);
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
