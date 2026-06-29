# RentFlow: Gestión de alquileres de propiedades

Backend de la plataforma de gestion de alquileres de propiedades. API REST construida con Spring Boot 4, Java 21 y PostgreSQL.

---

## Resumen del proyecto
RentFlow es una plataforma web para la gestión de alquileres de propiedades, orientada a facilitar la interacción entre administradores, propietarios e inquilinos. El sistema permite administrar el ciclo completo de una reserva, desde la publicación y búsqueda de propiedades hasta la creación de reservas, generación de contratos, gestión de pagos, códigos de acceso, mantenimiento, calificaciones y notificaciones.

La plataforma cuenta con diferentes roles de usuario, cada uno con funcionalidades específicas. Los administradores pueden supervisar usuarios, propiedades y actividad general del sistema. Los propietarios pueden gestionar sus propiedades, revisar reservas, atender solicitudes de mantenimiento, consultar reportes y administrar contratos. Los inquilinos pueden buscar propiedades disponibles, realizar reservas, consultar sus contratos, acceder a códigos de ingreso, reportar problemas de mantenimiento, recibir notificaciones y calificar su experiencia.

El proyecto está dividido en frontend y backend. El backend expone una API REST segura que centraliza la lógica de negocio, validaciones, persistencia de datos y control de acceso. El frontend consume esta API para ofrecer una interfaz visual y funcional según el rol del usuario autenticado.

El objetivo principal del sistema es ofrecer una solución organizada, segura y escalable para administrar propiedades en alquiler, automatizando procesos importantes como reservas, contratos, códigos de acceso, notificaciones y control de estados.

---

## Tecnologias

### Backend

- **Java 21**: lenguaje principal utilizado para el desarrollo del backend.
- **Spring Boot**: framework utilizado para construir la API REST.
- **Spring Security**: utilizado para manejar la autenticación y autorización de usuarios.
- **JWT (JSON Web Token)**: utilizado para proteger endpoints mediante tokens de acceso.
- **Spring Data JPA**: utilizado para la comunicación con la base de datos.
- **Hibernate**: ORM utilizado para mapear las entidades Java con las tablas de la base de datos.
- **Maven**: herramienta utilizada para la gestión de dependencias y construcción del proyecto.
- **Lombok**: utilizado para reducir código repetitivo en las clases Java.

### Frontend
- **Next.js:** framework utilizado para construir la aplicación web del lado del cliente.
- **React:** librería utilizada para crear componentes reutilizables e interfaces dinámicas.
- **TypeScript:** utilizado para agregar tipado estático y mejorar la mantenibilidad del código.
- **Tailwind CSS:** utilizado para el diseño y estilos de la interfaz.
- **shadcn/ui:** conjunto de componentes reutilizables para construir la interfaz de usuario.
- **Lucide React:** librería de íconos utilizada en la interfaz.
- **Fetch/API Client:** utilizado para consumir los endpoints expuestos por el backend.

### Base de datos y herramientas

- **PostgreSQL**: sistema gestor de base de datos relacional utilizado por el proyecto.
- **Docker**: utilizado para ejecutar servicios como la base de datos en contenedores.
- **Git y GitHub**: utilizados para el control de versiones y colaboración del equipo.

---

## Servicios externos

| Servicio | Proposito |
|---|---|
| **Neon** | Base de datos PostgreSQL serverless. Se conecta a traves de las variables `POSTGRES_URL`, `POSTGRES_USER` y `POSTGRES_PASSWORD`. |
| **Cloudinary** | Almacenamiento en la nube para imagenes y documentos PDF de contratos. Se configura con `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY` y `CLOUDINARY_API_SECRET`. |
| **Next.js Frontend** | Cliente web que consume esta API. El origen permitido se configura con `CORS_ALLOWED_ORIGINS`. |

---

## Estructura del proyecto

Carpeta inicial: `com.example.propertyrentalmanagement`

