document.addEventListener('DOMContentLoaded', function () {
  console.log("✅ lote.js cargado");

  const fileInput = document.getElementById('csvFile');
  const fileNameDiv = document.getElementById('fileName');
  const uploadBtn = document.getElementById('uploadBtn');
  const resultadosDiv = document.getElementById('resultados');
  const summaryDiv = document.getElementById('summary');
  const detailsDiv = document.getElementById('details');

  // Actualizar nombre de archivo al seleccionar
  fileInput.addEventListener('change', function () {
    if (this.files.length > 0) {
      const file = this.files[0];
      if (!file.name.endsWith('.csv')) {
        alert('⚠️ Solo se permiten archivos .csv');
        this.value = ''; // reset
        fileNameDiv.textContent = 'Ningún archivo seleccionado';
        uploadBtn.disabled = true;
        return;
      }
      fileNameDiv.textContent = `✅ ${file.name} (${Math.round(file.size / 1024)} KB)`;
      uploadBtn.disabled = false;
    } else {
      fileNameDiv.textContent = 'Ningún archivo seleccionado';
      uploadBtn.disabled = true;
    }
  });

  // Manejar envío
  uploadBtn.addEventListener('click', function () {
    const file = fileInput.files[0];
    if (!file) {
      alert('Por favor selecciona un archivo CSV.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    // UI: estado de carga
    uploadBtn.disabled = true;
    uploadBtn.textContent = '⏳ Procesando...';
    resultadosDiv.style.display = 'none';

    fetch('/predict/batch', {
      method: 'POST',
      body: formData
    })
      .then(response => {
        if (!response.ok) {
          return response.json().then(err => Promise.reject(err));
        }
        return response.json();
      })
      .then(data => mostrarResultados(data))
      .catch(error => {
        console.error('Error:', error);
        const msg = error.message || 'Error desconocido en el servidor';
        summaryDiv.innerHTML = `<div class="summary error">❌ Error: ${msg}</div>`;
        resultadosDiv.style.display = 'block';
      })
      .finally(() => {
        uploadBtn.disabled = false;
        uploadBtn.textContent = '📤 Cargar y predecir';
      });
  });

  function mostrarResultados(respuestas) {
    // Resumen
    const total = respuestas.length;
    const exitos = respuestas.filter(r => r.estado === 'OK').length;
    const fallos = total - exitos;

    let summaryClass = 'success';
    if (fallos > 0 && exitos > 0) summaryClass = 'warning';
    else if (exitos === 0) summaryClass = 'error';

    summaryDiv.innerHTML = `
      <div class="summary ${summaryClass}">
        <h3>📊 Resultado del lote (${total} vuelos)</h3>
        <p>✅ Éxitos: <strong>${exitos}</strong> | ❌ Errores: <strong>${fallos}</strong></p>
      </div>
    `;

    // Detalles (tabla)
    if (total > 0) {
      let rows = '';
      respuestas.forEach(r => {
        const statusClass = r.estado === 'OK' ? 'status-ok' : 'status-err';
        const statusText = r.estado === 'OK' ? '✅ OK' : `⚠️ ${r.estado}`;
        const resultado = r.estado === 'OK'
          ? `${r.prevision} (${(r.probabilidad * 100).toFixed(1)}%)`
          : r.mensajeError || 'Error no especificado';

        rows += `
          <tr>
            <td>${r.fila}</td>
            <td>${r.aerolinea || ''}</td>
            <td>${r.origen || ''} → ${r.destino || ''}</td>
            <td>${r.fechaPartida ? r.fechaPartida.replace('T', ' ') : ''}</td>
            <td class="${statusClass}">${statusText}</td>
            <td>${resultado}</td>
          </tr>
        `;
      });

      detailsDiv.innerHTML = `
        <h4>📋 Detalle por vuelo</h4>
        <table>
          <thead>
            <tr>
              <th>Fila</th>
              <th>Aerol.</th>
              <th>Ruta</th>
              <th>Partida</th>
              <th>Estado</th>
              <th>Resultado / Error</th>
            </tr>
          </thead>
          <tbody>
            ${rows}
          </tbody>
        </table>
      `;
    }

    resultadosDiv.style.display = 'block';
  }
});