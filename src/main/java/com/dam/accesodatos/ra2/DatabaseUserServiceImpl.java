package com.dam.accesodatos.ra2;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.dam.accesodatos.config.DatabaseConfig;
import com.dam.accesodatos.model.User;
import com.dam.accesodatos.model.UserCreateDto;
import com.dam.accesodatos.model.UserQueryDto;
import com.dam.accesodatos.model.UserUpdateDto;

import ch.qos.logback.core.joran.conditional.ElseAction;

/**
 * Implementación del servicio JDBC para gestión de usuarios
 *
 * ESTRUCTURA DE IMPLEMENTACIÓN:
 * - ✅ 5 MÉTODOS IMPLEMENTADOS (ejemplos para estudiantes)
 * - ❌ 10 MÉTODOS TODO (estudiantes deben implementar)
 *
 * MÉTODOS IMPLEMENTADOS (Ejemplos):
 * 1. testConnection() - Ejemplo básico de conexión JDBC
 * 2. createUser() - INSERT con PreparedStatement y getGeneratedKeys
 * 3. findUserById() - SELECT y mapeo de ResultSet a objeto
 * 4. updateUser() - UPDATE statement con validación
 * 5. transferData() - Transacción manual con commit/rollback
 *
 * MÉTODOS TODO (Estudiantes implementan):
 * 1. getConnectionInfo()
 * 2. deleteUser()
 * 3. findAll()
 * 4. findUsersByDepartment()
 * 5. searchUsers()
 * 6. findUsersWithPagination()
 * 7. batchInsertUsers()
 * 8. getDatabaseInfo()
 * 9. getTableColumns()
 * 10. executeCountByDepartment()
 */
@Service
public class DatabaseUserServiceImpl implements DatabaseUserService {

    // JDBC PURO - SIN Spring DataSource
    // Los estudiantes usan DatabaseConfig.getConnection() directamente
    // para obtener conexiones usando DriverManager

    // ========== CE2.a: Connection Management ==========

