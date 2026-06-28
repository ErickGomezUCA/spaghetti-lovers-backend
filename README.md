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
