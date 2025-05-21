/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * Go implementation of HTTP server functionality
 */
package main

import (
	"C"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"sync"
	"time"

	"github.com/google/uuid"
	"github.com/gorilla/mux"
	"github.com/gorilla/websocket"
)

// Server management
type HttpServer struct {
	server      *http.Server
	router      *mux.Router
	isRunning   bool
	mu          sync.Mutex
	wsEndpoints map[int]*WebSocketEndpoint
	handlers    map[string]func(int)
}

type WebSocketEndpoint struct {
	path       string
	clients    map[string]*websocket.Conn
	clientsMu  sync.Mutex
}

type RequestContext struct {
	id          int
	w           http.ResponseWriter
	r           *http.Request
	headersSent bool
}

var (
	servers       = make(map[int]*HttpServer)
	requests      = make(map[int]*RequestContext)
	serverCounter = 0
	requestCounter = 0
	endpointCounter = 0
	globalMu      sync.Mutex
)

//export createServer
func createServer(port int) int {
	globalMu.Lock()
	defer globalMu.Unlock()

	serverID := serverCounter
	serverCounter++

	router := mux.NewRouter()
	srv := &http.Server{
		Addr:    fmt.Sprintf(":%d", port),
		Handler: router,
	}

	server := &HttpServer{
		server:      srv,
		router:      router,
		isRunning:   false,
		wsEndpoints: make(map[int]*WebSocketEndpoint),
		handlers:    make(map[string]func(int)),
	}

	servers[serverID] = server

	// Start the server in a goroutine
	go func() {
		server.mu.Lock()
		server.isRunning = true
		server.mu.Unlock()

		err := srv.ListenAndServe()
		if err != nil && err != http.ErrServerClosed {
			log.Printf("HTTP server error: %v", err)
		}

		server.mu.Lock()
		server.isRunning = false
		server.mu.Unlock()
	}()

	// Wait a bit to ensure the server starts
	time.Sleep(100 * time.Millisecond)
	return serverID
}

//export stopServer
func stopServer(serverHandle int) {
	globalMu.Lock()
	defer globalMu.Unlock()

	server, exists := servers[serverHandle]
	if !exists {
		return
	}

	server.mu.Lock()
	defer server.mu.Unlock()

	if server.isRunning {
		ctx, cancel := C.createContext(2 * time.Second)
		defer cancel()
		
		server.server.Shutdown(ctx)
		server.isRunning = false
	}

	delete(servers, serverHandle)
}

//export isRunning
func isRunning(serverHandle int) bool {
	globalMu.Lock()
	defer globalMu.Unlock()

	server, exists := servers[serverHandle]
	if !exists {
		return false
	}

	server.mu.Lock()
	defer server.mu.Unlock()

	return server.isRunning
}

// Route handling
//export addRoute
func addRoute(serverHandle int, method, path, handlerName *C.char) {
	globalMu.Lock()
	defer globalMu.Unlock()

	server, exists := servers[serverHandle]
	if !exists {
		return
	}

	methodStr := C.GoString(method)
	pathStr := C.GoString(path)
	handlerNameStr := C.GoString(handlerName)

	handler := func(w http.ResponseWriter, r *http.Request) {
		globalMu.Lock()
		reqID := requestCounter
		requestCounter++
		
		// Store request context
		requests[reqID] = &RequestContext{
			id:          reqID,
			w:           w,
			r:           r,
			headersSent: false,
		}
		globalMu.Unlock()

		// Call the registered handler by name
		if handlerFunc, exists := server.handlers[handlerNameStr]; exists {
			handlerFunc(reqID)
		}

		// Clean up request context after a delay to ensure all processing is done
		go func() {
			time.Sleep(30 * time.Second)
			globalMu.Lock()
			delete(requests, reqID)
			globalMu.Unlock()
		}()
	}

	server.router.HandleFunc(pathStr, handler).Methods(methodStr)
}

//export removeRoute
func removeRoute(serverHandle int, method, path *C.char) {
	// Note: mux doesn't directly support route removal
	// In a real implementation, you might need to recreate the router
	// For simplicity, this is a placeholder
}

// Response utilities
//export setResponseHeader
func setResponseHeader(requestId int, name, value *C.char) {
	globalMu.Lock()
	defer globalMu.Unlock()

	request, exists := requests[requestId]
	if !exists {
		return
	}

	if request.headersSent {
		return
	}

	nameStr := C.GoString(name)
	valueStr := C.GoString(value)
	request.w.Header().Set(nameStr, valueStr)
}

