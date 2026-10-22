<img src="app/src/main/res/drawable/icon_facturador.png" width="88" alt="Icono de Factura Móvil">

# Factura Móvil

Aplicación Android desarrollada en equipo como proyecto académico en 2023. Reúne la gestión de clientes, productos y empleados con un carrito y el registro de facturas por negocio, utilizando Kotlin y Firebase.

Se conserva como muestra del trabajo de integración móvil del equipo. Es un prototipo: sus flujos de facturación y permisos tienen limitaciones pendientes, y no implementa emisión de comprobantes electrónicos autorizados.

## Alcance

| Área | Implementación en el código |
| --- | --- |
| Acceso | Inicio de sesión con correo y contraseña mediante Firebase Authentication; perfiles de administrador y vendedor en la interfaz. |
| Clientes y empleados | Formularios, listados, búsqueda y edición; eliminación de registros de empleados. |
| Productos | Gestión de productos, imágenes en Firebase Storage y lectura de códigos de barras. |
| Facturas | Carrito en memoria, captura de cantidades y descuentos, registro, listado, detalle y filtros. |
| Anulación | Cambio de estado de la factura desde una pantalla denominada «Nota de crédito»; no genera un documento independiente. |
| Interfaz | Modo oscuro y dictado por voz en varios formularios. |

La pantalla de firma permite seleccionar y subir un archivo `.p12`; no firma facturas. Tampoco se implementan autorización tributaria, envío de comprobantes ni validación de sus resultados.

## Tecnologías y estructura

Kotlin, layouts XML, View Binding, Firebase Authentication, Realtime Database y Storage, Retrofit/Gson y ZXing.

```text
.
├── app/
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── java/dev/android/appfacturador/
│       │   │   ├── database/    # Interfaces REST
│       │   │   ├── model/       # Modelos de datos
│       │   │   ├── utils/       # Constantes y dictado por voz
│       │   │   └── …            # Activities, adapters y carrito
│       │   └── res/             # Layouts, temas e imágenes
│       ├── test/
│       └── androidTest/
├── docs/
├── gradle/wrapper/
├── build.gradle
├── settings.gradle
└── gradlew
```

La lógica de presentación y acceso a datos reside principalmente en las Activities. El proyecto no sigue una separación MVVM ni una capa de repositorios.

## Abrir el proyecto

```bash
git clone https://github.com/YadiraAllauca/FacturaMovilGCS.git
cd FacturaMovilGCS
```

Abre **esta carpeta raíz** en Android Studio: contiene `settings.gradle`, `gradlew` y el módulo `app`. Ya no es necesario entrar en `appFacturador/`.

Para preparar el entorno, consulta [Configuración local](docs/configuracion-local.md). Requiere un proyecto Firebase propio y datos de prueba; clonar el repositorio no proporciona cuentas ni una base de datos preparada.

## Estado y límites

- Los cálculos de IVA, descuentos y la numeración requieren correcciones antes de usar las facturas como registros fiables.
- El modelo conserva una copia de la contraseña del empleado fuera de Firebase Auth; también se incluye el objeto de empleado en la factura. No usar datos ni credenciales reales.
- Los filtros por negocio y los controles de rol del cliente no acreditan autorización en el servidor. No se incluyen reglas de Firebase verificadas.
- Solo existen las dos pruebas de plantilla de Android. No hay pruebas automatizadas de facturación ni integración continua.
- Esta preparación del repositorio se verificó de forma estática. No se compiló ni ejecutó la aplicación por falta de SDK Android en el entorno de revisión.

Los detalles y prioridades están en [Estado del proyecto](docs/estado-del-proyecto.md).

## Equipo

El historial recoge contribuciones de **Yadira Allauca (`YadiraAllauca`)**, **Thais Armijos (`ArmijosThais`)**, **`Keybrish`** y **`LennyMA` / Lenin**. La autoría es colectiva; no se atribuyen módulos individuales sin confirmación.

La versión desarrollada se encontraba en `develop`. Su historial se incorporó a la `main` local mediante un avance directo antes de reorganizar las carpetas, conservando los commits del equipo.
