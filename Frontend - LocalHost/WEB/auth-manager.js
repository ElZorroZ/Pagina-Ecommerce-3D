// auth-manager.js - Gestor centralizado de autenticación
const API_BASE_URL = "http://localhost:8080";

class AuthManager {
  constructor() {
    this.refreshPromise = null;
    this.isRefreshing = false;
    this.pendingRequests = []; // Cola de peticiones esperando refresh
  }

  getAccessToken() {
    return localStorage.getItem("accessToken");
  }

  getRefreshToken() {
    return localStorage.getItem("refreshToken");
  }

  getUserId() {
    return localStorage.getItem("usuarioId");
  }

  isAuthenticated() {
    return !!(this.getAccessToken() && this.getRefreshToken() && this.getUserId());
  }
  
  getUserInfo() {
    const token = this.getAccessToken(); // ✅ obtener token
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      return {
        gmail: payload.sub, // depende de cómo armes el JWT
        roles: payload.roles || []
      };
    } catch (e) {
      console.error("Error al decodificar token:", e);
      return null;
    }
  }


  saveAuthData(accessToken, refreshToken, usuarioId) {
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);
    localStorage.setItem("usuarioId", usuarioId.toString());
    console.log("✅ Datos guardados:", { usuarioId });
  }

  clearAuthData() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("usuarioId");
    this.refreshPromise = null;
    this.isRefreshing = false;
    this.pendingRequests = [];
    console.log("🗑️ Datos limpiados");
  }

  async refreshAccessToken() {
    // Si ya hay un refresh en progreso, retornar esa promesa
    if (this.refreshPromise) {
      console.log("⏳ Refresh en progreso, esperando...");
      return this.refreshPromise;
    }

    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      this.clearAuthData();
      return null;
    }

    // Crear la promesa y guardarla inmediatamente
    this.refreshPromise = this._performRefresh(refreshToken);
    
    try {
      const result = await this.refreshPromise;
      return result;
    } finally {
      // Limpiar la promesa al finalizar
      this.refreshPromise = null;
    }
  }

  async _performRefresh(refreshToken) {
    try {
      console.log("🔄 Enviando refresh...");
      
      const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
      });

      if (response.ok) {
        const data = await response.json();
        console.log("✅ Refresh exitoso");
        this.saveAuthData(data.accessToken, data.refreshToken, data.usuarioId);
        return data.accessToken;
      } else {
        console.log("❌ Refresh falló:", response.status);
        this.clearAuthData();
        return null;
      }
    } catch (err) {
      console.error("❌ Error en refresh:", err);
      this.clearAuthData();
      return null;
    }
  }

  async fetchWithAuth(url, options = {}) {
    if (!this.isAuthenticated()) {
      console.log("❌ No autenticado");
      this.redirectToLogin();
      throw new Error('Usuario no autenticado');
    }

    // Crear opciones limpias
    const requestOptions = {
      ...options,
      headers: {
        ...options.headers,
        'Authorization': `Bearer ${this.getAccessToken()}`
      }
    };

    // Manejar Content-Type
    if (!(requestOptions.body instanceof FormData)) {
      if (!requestOptions.headers['Content-Type']) {
        requestOptions.headers['Content-Type'] = 'application/json';
      }
    } else {
      delete requestOptions.headers['Content-Type'];
    }

    let response = await fetch(url, requestOptions);

    // Si 401, intentar refresh UNA SOLA VEZ
    if (response.status === 401) {
      console.log("🔒 401 detectado, refrescando...");
      
      const newToken = await this.refreshAccessToken();
      if (newToken) {
        console.log("✅ Reintentando con nuevo token");
        // Reintentar con el nuevo token
        requestOptions.headers['Authorization'] = `Bearer ${newToken}`;
        response = await fetch(url, requestOptions);
      } else {
        console.log("❌ Refresh falló, redirigiendo");
        this.redirectToLogin();
        throw new Error('Sesión expirada');
      }
    }

    return response;
  }

  async login(gmail, password) {
    try {
      console.log("📡 Login para:", gmail);
      const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ gmail, password })
      });

      if (response.ok) {
        const data = await response.json();
        console.log("✅ Login exitoso");
        this.saveAuthData(data.accessToken, data.refreshToken, data.usuarioId);
        return { success: true, data };
      } else {
        const errorMsg = await response.text();
        return { success: false, error: errorMsg };
      }
    } catch (error) {
      console.error("❌ Error login:", error);
      return { success: false, error: "Error de conexión" };
    }
  }

  logout() {
    console.log("👋 Logout");
    this.clearAuthData();
    this.redirectToLogin();
  }

  redirectToLogin() {
    if (window.location.pathname !== '/login.html') {
      console.log("🔄 Redirigiendo...");
      window.location.href = '/usuario/login/login.html';
    }
  }

  debugAuthStatus() {
    console.log('🔍 Estado:', {
      accessToken: !!this.getAccessToken(),
      refreshToken: !!this.getRefreshToken(),
      usuarioId: this.getUserId(),
      isAuthenticated: this.isAuthenticated(),
      isRefreshing: !!this.refreshPromise
    });
  }
}

const authManager = new AuthManager();
window.authManager = authManager;
window.fetchWithAuth = (url, options) => authManager.fetchWithAuth(url, options);
window.isAuthenticated = () => authManager.isAuthenticated();
window.logout = () => authManager.logout();