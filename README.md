# 🐈‍⬛ DoramionDB

> **Un motor de base de datos relacional escrito desde cero en Java puro.**

**DoramionDB** es un proyecto educativo diseñado para desmitificar el funcionamiento interno de las bases de datos. El objetivo no es reemplazar a PostgreSQL o MySQL, sino entender cómo funcionan "las tripas" de un sistema de gestión de bases de datos (DBMS) construyendo uno pieza por pieza.

El proyecto sigue la arquitectura clásica de **SQLite**, implementando un motor de almacenamiento basado en B-Trees, un sistema de paginación de memoria y un compilador SQL básico.

---

## 🏗 Arquitectura

El sistema está dividido en tres capas principales que separan las responsabilidades lógicas de las físicas:

1. **Core & Frontend:** Maneja la interacción con el usuario (REPL), el tokenizado de comandos y la máquina virtual (VM).

2. **SQL Compiler:** Transforma texto (`SELECT...`) en bytecode o instrucciones ejecutables.

3. **Backend (Storage Engine):** La joya de la corona. Gestiona la persistencia en disco, la paginación (`Pager`) y las estructuras de datos (`B-Tree`).


---

## 🗺 Roadmap y Hitos Técnicos

El desarrollo es iterativo. Empezamos con una lista en memoria y terminaremos con un árbol balanceado persistente en disco.

### 🏁 Fase 1: El MVP en Memoria (The In-Memory REPL)

El objetivo es tener una interfaz funcional y un almacenamiento volátil. Entenderemos el flujo básico de ejecución.

- [x] **Configuración del Entorno:** Setup de Maven/Gradle, Java 21+ y JUnit 5.

- [x] **El REPL (Read-Eval-Print Loop):** Crear un bucle infinito que acepte comandos (`Scanner`).

- [x] **Tokenizer Básico:** Romper cadenas de texto (`insert 1 user`) en tokens manejables.

- [x] **Hardcoded Table:** Implementar una tabla fija con columnas predefinidas (`id`, `username`, `email`) usando Java Records.

- [x] **Motor de Ejecución (VM):** Lógica simple para `INSERT` y `SELECT` sobre un `ArrayList`.

- [x] **Manejo de Errores:** Respuestas controladas ante comandos inválidos o sintaxis incorrecta.


### 💾 Fase 2: Persistencia y Abstracción del Disco

Aquí abandonamos el `ArrayList` y empezamos a trabajar con bytes crudos. Si apagas el programa, los datos deben seguir ahí.

- [x] **Serialización de Filas:** Convertir un objeto `Record` a un array de bytes (`byte[]`) y viceversa.

- [x] **Implementación del Pager:** Crear la clase que lee/escribe bloques de 4KB desde el disco a la memoria.

- [ ] **Capa de Abstracción de Archivos:** Usar `RandomAccessFile` o `FileChannel` de Java NIO.

- [ ] **El Cursor:** Implementar un objeto iterador que sepa moverse por las filas dentro de las páginas binarias.

- [ ] **Persistencia Básica:** Lograr que al reiniciar la app, los datos se lean del archivo `.db`.


### 🌳 Fase 3: La Estructura B-Tree (El Núcleo)

El `ArrayList` es $O(n)$. Para ser una base de datos real, necesitamos búsquedas logarítmicas $O(\log n)$. Implementaremos un B-Tree (o B+Tree).

- [ ] **Formato de Nodo Hoja (Leaf Node):** Definir cabeceras y estructura de bytes para almacenar celdas en una página.

- [ ] **Inserción en Hoja:** Lógica para insertar claves ordenadas dentro de un nodo.

- [ ] **División de Nodos (Splitting):** El algoritmo crítico. Cuando una página se llena, dividirla en dos y crear un padre.

- [ ] **Nodos Internos:** Implementar nodos que solo guardan claves y punteros a hijos.

- [ ] **Búsqueda Binaria:** Reemplazar el escaneo lineal por búsqueda binaria dentro de las páginas.

- [ ] **Recorrido del Árbol:** Lógica para navegar desde la raíz hasta las hojas.


### 🚀 Fase 4: Optimizaciones y SQL (Going Beyond)

Superando el tutorial básico. Hacemos que DoramionDB sea más robusto y flexible.

- [ ] **Soporte para Cadenas de Longitud Variable (VarChar):** Dejar de usar `String` de tamaño fijo (32 bytes) e implementar un sistema de punteros o _slots_ dinámicos.

- [ ] **Where Clause:** Implementar un evaluador de expresiones simple para `SELECT * FROM users WHERE id > 10`.

- [ ] **Testing de Propiedades:** Tests automatizados que inserten miles de registros aleatorios y verifiquen la integridad del árbol.

- [ ] **Caché de Páginas (Buffer Pool):** No leer del disco si la página ya está en memoria (LRU Cache).


### 🔮 Fase 5: Características Avanzadas (Future Scope)

- [ ] **Concurrencia:** Permitir lecturas y escrituras simultáneas (Locking).

- [ ] **Servidor TCP:** Convertir la app de terminal en un servidor que escuche en un puerto (Sockets).

- [ ] **WAL (Write Ahead Log):** Implementar transacciones seguras a prueba de fallos de luz (ACID).


---

## 🛠 Tecnologías

- **Lenguaje:** Java 21 (Records, Pattern Matching, Virtual Threads).

- **Build Tool:** Maven.

- **Testing:** JUnit 5.

- **Sin Dependencias Externas:** Cero magia de frameworks (No Spring, No Hibernate). Solo la librería estándar.
    