| Paquete | Responsabilidad |
|---|---|
| `config` | Configuracion de Spring: seguridad, CORS, Cloudinary, etc... |
| `controllers` | Contiene todos los endpoints de cada modulo. Se reciben las peticiones HTTP, y se delegan a los servicios. |
| `dto` | Records para los `request` y `response` para los endpoints en el controller. |
| `entitites` | Entidades el cual definen como seran las tablas de PostgreSQL. |
| `enums` | Enumeraciones para las entidades. |
| `exceptions` | Excepciones personalizadas. Cada una siendo capturadas por `GlobalExceptionHandler`. |
| `jobs` | Tareas programadas, funciona para la actualizacion de mantenimientos preventivos. |
| `repositories` | Interfaces que contienen las conexiones necesarias para las bases de datos a usar. |
| `security` | Filtro JWT (`JwtAuthenticationFilter`), servicio de generacion/validacion de tokens (`JwtService`) y proveedor del usuario autenticado (`AuthenticatedUserProvider`). |
| `services` | Engloba a la logica de negocio. Se ha definido cada servicio con su forma de interfaz como tambien en su forma de implementacion (`ServiceImpl`). |
| `utils` | Clases de utilidad con multiples fines. |

---

## Aspectos de seguridad

El backend implementa un esquema de seguridad basado en Spring Security y JWT (JSON Web Token). Este mecanismo permite autenticar usuarios mediante credenciales y proteger los endpoints de la API según el rol del usuario autenticado.

**1. Authentication**

La autenticación permite verificar la identidad de un usuario. En el sistema, los usuarios pueden autenticarse mediante el endpoint de login:

POST /api/users/login

Cuando las credenciales son correctas, el backend genera un token JWT que contiene información básica del usuario, como:

Identificador del usuario.
Correo electrónico.
Rol del usuario.
Fecha de emisión.
Fecha de expiración.

El token es generado por la clase JwtService, que se encarga de construir, firmar, validar y leer los datos almacenados dentro del JWT.

Una vez generado el token, el frontend debe enviarlo en cada request protegido usando el header:

Authorization: Bearer <token>

**2. JWT Authentication Filter**

La clase JwtAuthenticationFilter se ejecuta antes del filtro estándar de autenticación de Spring Security. Su responsabilidad es interceptar cada request entrante y verificar si contiene un token JWT válido.

El filtro realiza los siguientes pasos:

Lee el header Authorization.
Verifica que el header empiece con Bearer.
Extrae el token.
Obtiene el ID del usuario desde el token.
Busca el usuario en la base de datos.
Valida que el token pertenezca al usuario y que no esté expirado.
Crea un objeto CustomUserDetails.
Registra la autenticación en el SecurityContextHolder.

Esto permite que el backend reconozca al usuario autenticado durante toda la ejecución del request.

**3. Security Configuration**

La clase SecurityConfig define la configuración principal de seguridad del backend.

Entre sus responsabilidades se encuentran:

Habilitar CORS.
Desactivar CSRF, ya que la API usa JWT y no sesiones tradicionales.
Configurar la aplicación como stateless.
Definir endpoints públicos.
Proteger el resto de endpoints.
Registrar el filtro JWT.
Configurar respuestas personalizadas para errores 401 Unauthorized y 403 Forbidden.

**4. Endpoints públicos**

Algunos endpoints están disponibles sin autenticación. Estos se configuran en SecurityConfig.

Los endpoints públicos principales son:

POST /api/users/register
POST /api/users/login

**5. Endpoints protegidos**

Todos los demás endpoints requieren autenticación:

.anyRequest().authenticated()

Esto significa que cualquier request que no esté explícitamente marcado como público debe incluir un JWT válido.

Si el usuario no envía token, envía un token inválido o el token expiró, el backend responde con:

401 Unauthorized

**6. Authorization**

El sistema maneja tres roles principales:

ADMIN
LANDLORD
TENANT

La autorización se implementa usando @PreAuthorize junto con la clase AuthorizationService.

Para habilitar estas validaciones por método, SecurityConfig usa:

@EnableMethodSecurity

Esto permite proteger endpoints específicos directamente en los controllers.

**7. AuthorizationService**

La clase AuthorizationService centraliza las validaciones de permisos por rol y por usuario autenticado.

Incluye métodos como:

public boolean isAdmin()
public boolean isLandlord()
public boolean isTenant()
public boolean isCurrentUser(UUID userId)
public boolean isAdminOrCurrentUser(UUID userId)

