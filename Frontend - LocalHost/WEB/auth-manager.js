// auth-manager.js - Gestor optimizado para tokens de 15 minutos + OAuth2
const API_BASE_URL = "http://localhost:8080";

class AuthManager {
  constructor() {
    this.refreshPromise = null;
    this.isRefreshing = false;
    this.pendingRequests = [];
    this.lastRefreshTime = 0;
    
    // ⏰ Para tokens de 15 min, ser más agresivo en la renovación
    this.REFRESH_BUFFER = 2 * 60 * 1000; // 2 minutos antes de expirar
    this.MIN_REFRESH_INTERVAL = 30 * 1000; // Mínimo 30 segundos entre refresh
    
    // 🔍 Procesar tokens de OAuth2 al inicializar
    this.processOAuth2Tokens();
    
    // 🚀 Iniciar renovación proactiva
    this.startProactiveRefresh();
  }

  // 🔍 Procesar tokens de OAuth2 desde URL
  processOAuth2Tokens() {
    const urlParams = new URLSearchParams(window.location.search);
    const accessToken = urlParams.get('accessToken');
    const refreshToken = urlParams.get('refreshToken');
    const usuarioId = urlParams.get('usuarioId');

    if (accessToken && refreshToken && usuarioId) {
      console.log('🔍 Tokens OAuth2 detectados en URL');
      
      // Guardar tokens
      this.saveAuthData(accessToken, refreshToken, usuarioId);
      
      // Limpiar URL sin recargar la página
      const newUrl = window.location.protocol + "//" + 
                    window.location.host + 
                    window.location.pathname;
      window.history.replaceState({}, document.title, newUrl);
      
      console.log('✅ Tokens OAuth2 procesados y URL limpiada');
    }
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
  getUserRole() {
    const userInfo = this.getUserInfo();
    if (!userInfo || !userInfo.roles || userInfo.roles.length === 0) return null;
    return userInfo.roles[0]; // Si puede tener varios roles, elegís el primero
}

  getUserInfo() {
    const token = this.getAccessToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      return {
        gmail: payload.sub,
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
    
    // 🚀 Reiniciar el ciclo de renovación proactiva
    this.startProactiveRefresh();
    
    console.log("✅ Datos guardados:", { usuarioId });
  }

  clearAuthData() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("usuarioId");
    this.refreshPromise = null;
    this.isRefreshing = false;
    this.pendingRequests = [];
    this.lastRefreshTime = 0;
    
    // 🛑 Detener renovación proactiva
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
      this.refreshTimer = null;
    }
    
    console.log("🗑️ Datos limpiados");
  }

  // 🚀 Sistema de renovación proactiva para tokens de 15 minutos
  startProactiveRefresh() {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
    }

    const token = this.getAccessToken();
    if (!token) return;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirationTime = payload.exp * 1000;
      const currentTime = Date.now();
      const timeUntilExpiration = expirationTime - currentTime;
      const timeUntilRefresh = Math.max(timeUntilExpiration - this.REFRESH_BUFFER, 30000); // Mínimo 30s

      if (timeUntilRefresh > 0 && timeUntilRefresh < 20 * 60 * 1000) { // Máximo 20 min
        console.log(`⏰ Renovación programada en ${Math.round(timeUntilRefresh / 1000)} segundos`);
        
        this.refreshTimer = setTimeout(() => {
          this.refreshAccessTokenProactively();
        }, timeUntilRefresh);
      }
    } catch (error) {
      console.error('Error programando renovación:', error);
    }
  }

  // 🔄 Renovación proactiva (antes de que expire)
  async refreshAccessTokenProactively() {
    console.log('🔄 Iniciando renovación proactiva...');
    const newToken = await this.refreshAccessToken();
    if (newToken) {
      console.log('✅ Renovación proactiva exitosa');
      // Programar la siguiente renovación
      this.startProactiveRefresh();
    }
  }

  // ⚡ Verificar si necesita refresh urgente (para requests inmediatos)
  needsImmediateRefresh() {
    const token = this.getAccessToken();
    if (!token) return true;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirationTime = payload.exp * 1000;
      const currentTime = Date.now();
      const timeUntilExpiration = expirationTime - currentTime;
      
      // Necesita refresh si expira en menos de 1 minuto
      return timeUntilExpiration < 60 * 1000;
    } catch (error) {
      return true;
    }
  }

  async refreshAccessToken() {
    // ⚡ Evitar refresh muy frecuentes
    const now = Date.now();
    if (now - this.lastRefreshTime < this.MIN_REFRESH_INTERVAL) {
      console.log("⏳ Refresh muy reciente, usando token actual");
      return this.getAccessToken();
    }

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
      this.lastRefreshTime = now;
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

    // 🚀 Refresh proactivo si el token expira muy pronto
    if (this.needsImmediateRefresh()) {
      console.log("⚡ Token expira pronto, refrescando antes del request");
      await this.refreshAccessToken();
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
    const token = this.getAccessToken();
    let tokenInfo = null;
    
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const expirationTime = payload.exp * 1000;
        const currentTime = Date.now();
        const timeUntilExpiration = expirationTime - currentTime;
        
        tokenInfo = {
          expiresIn: Math.round(timeUntilExpiration / 1000),
          needsRefresh: this.needsImmediateRefresh(),
          payload: payload
        };
      } catch (e) {
        tokenInfo = { error: 'Token inválido' };
      }
    }

    console.log('🔍 Estado Auth:', {
      accessToken: !!this.getAccessToken(),
      refreshToken: !!this.getRefreshToken(),
      usuarioId: this.getUserId(),
      isAuthenticated: this.isAuthenticated(),
      isRefreshing: !!this.refreshPromise,
      tokenInfo
    });
    
    return {
      accessToken: !!this.getAccessToken(),
      refreshToken: !!this.getRefreshToken(),
      usuarioId: this.getUserId(),
      isAuthenticated: this.isAuthenticated(),
      tokenInfo
    };
  }
}

const authManager = new AuthManager();
window.authManager = authManager;
window.fetchWithAuth = (url, options) => authManager.fetchWithAuth(url, options);
window.isAuthenticated = () => authManager.isAuthenticated();
window.logout = () => authManager.logout();