    /**
     * ✅ EJEMPLO IMPLEMENTADO 1/5: Prueba de conexión básica
     *
     * Este método muestra el patrón fundamental de JDBC PURO:
     * 1. Obtener conexión usando DriverManager (vía DatabaseConfig)
     * 2. Ejecutar una query simple
     * 3. Procesar resultados
     * 4. Cerrar recursos con try-with-resources
     */
    @Override
    public String testConnection() {
        // Patrón try-with-resources: cierra automáticamente Connection, Statement, ResultSet
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 as test, DATABASE() as db_name")) {

            // Validar que la conexión está abierta
            if (conn.isClosed()) {
                throw new RuntimeException("La conexión está cerrada");
            }

            // Navegar al primer (y único) resultado
            if (rs.next()) {
                int testValue = rs.getInt("test");
                String dbName = rs.getString("db_name");

                // Obtener información adicional de la conexión
                DatabaseMetaData metaData = conn.getMetaData();
                String dbProduct = metaData.getDatabaseProductName();
                String dbVersion = metaData.getDatabaseProductVersion();

                return String.format("✓ Conexión exitosa a %s %s | Base de datos: %s | Test: %d",
                        dbProduct, dbVersion, dbName, testValue);
            } else {
                throw new RuntimeException("No se obtuvieron resultados de la query de prueba");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al probar la conexión: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> getConnectionInfo() {
        throw new UnsupportedOperationException("TODO: Método getConnectionInfo() para implementar por estudiantes");
    }

    // ========== CE2.b: CRUD Operations ==========

    /**
     * ✅ EJEMPLO IMPLEMENTADO 2/5: INSERT con PreparedStatement
     *
     * Este método muestra cómo:
     * - Usar PreparedStatement para prevenir SQL injection
     * - Setear parámetros con tipos específicos
     * - Obtener IDs autogenerados con getGeneratedKeys()
     * - Manejar excepciones SQL
     */
    @Override
    public User createUser(UserCreateDto dto) {
        String sql = "INSERT INTO users (name, email, department, role, active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Setear parámetros del PreparedStatement
            // Índices empiezan en 1, no en 0
            pstmt.setString(1, dto.getName());
            pstmt.setString(2, dto.getEmail());
            pstmt.setString(3, dto.getDepartment());
            pstmt.setString(4, dto.getRole());
            pstmt.setBoolean(5, true); // active por defecto
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now())); // created_at
            pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now())); // updated_at

            // Ejecutar INSERT y obtener número de filas afectadas
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("Error: INSERT no afectó ninguna fila");
            }

            // Obtener el ID autogenerado
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long generatedId = generatedKeys.getLong(1);

                    // Crear objeto User con el ID generado
                    User newUser = new User(generatedId, dto.getName(), dto.getEmail(),
                            dto.getDepartment(), dto.getRole());
                    newUser.setActive(true);
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setUpdatedAt(LocalDateTime.now());

                    return newUser;
                } else {
                    throw new RuntimeException("Error: INSERT exitoso pero no se generó ID");
                }
            }

        } catch (SQLException e) {
            // Manejar errores específicos como email duplicado
            if (e.getMessage().contains("Unique index or primary key violation")) {
                throw new RuntimeException("Error: El email '" + dto.getEmail() + "' ya está registrado", e);
            }
            throw new RuntimeException("Error al crear usuario: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ EJEMPLO IMPLEMENTADO 3/5: SELECT y mapeo de ResultSet
     *
     * Este método muestra cómo:
     * - Usar PreparedStatement para queries parametrizadas
     * - Navegar ResultSet con rs.next()
     * - Mapear columnas SQL a campos Java
     * - Manejar tipos de datos (Long, String, Boolean, Timestamp)
     */
    @Override
    public User findUserById(Long id) {
        String sql = "SELECT id, name, email, department, role, active, created_at, updated_at " +
                     "FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Setear parámetro WHERE id = ?
            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                // next() retorna true si hay un resultado, false si no
                if (rs.next()) {
                    // Mapear ResultSet a objeto User
                    return mapResultSetToUser(rs);
                } else {
                    // No se encontró usuario con ese ID
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario con ID " + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * ✅ EJEMPLO IMPLEMENTADO 4/5: UPDATE statement
     *
     * Este método muestra cómo:
     * - Validar que un registro existe antes de actualizar
     * - Construir UPDATE statement con campos opcionales
     * - Actualizar solo los campos proporcionados
     * - Verificar filas afectadas
     */
    @Override
    public User updateUser(Long id, UserUpdateDto dto) {
        // Primero verificar que el usuario existe
        User existing = findUserById(id);
        if (existing == null) {
            throw new RuntimeException("No se encontró usuario con ID " + id);
        }

        // Aplicar actualizaciones del DTO al usuario existente
        dto.applyTo(existing);

        // Construir UPDATE statement
        String sql = "UPDATE users SET name = ?, email = ?, department = ?, role = ?, " +
                     "active = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Setear todos los parámetros (incluso los no modificados)
            pstmt.setString(1, existing.getName());
            pstmt.setString(2, existing.getEmail());
            pstmt.setString(3, existing.getDepartment());
            pstmt.setString(4, existing.getRole());
            pstmt.setBoolean(5, existing.getActive());
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(7, id);

            // Ejecutar UPDATE
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("Error: UPDATE no afectó ninguna fila");
            }

            // Retornar usuario actualizado
            return findUserById(id);

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar usuario con ID " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteUser(Long id) {

        // Verifico que el usuario existe
        User user = findUserById(id);
        if (user == null){
            throw new RuntimeException("No se encontró usuario con ID " + id);
        }
        
        // Si existe, preparo la query
        String sql = "DELETE from users WHERE id=?";

        try(Connection con = DatabaseConfig.getConnection();
        PreparedStatement pStatement = con.prepareStatement(sql)){

            pStatement.setLong(1, id);

            if (pStatement.executeUpdate() != 0){
                return true;
            }else{
                throw new RuntimeException("Error: DELETE no afectó ninguna fila");
            }

        }catch (SQLException e){
            throw new RuntimeException("Error al borrar usuario con ID "+ id +": "+e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() {
        // Preparo la query
        String sql = "SELECT * FROM users";

        // Creo las variables
        List<User> usuarios = new ArrayList<>();

        try(Connection con = DatabaseConfig.getConnection();
        PreparedStatement psmt = con.prepareStatement(sql)){

            try (ResultSet rs = psmt.executeQuery()){
                while (rs.next()){
                    usuarios.add(mapResultSetToUser(rs));
                }
            }

            return usuarios;

        }catch (SQLException e){
            throw new RuntimeException("Error al listar usuarios: "+e.getMessage(), e);
        }
    }

    // ========== CE2.c: Advanced Queries ==========

    @Override
    public List<User> findUsersByDepartment(String department) {
        throw new UnsupportedOperationException("TODO: Método findUsersByDepartment() para implementar por estudiantes");
    }

    @Override
    public List<User> searchUsers(UserQueryDto query) {
        throw new UnsupportedOperationException("TODO: Método searchUsers() para implementar por estudiantes");
    }

    @Override
    public List<User> findUsersWithPagination(int offset, int limit) {
        throw new UnsupportedOperationException("TODO: Método findUsersWithPagination() para implementar por estudiantes");
    }

    // ========== CE2.d: Transactions ==========

    /**
     * ✅ EJEMPLO IMPLEMENTADO 5/5: Transacción manual con commit/rollback
     *
     * Este método muestra cómo:
     * - Desactivar auto-commit para control manual de transacciones
     * - Realizar múltiples operaciones en una transacción
     * - Hacer commit si todo tiene éxito
     * - Hacer rollback si hay algún error
     * - Restaurar auto-commit al estado original
     */
    @Override
    public boolean transferData(List<User> users) {
        Connection conn = null;

        try {
            // Obtener conexión
            conn = DatabaseConfig.getConnection();

            // IMPORTANTE: Desactivar auto-commit para control manual
            conn.setAutoCommit(false);

            String sql = "INSERT INTO users (name, email, department, role, active, created_at, updated_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Insertar cada usuario en la transacción
                for (User user : users) {
                    pstmt.setString(1, user.getName());
                    pstmt.setString(2, user.getEmail());
                    pstmt.setString(3, user.getDepartment());
                    pstmt.setString(4, user.getRole());
                    pstmt.setBoolean(5, user.getActive() != null ? user.getActive() : true);
                    pstmt.setTimestamp(6, Timestamp.valueOf(
                            user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()));
                    pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

                    pstmt.executeUpdate();
                }
            }

            // Si llegamos aquí, todas las inserciones fueron exitosas
            // COMMIT: hacer permanentes los cambios
            conn.commit();

            return true;

        } catch (SQLException e) {
            // Si hubo algún error, deshacer TODOS los cambios
            if (conn != null) {
                try {
                    // ROLLBACK: deshacer todos los cambios de la transacción
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new RuntimeException("Error crítico en rollback: " + rollbackEx.getMessage(), rollbackEx);
                }
            }

            throw new RuntimeException("Error en transacción, se hizo rollback: " + e.getMessage(), e);

        } finally {
            // IMPORTANTE: Restaurar auto-commit y cerrar conexión
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar estado original
                    conn.close();
                } catch (SQLException e) {
                    // Registrar error pero no lanzar excepción en finally
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public int batchInsertUsers(List<User> users) {
        throw new UnsupportedOperationException("TODO: Método batchInsertUsers() para implementar por estudiantes");
    }

    // ========== CE2.e: Metadata ==========

    @Override
    public String getDatabaseInfo() {
        throw new UnsupportedOperationException("TODO: Método getDatabaseInfo() para implementar por estudiantes");
    }

    @Override
    public List<Map<String, Object>> getTableColumns(String tableName) {
        throw new UnsupportedOperationException("TODO: Método getTableColumns() para implementar por estudiantes");
    }

    // ========== CE2.f: Stored Procedures ==========

    @Override
    public int executeCountByDepartment(String department) {
        throw new UnsupportedOperationException("TODO: Método executeCountByDepartment() para implementar por estudiantes");
    }

    // ========== HELPER METHODS ==========

    /**
     * Método auxiliar para mapear ResultSet a objeto User
     *
     * Este método se usa en múltiples lugares para evitar duplicación de código.
     * Extrae todas las columnas del ResultSet y crea un objeto User.
     *
     * @param rs ResultSet posicionado en una fila válida
     * @return User object con datos de la fila
     * @throws SQLException si hay error al leer el ResultSet
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();

        // Mapear tipos primitivos y objetos
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setDepartment(rs.getString("department"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("active"));

        // Mapear Timestamps a LocalDateTime
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        if (createdAtTimestamp != null) {
            user.setCreatedAt(createdAtTimestamp.toLocalDateTime());
        }

        Timestamp updatedAtTimestamp = rs.getTimestamp("updated_at");
        if (updatedAtTimestamp != null) {
            user.setUpdatedAt(updatedAtTimestamp.toLocalDateTime());
        }

        return user;
    }
}