Estos métodos consultan al usuario autenticado mediante AuthenticatedUserProvider y verifican si cumple con la condición requerida.

**8. Password Encoder**

Las contraseñas no se almacenan en texto plano. El sistema usa BCryptPasswordEncoder para encriptar las contraseñas.

---

## Modulos

### Usuarios

Gestiona el ciclo de vida de las cuentas de usuario. Soporta tres roles: `TENANT`, `LANDLORD` y `ADMIN`.

**Endpoint:** `/api/users`

**Entidad:** `AppUser`: almacena nombre, email, contrasena encriptada, rol y telefono.

**Acciones:**
- Registro y login con generacion de JWT.
- Consulta y actualizacion de perfil propio.
- Cambio de contrasena.
- Listado y busqueda paginada de usuarios (exclusivo para `ADMIN`).
- Consulta de resumen mensual del dashboard de administrador.
- Creacion y consulta de calificaciones entre usuarios.

---

### Propiedades

Gestiona las propiedades que los landlords publican en la plataforma.

**Endpoint:** `/api/properties`

**Entidad:** `Property`: almacena titulo, descripcion, ubicacion, precios (por noche, limpieza, deposito), capacidad, tipo, estado y fotos.

**Acciones:**
- Creacion de propiedades por parte de landlords, con carga opcional de fotos al momento de crear.
- Adjuntar fotos adicionales a una propiedad existente.
- Consulta publica paginada con filtros por termino de busqueda, tipo, capacidad minima y estado.
- Actualizacion y eliminacion de propiedades (solo el propietario).
- Consulta de disponibilidad por rango de fechas.
- Dashboard y calendario mensual del propietario.
- Generacion de reportes de ocupacion e ingresos por propiedad o por propietario.

---

### Calificaciones

Gestiona las calificaciones que los usuarios se otorgan entre sí al finalizar una reserva, permitiendo construir una reputación tanto para inquilinos como para propietarios dentro de la plataforma.

**Endpoint:** `/api/users/ratings`

**Entidad:** `Rating`: almacena la reserva asociada, el usuario que califica (`reviewer`), el usuario calificado (`reviewed`), la puntuación otorgada, un comentario opcional y la fecha de creación.

**Acciones:**
- Creación de una calificación asociada a una reserva, identificando automáticamente al usuario calificado según el rol del usuario autenticado dentro de esa reserva (`tenant` o `landlord`).
- Validación de que el usuario autenticado forme parte de la reserva antes de permitir la calificación.
- Restricción de una única calificación por usuario y por reserva, evitando calificaciones duplicadas.
- Consulta de las calificaciones recibidas por un usuario, junto con el promedio y el total de calificaciones obtenidas.
- Notificación al usuario calificado cuando recibe una nueva calificación.

---

### Reporte de Ocupación e Ingresos por Propiedad

Permite a landlords y administradores analizar el rendimiento de propiedades en un período de tiempo determinado, visualizando métricas de ocupación, ingresos y reservas a través de KPIs y gráficos comparativas.

**Endpoints:** `GET /api/properties/:propertyId/report` · `GET /api/properties/report`

**Entidades involucradas:** `Reservation`: fuente de las métricas de ocupación, noches ocupadas y totales base. `Payment`: fuente del ingreso real (`revenue.total`), sumando únicamente pagos de tipo `RESERVATION` y `EXTENSION`. `Property`: provee el título de la propiedad y la relación con el landlord. `AppUser`: permite al Admin filtrar por landlord específico mediante `GET /api/users/landlords`.

**Acciones:**

* Generación de reporte individual por propiedad, calculando tasa de ocupación, noches ocupadas, total de reservas e ingresos desglosados en base, limpieza, penalizaciones y total real desde pagos.
* Generación de reporte de todas las propiedades del landlord autenticado, reporte de las propiedades de cada landlord o todas las propiedades del sistema si el rol es Admin.
* Filtro por landlord específico disponible exclusivamente para el rol Admin, mediante el parámetro opcional `?landlordId=` en el endpoint de reporte global.
* Exclusión automática de reservas con estado `CANCELLED` en todos los cálculos.
* Restricción de fechas: solo entran reservas cuyo `check_out_date` cae dentro del rango solicitado (`check_out_date <= endDate`). El backend lanza excepción si `startDate >= endDate`.
* El campo `revenue.total` proviene exclusivamente de la tabla `payment`. Si una reserva no tiene un pago de tipo `RESERVATION` o `EXTENSION` registrado, su ingreso total será `$0` aunque tenga valores en `base_total` y `cleaning_fee`.

