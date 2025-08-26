# GUI Overlays de Atributos - RPG Stats Mod

## Descripción
Este sistema añade **tres overlays GUI** que muestran información de atributos del jugador en tiempo real:
1. **Overlay de Puntos** (esquina inferior derecha) - Muestra puntos temporales de atributos
2. **Overlay de Valores** (esquina inferior izquierda) - Muestra valores reales de atributos RPG
3. **Overlay de Atributos Modificados** (esquina superior derecha) - Muestra atributos de Minecraft modificados

## Características

### 🎯 **Funcionalidades Principales**
- **Visualización triple** de puntos, valores RPG y atributos modificados
- **Posicionamiento inteligente** en tres esquinas diferentes
- **Actualización optimizada** solo cuando cambian los valores
- **Estilos diferenciados** para fácil distinción
- **Control independiente** de cada overlay

### 📊 **Información Mostrada**

#### **Overlay de Puntos (Inferior Derecha)**
- Puntos de Fuerza (temporales)
- Puntos de Destreza (temporales)
- Puntos de Vitalidad (temporales)
- Puntos de Constitución (temporales)
- Puntos de Inteligencia (temporales)

#### **Overlay de Valores (Inferior Izquierda)**
- Fuerza (valor real del atributo RPG)
- Destreza (valor real del atributo RPG)
- Vitalidad (valor real del atributo RPG)
- Constitución (valor real del atributo RPG)
- Inteligencia (valor real del atributo RPG)

#### **Overlay de Atributos Modificados (Superior Derecha)**
- Vida Máxima (atributo de Minecraft)
- Daño de Ataque (atributo de Minecraft)
- Armadura (atributo de Minecraft)
- Velocidad de Movimiento (atributo de Minecraft)
- Velocidad de Ataque (atributo de Minecraft)
- Resistencia al Knockback (atributo de Minecraft)
- Dureza de Armadura (atributo de Minecraft)

### 🎨 **Diseño Visual**

#### **Overlay de Puntos (Inferior Derecha)**
- **Posición**: Esquina inferior derecha
- **Fondo**: Negro semi-transparente con borde
- **Título**: "Puntos de Atributos" en dorado
- **Nombres**: Nombres de atributos en blanco
- **Valores**: Valores numéricos en verde

#### **Overlay de Valores (Inferior Izquierda)**
- **Posición**: Esquina inferior izquierda
- **Fondo**: Negro semi-transparente con borde
- **Título**: "Atributos del Jugador" en azul real
- **Nombres**: Nombres de atributos en blanco
- **Valores**: Valores numéricos en dorado

#### **Overlay de Atributos Modificados (Superior Derecha)**
- **Posición**: Esquina superior derecha
- **Fondo**: Negro semi-transparente con borde
- **Título**: "Atributos Modificados" en verde lima
- **Nombres**: Nombres de atributos en blanco
- **Valores**: Valores numéricos en naranja/tomate

### 📱 **Vista Previa**

```
Izquierda                                    Derecha

┌─────────────────────────┐    ┌─────────────────────────┐    ┌─────────────────────────┐
│ Atributos del Jugador:  │    │ Puntos de Atributos:    │    │ Atributos Modificados:  │
│ Fuerza: 15.0            │    │ Fuerza: 5.0             │    │ Vida Máxima: 25.4      │
│ Destreza: 12.3          │    │ Destreza: 3.2           │    │ Daño de Ataque: 2.5     │
│ Vitalidad: 18.7         │    │ Vitalidad: 7.1          │    │ Armadura: 3.2           │
│ Constitución: 14.5      │    │ Constitución: 2.5       │    │ Vel. Movimiento: 0.105  │
│ Inteligencia: 16.8      │    │ Inteligencia: 4.8       │    │ Vel. Ataque: 4.12       │
└─────────────────────────┘    └─────────────────────────┘    │ Resist. Knockback: 0.14 │
                                                               │ Dureza Armadura: 0.19   │
                                                               └─────────────────────────┘
```

## Comandos de Control

### **Comandos para Overlay de Puntos**
- `/rpgstats-client points toggle` - Alternar visibilidad
- `/rpgstats-client points show` - Mostrar overlay
- `/rpgstats-client points hide` - Ocultar overlay

### **Comandos para Overlay de Valores**
- `/rpgstats-client values toggle` - Alternar visibilidad
- `/rpgstats-client values show` - Mostrar overlay
- `/rpgstats-client values hide` - Ocultar overlay
- `/rpgstats-client values refresh` - Forzar actualización

### **Comandos para Overlay de Atributos Modificados**
- `/rpgstats-client modified toggle` - Alternar visibilidad
- `/rpgstats-client modified show` - Mostrar overlay
- `/rpgstats-client modified hide` - Ocultar overlay
- `/rpgstats-client modified refresh` - Forzar actualización

### **Comandos para Todos los Overlays Principales**
- `/rpgstats-client all toggle` - Alternar todos los overlays principales
- `/rpgstats-client all show` - Mostrar todos los overlays principales
- `/rpgstats-client all hide` - Ocultar todos los overlays principales

### **Comandos de Información**
- `/rpgstats-client status` - Ver estado actual de todos los overlays
- `/rpgstats-client help` - Mostrar ayuda

## Comportamiento

### 🔄 **Actualización Inteligente**

#### **Overlay de Puntos**
- Se actualiza cada segundo
- Detecta cambios en puntos temporales automáticamente
- Se sincroniza con datos del servidor

