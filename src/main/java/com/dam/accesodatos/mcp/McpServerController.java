package com.dam.accesodatos.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dam.accesodatos.ra2.DatabaseUserService;
import com.dam.accesodatos.model.User;
import com.dam.accesodatos.model.UserCreateDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST que expone las herramientas MCP via HTTP para operaciones
 * JDBC.
 *
 * Proporciona endpoints para que los LLMs puedan:
 * - Listar herramientas JDBC disponibles
 * - Ejecutar operaciones JDBC específicas
 * - Obtener información sobre el servidor MCP
 */
@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*")
public class McpServerController {

    private static final Logger logger = LoggerFactory.getLogger(McpServerController.class);

    @Autowired
    private DatabaseUserService databaseUserService;

    @Autowired
    private McpToolRegistry toolRegistry;

    /**
     * Endpoint de health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> getHealth() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "MCP Server RA2 JDBC");

        return ResponseEntity.ok(health);
    }

    /**
     * Endpoint para listar todas las herramientas MCP disponibles
     */
    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> getTools() {
        logger.debug("Solicitadas herramientas MCP JDBC disponibles");

        List<McpToolRegistry.McpToolInfo> tools = toolRegistry.getRegisteredTools();

        List<Map<String, String>> toolsList = tools.stream()
                .map(tool -> {
                    Map<String, String> toolMap = new HashMap<>();
                    toolMap.put("name", tool.getName());
                    toolMap.put("description", tool.getDescription());
                    return toolMap;
                })
                .collect(java.util.stream.Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("tools", toolsList);
        response.put("count", toolsList.size());
        response.put("server", "MCP Server - RA2 JDBC DAM");
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

    // ========== JDBC OPERATION ENDPOINTS ==========

    /**
     * Prueba la conexión a la base de datos
     */
    @PostMapping("/test_connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        logger.debug("Probando conexión JDBC");

        try {
            String result = databaseUserService.testConnection();

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "test_connection");
            response.put("result", result);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error probando conexión", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error probando conexión: " + e.getMessage());
            error.put("tool", "test_connection");
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Crea un nuevo usuario
     */
    @PostMapping("/create_user")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> request) {
        logger.debug("Creando usuario");

        try {
            String name = request.get("name");
            String email = request.get("email");
            String department = request.get("department");
            String role = request.get("role");

            UserCreateDto dto = new UserCreateDto(name, email, department, role);
            User user = databaseUserService.createUser(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "create_user");
            response.put("result", user);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creando usuario", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error creando usuario: " + e.getMessage());
            error.put("tool", "create_user");
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Busca un usuario por ID
     */
    @PostMapping("/find_user_by_id")
    public ResponseEntity<Map<String, Object>> findUserById(@RequestBody Map<String, Object> request) {
        logger.debug("Buscando usuario por ID");

        try {
            Long userId = ((Number) request.get("userId")).longValue();
            User user = databaseUserService.findUserById(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "find_user_by_id");
            response.put("result", user);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error buscando usuario", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error buscando usuario: " + e.getMessage());
            error.put("tool", "find_user_by_id");
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Actualiza un usuario
     */
    @PostMapping("/update_user")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody Map<String, Object> request) {
        logger.debug("Actualizando usuario");

        try {
            Long userId = ((Number) request.get("userId")).longValue();
            String name = (String) request.get("name");
            String email = (String) request.get("email");
            String department = (String) request.get("department");
            String role = (String) request.get("role");

            com.dam.accesodatos.model.UserUpdateDto dto = new com.dam.accesodatos.model.UserUpdateDto(name, email,
                    department, role, true);
            User user = databaseUserService.updateUser(userId, dto);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "update_user");
            response.put("result", user);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error actualizando usuario", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error actualizando usuario: " + e.getMessage());
            error.put("tool", "update_user");
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Elimina un usuario
     */
    @PostMapping("/delete_user")
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestBody Map<String, Object> request) {
        logger.debug("Eliminando usuario");

        try {
            Long userId = ((Number) request.get("userId")).longValue();
            boolean result = databaseUserService.deleteUser(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "delete_user");
            response.put("result", result);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error eliminando usuario", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error eliminando usuario: " + e.getMessage());
            error.put("tool", "delete_user");
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Obtiene todos los usuarios
     */
    @PostMapping("/find_all_users")
    public ResponseEntity<Map<String, Object>> findAllUsers() {
        logger.debug("Obteniendo todos los usuarios");

        try {
            List<User> users = databaseUserService.findAll();

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "find_all_users");
            response.put("result", users);
            response.put("count", users.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error obteniendo usuarios", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo usuarios: " + e.getMessage());
            error.put("tool", "find_all_users");
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Busca usuarios por departamento
     */
    @PostMapping("/find_users_by_department")
    public ResponseEntity<Map<String, Object>> findUsersByDepartment(@RequestBody Map<String, String> request) {
        logger.debug("Buscando usuarios por departamento");

        try {
            String department = request.get("department");
            List<User> users = databaseUserService.findUsersByDepartment(department);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "find_users_by_department");
            response.put("result", users);
            response.put("count", users.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error buscando usuarios por departamento", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error buscando usuarios: " + e.getMessage());
            error.put("tool", "find_users_by_department");
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Busca usuarios con filtros múltiples
     */
    @PostMapping("/search_users")
    public ResponseEntity<Map<String, Object>> searchUsers(@RequestBody Map<String, Object> request) {
        logger.debug("Buscando usuarios con filtros");

        try {
            // Extraer parámetros del request
            String department = (String) request.get("department");
            String role = (String) request.get("role");
            Boolean active = request.get("active") != null ? Boolean.valueOf(request.get("active").toString()) : null;
            Integer limit = request.get("limit") != null ? ((Number) request.get("limit")).intValue() : null;
            Integer offset = request.get("offset") != null ? ((Number) request.get("offset")).intValue() : null;

            // Crear DTO con los filtros
            com.dam.accesodatos.model.UserQueryDto query = new com.dam.accesodatos.model.UserQueryDto(department, role,
                    active, limit, offset);

            // Ejecutar búsqueda
            List<User> users = databaseUserService.searchUsers(query);

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "search_users");
            response.put("result", users);
            response.put("count", users.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error buscando usuarios con filtros", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error buscando usuarios: " + e.getMessage());
            error.put("tool", "search_users");
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Obtiene información de conexión JDBC
     */
    @PostMapping("/get_connection_info")
    public ResponseEntity<Map<String, Object>> getConnectionInfo() {
        logger.debug("Obteniendo información de conexión");

        try {
            Map<String, String> info = databaseUserService.getConnectionInfo();

            Map<String, Object> response = new HashMap<>();
            response.put("tool", "get_connection_info");
            response.put("result", info);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error obteniendo info de conexión", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo info: " + e.getMessage());
            error.put("tool", "get_connection_info");
            error.put("status", "error");

            return ResponseEntity.status(500).body(error);
        }
    }
}