//export sendResponse
func sendResponse(requestId int, statusCode int, contentType, body *C.char) {
	globalMu.Lock()
	request, exists := requests[requestId]
	if !exists {
		globalMu.Unlock()
		return
	}

	if request.headersSent {
		globalMu.Unlock()
		return
	}

	contentTypeStr := C.GoString(contentType)
	bodyStr := C.GoString(body)
	
	request.headersSent = true
	globalMu.Unlock()

	request.w.Header().Set("Content-Type", contentTypeStr)
	request.w.WriteHeader(statusCode)
	request.w.Write([]byte(bodyStr))
}

//export sendJsonResponse
func sendJsonResponse(requestId int, statusCode int, jsonBody *C.char) {
	globalMu.Lock()
	request, exists := requests[requestId]
	if !exists {
		globalMu.Unlock()
		return
	}

	if request.headersSent {
		globalMu.Unlock()
		return
	}

	bodyStr := C.GoString(jsonBody)
	
	request.headersSent = true
	globalMu.Unlock()

	request.w.Header().Set("Content-Type", "application/json")
	request.w.WriteHeader(statusCode)
	request.w.Write([]byte(bodyStr))
}

//export sendFileResponse
func sendFileResponse(requestId int, filePath *C.char) {
	globalMu.Lock()
	request, exists := requests[requestId]
	if !exists {
		globalMu.Unlock()
		return
	}

	if request.headersSent {
		globalMu.Unlock()
		return
	}

	filePathStr := C.GoString(filePath)
	
	request.headersSent = true
	globalMu.Unlock()

	// Check if file exists
	file, err := os.Open(filePathStr)
	if err != nil {
		request.w.WriteHeader(http.StatusNotFound)
		request.w.Write([]byte("File not found"))
		return
	}
	defer file.Close()

	// Set content type based on file extension
	ext := filepath.Ext(filePathStr)
	contentType := getContentTypeFromExtension(ext)
	request.w.Header().Set("Content-Type", contentType)

	// Send file
	request.w.WriteHeader(http.StatusOK)
	io.Copy(request.w, file)
}

// Get content type based on file extension
func getContentTypeFromExtension(ext string) string {
	switch ext {
	case ".html", ".htm":
		return "text/html"
	case ".css":
		return "text/css"
	case ".js":
		return "application/javascript"
	case ".json":
		return "application/json"
	case ".png":
		return "image/png"
	case ".jpg", ".jpeg":
		return "image/jpeg"
	case ".gif":
		return "image/gif"
	case ".svg":
		return "image/svg+xml"
	case ".pdf":
		return "application/pdf"
	default:
		return "application/octet-stream"
	}
}

// Request information
//export getRequestPath
func getRequestPath(requestId int) *C.char {
	globalMu.Lock()
	defer globalMu.Unlock()

	request, exists := requests[requestId]
	if !exists {
		return C.CString("")
	}

	return C.CString(request.r.URL.Path)
}

//export getRequestMethod
func getRequestMethod(requestId int) *C.char {
	globalMu.Lock()
	defer globalMu.Unlock()

	request, exists := requests[requestId]
	if !exists {
		return C.CString("")
	}

	return C.CString(request.r.Method)
}

//export getRequestHeader
func getRequestHeader(requestId int, headerName *C.char) *C.char {
	globalMu.Lock()
	defer globalMu.Unlock()

	request, exists := requests[requestId]
	if !exists {
		return C.CString("")
	}

	headerNameStr := C.GoString(headerName)
	return C.CString(request.r.Header.Get(headerNameStr))
}

//export getRequestBody
func getRequestBody(requestId int) *C.char {
	globalMu.Lock()
	request, exists := requests[requestId]
	globalMu.Unlock()
	
	if !exists {
		return C.CString("")
	}

	// Read body
	bodyBytes, err := io.ReadAll(request.r.Body)
	if err != nil {
		return C.CString("")
	}

	return C.CString(string(bodyBytes))
}

//export getQueryParam
func getQueryParam(requestId int, paramName *C.char) *C.char {
	globalMu.Lock()
	defer globalMu.Unlock()

	request, exists := requests[requestId]
	if !exists {
		return C.CString("")
	}

	paramNameStr := C.GoString(paramName)
	return C.CString(request.r.URL.Query().Get(paramNameStr))
}

