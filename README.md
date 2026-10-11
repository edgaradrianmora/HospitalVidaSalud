--Hospital Vida Salud

Arquitectura de microservicios----------------------------------------

--Servicios del Sistema--

1.Eureka (Puerto 8761): Registro y descubrimiento de servicios.
2.Gateway (Puerto 8080): Punto de entrada único que enruta por nombre de servicio.
3.Pacientes (Puerto 8081): Registro, consulta, actualización y estado de pacientes; rechaza documentos duplicados (Base de datos: bd_pacientes).
4.Citas (Puerto 8082): Programación de citas y control de estados con transiciones válidas (Base de datos: bd_citas).
5.Atenciones (Puerto 8083): Registro de la atención vinculada a una cita CONFIRMADA (Base de datos: bd_atenciones).
6.Notificaciones (Puerto 8084): Consume eventos de RabbitMQ y emite avisos simulados en log.

Cada servicio maneja su propia base de datos de manera independiente y se comunican mediante identificadores y llamadas HTTP

--Reglas de Negocio Principales--

.No se registran dos pacientes con el mismo tipo y número de documento (HTTP 409).
.Una cita solo se crea para un paciente existente y ACTIVO; nace en estado PENDIENTE.
.Transiciones de cita permitidas: PENDIENTE hacia CONFIRMADA o CANCELADA; CONFIRMADA hacia ATENDIDA, CANCELADA o AUSENTE. Cualquier otra se rechaza.
.Una atencion solo se registra si la cita está CONFIRMADA.

--Endpoints Principales (A través del Gateway en http://localhost:8080)--

-GET /api/pacientes: Lista los pacientes.
-GET /api/pacientes/{id}: Consulta un paciente específico (404 si no existe).
-POST /api/pacientes: Registra un paciente (201; 409 si el documento está duplicado).
-PUT /api/pacientes/{id}: Actualiza datos de contacto.
-PUT /api/pacientes/{id}/estado/{estado}: Cambia el estado del paciente.
-GET /api/citas, /api/citas/{id}, /api/citas/paciente/{pacienteId}: Consultas de citas.
-POST /api/citas: Registra una cita.
-PUT /api/citas/{id}/estado/{estado}: Cambia el estado validando la transición.
-GET /api/atenciones, /api/atenciones/{id}, /api/atenciones/paciente/{pacienteId}: Consultas de atenciones.
-POST /api/atenciones: Registra una atención.
-PUT /api/atenciones/{id}/estado/{estado}: Cambia el estado de la atención.

Requisitos del Entorno

Java 21.
Maven 3.9 o superior (o el Maven Wrapper mvnw incluido).
PostgreSQL con las bases bd_pacientes, bd_citas y bd_atenciones.
RabbitMQ en localhost:5672.
Configuración y Variables de Entorno
Las credenciales no se encuentran en el repositorio. Deben definirse previamente:
DB_PASSWORD: Contraseña del usuario postgres.
RABBITMQ_USERNAME: Usuario de RabbitMQ.
RABBITMQ_PASSWORD: Contraseña de RabbitMQ.

Cómo Ejecutar

Primero iniciar los servicios en el siguiente orden, ejecutando mvnw spring-boot:run o desde el IDE en cada carpeta:

Eureka (8761)
Gateway (8080)
Pacientes (8081)
Citas (8082)
Atenciones (8083)
Notificaciones (8084)

Panel de Eureka disponible en http://localhost:8761.

Pruebas e Integración Continua

Ejecución de pruebas locales: Ingresar a la carpeta del servicio correspondiente (por ejemplo, Pacientes como prueba inicial en este proyecto) y ejecutamos mvn test. Las pruebas unitarias usan Mockito y reactor-test sin requerir PostgreSQL ni RabbitMQ.