---

### Calendario de Disponibilidad Sincronizado

Permite al landlord visualizar en una vista de calendario mensual todos los bloqueos activos de una propiedad, incluyendo reservas confirmadas y mantenimientos programados, manteniendo sincronización automática con el estado real de la propiedad.

**Endpoint:** `GET /api/properties/:propertyId/availability`

**Entidades involucradas:** `AvailabilityCalendar`: fuente de verdad de todos los bloqueos de la propiedad, clasificados por tipo (`RESERVATION`, `MAINTENANCE`, `PREVENTIVE_MAINTENANCE`), con referencia a la reserva o mantenimiento que generó el bloqueo. `Property`: provee la lista de propiedades del landlord para el selector del calendario. `Maintenance` y `Reservation`: generan y eliminan registros en `AvailabilityCalendar` automáticamente según su ciclo de vida.

**Acciones:**

* Consulta de disponibilidad de una propiedad en un rango de fechas, retornando todos los conflictos activos con su tipo, motivo y fechas de inicio y fin.
* Detección de solapamientos mediante la condición `timestamp_start < endDate AND timestamp_end > startDate`, cubriendo bloqueos parciales y totales dentro del rango.
* Sincronización automática del calendario ante los siguientes eventos: creación de reserva (`INSERT` con `block_type = RESERVATION`), cancelación o completación de reserva (`DELETE` del registro), extensión de reserva (`UPDATE` de `timestamp_end`), confirmación de mantenimiento con bloqueo (`INSERT` con `block_type = MAINTENANCE`), generación de mantenimiento preventivo sin reserva activa (`INSERT` con `block_type = PREVENTIVE_MAINTENANCE`), y resolución de mantenimiento (`DELETE` del registro correspondiente).
* Cuando la propiedad seleccionada es "Todas las propiedades", el calendario no muestra eventos y solicita al usuario seleccionar una propiedad específica, ya que la consulta de disponibilidad requiere un `propertyId` concreto.

---

### Mantenimiento

Gestiona solicitudes de mantenimiento correctivo reportadas por tenants sobre propiedades activas.

**Endpoint:** `/api/maintenances`

