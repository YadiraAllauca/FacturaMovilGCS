# Estado del proyecto

Proyecto académico de equipo de 2023, conservado como evidencia de integración Android. La revisión de octubre de 2026 se realizó sobre la referencia local `origin/develop` en `bafab39`.

## Presentación y configuración

- Se incorporó la versión desarrollada a `main` local mediante fast-forward, conservando el historial colectivo.
- El proyecto Gradle y el módulo `app` se trasladaron desde `appFacturador/` a la raíz.
- Se retiraron metadatos de Android Studio y un drawable duplicado con mayúsculas en su nombre; se conserva el recurso idéntico con nombre en minúsculas.
- La configuración Firebase se mantiene local y excluida de Git. Las cinco llamadas Retrofit usan la base configurada por el SDK en lugar de la URL histórica literal.
- Se eliminó la dependencia duplicada y dinámica `core-ktx:+`, conservando la versión fija `1.10.1`. Las declaraciones de plugins se reunieron en el bloque `plugins`, con una sola versión de Kotlin.
- Se añadió documentación de alcance, entorno y autoría. No se implementaron correcciones de facturación o de autorización.

## Hallazgos pendientes

Los siguientes puntos proceden de lectura del código. No se reprodujeron en una aplicación Android ni se consultaron servicios Firebase.

| Área | Comportamiento observado |
| --- | --- |
| IVA | `AddBillActivity.calculateTotalIVA()` calcula una unidad por línea, sin multiplicar por cantidad ni considerar el descuento. Los importes usan `Float`. |
| Número de factura | La lectura del contador es asíncrona, pero la factura se construye sin esperar esa lectura. El guardado y el incremento del contador son operaciones separadas, sin transacción. |
| Validación | Se puede construir una factura sin exigir líneas. Cantidades y descuentos no tienen límites de dominio en el adapter; el descuento máximo del producto no se aplica allí. |
| Cliente seleccionado | La búsqueda por identificación no limita por negocio. Una búsqueda sin resultados cambia los textos pero no limpia el cliente previamente seleccionado. |
| Contraseñas | `EMPLEADO.clave` recibe la contraseña introducida, se guarda en Realtime Database y se lee en el listado. `FACTURA` incluye el objeto completo de empleado. Firebase Auth sí se usa en el login. |
| Empleados | El alta y la edición confirman éxito desde `onResponse` sin comprobar el estado HTTP y llaman a crear una cuenta sin esperar el resultado. Editar no actualiza la cuenta existente; eliminar el registro no elimina su cuenta de Authentication. |
| Sesión | La recuperación de contexto parte de un email guardado localmente. La creación de usuarios usa la instancia de Auth de la sesión actual; requiere revisar el cambio de identidad durante el alta. |
| Autorización | No se versionan reglas verificadas de Realtime Database o Storage. Parte de los listados filtra por negocio en el dispositivo; las llamadas REST no adjuntan credenciales. No se conoce el estado de las reglas remotas. |
| Nota de crédito | Solo cambia `Factura/{id}/estado` a `-1`. No persiste motivo, importe ni un documento independiente. |
| Firma | Selecciona y sube un `.p12`; no firma documentos ni tramita autorización tributaria. |
| Guardados | Algunos flujos muestran éxito y navegan antes de confirmar la escritura. |

Los controles de roles en pantalla no demuestran permisos en el servidor. Tampoco se ha demostrado acceso a una base pública, una doble numeración real o disponibilidad actual de los servicios históricos.

## Si se retoma una demo funcional

1. Eliminar la copia de contraseñas del modelo, las vistas y las facturas; separar la administración de cuentas de la sesión del operador.
2. Definir y probar reglas de acceso por identidad, rol y negocio, con escrituras autenticadas y datos sintéticos.
3. Corregir los cálculos monetarios, validar cantidades y descuentos, y coordinar numeración y guardado.
4. Resolver la selección de clientes, las respuestas HTTP y los mensajes de éxito antes de ampliar funciones.
5. Añadir regresiones de esos flujos y verificar compilación e integración con un entorno de pruebas.

## Verificación de esta preparación

Se comprobaron la conservación de archivos trasladados, referencias locales de documentación y recursos, sintaxis XML, sintaxis shell del wrapper y reglas de exclusión. Estas comprobaciones no equivalen a compilar Android.

No se ejecutaron Gradle ni las pruebas Android: el entorno de revisión dispone de Java 21, pero no de SDK Android ni del JDK elegido para este wrapper. No se consultó Firebase ni se crearon cuentas, cargaron certificados o modificaron reglas remotas.

Las dos pruebas existentes son las de plantilla. No hay una suite que verifique los flujos anteriores ni un pipeline de CI.
