# Spring WebSocket and STOMP with Spring Boot

**Beginner-Friendly Complete Tutorial**

---

## 1. Introduction to WebSockets

### 1.1 What are WebSockets

WebSockets provide a persistent, bidirectional communication channel between the client and server over a single TCP connection.
Unlike HTTP which is request–response based, WebSockets allow both sides to send data independently at any time.

### 1.2 Why WebSockets

Use WebSockets when you need:

1. Real-time updates
2. Low latency communication
3. Features such as chat, notifications, analytics dashboards
4. Continuous streaming of data

### 1.3 Limitations of pure WebSockets

Raw WebSockets only send text or binary messages.
They do not include concepts like:

* Topics
* Broadcast messaging
* Message acknowledgment
* Message types
* Routing and subscription handling

This is where **STOMP** helps.

---

## 2. What is STOMP

### 2.1 Definition

STOMP stands for **Simple Text Oriented Messaging Protocol**.
It is a messaging protocol used over WebSocket to structure messages like a message queue.

### 2.2 Why STOMP over WebSocket

STOMP provides:

* Topic-based messaging
* Destination-based routing (/topic, /queue)
* Support for brokers (in-memory or external)
* Built-in commands (SEND, SUBSCRIBE, CONNECT, DISCONNECT)

### 2.3 STOMP Destinations

* **/app/** – messages sent from client to server endpoint
* **/topic/** – broadcast messages to all subscribers
* **/queue/** – one-to-one communication
* **/user/** – user-specific messages

---

# 3. Spring Boot WebSocket + STOMP Architecture

1. Client connects to WebSocket endpoint
2. Client subscribes to a destination (/topic/messages)
3. Client sends message to server via /app/sendMessage
4. Server receives message in a @MessageMapping method
5. Server broadcasts message back to subscribers via @SendTo
6. All connected users receive the message in real-time

---

# 4. Setup: Dependencies and Configuration

## 4.1 Add Required Dependencies

Use Gradle or Maven.

### Maven

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
</dependencies>
```

---

# 5. Enable WebSocket Message Broker

Create a configuration class for STOMP WebSocket.

## 5.1 WebSocket Configuration

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
```

### Explanation

1. **@EnableWebSocketMessageBroker**
   Enables STOMP WebSocket messaging in Spring.

2. **registerStompEndpoints**
   Defines endpoint for client connection.
   `/ws` is the WebSocket handshake endpoint.

3. **withSockJS()**
   Provides fallback for browsers without WebSocket support.

4. **enableSimpleBroker("/topic")**
   Enables in-memory message broker for broadcasting.

5. **setApplicationDestinationPrefixes("/app")**
   Client SEND requests must start with `/app`.

---

# 6. Creating Message Models

### Message Request Model

```java
public class ChatMessage {
    private String sender;
    private String content;

    // getters and setters
}
```

### Explanation

This object represents the message structure received from and sent to the client.

---

# 7. Creating a Message Controller

## 7.1 Server-Side STOMP Controller

```java
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public ChatMessage broadcast(ChatMessage message) {
        return message;
    }
}
```

### Explanation

1. **@MessageMapping("/sendMessage")**
   Listens to messages sent from client to `/app/sendMessage`.

2. **@SendTo("/topic/messages")**
   Broadcasts the returned object to all subscribers.

3. **broadcast()**
   Business logic can be added here before sending output.

---

# 8. Frontend WebSocket Client Example (JavaScript)

Create a simple HTML/JS client using SockJS and Stomp.

```html
<script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs/lib/stomp.min.js"></script>

<script>
    var socket = new SockJS('/ws');
    var stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/messages', function (message) {
            console.log("Received: ", JSON.parse(message.body));
        });
    });

    function send() {
        var message = {
            sender: "User1",
            content: "Hello World"
        };
        stompClient.send("/app/sendMessage", {}, JSON.stringify(message));
    }
</script>

<button onclick="send()">Send</button>
```

### Explanation

1. Connect to WebSocket `/ws`
2. Subscribe to `/topic/messages`
3. Send message via `/app/sendMessage`
4. Receive broadcast messages in real-time

---

# 9. User-Specific Messaging (Optional Advanced Concept)

To send messages to individual users:

### Controller

```java
@Autowired
private SimpMessagingTemplate template;

@MessageMapping("/private")
public void privateMessage(ChatMessage message) {
    template.convertAndSendToUser(message.getSender(), "/queue/reply", message);
}
```

### Explanation

* Each user gets messages at `/user/{username}/queue/reply`.

---

# 10. Testing WebSocket Endpoints

Testing strategies:

1. Browser dev tools:

    * Network -> WS
    * Inspect frames
2. Use Postman WebSocket support
3. Write automated integration tests using
   `WebSocketStompClient`, `SockJsClient`, `WebSocketClient`.

---

# 11. Common Errors and Solutions

### 1. 404 on /ws

Ensure correct endpoint in configuration.

### 2. CORS issues

Use `setAllowedOriginPatterns("*")`.

### 3. No messages being broadcast

Check message destination prefixes.

### 4. JavaScript failing to connect

Ensure correct SockJS and Stomp versions.

---

# 12. Summary of Learning

You now understand:

1. What WebSockets are and why they are used
2. Why STOMP is needed
3. WebSocket + STOMP architecture
4. Spring Boot WebSocket configuration
5. Creating message models
6. Writing STOMP controllers
7. Building frontend WebSocket clients
8. Broadcasting and private messaging
9. Common troubleshooting

---