**Entidad:** `Maintenance`: almacena la propiedad, la reserva asociada, el usuario que reporta, titulo, descripcion, nivel de urgencia (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`), ventana de atencion programada, notas de resolucion, estado y fotos de antes/despues.


**Acciones:**
- Tenants reportan solicitudes vinculadas a una reserva activa, con fotos opcionales.
- Landlords confirman la solicitud asignando una ventana de atencion y bloqueando opcionalmente el calendario de disponibilidad.
- Landlords resuelven la solicitud con notas y fotos de respuesta; el bloqueo del calendario se elimina automaticamente.
- Listado paginado con scope por rol: tenants ven sus reportes, landlords ven los de sus propiedades, admins ven todo.

---

### Programacion de mantenimientos

Gestiona planes de mantenimiento preventivo recurrente para las propiedades de un propietario.

**Endpoint:** `/api/maintenance-schedules`

**Entidad:** `MaintenanceSchedule`: almacena la propiedad, el propietario que programa, título, descripcion, frecuencia (`DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`), intervalo numerico, fecha del ultimo ciclo completado, proxima fecha programada y estado.

**Acciones:**
- Landlords crean planes de mantenimiento recurrente para sus propiedades.
- Activacion manual de un ciclo de mantenimiento.
- Activacion automatica mediante cron job: busca todos los schedules con una fecha alcanzada y los ejecuta
- Cuando se activa un ciclo, se genera un registro `Maintenance`, se bloquea el calendario de disponibilidad por un dia (si no hay conflicto), se notifica al propietario y se avanza `nextScheduledDate` segun la frecuencia e intervalo.
- Listado paginado de schedules por propiedad (solo para el propietario).

---

### Contratos

Gestiona la generacion y firma digital de contratos de arrendamiento.

**Endpoint:** `/api/contracts`

**Entidad:** `Contract`: almacena la URL del PDF en Cloudinary, el estado, las firmas del inquilino y del propietario, y las marcas de tiempo de creación y vencimiento.

**Acciones:**
- Generación del contrato en PDF a partir de los datos de la reserva usando una plantilla. el PDF se sube al servicio de almacenamiento en nube.
- Firma del contrato por parte del inquilino y del propietario de forma independiente; el estado cambia a `SIGNED` cuando ambas firmas estan registradas.
- Consulta de contratos propios (cada rol ve solo los contratos que le corresponden).
- Al extender una reserva, el contrato existente se regenera, se restablece a `PENDING_SIGNATURES` y se borran las firmas previas.

---

### Reservas

Gestiona el ciclo completo de reservas realizadas por tenants sobre las propiedades disponibles en la plataforma.

**Endpoint:** /api/reservations

**Entidad:** Reservation: almacena la propiedad reservada, el tenant asociado, fechas de check-in y check-out, cantidad de huéspedes, noches totales, precios, estado de la reserva y fechas de creación/actualización.

**Acciones:**

- Creación de reservas por parte de tenants sobre propiedades disponibles.
- Validación de disponibilidad de la propiedad en el rango de fechas seleccionado.
- Consulta paginada de reservas propias para tenants.
- Consulta paginada de reservas asociadas a propiedades de un landlord.
- Consulta de detalle de una reserva específica.
- Extensión de reservas activas o reservadas, validando disponibilidad y generando el pago adicional correspondiente.
- Vista previa de cancelación con cálculo de penalización y montos de reembolso.
- Cancelación de reservas aplicando la política de penalización según la cercanía del check-in.
- Finalización de reservas por parte del landlord o administrador.
- Actualización automática de estado de reservas mediante procesos programados.
- Asociación con pagos, contratos, códigos de acceso, mantenimiento y calificaciones.

---

### Notificaciones

Gestiona las notificaciones generadas por el sistema para informar a los usuarios sobre eventos importantes relacionados con reservas, contratos, mantenimiento, calificaciones, códigos de acceso y acciones administrativas.

**Endpoint:** /api/notifications

**Entidad:** Notification: almacena el usuario destinatario, tipo de notificación, título, mensaje, estado de lectura, fecha de creación y reserva asociada cuando aplica.

**Acciones:**

- Consulta paginada de notificaciones del usuario autenticado.
- Filtro de notificaciones no leídas mediante el parámetro unreadOnly.
- Conteo de notificaciones no leídas para mostrar indicadores en la interfaz.
- Marcado de una notificación específica como leída.
- Marcado de todas las notificaciones del usuario como leídas.
- Eliminación de notificaciones propias.
- Generación de notificaciones para tenants, landlords y admins según eventos del sistema.
- Notificación a tenants sobre reservas confirmadas, contratos pendientes de firma, códigos de acceso generados y actualizaciones de mantenimiento.
- Notificación a landlords sobre nuevas reservas, cancelaciones, contratos firmados, mantenimientos críticos y nuevas calificaciones.
- Notificación a admins sobre registros de usuarios, documentos pendientes de verificación y mantenimientos críticos.

---

### Documentos de identidad

<!-- TODO -->

---

### Multas

<!-- TODO -->

---

### Codigos de acceso

Gestiona los códigos de acceso generados para que los tenants puedan ingresar a las propiedades durante el período de su reserva.

**Endpoint:** /api/access-codes

**Entidad:** AccessCode: almacena la propiedad, la reserva asociada, el código generado, el tipo de código, fecha de inicio de validez, fecha de expiración, estado activo/inactivo y fecha de creación.

**Acciones:**

- Generación automática de códigos de acceso al crear o confirmar una reserva.
- Consulta de códigos de acceso por parte del tenant.
- Consulta de códigos asociados a propiedades de un landlord.
- Visualización del estado del código: ACTIVE, PENDING, EXPIRED o INACTIVE.
- Validación de vigencia del código según las fechas de check-in y check-out de la reserva.
- Invalidación automática o manual de códigos cuando una reserva es cancelada o deja de estar vigente.
- Extensión de la vigencia del código cuando una reserva es extendida.
- Listado paginado de códigos de acceso para mejorar el manejo de múltiples reservas.

---

### Subida de archivos

El proyecto implementa un servicio de almacenamiento en la nube utilizando **Cloudinary** para la gestión de archivos multimedia. Esta integración permite almacenar archivos de forma segura sin depender del almacenamiento local del servidor.

**Características**
- Almacenamiento de archivos en la nube mediante Cloudinary.
- Configuración segura utilizando variables de entorno.
- Soporte para carga de imágenes y documentos PDF.
- Validación de formatos permitidos antes de procesar la solicitud.
- Generación de una URL pública del archivo almacenado para su posterior consulta.

**Formatos soportados:**
#### Imágenes
- `.png`
- `.jpg`
- `.jpeg`
- `.webp`

#### Documentos
- `.pdf`

**Funcionamiento**
1. El cliente envía un archivo mediante una petición `multipart/form-data`.
2. El backend recibe el archivo.
3. Se valida que el formato del archivo sea permitido.
4. El archivo se carga en Cloudinary.
5. Cloudinary devuelve la información del recurso almacenado, incluyendo su URL pública.
6. La URL es utilizada por la aplicación para acceder al archivo cuando sea necesario.

**Seguridad**
- Toda la configuración se realiza mediante variables de entorno.
- Se restringe la carga únicamente a los formatos soportados por la aplicación.

---

## Variables de entorno

- `POSTGRES_URL`: URL de conexion JDBC a la base de datos PostgreSQL.
- `POSTGRES_USER`: Usuario de la base de datos.
- `POSTGRES_PASSWORD`: Contrasena de la base de datos.
- `JWT_SECRET`: Clave secreta para firmar y validar tokens JWT (minimo 32 caracteres).
- `JWT_EXPIRATION_MS`: Tiempo de vida del token en milisegundos. Por defecto: `86400000` (24 h).
- `CLOUDINARY_CLOUD_NAME`: Nombre del cloud en Cloudinary.
- `CLOUDINARY_API_KEY`: API key de Cloudinary.
- `CLOUDINARY_API_SECRET`: API secret de Cloudinary.
- `CORS_ALLOWED_ORIGINS`: Origenes permitidos para CORS, separados por coma. Por defecto: `http://localhost:3000`.
- `PORT`: Puerto en el que arranca el servidor. Por defecto: `8080`.

---

## Ejecucion local

**1. Configurar variables de entorno**

Cargar las variables definidas en la seccion anterior en el entorno de ejecucion o en el archivo de configuracion del IDE.

**2. Levantar servicios externos**

- Crear una base de datos en Neon y copiar la cadena de conexion. O bien usar una base de datos local con PostgreSQL.
- Crear una cuenta en Cloudinary y obtener las credenciales del dashboard.
- Levantar el frontend (Next.js) y configurar `CORS_ALLOWED_ORIGINS` con su URL. Ya sea desde deploy a producción, o levantamiento local (en puerto 3000)

**3. Ejecutar con Maven**

Desde el directorio `property-rental-management/`:

```bash
./mvnw spring-boot:run
```

La API quedara disponible en `http://localhost:8080`.

---

## Dockerizacion

El proyecto incluye un `Dockerfile`: la primera etapa compila el JAR con Maven y la segunda genera una imagen para mantener la aplicación en ejecución.

Construir la imagen:

```bash
docker build -t property-rental-management:latest .
```

Ejecutar el contenedor cargando un archivo `.env`:

```bash
docker run -p 8080:8080 --env-file .env property-rental-management:latest
```

También, pasar las variables de forma directamente:

```bash
docker run -p 8080:8080 \
  -e POSTGRES_URL=<url> \
  -e POSTGRES_USER=<usuario> \
  -e POSTGRES_PASSWORD=<contrasena> \
  -e JWT_SECRET=<secreto> \
  -e CLOUDINARY_CLOUD_NAME=<cloud_name> \
  -e CLOUDINARY_API_KEY=<api_key> \
  -e CLOUDINARY_API_SECRET=<api_secret> \
  property-rental-management:latest
```