#### **Overlay de Valores**
- Se actualiza **solo cuando cambian los atributos reales**
- Verificación cada segundo con tolerancia de 0.01
- Cache inteligente para evitar renderizado innecesario

#### **Overlay de Atributos Modificados**
- Se actualiza **solo cuando cambian los atributos de Minecraft**
- Verificación cada 0.5 segundos con comparación exacta
- Cache inteligente optimizado para atributos de juego

### 👁️ **Visibilidad Inteligente**
- Se ocultan automáticamente en menús
- Respetan la configuración de HUD oculto (F1)
- Auto-ocultación cuando no hay valores significativos
- Control independiente de cada overlay
- El overlay de atributos modificados es visible por defecto

### ⚡ **Optimización Avanzada**
- **Cache diferenciado** para cada tipo de dato
- **Renderizado condicional** basado en cambios
- **Detección de cambios** con tolerancia para punto flotante
- **Limpieza automática** de cache al ocultar

## Archivos del Sistema

### 📁 **Estructura Actualizada**
```
src/main/java/net/iaxsro/rpgstats/client/
├── ClientSetup.java
├── command/
│   └── ClientCommands.java (actualizado para 3 overlays)
├── event/
│   ├── ClientAttributeEvents.java (nuevo)
│   ├── ClientDataEvents.java
│   └── ClientGuiEvents.java (actualizado para 3 overlays)
└── gui/
    ├── AttributePointsOverlay.java
    ├── AttributeValuesOverlay.java
    ├── ModifiedAttributesOverlay.java ⭐ **NUEVO**
    └── SimpleTestOverlay.java (debug)
```

### 📄 **Archivos Nuevos/Actualizados**

#### `ModifiedAttributesOverlay.java` ⭐ **NUEVO**
- Overlay para atributos de Minecraft modificados
- Posicionado en esquina superior derecha
- Cache inteligente con actualización solo en cambios
- Estilo diferenciado (verde lima y naranja)
- Muestra efectos reales de las bonificaciones RPG

#### `AttributeValuesOverlay.java` ⭐ **PREVIAMENTE AÑADIDO**
- Overlay para valores reales de atributos RPG
- Posicionado en esquina inferior izquierda
- Cache inteligente con actualización solo en cambios
- Estilo diferenciado (azul y dorado)

#### `ClientCommands.java` 🔄 **ACTUALIZADO**
- Comandos separados para cada overlay
- Comandos combinados para ambos
- Sistema de ayuda integrado
- Comando de estado para debugging

## Diferencias Clave

### **Overlay de Puntos vs Overlay de Valores**

| Aspecto | Puntos (Derecha) | Valores (Izquierda) |
|---------|------------------|---------------------|
| **Datos** | Puntos temporales | Atributos reales |
| **Fuente** | Capability PlayerStats | AttributeRegistry |
| **Actualización** | Cada segundo | Solo en cambios |
| **Color Título** | Dorado | Azul real |
| **Color Valores** | Verde | Dorado |
| **Posición** | Inferior derecha | Inferior izquierda |
| **Comando Base** | `points` | `values` |

## Casos de Uso

### 🎮 **Para Jugadores**
- **Monitoreo en tiempo real** de progreso de atributos
- **Comparación visual** entre puntos temporales y valores aplicados
- **Control personalizado** de qué información ver
- **Interfaz no intrusiva** que no interfiere con el gameplay

### 🛠️ **Para Desarrolladores**
- **Sistema extensible** para añadir más overlays
- **Arquitectura modular** con componentes independientes
- **Logging detallado** para debugging
- **API simple** para control programático

## Integración Técnica

### 🔧 **Modificaciones al Mod Principal**
- **RpgStatsMod.java**: Inicialización del cliente con DistExecutor
- **Compatibilidad**: Funciona con el sistema existente de capabilities
- **Rendimiento**: Optimizado para no afectar FPS

### 🎯 **Puntos de Extensión**
```java
// Añadir nuevos overlays
event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "nuevo_overlay", new NuevoOverlay());

// Controlar visibilidad programáticamente
AttributePointsOverlay.setVisible(false);
AttributeValuesOverlay.setVisible(true);

// Forzar actualizaciones
AttributeValuesOverlay.forceUpdate();
ClientAttributeEvents.resetCache();
```

## Configuración Avanzada

### 🎨 **Personalización de Colores**
```java
// En AttributeValuesOverlay.java
private static final int HEADER_COLOR = 0xFF4169E1; // Azul real
private static final int VALUE_COLOR = 0xFFFFD700;  // Dorado

// En AttributePointsOverlay.java  
private static final int HEADER_COLOR = 0xFFD700;   // Dorado
private static final int VALUE_COLOR = 0x00FF00;    // Verde
```

### 📐 **Ajuste de Posiciones**
```java
// Overlay de valores (izquierda)
private static final int MARGIN_LEFT = 10;
private static final int MARGIN_BOTTOM = 10;

// Overlay de puntos (derecha)
private static final int MARGIN_RIGHT = 10;
private static final int MARGIN_BOTTOM = 10;
```

## Estado del Sistema

### ✅ **Completamente Implementado**
- ✅ Dual overlay system
- ✅ Actualización optimizada
- ✅ Comandos completos
- ✅ Documentación detallada
- ✅ Sistema de cache inteligente
- ✅ Control independiente
- ✅ Logging comprehensivo

### 🚀 **Listo para Producción**
El sistema de dual overlays está **completamente funcional** y optimizado para uso en producción, proporcionando una experiencia de usuario superior con información completa y control granular.