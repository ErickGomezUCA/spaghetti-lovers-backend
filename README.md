# property-rental-management

Backend de la plataforma de gestion de alquileres de propiedades. API REST construida con Spring Boot 4, Java 21 y PostgreSQL.

---

## Resumen del proyecto

<!-- TODO: Agregar resumen general del proyecto -->

---

## Tecnologias

<!-- TODO: Agregar descripcion de dependencias principales de Maven -->

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

<!-- TODO: Describir configuracion de autenticacion y autorizacion -->

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

