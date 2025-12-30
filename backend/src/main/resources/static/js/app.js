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
            body: JSON.stringify(datos)
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


/*
document.getElementById('formulario').addEventListener('submit', function(event) {
    event.preventDefault(); // ← ahora 'event' sí está definido

    const aerolinea = document.getElementById('aerolinea').value.trim().toUpperCase();
    const origen = document.getElementById('origen').value.trim().toUpperCase();
    const destino = document.getElementById('destino').value.trim().toUpperCase();
    const fechaPartida = document.getElementById('fechaPartida').value;
    const distancia = parseInt(document.getElementById('distancia').value) || 0;

    // Validación básica antes de enviar, para no ir a VueloRequest (servidor) y verificar
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

    const datos = {
        aerolinea,
        origen,
        destino,
        fecha_partida: fechaPartida ? new Date(fechaPartida).toISOString() : null,
        distancia: distancia
    };

    console.log("Enviando:", datos);

    fetch('/predict', {  // ← usa ruta relativa: /predict
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datos)
    })
    .then(async response => {
        const body = await response.json();
        if (!response.ok) throw new Error(body.message || 'Error desconocido');
        return body;
    })
    .then(data => {
        document.getElementById('resultado').innerHTML = `
            <h3>✅ Predicción</h3>
            <p><strong>Estado:</strong> <span style="color:${data.prevision === 'Puntual' ? 'green' : 'red'}">
                ${data.prevision === 'Puntual' ? '🟢 Puntual' : '🔴 Retrasado'}
            </span></p>
            <p><strong>Probabilidad:</strong> ${(data.probabilidad * 100).toFixed(1)}%</p>
        `;
    })
    .catch(err => {
        document.getElementById('resultado').innerHTML =
            `<p style="color:red">⚠️ ${err.message}</p>`;
        console.error(err);
    });
}
*/


/*
document.getElementById('formulario').addEventListener('submit', function(event) {
    event.preventDefault(); // Evita recarga de página

    // 1: Obtener y preparar datos
    const aerolinea = document.getElementById('aerolinea').value.trim();
    const origen = document.getElementById('origen').value.trim();
    const destino = document.getElementById('destino').value.trim();
    const fechaPartida = document.getElementById('fechaPartida').value;
    // Le damos el formato requerido despues de declaracion
    const fechaPartidaISO = fechaPartida ? new Date(fechaPartida).toISOString() : null;
    // Es un string y queremos integer
    const distancia = parseFloat(document.getElementById('distancia').value) || 0;

    // 2. Crear objeto JavaScript
    const datosUsuario = {
        aerolinea: aerolinea,
        origen: origen,
        destino: destino,
        fechaPartida: fechaPartidaISO,
        distancia: distancia
    };

    // 3. Referencias UI (para feedback)
    const btn = event.submitter; // ← ¡el botón que disparó el submit!
    const resultadoDiv = document.getElementById('resultado');

    // 4. Deshabilitar botón y mostrar "cargando"
    btn.disabled = true;
    btn.textContent = "⏳ Enviando...";
    resultadoDiv.style.display = "block";

    // 5. Petición con manejo de errores COMPLETO
    fetch('http://localhost:8080/predict', {
        console.log("Enviando datos:", datosUsuario);
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datosUsuario) // ← no necesitas variable intermedia
    })
    .then(response => {
        // Aquí va la mejora: detectar errores 4xx/5xx
        if (!response.ok) {
            // Si no es 2xx, parseamos el cuerpo de error y lo "lanzamos"
            return response.json().then(errorBody => {
                throw new Error(
                    errorBody.message ||
                    errorBody.error ||
                    `Error ${response.status}: ${response.statusText}`
                );
            });
        }
        // Si es 2xx, devolvemos los datos
        return response.json();
    })
    .then(data => {
        // Éxito: mostrar respuesta bonita
        resultadoDiv.innerHTML = `
            <h3>✅ Predicción</h3>
            <p><strong>Estado:</strong> <span style="color:${data.prevision === 'PUNTUAL' ? 'green' : 'red'}">
                ${data.prevision === 'PUNTUAL' ? '🟢 Puntual' : '🔴 Retrasado'}
            </span></p>
            <p><strong>Probabilidad:</strong> ${(data.probabilidad * 100).toFixed(1)}%</p>
        `;
    })
    .catch(error => {
        console.error("Error:", error);
        alert("Error: " + error.message); // ← temporal, para ver sin F12
        // Cualquier error (red o 400/500) llega aquí
        resultadoDiv.innerHTML = `<p style="color:red; font-weight:bold;">⚠️ ${error.message}</p>`;
        console.error("Error en predicción:", error);
    })
    .finally(() => {
        // Siempre reactivar el botón
        btn.disabled = false;
        btn.textContent = "🔍 Predecir";
    });
});

*/
