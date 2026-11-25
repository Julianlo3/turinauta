# Radio temporal para pruebas de visitas

**Fecha:** 25 de noviembre de 2026. **Estado:** cambio temporal solicitado por el usuario.

El radio original de registro es **200 m**. Para probar el flujo desde lugares más lejanos, el frontend permite enviar una visita si la distancia estimada al lugar es de **hasta 10 km**. El usuario indicó que ya retiró la restricción de 200 m en el backend; ese cambio del servidor no se hizo ni se verificó en este repositorio.

## Implementación temporal

- `app/src/main/java/com/unicauca/turinauta/feature/visits/VisitRegistrationViewModel.kt`: `TEMP_VISIT_TEST_RADIUS_METERS = 10_000` controla el botón y el envío de la visita.
- `VisitRecord.MAX_VISIT_DISTANCE_METERS = 200` y los textos, iconos y colores de la ventana permanecen como estaban por solicitud expresa. **Entre 200 m y 10 km, la interfaz aún muestra el aviso de 200 m aunque permite enviar la visita.** Esto puede resultar confuso durante las pruebas.
- La aplicación todavía exige foto y ubicación. La distancia se calcula con Haversine y el backend sigue decidiendo si registra la visita.

## Para restablecer 200 m

1. Volver a activar la validación de 200 m en el backend.
2. En `VisitRegistrationViewModel.kt`, cambiar la condición de `canSubmit` para usar `VisitRecord.MAX_VISIT_DISTANCE_METERS` y eliminar `TEMP_VISIT_TEST_RADIUS_METERS`.
3. Mantener la comprobación de `current.canSubmit` en `submitVisit`, ejecutar `assembleDebug testDebugUnitTest` y probar el límite a ambos lados de 200 m.
4. Retirar este documento cuando el cambio temporal haya terminado.
