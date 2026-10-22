# Configuración local

## Herramientas

El repositorio conserva Gradle **7.4** y Android Gradle Plugin **7.3.1**. La declaración de Kotlin se unificó en **1.9.0**, versión que ya figuraba en el build anterior. El módulo declara `compileSdk` y `targetSdk` **34**, y `minSdk` **24**.

Para preparar una comprobación, instala Android Studio, Android SDK Platform 34 y un JDK compatible con Gradle 7.4, por ejemplo **JDK 17**. Configura ese JDK en las opciones de Gradle de Android Studio; un JDK reciente incluido en el IDE puede no ser compatible con este wrapper antiguo.

La combinación de AGP y API 34 es heredada y no se ha validado en esta revisión. No se realizó una migración general de herramientas o dependencias.

Referencias: [compatibilidad de Java y Gradle](https://docs.gradle.org/current/userguide/compatibility.html) y [requisitos de AGP 7.3](https://developer.android.com/build/releases/agp-7-3-0-release-notes).

## Proyecto Firebase

1. Crea un proyecto Firebase de pruebas y registra una aplicación Android con el identificador `dev.android.appfacturador`.
2. Configura Authentication con el proveedor **correo electrónico y contraseña**, Realtime Database y Storage.
3. Descarga `google-services.json` después de crear Realtime Database y colócalo en `app/google-services.json`. Debe incluir la URL de esa base de datos.
4. Configura el SDK Android desde Android Studio o mediante `local.properties`, con `sdk.dir` apuntando a tu instalación.

`google-services.json` y `local.properties` son archivos locales excluidos de Git. La configuración Firebase histórica se conserva en la copia local, pero no forma parte de la estructura preparada para publicar. Los identificadores de una aplicación Firebase no sustituyen las reglas de acceso.

Las llamadas Retrofit obtienen ahora su URL de `Constants.DATABASE_URL`, derivada de la instancia Firebase configurada. No hace falta reemplazar la URL del antiguo proyecto en varias Activities.

## Datos mínimos para explorar la aplicación

No se incluye una base exportada ni un proceso automático de carga. El acceso requiere tanto una cuenta en Firebase Authentication como un perfil correspondiente en Realtime Database.

Para una exploración con datos ficticios:

1. Crea una cuenta de prueba en Authentication.
2. Añade un negocio en `Negocios`, con una clave propia, sus datos de contacto y un `contador` numérico inicial.
3. Añade un perfil en `Empleado` con la misma dirección de `correo_electronico` de la cuenta y `negocio` igual a la clave anterior. El código distingue `tipo_empleado` con `A` para administrador y `V` para vendedor.
4. Usa nombres y datos ficticios en los campos definidos en [`Biller.kt`](../app/src/main/java/dev/android/appfacturador/model/Biller.kt). Las otras colecciones usadas son `Cliente`, `Product` y `Factura`.

El proyecto mezcla consultas mediante el SDK y escrituras mediante REST. Estas últimas **no adjuntan un token de autenticación**, por lo que pueden ser rechazadas por reglas restrictivas aunque se haya iniciado sesión. No abras las reglas para conseguir que funcionen: esa integración y la autorización por negocio están pendientes de reparación. Los filtros locales no sustituyen esas reglas. Consulta la [autenticación REST de Firebase](https://firebase.google.com/docs/database/rest/auth).

La aplicación también guarda contraseñas en el modelo de empleado y no coordina correctamente todos los cambios con Authentication. No uses cuentas compartidas con otros servicios, información personal, certificados ni firmas reales. La carga de `.p12` es solo un prototipo de subida a Storage.

## Comandos de comprobación

Con el JDK, SDK, configuración Firebase y dependencias preparados, ejecuta desde la raíz:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

En Windows usa `gradlew.bat`. Para la prueba instrumentada se necesita además un dispositivo o emulador:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Estos comandos son instrucciones para verificar el proyecto; no son resultados obtenidos durante esta preparación. Las pruebas existentes solo comprueban una suma y el nombre del paquete.
