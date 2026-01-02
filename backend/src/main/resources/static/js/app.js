// ✅ Espera a que el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    console.log("✅ app.js cargado y ejecutándose"); // Borrar luego
    const formulario = document.getElementById('formulario');
    if (!formulario) {
        console.error("❌ Formulario no encontrado");
        return;
    }

    formulario.addEventListener('submit', function(event) {
        event.preventDefault(); // ← ¡esto evita la recarga!

        // 1. Obtener valores
        const aerolinea = document.getElementById('aerolinea').value.trim().toUpperCase();
        const origen = document.getElementById('origen').value.trim().toUpperCase();
        const destino = document.getElementById('destino').value.trim().toUpperCase();
        const fechaPartida = document.getElementById('fechaPartida').value;
        const distancia = parseInt(document.getElementById('distancia').value) || 0;

        // 2. Validación inmediata (frontend)
        if (!aerolinea || !origen || !destino || !fechaPartida || !distancia) {
            alert("⚠️ Todos los campos son obligatorios");
            return;
        }
        if (aerolinea.length !== 2) {
            alert("⚠️ Aerolínea debe tener 2 caracteres");
            return;
        }
        if (origen.length !== 3 || destino.length !== 3) {
            alert("⚠️ Origen y destino deben tener 3 letras");
            return;
        }

        // 3. Preparar datos
        const datos = {
            aerolinea: aerolinea,
            origen: origen,
            destino: destino,
            // fecha_partida: fechaPartida ? new Date(fechaPartida).toISOString() : null,
            fecha_partida: fechaPartida,
            distancia: distancia
        };

        console.log("✅ Enviando:", datos);

        // 4. Petición
        const btn = event.submitter || formulario.querySelector('button[type="submit"]');
        const resultadoDiv = document.getElementById('resultado');

        btn.disabled = true;
        btn.textContent = "⏳ Prediciendo...";
        resultadoDiv.style.display = "block";
        resultadoDiv.innerHTML = "<p>Enviando solicitud...</p>";

        fetch('/predict', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(datos) // Enviando datos
        })
        .then(response => response.json().then(data => ({ ok: response.ok, data })))
        .then(({ ok, data }) => {
            if (!ok) {
                throw new Error(data.message || 'Error en el servidor');
            }
            resultadoDiv.innerHTML = `
                <h3>✅ Predicción</h3>
                <p><strong>Estado:</strong> <span style="color:${data.prevision === 'Puntual' ? 'green' : 'red'}">
                    ${data.prevision === 'Puntual' ? '🟢 Puntual' : '🔴 Retrasado'}
                </span></p>
                <p><strong>Probabilidad:</strong> ${(data.probabilidad * 100).toFixed(1)}%</p>
            `;
        })
        .catch(err => {
            resultadoDiv.innerHTML = `<p style="color:red; font-weight:bold;">⚠️ ${err.message}</p>`;
            console.error("Error:", err);
        })
        .finally(() => {
            btn.disabled = false;
            btn.textContent = "🔍 Predecir";
        });
    });
});