// Middleware
//export useMiddleware
func useMiddleware(serverHandle int, middlewareName *C.char) {
	// This would normally register middleware with the server
	// For simplicity, this is a placeholder
}

// Utility functions
//export urlEncode
func urlEncode(input *C.char) *C.char {
	// Implement URL encoding
	// For simplicity, this is a placeholder
	return input
}

//export urlDecode
func urlDecode(input *C.char) *C.char {
	// Implement URL decoding
	// For simplicity, this is a placeholder
	return input
}

//export generateUuid
func generateUuid() *C.char {
	return C.CString(uuid.New().String())
}

// WebSocket support
var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin: func(r *http.Request) bool {
		return true // Allow all origins for simplicity
	},
}

//export createWebSocketEndpoint
func createWebSocketEndpoint(serverHandle int, path *C.char) int {
	globalMu.Lock()
	defer globalMu.Unlock()

	server, exists := servers[serverHandle]
	if !exists {
		return -1
	}

	pathStr := C.GoString(path)
	
	endpointID := endpointCounter
	endpointCounter++
	
	wsEndpoint := &WebSocketEndpoint{
		path:    pathStr,
		clients: make(map[string]*websocket.Conn),
	}
	
	server.wsEndpoints[endpointID] = wsEndpoint
	
	// Handle WebSocket connections
	server.router.HandleFunc(pathStr, func(w http.ResponseWriter, r *http.Request) {
		conn, err := upgrader.Upgrade(w, r, nil)
		if err != nil {
			log.Printf("WebSocket upgrade error: %v", err)
			return
		}
		
		// Generate client ID
		clientID := uuid.New().String()
		
		// Store connection
		wsEndpoint.clientsMu.Lock()
		wsEndpoint.clients[clientID] = conn
		wsEndpoint.clientsMu.Unlock()
		
		// Handle disconnect
		defer func() {
			conn.Close()
			wsEndpoint.clientsMu.Lock()
			delete(wsEndpoint.clients, clientID)
			wsEndpoint.clientsMu.Unlock()
		}()
		
		// Message handling loop
		for {
			messageType, message, err := conn.ReadMessage()
			if err != nil {
				break
			}
			
			if messageType == websocket.TextMessage {
				// Handle message (callbacks would be implemented here)
				// For now, we'll just echo it back
				conn.WriteMessage(websocket.TextMessage, message)
			}
		}
	})
	
	return endpointID
}

//export sendWebSocketMessage
func sendWebSocketMessage(endpointHandle int, clientId, message *C.char) {
	globalMu.Lock()
	defer globalMu.Unlock()
	
	clientIdStr := C.GoString(clientId)
	messageStr := C.GoString(message)
	
	// Find endpoint
	for _, server := range servers {
		if endpoint, exists := server.wsEndpoints[endpointHandle]; exists {
			endpoint.clientsMu.Lock()
			if conn, exists := endpoint.clients[clientIdStr]; exists {
				conn.WriteMessage(websocket.TextMessage, []byte(messageStr))
			}
			endpoint.clientsMu.Unlock()
			return
		}
	}
}

//export broadcastWebSocketMessage
func broadcastWebSocketMessage(endpointHandle int, message *C.char) {
	globalMu.Lock()
	defer globalMu.Unlock()
	
	messageStr := C.GoString(message)
	
	// Find endpoint
	for _, server := range servers {
		if endpoint, exists := server.wsEndpoints[endpointHandle]; exists {
			endpoint.clientsMu.Lock()
			for _, conn := range endpoint.clients {
				conn.WriteMessage(websocket.TextMessage, []byte(messageStr))
			}
			endpoint.clientsMu.Unlock()
			return
		}
	}
}

//export closeWebSocketConnection
func closeWebSocketConnection(endpointHandle int, clientId *C.char) {
	globalMu.Lock()
	defer globalMu.Unlock()
	
	clientIdStr := C.GoString(clientId)
	
	// Find endpoint
	for _, server := range servers {
		if endpoint, exists := server.wsEndpoints[endpointHandle]; exists {
			endpoint.clientsMu.Lock()
			if conn, exists := endpoint.clients[clientIdStr]; exists {
				conn.Close()
				delete(endpoint.clients, clientIdStr)
			}
			endpoint.clientsMu.Unlock()
			return
		}
	}
}

func main() {}