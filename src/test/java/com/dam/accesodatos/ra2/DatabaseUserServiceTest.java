package com.dam.accesodatos.ra2;

import com.dam.accesodatos.config.TestDataSourceConfig;
import com.dam.accesodatos.model.User;
import com.dam.accesodatos.model.UserCreateDto;
import com.dam.accesodatos.model.UserQueryDto;
import com.dam.accesodatos.model.UserUpdateDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de tests para DatabaseUserService
 *
 * Verifica todos los métodos JDBC implementados:
 * - 5 métodos ejemplo (ya implementados)
 * - 10 métodos TODO (implementados por estudiantes)
 *
 * PATRÓN AAA (Arrange-Act-Assert):
 * Cada test sigue el patrón:
 * - Arrange: Preparar datos y estado inicial
 * - Act: Ejecutar el método bajo prueba
 * - Assert: Verificar el resultado esperado
 */
@SpringBootTest
@Import(TestDataSourceConfig.class)
@Sql(scripts = {"/test-schema.sql", "/test-data.sql"},
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class DatabaseUserServiceTest {

    @Autowired
    private DatabaseUserService service;

    // ========== Tests para métodos EJEMPLO (ya implementados) ==========

    @Test
    void testConnection_shouldReturnSuccessMessage() {
        // Arrange: No hay datos a preparar, el servicio ya está inyectado

        // Act: Ejecutar test de conexión
        String result = service.testConnection();

        // Assert: Verificar que la conexión fue exitosa
        assertNotNull(result, "El resultado no debe ser null");
        assertTrue(result.contains("Conexión exitosa"), "Debe contener mensaje de éxito");
        assertTrue(result.contains("H2"), "Debe contener el nombre de la BD");
    }

    @Test
    void testCreateUser_shouldInsertAndReturnUserWithId() {
        // Arrange: Preparar datos del nuevo usuario
        UserCreateDto dto = new UserCreateDto(
            "New Test User",
            "newuser@example.com",
            "IT",
            "Developer"
        );

        // Act: Crear el usuario en la BD
        User created = service.createUser(dto);

        // Assert: Verificar que el usuario fue creado correctamente
        assertNotNull(created, "El usuario creado no debe ser null");
        assertNotNull(created.getId(), "El ID debe haber sido generado");
        assertEquals("New Test User", created.getName(), "El nombre debe coincidir");
        assertEquals("newuser@example.com", created.getEmail(), "El email debe coincidir");
        assertEquals("IT", created.getDepartment(), "El departamento debe coincidir");
        assertEquals("Developer", created.getRole(), "El rol debe coincidir");
        assertTrue(created.getActive(), "El usuario debe estar activo por defecto");
        assertNotNull(created.getCreatedAt(), "created_at debe estar establecido");
        assertNotNull(created.getUpdatedAt(), "updated_at debe estar establecido");
    }

    @Test
    void testCreateUser_withDuplicateEmail_shouldThrowException() {
        // Arrange: Preparar DTO con email duplicado (ya existe en test-data.sql)
        UserCreateDto dto = new UserCreateDto(
            "Duplicate",
            "test1@example.com",
            "IT",
            "Dev"
        );

        // Act & Assert: Verificar que se lanza excepción por email duplicado
        assertThrows(RuntimeException.class, () -> service.createUser(dto),
            "Debe lanzar RuntimeException por email duplicado");
    }

    @Test
    void testFindUserById_shouldReturnUser() {
        // Arrange: ID del usuario a buscar (existe en test-data.sql)
        Long userId = 1L;

        // Act: Buscar usuario por ID
        User user = service.findUserById(userId);

        // Assert: Verificar que se encontró el usuario correcto
        assertNotNull(user, "El usuario debe existir");
        assertEquals(1L, user.getId(), "El ID debe ser 1");
        assertEquals("Test User 1", user.getName(), "El nombre debe coincidir");
        assertEquals("test1@example.com", user.getEmail(), "El email debe coincidir");
    }

    @Test
    void testFindUserById_withNonExistentId_shouldReturnNull() {
        // Arrange: ID de usuario que no existe
        Long nonExistentId = 9999L;

        // Act: Intentar buscar usuario inexistente
        User user = service.findUserById(nonExistentId);

        // Assert: Verificar que retorna null
        assertNull(user, "Debe retornar null para ID inexistente");
    }

    @Test
    void testUpdateUser_shouldModifyExistingUser() {
        // Arrange: Preparar datos de actualización
        Long userId = 1L;
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("Test User Updated");
        dto.setRole("Senior Developer");

        // Act: Actualizar el usuario
        User updated = service.updateUser(userId, dto);

        // Assert: Verificar que los cambios se aplicaron
        assertNotNull(updated, "El usuario actualizado no debe ser null");
        assertEquals("Test User Updated", updated.getName(), "El nombre debe haberse actualizado");
        assertEquals("Senior Developer", updated.getRole(), "El rol debe haberse actualizado");
        assertEquals("test1@example.com", updated.getEmail(), "El email no debe haber cambiado");
    }

    @Test
    void testUpdateUser_withNonExistentId_shouldThrowException() {
        // Arrange: Preparar actualización para usuario inexistente
        Long nonExistentId = 9999L;
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("Nobody");

        // Act & Assert: Verificar que se lanza excepción
        assertThrows(RuntimeException.class, () -> service.updateUser(nonExistentId, dto),
            "Debe lanzar RuntimeException al actualizar usuario inexistente");
    }

    @Test
    void testTransferData_shouldInsertMultipleUsersInTransaction() {
        // Arrange: Preparar lista de usuarios para transferencia
        List<User> users = new ArrayList<>();

        User user1 = new User();
        user1.setName("Transfer User A");
        user1.setEmail("transfera@example.com");
        user1.setDepartment("IT");
        user1.setRole("Dev");

        User user2 = new User();
        user2.setName("Transfer User B");
        user2.setEmail("transferb@example.com");
        user2.setDepartment("HR");
        user2.setRole("Manager");

        users.add(user1);
        users.add(user2);

        // Act: Ejecutar transferencia transaccional
        boolean result = service.transferData(users);

        // Assert: Verificar que la transacción fue exitosa
        assertTrue(result, "La transferencia debe ser exitosa");

        // Verificar que se insertaron consultando todos los usuarios
        List<User> allUsers = service.findAll();
        assertTrue(allUsers.size() >= 5, "Debe haber al menos 5 usuarios (3 originales + 2 nuevos)");
    }

    // ========== Tests para métodos TODO (implementados por estudiantes) ==========

    // CE2.a: Connection Management

    @Test
    void testGetConnectionInfo_shouldReturnConnectionDetails() {
        // Arrange: No hay preparación necesaria

        // Act: Obtener información de conexión
        Map<String, String> info = service.getConnectionInfo();

        // Assert: Verificar que se obtuvieron todos los datos necesarios
        assertNotNull(info, "El mapa de información no debe ser null");
        assertNotNull(info.get("url"), "Debe contener la URL de conexión");
        assertNotNull(info.get("user"), "Debe contener el usuario");
        assertNotNull(info.get("databaseProductName"), "Debe contener el nombre del producto");
        assertNotNull(info.get("driverName"), "Debe contener el nombre del driver");
        assertTrue(info.get("databaseProductName").contains("H2"), "El producto debe ser H2");
    }

    // CE2.b: CRUD Operations

    @Test
    void testDeleteUser_shouldRemoveExistingUser() {
        // Arrange: Verificar que el usuario existe antes de eliminar
        Long userId = 1L;
        User existing = service.findUserById(userId);
        assertNotNull(existing, "El usuario debe existir antes de eliminarlo");

        // Act: Eliminar el usuario
        boolean deleted = service.deleteUser(userId);

        // Assert: Verificar que se eliminó correctamente
        assertTrue(deleted, "La eliminación debe retornar true");

        // Verificar que ya no existe
        User notFound = service.findUserById(userId);
        assertNull(notFound, "El usuario no debe existir después de eliminarlo");
    }

    @Test
    void testDeleteUser_withNonExistentId_shouldReturnFalse() {
        // Arrange: ID que no existe
        Long nonExistentId = 9999L;

        // Act: Intentar eliminar usuario inexistente
        boolean deleted = service.deleteUser(nonExistentId);

        // Assert: Debe retornar false
        assertFalse(deleted, "Debe retornar false al eliminar usuario inexistente");
    }

    @Test
    void testFindAll_shouldReturnAllUsers() {
        // Arrange: La BD ya tiene usuarios cargados desde test-data.sql

        // Act: Obtener todos los usuarios
        List<User> users = service.findAll();

        // Assert: Verificar que se obtuvieron todos los usuarios
        assertNotNull(users, "La lista no debe ser null");
        assertFalse(users.isEmpty(), "La lista no debe estar vacía");
        assertEquals(3, users.size(), "Debe haber 3 usuarios (de test-data.sql)");

        // Verificar ordenamiento por created_at DESC
        assertEquals("Test User 3", users.get(0).getName(),
            "El primer usuario debe ser el más reciente (Test User 3)");
    }

    // CE2.c: Advanced Queries

    @Test
    void testFindUsersByDepartment_shouldReturnFilteredUsers() {
        // Arrange: Departamento a buscar
        String department = "IT";

        // Act: Buscar usuarios del departamento IT
        List<User> itUsers = service.findUsersByDepartment(department);

        // Assert: Verificar resultados
        assertNotNull(itUsers, "La lista no debe ser null");
        assertFalse(itUsers.isEmpty(), "Debe haber usuarios de IT");

        // Verificar que todos son del departamento IT y están activos
        for (User user : itUsers) {
            assertEquals("IT", user.getDepartment(), "Todos deben ser del departamento IT");
            assertTrue(user.getActive(), "Todos deben estar activos");
        }
    }

    @Test
    void testFindUsersByDepartment_withNonExistentDept_shouldReturnEmptyList() {
        // Arrange: Departamento que no existe
        String nonExistentDept = "NONEXISTENT";

        // Act: Buscar usuarios de departamento inexistente
        List<User> users = service.findUsersByDepartment(nonExistentDept);

        // Assert: Debe retornar lista vacía
        assertNotNull(users, "La lista no debe ser null");
        assertTrue(users.isEmpty(), "La lista debe estar vacía para departamento inexistente");
    }

    @Test
    void testSearchUsers_withMultipleFilters_shouldReturnMatchingUsers() {
        // Arrange: Preparar query con múltiples filtros
        UserQueryDto query = new UserQueryDto();
        query.setDepartment("IT");
        query.setActive(true);
        query.setLimit(10);
        query.setOffset(0);

        // Act: Ejecutar búsqueda con filtros
        List<User> users = service.searchUsers(query);

        // Assert: Verificar que todos los resultados cumplen los filtros
        assertNotNull(users, "La lista no debe ser null");

        for (User user : users) {
            assertEquals("IT", user.getDepartment(), "Debe ser del departamento IT");
            assertTrue(user.getActive(), "Debe estar activo");
        }
    }

    @Test
    void testSearchUsers_withOnlyPagination_shouldReturnLimitedResults() {
        // Arrange: Preparar query solo con paginación
        UserQueryDto query = new UserQueryDto();
        query.setLimit(3);
        query.setOffset(0);

        // Act: Ejecutar búsqueda paginada
        List<User> users = service.searchUsers(query);

        // Assert: Verificar que se respeta el límite
        assertNotNull(users, "La lista no debe ser null");
        assertTrue(users.size() <= 3, "No debe exceder el límite de 3 usuarios");
    }

    @Test
    void testSearchUsers_withRoleFilter_shouldReturnMatchingUsers() {
        // Arrange: Preparar query con filtro de rol
        UserQueryDto query = new UserQueryDto();
        query.setRole("Developer");
        query.setLimit(10);
        query.setOffset(0);

        // Act: Buscar usuarios con rol Developer
        List<User> users = service.searchUsers(query);

        // Assert: Verificar que todos tienen el rol correcto
        assertNotNull(users, "La lista no debe ser null");

        for (User user : users) {
            assertEquals("Developer", user.getRole(), "Todos deben tener rol Developer");
        }
    }

    @Test
    void testFindUsersWithPagination_shouldReturnCorrectPage() {
        // Arrange: Parámetros de paginación para dos páginas
        int offset1 = 0;
        int limit = 3;
        int offset2 = 3;

        // Act: Obtener primera y segunda página
        List<User> page1 = service.findUsersWithPagination(offset1, limit);
        List<User> page2 = service.findUsersWithPagination(offset2, limit);

        // Assert: Verificar ambas páginas
        assertNotNull(page1, "La primera página no debe ser null");
        assertTrue(page1.size() <= 3, "La primera página no debe exceder el límite");

        assertNotNull(page2, "La segunda página no debe ser null");
        assertTrue(page2.size() <= 3, "La segunda página no debe exceder el límite");

        // Verificar que son diferentes usuarios (si ambas tienen datos)
        if (!page1.isEmpty() && !page2.isEmpty()) {
            assertNotEquals(page1.get(0).getId(), page2.get(0).getId(),
                "Las páginas deben contener usuarios diferentes");
        }
    }

    @Test
    void testFindUsersWithPagination_withLargeOffset_shouldReturnEmptyList() {
        // Arrange: Offset muy grande que excede el número de usuarios
        int largeOffset = 1000;
        int limit = 10;

        // Act: Intentar obtener página con offset muy grande
        List<User> users = service.findUsersWithPagination(largeOffset, limit);

        // Assert: Debe retornar lista vacía
        assertNotNull(users, "La lista no debe ser null");
        assertTrue(users.isEmpty(), "Debe retornar lista vacía con offset muy grande");
    }

    // CE2.d: Transactions

    @Test
    void testBatchInsertUsers_shouldInsertMultipleUsers() {
        // Arrange: Preparar lista de usuarios para inserción batch
        List<User> users = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            User user = new User();
            user.setName("Batch User " + i);
            user.setEmail("batch" + i + "@example.com");
            user.setDepartment("Sales");
            user.setRole("Agent");
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            users.add(user);
        }

        // Act: Insertar usuarios en batch
        int insertedCount = service.batchInsertUsers(users);

        // Assert: Verificar que se insertaron todos
        assertEquals(5, insertedCount, "Deben insertarse 5 usuarios");

        // Verificar consultando todos los usuarios
        List<User> allUsers = service.findAll();
        assertEquals(8, allUsers.size(), "Debe haber 8 usuarios (3 originales + 5 nuevos)");
    }

    @Test
    void testBatchInsertUsers_withEmptyList_shouldReturnZero() {
        // Arrange: Lista vacía de usuarios
        List<User> users = new ArrayList<>();

        // Act: Intentar inserción batch con lista vacía
        int insertedCount = service.batchInsertUsers(users);

        // Assert: Debe retornar 0
        assertEquals(0, insertedCount, "Debe retornar 0 para lista vacía");
    }

    // CE2.e: Metadata

    @Test
    void testGetDatabaseInfo_shouldReturnFormattedInfo() {
        // Arrange: No hay preparación necesaria

        // Act: Obtener información de la base de datos
        String info = service.getDatabaseInfo();

        // Assert: Verificar que contiene toda la información esperada
        assertNotNull(info, "La información no debe ser null");
        assertTrue(info.contains("Base de Datos"), "Debe contener 'Base de Datos'");
        assertTrue(info.contains("H2"), "Debe contener 'H2'");
        assertTrue(info.contains("Driver JDBC"), "Debe contener 'Driver JDBC'");
        assertTrue(info.contains("URL"), "Debe contener 'URL'");
        assertTrue(info.contains("Usuario"), "Debe contener 'Usuario'");
        assertTrue(info.contains("Soporta Batch"), "Debe contener 'Soporta Batch'");
        assertTrue(info.contains("Soporta Transacciones"), "Debe contener 'Soporta Transacciones'");
    }

    @Test
    void testGetTableColumns_shouldReturnColumnMetadata() {
        // Arrange: Nombre de la tabla a consultar
        String tableName = "users";

        // Act: Obtener metadatos de las columnas
        List<Map<String, Object>> columns = service.getTableColumns(tableName);

        // Assert: Verificar que se obtuvieron las columnas
        assertNotNull(columns, "La lista de columnas no debe ser null");
        assertFalse(columns.isEmpty(), "Debe haber columnas en la tabla users");

        // Verificar que tenemos las columnas principales
        boolean hasIdColumn = columns.stream()
            .anyMatch(col -> "ID".equals(col.get("name")));
        boolean hasNameColumn = columns.stream()
            .anyMatch(col -> "NAME".equals(col.get("name")));
        boolean hasEmailColumn = columns.stream()
            .anyMatch(col -> "EMAIL".equals(col.get("name")));

        assertTrue(hasIdColumn, "Debe tener columna ID");
        assertTrue(hasNameColumn, "Debe tener columna NAME");
        assertTrue(hasEmailColumn, "Debe tener columna EMAIL");

        // Verificar estructura de cada columna
        for (Map<String, Object> column : columns) {
            assertNotNull(column.get("name"), "Cada columna debe tener nombre");
            assertNotNull(column.get("typeName"), "Cada columna debe tener tipo");
            assertNotNull(column.get("nullable"), "Cada columna debe indicar si es nullable");
        }
    }

    @Test
    void testGetTableColumns_withNonExistentTable_shouldReturnEmptyList() {
        // Arrange: Nombre de tabla que no existe
        String nonExistentTable = "nonexistent_table";

        // Act: Intentar obtener columnas de tabla inexistente
        List<Map<String, Object>> columns = service.getTableColumns(nonExistentTable);

        // Assert: Debe retornar lista vacía
        assertNotNull(columns, "La lista no debe ser null");
        assertTrue(columns.isEmpty(), "Debe retornar lista vacía para tabla inexistente");
    }

    // CE2.f: Stored Procedures

    @Test
    void testExecuteCountByDepartment_shouldReturnCorrectCount() {
        // Arrange: Departamento a contar
        String department = "IT";

        // Act: Ejecutar conteo por departamento
        int count = service.executeCountByDepartment(department);

        // Assert: Verificar que el conteo es correcto
        assertTrue(count > 0, "Debe haber usuarios de IT");

        // Verificar contra findUsersByDepartment (debe coincidir)
        List<User> itUsers = service.findUsersByDepartment(department);
        assertEquals(itUsers.size(), count, "El conteo debe coincidir con findUsersByDepartment");
    }

    @Test
    void testExecuteCountByDepartment_withNonExistentDept_shouldReturnZero() {
        // Arrange: Departamento que no existe
        String nonExistentDept = "NONEXISTENT";

        // Act: Ejecutar conteo para departamento inexistente
        int count = service.executeCountByDepartment(nonExistentDept);

        // Assert: Debe retornar 0
        assertEquals(0, count, "Debe retornar 0 para departamento inexistente");
    }

    @Test
    void testExecuteCountByDepartment_shouldCountOnlyActiveUsers() {
        // Arrange: Departamento a contar (solo activos)
        String department = "HR";

        // Act: Ejecutar conteo (debe contar solo usuarios activos)
        int hrCount = service.executeCountByDepartment(department);

        // Assert: Verificar que solo cuenta usuarios activos
        assertTrue(hrCount >= 0, "El conteo debe ser >= 0");

        // Verificar contra findUsersByDepartment (que también filtra por activos)
        List<User> hrUsers = service.findUsersByDepartment(department);
        assertEquals(hrUsers.size(), hrCount,
            "Debe coincidir con findUsersByDepartment (solo activos)");
    }
}
